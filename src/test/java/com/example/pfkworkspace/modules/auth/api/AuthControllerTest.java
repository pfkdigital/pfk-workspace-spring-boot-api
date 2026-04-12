package com.example.pfkworkspace.modules.auth.api;

import com.example.pfkworkspace.common.error.ApiAccessDeniedHandler;
import com.example.pfkworkspace.common.error.ApiAuthenticationEntryPoint;
import com.example.pfkworkspace.modules.auth.api.dto.*;
import com.example.pfkworkspace.modules.auth.application.impl.AuthServiceImpl;
import com.example.pfkworkspace.modules.auth.application.impl.UserDetailsServiceImpl;
import com.example.pfkworkspace.modules.auth.infrastructure.CustomLogoutHandler;
import com.example.pfkworkspace.modules.auth.infrastructure.JwtAuthFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.JsonPathResultMatchers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AuthServiceImpl authService;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    @MockitoBean
    private UserDetailsServiceImpl userDetailsService;

    @MockitoBean
    private CustomLogoutHandler customLogoutHandler;

    @MockitoBean
    private ApiAuthenticationEntryPoint apiAuthenticationEntryPoint;

    @MockitoBean
    private ApiAccessDeniedHandler apiAccessDeniedHandler;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void register_ShouldReturnCreated() throws Exception {
        RegisterRequestDto request = new RegisterRequestDto(
                "test@example.com",
                "password123",
                "JohnDoe",
                "Lastname",
                "johndoe"
        );

        RegisterResponseDto response = RegisterResponseDto.builder()
                .message("User registered successfully")
                .build();

        when(authService.register(any(RegisterRequestDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPanel("success").value(true))
                .andExpect(jsonPanel("message").value("User registered successfully"));
    }

    @Test
    void verifyEmail_ShouldReturnOk() throws Exception {
        VerifyResponseDto response = VerifyResponseDto.builder()
                .message("Email verified successfully")
                .build();
        when(authService.verify("token123")).thenReturn(response);

        mockMvc.perform(get("/api/v1/auth/verify")
                        .param("token", "token123"))
                .andExpect(status().isOk())
                .andExpect(jsonPanel("message").value("Email verified successfully"));
    }

    @Test
    void authenticate_ShouldReturnOk() throws Exception {
        AuthenticateRequestDto request = new AuthenticateRequestDto("johndoe", "password123");
        AuthenticateResponseDto response = AuthenticateResponseDto.builder()
                .message("Authenticated successfully")
                .build();

        when(authService.authenticate(any(AuthenticateRequestDto.class), any(HttpServletResponse.class)))
                .thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/authenticate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPanel("message").value("Authenticated successfully"));
    }

    @Test
    void refreshToken_ShouldReturnOk() throws Exception {
        RefreshResponseDto response = RefreshResponseDto.builder()
                .message("Token refreshed successfully")
                .build();
        when(authService.refresh(any(), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/refresh"))
                .andExpect(status().isOk())
                .andExpect(jsonPanel("message").value("Token refreshed successfully"));
    }

    @Test
    void getCurrentUser_ShouldReturnOk() throws Exception {
        CurrentUserResponseDto response = CurrentUserResponseDto.builder()
                .email("test@example.com")
                .firstName("John")
                .lastName("Doe")
                .username("johndoe")
                .build();
        when(authService.getCurrentUser()).thenReturn(response);

        mockMvc.perform(get("/api/v1/auth/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPanel("data.email").value("test@example.com"));
    }

    @Test
    void forgotPassword_ShouldReturnOk() throws Exception {
        ForgotPasswordRequestDto request = new ForgotPasswordRequestDto("test@example.com");
        ForgotPasswordResponseDto response = ForgotPasswordResponseDto.builder()
                .message("Reset email sent")
                .build();

        when(authService.forgotPassword(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPanel("message").value("Reset email sent"));
    }

    @Test
    void updatePassword_ShouldReturnOk() throws Exception {
        UpdatePasswordRequestDto request = new UpdatePasswordRequestDto("newPassword123");
        UpdatePasswordResponseDto response = UpdatePasswordResponseDto.builder()
                .message("Password updated")
                .build();

        when(authService.updatePassword(any(), eq("token123"), any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/auth/update-password")
                        .param("token", "token123")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPanel("message").value("Password updated"));
    }

    private JsonPathResultMatchers jsonPanel(String path) {
        return jsonPath("$." + path);
    }
}
