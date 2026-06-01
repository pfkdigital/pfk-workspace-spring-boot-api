package com.example.pfkworkspace.modules.auth.application;

import com.example.pfkworkspace.modules.workspace.api.dto.CreateInvitationRequestDto;

public interface EmailService {
    void sendVerificationEmail(String to, String token);
    void sendAccountVerifiedEmail(String to);
    void sendWorkspaceInvitationEmail(String to, CreateInvitationRequestDto.WorkspaceInvitationParams params);
}
