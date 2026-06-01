package com.example.pfkworkspace.modules.auth.application.impl;

import com.example.pfkworkspace.common.error.BadRequestException;
import com.example.pfkworkspace.common.error.ConflictException;
import com.example.pfkworkspace.common.util.CookieUtil;
import com.example.pfkworkspace.modules.auth.api.dto.*;
import com.example.pfkworkspace.modules.auth.domain.EmailVerificationToken;
import com.example.pfkworkspace.modules.auth.domain.RefreshToken;
import com.example.pfkworkspace.modules.auth.infrastructure.JwtUtility;
import com.example.pfkworkspace.modules.auth.infrastructure.repo.EmailVerificationTokenRepository;
import com.example.pfkworkspace.modules.auth.infrastructure.repo.PasswordResetTokenRepository;
import com.example.pfkworkspace.modules.auth.infrastructure.repo.RefreshTokenRepository;
import com.example.pfkworkspace.modules.user.domain.User;
import com.example.pfkworkspace.modules.user.infrastructure.repo.UserRepository;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.http.ResponseCookie;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private EmailServiceImpl emailService;
    @Mock
    private EmailVerificationTokenRepository emailVerificationTokenRepository;
    @Mock
    private RefreshTokenRepository refreshTokenRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private JwtUtility jwtUtility;
    @Mock
    private CookieUtil cookieUtil;

    @InjectMocks
    private AuthServiceImpl authService;

    private RegisterRequestDto registerRequest;
    private User user;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequestDto(
                "test@example.com",
                "password123",
                "JohnDoe",
                "Lastname",
                "johndoe"
        );

        user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@example.com");
        user.setUsername("johndoe");
    }

    @Test
    void register_WhenEmailAlreadyExists_ShouldThrowConflictException() {
        when(userRepository.existsByEmail(registerRequest.email())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Email is already in use");
    }

    @Test
    void register_WhenUsernameAlreadyExists_ShouldThrowConflictException() {
        when(userRepository.existsByEmail(registerRequest.email())).thenReturn(false);
        when(userRepository.existsByUsername(registerRequest.username())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(registerRequest))
                .isInstanceOf(ConflictException.class)
                .hasMessage("Username is already in use");
    }

    @Test
    void register_Success_ShouldReturnResponse() {
        when(userRepository.existsByEmail(registerRequest.email())).thenReturn(false);
        when(userRepository.existsByUsername(registerRequest.username())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");

        RegisterResponseDto response = authService.register(registerRequest);

        assertThat(response.getMessage()).contains("User registered successfully");
        verify(userRepository).save(any(User.class));
        verify(emailVerificationTokenRepository).save(any(EmailVerificationToken.class));
        verify(emailService).sendVerificationEmail(eq("test@example.com"), anyString());
    }

    @Test
    void verify_WhenTokenInvalid_ShouldThrowBadRequestException() {
        when(emailVerificationTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.verify("invalid-token"))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("Invalid verification token");
    }

    @Test
    void verify_Success_ShouldUpdateUserAndReturnResponse() {
        EmailVerificationToken token = EmailVerificationToken.builder()
                .userId(user.getId())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build();

        when(emailVerificationTokenRepository.findByTokenHash(anyString())).thenReturn(Optional.of(token));
        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        VerifyResponseDto response = authService.verify("valid-token");

        assertThat(response.getMessage()).contains("Email verified successfully");
        assertThat(user.isEmailVerified()).isTrue();
        verify(userRepository).save(user);
        verify(emailVerificationTokenRepository).save(token);
        verify(emailService).sendAccountVerifiedEmail(eq(user.getEmail()));
    }

    @Test
    void authenticate_Success_ShouldReturnResponse() {
        AuthenticateRequestDto request = new AuthenticateRequestDto("johndoe", "password123");
        Authentication authentication = mock(Authentication.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        ResponseCookie cookie = mock(ResponseCookie.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user);
        when(jwtUtility.generateAccessToken(user)).thenReturn("access-token");
        when(jwtUtility.generateRefreshToken(user)).thenReturn("refresh-token");
        when(cookieUtil.createAccessTokenCookie("access-token")).thenReturn(cookie);
        when(cookieUtil.createRefreshTokenCookie("refresh-token")).thenReturn(cookie);
        when(cookie.toString()).thenReturn("cookie-string");

        AuthenticateResponseDto result = authService.authenticate(request, response);

        assertThat(result.getMessage()).isEqualTo("Authenticated successfully");
        verify(userRepository).save(user);
        verify(refreshTokenRepository).save(any(RefreshToken.class));
        verify(response, times(2)).addHeader(eq("Set-Cookie"), anyString());
    }
}
