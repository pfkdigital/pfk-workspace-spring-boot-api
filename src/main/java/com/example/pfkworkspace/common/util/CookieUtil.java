package com.example.pfkworkspace.common.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CookieUtil {

    @Value("${pfk.auth.cookies.access-token-name}")
    private String cookieAccessTokenName;

    @Value("${pfk.auth.cookies.refresh-token-name}")
    private String cookieRefreshTokenName;

    @Value("${pfk.auth.cookies.secure}")
    private boolean cookieSecureFlag;

    @Value("${pfk.auth.cookies.same-site}")
    private String sameSite;

    @Value("${pfk.auth.cookies.domain}")
    private String domain;

    @Value("${pfk.auth.cookies.path}")
    private String path;

    @Value("${pfk.security.jwt.access-token-ttl}")
    private Long accessTokenExpirationMs;

    @Value("${pfk.security.jwt.refresh-token-ttl}")
    private Long refreshTokenExpirationMs;

    public String getCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) return null;
        return Arrays.stream(request.getCookies())
                .filter(c -> name.equals(c.getName()))
                .findFirst().map(Cookie::getValue)
                .orElse(null);
    }

    public Cookie deleteCookie(String name, String path) {
        Cookie cookie = new Cookie(name, "");
        cookie.setPath(path == null ? "/" : path);
        cookie.setMaxAge(0);
        return cookie;
    }

    public ResponseCookie createAccessTokenCookie(String token) {
        return createCookie(cookieAccessTokenName, token, accessTokenExpirationMs);
    }

    public ResponseCookie createRefreshTokenCookie(String token) {
        return createCookie(cookieRefreshTokenName, token, refreshTokenExpirationMs);
    }

    private ResponseCookie createCookie(String name, String value, Long expirationMs) {
        return ResponseCookie.from(name, value)
                .httpOnly(true)
                .secure(cookieSecureFlag)
                .path(path)
                .maxAge(expirationMs / 1000)
                .sameSite(sameSite)
                .domain(domain != null && !domain.isBlank() ? domain : null)
                .build();
    }
}
