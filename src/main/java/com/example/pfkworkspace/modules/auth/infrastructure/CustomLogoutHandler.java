package com.example.pfkworkspace.modules.auth.infrastructure;

import com.example.pfkworkspace.common.util.CookieUtil;
import com.example.pfkworkspace.modules.auth.api.dto.LogoutResponseDto;
import com.example.pfkworkspace.modules.auth.api.exception.RefreshTokenNotFoundException;
import com.example.pfkworkspace.modules.auth.domain.RefreshToken;
import com.example.pfkworkspace.modules.auth.infrastructure.repo.RefreshTokenRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomLogoutHandler implements LogoutSuccessHandler {
  private final CookieUtil cookieUtil;
  private final ObjectMapper objectMapper;
  private final RefreshTokenRepository refreshTokenRepository;

  @Override
  public void onLogoutSuccess(
      HttpServletRequest request, HttpServletResponse response, Authentication authentication)
      throws IOException {
    String refreshToken = cookieUtil.getCookie(request, "refresh_token");

    if (refreshToken == null) {
      log.warn("Logout attempted with no refresh token cookie");
      throw new RefreshTokenNotFoundException("No refresh token found in cookies");
    }

    RefreshToken refreshTokenEntity =
        refreshTokenRepository
            .findByTokenHash(sha256Hash(refreshToken))
            .orElseThrow(
                () -> new RefreshTokenNotFoundException("Refresh token not found in database"));
    refreshTokenEntity.setRevoked(true);
    refreshTokenRepository.save(refreshTokenEntity);

    cookieUtil.deleteCookie(response, "access_token");
    cookieUtil.deleteCookie(response, "refresh_token");
    log.info(
        "User logged out: username={}",
        authentication != null ? authentication.getName() : "unknown");
    LogoutResponseDto logoutResponse = new LogoutResponseDto("Successfully logged out");
    response.setContentType("application/json");
    response.setCharacterEncoding("UTF-8");
    response.getWriter().write(objectMapper.writeValueAsString(logoutResponse));
    response.getWriter().flush();
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
}
