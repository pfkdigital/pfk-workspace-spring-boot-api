package com.example.pfkworkspace.modules.auth.application.impl;

import com.example.pfkworkspace.modules.auth.application.UserContextService;
import com.example.pfkworkspace.modules.user.domain.User;
import com.example.pfkworkspace.modules.user.infrastructure.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserContextServiceImpl implements UserContextService {
  private final UserRepository userRepository;

  @Override
  public User getCurrentUser() {
    String email = SecurityContextHolder.getContext().getAuthentication().getName();

    return userRepository
        .findByEmail(email)
        .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
  }
}
