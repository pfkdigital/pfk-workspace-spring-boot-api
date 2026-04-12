package com.example.pfkworkspace.modules.auth.application;

import com.example.pfkworkspace.modules.workspace.api.dto.CreateInvitationRequestDto;

public interface EmailService {
    void sendVerificationEmail(String to, String subject, String token);
    void sendAccountVerifiedEmail(String to, String subject);
    void sendWorkspaceInvitationEmail(String to, String subject, CreateInvitationRequestDto.WorkspaceInvitationParams params);
}
