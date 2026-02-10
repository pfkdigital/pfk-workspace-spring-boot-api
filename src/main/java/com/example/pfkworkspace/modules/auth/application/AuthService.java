package com.example.pfkworkspace.modules.auth.application;

import com.example.pfkworkspace.modules.auth.api.dto.*;
import com.example.pfkworkspace.modules.auth.domain.RefreshToken;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {
    RegisterResponseDto register(RegisterRequestDto registrationRequestDto);
    VerifyResponseDto verify(String token);
    AuthenticateResponseDto authenticate(AuthenticateRequestDto requestDto, HttpServletResponse response);
    RefreshResponseDto refresh(HttpServletRequest request, HttpServletResponse response);
}
