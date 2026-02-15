package com.example.pfkworkspace.modules.auth.infrastructure;

import com.example.pfkworkspace.common.util.CookieUtil;
import com.example.pfkworkspace.modules.auth.application.impl.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

  private final CookieUtil cookieUtil;
  private final JwtUtility jwtUtility;
  private final UserDetailsServiceImpl userDetailsService;

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
    }
    filterChain.doFilter(request, response);
  }
}
