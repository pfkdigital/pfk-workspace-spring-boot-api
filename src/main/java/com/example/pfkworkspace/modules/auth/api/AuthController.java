package com.example.pfkworkspace.modules.auth.api;

import com.example.pfkworkspace.common.api.ApiResponse;
import com.example.pfkworkspace.modules.auth.api.dto.AuthenticateRequestDto;
import com.example.pfkworkspace.modules.auth.api.dto.AuthenticateResponseDto;
import com.example.pfkworkspace.modules.auth.api.dto.CurrentUserResponseDto;
import com.example.pfkworkspace.modules.auth.api.dto.ForgotPasswordRequestDto;
import com.example.pfkworkspace.modules.auth.api.dto.ForgotPasswordResponseDto;
import com.example.pfkworkspace.modules.auth.api.dto.RefreshResponseDto;
import com.example.pfkworkspace.modules.auth.api.dto.RegisterRequestDto;
import com.example.pfkworkspace.modules.auth.api.dto.RegisterResponseDto;
import com.example.pfkworkspace.modules.auth.api.dto.UpdatePasswordRequestDto;
import com.example.pfkworkspace.modules.auth.api.dto.UpdatePasswordResponseDto;
import com.example.pfkworkspace.modules.auth.api.dto.VerifyResponseDto;
import com.example.pfkworkspace.modules.auth.application.impl.AuthServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthServiceImpl authService;

  @PostMapping("/register")
  public ResponseEntity<ApiResponse> register(
      @Valid @RequestBody RegisterRequestDto registerRequestDto) {
    RegisterResponseDto response = authService.register(registerRequestDto);
    return new ResponseEntity<>(
        ApiResponse.builder().success(true).message(response.getMessage()).data(response).build(),
        HttpStatus.CREATED);
  }

  @GetMapping("/verify")
  public ResponseEntity<ApiResponse> verifyEmail(@RequestParam String token) {
    VerifyResponseDto response = authService.verify(token);
    return new ResponseEntity<>(
        ApiResponse.builder().success(true).message(response.getMessage()).data(response).build(),
        HttpStatus.OK);
  }

  @PostMapping("/authenticate")
  public ResponseEntity<ApiResponse> authenticate(
      @Valid @RequestBody AuthenticateRequestDto requestDto, HttpServletResponse response) {
    AuthenticateResponseDto authResponse = authService.authenticate(requestDto, response);
    return new ResponseEntity<>(
        ApiResponse.builder()
            .success(true)
            .message(authResponse.getMessage())
            .data(authResponse)
            .build(),
        HttpStatus.OK);
  }

  @PostMapping("/refresh")
  public ResponseEntity<ApiResponse> refreshToken(
      HttpServletRequest request, HttpServletResponse response) {
    RefreshResponseDto refreshResponse = authService.refresh(request, response);
    return new ResponseEntity<>(
        ApiResponse.builder()
            .success(true)
            .message(refreshResponse.getMessage())
            .data(refreshResponse)
            .build(),
        HttpStatus.OK);
  }

  @GetMapping("/me")
  public ResponseEntity<ApiResponse> getCurrentUser() {
    CurrentUserResponseDto currentUser = authService.getCurrentUser();
    return new ResponseEntity<>(
        ApiResponse.builder()
            .success(true)
            .message("Current user retrieved successfully")
            .data(currentUser)
            .build(),
        HttpStatus.OK);
  }

  @PostMapping("/forgot-password")
  public ResponseEntity<ApiResponse> forgotPassword(
      @Valid @RequestBody ForgotPasswordRequestDto forgotPasswordRequestDto) {
    ForgotPasswordResponseDto response = authService.forgotPassword(forgotPasswordRequestDto);
    return new ResponseEntity<>(
        ApiResponse.builder().success(true).message(response.getMessage()).data(response).build(),
        HttpStatus.OK);
  }

  @PostMapping("/update-password")
  public ResponseEntity<ApiResponse> updatePassword(
      @Valid @RequestBody UpdatePasswordRequestDto updatePasswordRequestDto,
      @RequestParam(name = "token") String token,
      HttpServletResponse httpServletResponse) {
    UpdatePasswordResponseDto response =
        authService.updatePassword(updatePasswordRequestDto, token, httpServletResponse);
    return new ResponseEntity<>(
        ApiResponse.builder().success(true).message(response.getMessage()).data(response).build(),
        HttpStatus.OK);
  }
}
