package com.example.pfkworkspace.modules.auth.application;

import com.example.pfkworkspace.modules.auth.api.dto.*;
import jakarta.servlet.http.HttpServletResponse;

public interface AuthService {
    RegisterResponseDto register(RegisterRequestDto registrationRequestDto);
    VerifyResponseDto verify(String token);
    AuthenticateResponseDto authenticate(AuthenticateRequestDto requestDto, HttpServletResponse response);
}
