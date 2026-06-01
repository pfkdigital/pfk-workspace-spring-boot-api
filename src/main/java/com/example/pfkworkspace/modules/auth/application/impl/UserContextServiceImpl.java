package com.example.pfkworkspace.modules.auth.application.impl;

import com.example.pfkworkspace.modules.auth.application.UserContextService;
import com.example.pfkworkspace.modules.user.domain.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserContextServiceImpl implements UserContextService {

  @Override
  public User getCurrentUser() {
    Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    if(principal instanceof User user) {
      return user;
    }
    throw new UsernameNotFoundException("No authenticated user found");
  }
}
