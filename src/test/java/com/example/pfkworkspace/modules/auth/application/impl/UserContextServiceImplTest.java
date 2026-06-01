package com.example.pfkworkspace.modules.auth.application.impl;

import com.example.pfkworkspace.modules.user.domain.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class UserContextServiceImplTest {

    @InjectMocks
    private UserContextServiceImpl userContextService;

    @BeforeEach
    void setUp() {
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getCurrentUser_WhenUserExists_ShouldReturnUser() {
        User user = new User();
        user.setEmail("test@example.com");

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, List.of()));

        User result = userContextService.getCurrentUser();

        assertThat(result).isNotNull();
        assertThat(result.getEmail()).isEqualTo("test@example.com");
    }

    @Test
    void getCurrentUser_WhenPrincipalIsNotUser_ShouldThrowUsernameNotFoundException() {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken("some-string-principal", null, List.of()));

        assertThatThrownBy(() -> userContextService.getCurrentUser())
                .isInstanceOf(UsernameNotFoundException.class)
                .hasMessageContaining("No authenticated user found");
    }
}
