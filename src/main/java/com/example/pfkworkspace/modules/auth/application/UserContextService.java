package com.example.pfkworkspace.modules.auth.application;

import com.example.pfkworkspace.modules.user.domain.User;

public interface UserContextService {
    User getCurrentUser();
}
