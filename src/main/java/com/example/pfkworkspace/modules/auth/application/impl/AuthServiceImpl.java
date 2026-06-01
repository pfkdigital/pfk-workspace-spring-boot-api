package com.example.pfkworkspace.modules.auth.application.impl;

import com.example.pfkworkspace.common.error.BadRequestException;
import com.example.pfkworkspace.common.error.ConflictException;
import com.example.pfkworkspace.common.error.NotFoundException;
import com.example.pfkworkspace.common.error.UnauthorizedException;
import com.example.pfkworkspace.common.util.CookieUtil;
import com.example.pfkworkspace.common.util.RandomTokenGenerator;
import com.example.pfkworkspace.modules.auth.api.dto.*;
import com.example.pfkworkspace.modules.auth.application.AuthService;
import com.example.pfkworkspace.modules.auth.domain.EmailVerificationToken;
import com.example.pfkworkspace.modules.auth.domain.PasswordResetToken;
import com.example.pfkworkspace.modules.auth.domain.RefreshToken;
import com.example.pfkworkspace.modules.auth.infrastructure.JwtUtility;
import com.example.pfkworkspace.modules.auth.infrastructure.repo.EmailVerificationTokenRepository;
import com.example.pfkworkspace.modules.auth.infrastructure.repo.PasswordResetTokenRepository;
import com.example.pfkworkspace.modules.auth.infrastructure.repo.RefreshTokenRepository;
import com.example.pfkworkspace.modules.user.domain.User;
import com.example.pfkworkspace.modules.user.infrastructure.repo.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
  private final EmailServiceImpl emailService;
  private final EmailVerificationTokenRepository emailVerificationTokenRepository;
  private final RefreshTokenRepository refreshTokenRepository;
  private final UserRepository userRepository;
  private final PasswordResetTokenRepository passwordResetTokenRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final JwtUtility jwtUtility;
  private final CookieUtil cookieUtil;

  @Override
  public RegisterResponseDto register(RegisterRequestDto registrationRequestDto) {

    if (userRepository.existsByEmail(registrationRequestDto.email())) {
      throw new ConflictException("Email is already in use");
    }

    if (userRepository.existsByUsername(registrationRequestDto.username())) {
      throw new ConflictException("Username is already in use");
    }

    User newUser = createNewUser(registrationRequestDto);
    userRepository.save(newUser);

    String rawToken = RandomTokenGenerator.generateToken();
    String tokenHash = sha256Hash(rawToken);
    EmailVerificationToken verificationToken =
        EmailVerificationToken.builder()
            .tokenHash(tokenHash)
            .userId(newUser.getId())
            .expiresAt(java.time.Instant.now().plus(java.time.Duration.ofHours(24)))
            .build();
    emailVerificationTokenRepository.save(verificationToken);
    
    emailService.sendVerificationEmail(registrationRequestDto.email(), rawToken);

    log.info("User registered: username={}, userId={}", newUser.getUsername(), newUser.getId());

    return RegisterResponseDto.builder()
        .message("User registered successfully. Please check your email to verify your account.")
        .build();
  }

  @Override
  public VerifyResponseDto verify(String token) {
    String tokenHash = sha256Hash(token);
    EmailVerificationToken verificationToken =
        emailVerificationTokenRepository
            .findByTokenHash(tokenHash)
            .orElseThrow(() -> new BadRequestException("Invalid verification token"));

    if (verificationToken.isUsed()) {
      throw new BadRequestException("Verification token has already been used");
    }
    if (verificationToken.getExpiresAt().isBefore(java.time.Instant.now())) {
      throw new BadRequestException("Verification token has expired");
    }

    User user =
        userRepository
            .findById(verificationToken.getUserId())
            .orElseThrow(() -> new NotFoundException("User not found"));
    user.markEmailVerified();
    userRepository.save(user);

    verificationToken.setUsed(true);
    emailVerificationTokenRepository.save(verificationToken);
    
    emailService.sendAccountVerifiedEmail(user.getEmail());

    log.info("Email verified for userId={}", user.getId());

    return VerifyResponseDto.builder()
        .message(
            "Email verified successfully. Your account is now active. Please log in to continue.")
        .build();
  }

  @Override
  public AuthenticateResponseDto authenticate(
      AuthenticateRequestDto requestDto, HttpServletResponse response) {
    Authentication auth =
        authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(requestDto.username(), requestDto.password()));

    User user = (User) auth.getPrincipal();
    user.setLastLoginAt(Instant.now());
    userRepository.save(user);

    String jwtToken = jwtUtility.generateAccessToken(user);
    String refreshToken = jwtUtility.generateRefreshToken(user);
    RefreshToken refreshTokenEntity =
        RefreshToken.builder()
            .tokenHash(sha256Hash(refreshToken))
            .user(user)
            .user_id(user.getId())
            .expiresAt(Instant.now().plus(Duration.ofDays(7)))
            .build();
    refreshTokenRepository.save(refreshTokenEntity);

    this.addCookiesToResponse(response, jwtToken, refreshToken);

    log.info("User authenticated: username={}, userId={}", user.getUsername(), user.getId());

    return AuthenticateResponseDto.builder().message("Authenticated successfully").build();
  }

  @Override
  public RefreshResponseDto refresh(HttpServletRequest request, HttpServletResponse response) {
    String token = cookieUtil.getCookie(request, "refresh_token");
    if (token == null) {
      throw new UnauthorizedException("Refresh token is missing");
    }

    RefreshToken refreshToken =
        refreshTokenRepository
            .findByTokenHash(sha256Hash(token))
            .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

    if (refreshToken.isRevoked()) {
      throw new UnauthorizedException("Refresh token has been revoked, please sign in again");
    }

    String username = jwtUtility.extractUsername(token);
    User user =
        userRepository
            .findByUsername(username)
            .orElseThrow(() -> new UnauthorizedException("Invalid refresh token"));

    if (!jwtUtility.isTokenValid(token, user)) {
      throw new UnauthorizedException("Invalid refresh token");
    }

    String newJwtToken = jwtUtility.generateAccessToken(user);

    addCookiesToResponse(response, newJwtToken, token);

    log.info("Token refreshed for userId={}", user.getId());

    return RefreshResponseDto.builder().message("Token refreshed successfully").build();
  }

  @Override
  public CurrentUserResponseDto getCurrentUser() {

    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

    if (authentication == null
        || !authentication.isAuthenticated()
        || authentication.getPrincipal() == null) {
      throw new AuthenticationCredentialsNotFoundException("User not authenticated");
    }

    Object principal = authentication.getPrincipal();
    User user = null;

    if (principal instanceof User u) {
      user = u;
    } else if (principal instanceof UserDetails ud) {
      user = userRepository.findByUsername(ud.getUsername()).orElse(null);
    } else if (principal instanceof String username) {
      user = userRepository.findByUsername(username).orElse(null);
    }

    if (user == null) {
      throw new AuthenticationCredentialsNotFoundException(
          "Authenticated user not found in database");
    }

    return CurrentUserResponseDto.builder()
        .email(user.getEmail())
        .firstName(user.getFirstName())
        .lastName(user.getLastName())
        .username(user.getUsername())
        .build();
  }

  @Override
  public ForgotPasswordResponseDto forgotPassword(
      ForgotPasswordRequestDto forgotPasswordRequestDto) {
    final String genericMessage =
        "If an account with that email exists, a password reset link has been sent.";

    return userRepository
        .findByEmail(forgotPasswordRequestDto.email())
        .map(
            user -> {
              final String rawToken = RandomTokenGenerator.generateToken();
              final String tokenHash = sha256Hash(rawToken);
              final Instant expiresAt = Instant.now().plus(Duration.ofHours(1));

              PasswordResetToken tokenEntity =
                  passwordResetTokenRepository
                      .findPasswordResetTokenByUserId(user.getId())
                      .map(
                          existing -> {
                            existing.setTokenHash(tokenHash);
                            existing.setExpiresAt(expiresAt);
                            return existing;
                          })
                      .orElseGet(
                          () ->
                              PasswordResetToken.builder()
                                  .tokenHash(tokenHash)
                                  .userId(user.getId())
                                  .user(user)
                                  .used(false)
                                  .expiresAt(expiresAt)
                                  .build());

              passwordResetTokenRepository.save(tokenEntity);

              log.info("Password reset requested for userId={}", user.getId());

              emailService.sendPasswordResetEmail(forgotPasswordRequestDto.email(), rawToken);

              return ForgotPasswordResponseDto.builder().message(genericMessage).build();
            })
        .orElseGet(() -> ForgotPasswordResponseDto.builder().message(genericMessage).build());
  }

  @Override
  @Transactional
  public UpdatePasswordResponseDto updatePassword(
      UpdatePasswordRequestDto updatePasswordRequestDto, String token, HttpServletResponse response) {
    String tokenHash = sha256Hash(token);

    PasswordResetToken resetToken =
        passwordResetTokenRepository
            .findPasswordResetTokenByTokenHash(tokenHash)
            .orElseThrow(() -> new BadRequestException("Invalid password reset token"));

    if (resetToken.isUsed()) {
      throw new BadRequestException("Password reset token has already been used");
    }

    User user =
        userRepository
            .findById(resetToken.getUserId())
            .orElseThrow(() -> new UnauthorizedException("Invalid password reset token"));
    user.setPasswordHash(passwordEncoder.encode(updatePasswordRequestDto.newPassword()));
    userRepository.save(user);

    resetToken.setUsed(true);
    passwordResetTokenRepository.save(resetToken);

    refreshTokenRepository.deleteAllByUserId(user.getId());
    cookieUtil.deleteAccessTokenCookie(response);
    cookieUtil.deleteRefreshTokenCookie(response);

    emailService.sendPasswordUpdatedEmail(user.getEmail());

    log.info("Password updated for userId={}", user.getId());

    return UpdatePasswordResponseDto.builder()
        .message("Password updated successfully, please login with new password")
        .build();
  }

  private User createNewUser(RegisterRequestDto registrationRequestDto) {
    return User.builder()
        .username(registrationRequestDto.username())
        .email(registrationRequestDto.email())
        .passwordHash(passwordEncoder.encode(registrationRequestDto.password()))
        .firstName(registrationRequestDto.firstName())
        .lastName(registrationRequestDto.lastName())
        .roles(Set.of("ROLE_USER"))
        .build();
  }

  private String sha256Hash(String rawToken) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      byte[] hashBytes = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(hashBytes);
    } catch (NoSuchAlgorithmException e) {
      throw new RuntimeException("Error hashing token", e);
    }
  }

  private void addCookiesToResponse(
      HttpServletResponse response, String jwtToken, String refreshToken) {
    ResponseCookie accessTokenCookie = cookieUtil.createAccessTokenCookie(jwtToken);
    ResponseCookie refreshTokenCookie = cookieUtil.createRefreshTokenCookie(refreshToken);
    response.addHeader("Set-Cookie", accessTokenCookie.toString());
    response.addHeader("Set-Cookie", refreshTokenCookie.toString());
  }
}
