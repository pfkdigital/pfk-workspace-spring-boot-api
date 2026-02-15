package com.example.pfkworkspace.modules.auth.infrastructure;

import com.example.pfkworkspace.common.api.ApiResponse;
import com.example.pfkworkspace.common.error.ApiError;
import com.example.pfkworkspace.common.util.CookieUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
import com.example.pfkworkspace.modules.auth.application.impl.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

  private final CookieUtil cookieUtil;
  private final JwtUtility jwtUtility;
  private final UserDetailsServiceImpl userDetailsService;
  private final ObjectMapper objectMapper;

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {
    String token = cookieUtil.getCookie(request,"access_token");
    if (token == null) {
      filterChain.doFilter(request, response);
      return;
    }

    try {
      String username = jwtUtility.extractUsername(token);
      log.info("Extracted username from token: {}", username);

      if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

        if (jwtUtility.isTokenValid(token, userDetails)) {
          UsernamePasswordAuthenticationToken authToken =
              new UsernamePasswordAuthenticationToken(
                  userDetails, null, userDetails.getAuthorities());
          authToken.setDetails(new WebAuthenticationDetails(request));

          SecurityContextHolder.getContext().setAuthentication(authToken);

          log.info("JWT authenticated user: {}", username);
        }
      }

    } catch (Exception e) {
      log.error("JWT filter error: {}", e.getMessage(), e);
      handleJwtError(response, e);
      return;
    }
    filterChain.doFilter(request, response);
  }

  private void handleJwtError(HttpServletResponse response, Exception e) throws IOException {
    String message = "Invalid or expired access token.";
    if (e instanceof AuthenticationException) {
      message = "Authentication failed.";
    } else if (!(e instanceof JwtException) && e.getMessage() != null && !e.getMessage().isBlank()) {
      message = e.getMessage();
    }

    ApiError apiError =
        ApiError.builder()
            .status(HttpStatus.UNAUTHORIZED)
            .message(message)
            .timestamp(Instant.now())
            .build();
    ApiResponse apiResponse =
        ApiResponse.builder().success(false).message(message).data(apiError).build();
    response.setStatus(HttpStatus.UNAUTHORIZED.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    objectMapper.writeValue(response.getOutputStream(), apiResponse);
  }
}
