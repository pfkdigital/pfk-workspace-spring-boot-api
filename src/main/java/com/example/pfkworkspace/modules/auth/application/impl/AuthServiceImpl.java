package com.example.pfkworkspace.modules.auth.application.impl;

import com.example.pfkworkspace.common.util.CookieUtil;
import com.example.pfkworkspace.common.util.RandomTokenGenerator;
import com.example.pfkworkspace.modules.auth.api.dto.*;
import com.example.pfkworkspace.modules.auth.application.AuthService;
import com.example.pfkworkspace.modules.auth.domain.EmailVerificationToken;
import com.example.pfkworkspace.modules.auth.domain.RefreshToken;
import com.example.pfkworkspace.modules.auth.infrastructure.JwtUtility;
import com.example.pfkworkspace.modules.auth.infrastructure.repo.EmailVerificationTokenRepository;
import com.example.pfkworkspace.modules.auth.infrastructure.repo.RefreshTokenRepository;
import com.example.pfkworkspace.modules.user.domain.User;
import com.example.pfkworkspace.modules.user.infrastructure.repo.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.util.HexFormat;


@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
  private final EmailServiceImpl emailService;
  private final EmailVerificationTokenRepository emailVerificationTokenRepository;
  private final RefreshTokenRepository refreshTokenRepository;
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;
  private final AuthenticationManager authenticationManager;
  private final JwtUtility jwtUtility;
  private final CookieUtil cookieUtil;

  @Override
  public RegisterResponseDto register(RegisterRequestDto registrationRequestDto) {

    if (userRepository.existsByEmail(registrationRequestDto.email())) {
      throw new IllegalArgumentException("Email is already in use");
    }

    if (userRepository.existsByUsername(registrationRequestDto.username())) {
      throw new IllegalArgumentException("Username is already in use");
    }

    // Create and save the new user
    User newUser = createNewUser(registrationRequestDto);
    userRepository.save(newUser);

    // Create and save the email verification token
    String rawToken = RandomTokenGenerator.generateToken();
    String tokenHash = sha256Hash(rawToken);
    EmailVerificationToken verificationToken =
        EmailVerificationToken.builder()
            .tokenHash(tokenHash)
            .userId(newUser.getId())
            .expiresAt(java.time.Instant.now().plus(java.time.Duration.ofHours(24)))
            .build();
    emailVerificationTokenRepository.save(verificationToken);

    // Send the verification email
    emailService.sendVerificationEmail(
        registrationRequestDto.email(), "Verify your email", rawToken);

    return RegisterResponseDto.builder()
        .message("User registered successfully. Please check your email to verify your account.")
        .build();
  }

  @Override
  public VerifyResponseDto verify(String token) {
    // Find the verification token and associated user
    String tokenHash = sha256Hash(token);
    EmailVerificationToken verificationToken =
        emailVerificationTokenRepository
            .findByTokenHash(tokenHash)
            .orElseThrow(() -> new IllegalArgumentException("Invalid verification token"));

    if (verificationToken.isUsed() || verificationToken.getExpiresAt().isBefore(java.time.Instant.now())) {
        throw new IllegalArgumentException("Verification token has expired");
    }

    // Mark the user's email as verified and save the user
    User user =
        userRepository
            .findById(verificationToken.getUserId())
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
    user.markEmailVerified();
    userRepository.save(user);

    // Mark the token as used and delete it
    emailVerificationTokenRepository.delete(verificationToken);

    // Send account verified email
    emailService.sendAccountVerifiedEmail(user.getEmail(), "Your account has been verified");

    return VerifyResponseDto.builder()
        .message(
            "Email verified successfully. Your account is now active. Please log in to continue.")
        .build();
  }

  @Override
  public AuthenticateResponseDto authenticate(AuthenticateRequestDto requestDto, HttpServletResponse response) {
    Authentication auth = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(requestDto.username(), requestDto.password())
    );

    User user = (User) auth.getPrincipal();
    user.setLastLoginAt(Instant.now());
    userRepository.save(user);

    String jwtToken = jwtUtility.generateAccessToken(user);
    String refreshToken = jwtUtility.generateRefreshToken(user);
    RefreshToken refreshTokenEntity = RefreshToken.builder()
            .tokenHash(sha256Hash(refreshToken))
            .user(user)
            .user_id(user.getId())
            .expiresAt(Instant.now().plus(Duration.ofDays(7)))
            .build();
    refreshTokenRepository.save(refreshTokenEntity);

    addCookiesToResponse(response, jwtToken, refreshToken);

    return AuthenticateResponseDto.builder()
            .message("Authenticated successfully")
            .build();
  }

  private User createNewUser(RegisterRequestDto registrationRequestDto) {
    return User.builder()
        .username(registrationRequestDto.username())
        .email(registrationRequestDto.email())
        .passwordHash(passwordEncoder.encode(registrationRequestDto.password()))
        .firstName(registrationRequestDto.firstName())
        .lastName(registrationRequestDto.lastName())
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

  private void addCookiesToResponse(HttpServletResponse response, String jwtToken, String refreshToken) {
    ResponseCookie accessTokenCookie = cookieUtil.createAccessTokenCookie(jwtToken);
    ResponseCookie refreshTokenCookie = cookieUtil.createRefreshTokenCookie(refreshToken);
    response.addHeader("Set-Cookie", accessTokenCookie.toString());
    response.addHeader("Set-Cookie", refreshTokenCookie.toString());
  }
}
