package com.example.pfkworkspace.modules.auth.api;

import com.example.pfkworkspace.modules.auth.api.dto.AuthenticateRequestDto;
import com.example.pfkworkspace.modules.auth.api.dto.RegisterRequestDto;
import com.example.pfkworkspace.modules.auth.application.impl.AuthServiceImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
  public ResponseEntity<?> register(@RequestBody RegisterRequestDto registerRequestDto) {
    return new ResponseEntity<>(authService.register(registerRequestDto), HttpStatus.CREATED);
  }

  @GetMapping("/verify")
  public ResponseEntity<?> verifyEmail(@RequestParam String token) {
    return new ResponseEntity<>(authService.verify(token), HttpStatus.OK);
  }

  @PostMapping("/authenticate")
  public ResponseEntity<?> authenticate(
      @RequestBody AuthenticateRequestDto requestDto, HttpServletResponse response) {
    return new ResponseEntity<>(authService.authenticate(requestDto, response), HttpStatus.OK);
  }

  @PostMapping("/refresh")
  public ResponseEntity<?> refreshToken(HttpServletRequest request, HttpServletResponse response) {
    return new ResponseEntity<>(authService.refresh(request, response), HttpStatus.OK);
  }
}
