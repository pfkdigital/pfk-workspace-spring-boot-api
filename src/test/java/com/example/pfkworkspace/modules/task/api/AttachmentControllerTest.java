package com.example.pfkworkspace.modules.task.api;

import com.example.pfkworkspace.common.error.ApiAccessDeniedHandler;
import com.example.pfkworkspace.common.error.ApiAuthenticationEntryPoint;
import com.example.pfkworkspace.common.error.ConflictException;
import com.example.pfkworkspace.modules.auth.application.impl.UserDetailsServiceImpl;
import com.example.pfkworkspace.modules.auth.infrastructure.CustomLogoutHandler;
import com.example.pfkworkspace.modules.auth.infrastructure.JwtAuthFilter;
import com.example.pfkworkspace.modules.task.api.dto.request.CreateAttachmentRequestDto;
import com.example.pfkworkspace.modules.task.api.dto.response.CreateAttachmentResponseDto;
import com.example.pfkworkspace.modules.task.api.dto.response.GetAttachmentResponseDto;
import com.example.pfkworkspace.modules.task.api.dto.response.RemoveAttachmentResponseDto;
import com.example.pfkworkspace.modules.task.api.exception.AttachmentNotFoundException;
import com.example.pfkworkspace.modules.task.application.AttachmentService;
import com.example.pfkworkspace.modules.task.domain.AttachmentStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AttachmentController.class)
@AutoConfigureMockMvc(addFilters = false)
class AttachmentControllerTest {

    private static final String COLLECTION =
            "/api/v1/workspaces/{workspaceId}/projects/{projectId}/tasks/{taskId}/attachments";
    private static final String ITEM = COLLECTION + "/{attachmentId}";

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AttachmentService attachmentService;

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

    private UUID workspaceId;
    private UUID projectId;
    private UUID taskId;
    private UUID attachmentId;

    @BeforeEach
    void setUp() {
        workspaceId = UUID.randomUUID();
        projectId = UUID.randomUUID();
        taskId = UUID.randomUUID();
        attachmentId = UUID.randomUUID();
    }

    private CreateAttachmentRequestDto validRequest() {
        return new CreateAttachmentRequestDto("report.pdf", "pdf", "application/pdf", 1024L, "a".repeat(64));
    }

    @Nested
    @DisplayName("POST /attachments")
    class CreateAttachment {

        @Test
        @WithMockUser
        void shouldReturnThePresignedUrlEnvelope() throws Exception {
            CreateAttachmentResponseDto response = CreateAttachmentResponseDto.builder()
                    .filename("report.pdf")
                    .preSignedUrl("https://s3.example.com/presigned")
                    .status(AttachmentStatus.PENDING)
                    .build();
            when(attachmentService.createAttachment(
                    eq(workspaceId), eq(projectId), eq(taskId), any(CreateAttachmentRequestDto.class)))
                    .thenReturn(response);

            mockMvc.perform(post(COLLECTION, workspaceId, projectId, taskId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest())))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Attachment creation initialized"))
                    .andExpect(jsonPath("$.data.filename").value("report.pdf"))
                    .andExpect(jsonPath("$.data.preSignedUrl").value("https://s3.example.com/presigned"))
                    .andExpect(jsonPath("$.data.status").value("PENDING"))
                    .andExpect(jsonPath("$.timestamp").exists());
        }

        @ParameterizedTest(name = "{4}")
        @CsvSource({
                "'',              pdf,  application/pdf,  1024,      blank filename",
                "evil.exe,        exe,  application/pdf,  1024,      executable filename",
                "report.pdf,      exe,  application/pdf,  1024,      executable extension",
                "report.pdf,      pdf,  application/x-msdownload, 1024, disallowed content type",
                "report.pdf,      pdf,  application/pdf,  0,         zero size",
                "report.pdf,      pdf,  application/pdf,  26214401,  over the 25MB limit",
        })
        @WithMockUser
        void shouldRejectAnInvalidPayloadWithoutCallingTheService(
                String filename, String extension, String contentType, long sizeBytes, String description)
                throws Exception {
            CreateAttachmentRequestDto request =
                    new CreateAttachmentRequestDto(filename, extension, contentType, sizeBytes, "a".repeat(64));

            mockMvc.perform(post(COLLECTION, workspaceId, projectId, taskId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.errors").isArray());

            verifyNoInteractions(attachmentService);
        }

        @Test
        @WithMockUser
        void shouldRejectAChecksumThatIsNotHexSha256() throws Exception {
            CreateAttachmentRequestDto request =
                    new CreateAttachmentRequestDto("report.pdf", "pdf", "application/pdf", 1024L, "not-a-digest");

            mockMvc.perform(post(COLLECTION, workspaceId, projectId, taskId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(attachmentService);
        }

        @Test
        @WithMockUser
        void shouldReturnBadRequestWhenTheBodyIsMalformed() throws Exception {
            mockMvc.perform(post(COLLECTION, workspaceId, projectId, taskId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{ not json"))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(attachmentService);
        }

        @Test
        @WithMockUser
        void shouldReturnBadRequestWhenAPathVariableIsNotAUuid() throws Exception {
            mockMvc.perform(post(COLLECTION, "not-a-uuid", projectId, taskId)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(validRequest())))
                    .andExpect(status().isBadRequest());

            verifyNoInteractions(attachmentService);
        }
    }

    @Nested
    @DisplayName("GET /attachments/{attachmentId}")
    class GetAttachment {

        @Test
        @WithMockUser
        void shouldReturnTheDownloadEnvelope() throws Exception {
            GetAttachmentResponseDto response = GetAttachmentResponseDto.builder()
                    .presignedUrl("https://s3.example.com/download")
                    .fileName("report.pdf")
                    .contentType("application/pdf")
                    .attachmentSize(1024L)
                    .attachmentId(attachmentId)
                    .build();
            when(attachmentService.getAttachment(workspaceId, projectId, taskId, attachmentId))
                    .thenReturn(response);

            mockMvc.perform(get(ITEM, workspaceId, projectId, taskId, attachmentId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.presignedUrl").value("https://s3.example.com/download"))
                    .andExpect(jsonPath("$.data.fileName").value("report.pdf"))
                    .andExpect(jsonPath("$.data.attachmentSize").value(1024))
                    .andExpect(jsonPath("$.data.attachmentId").value(attachmentId.toString()));
        }

        @Test
        @WithMockUser
        void shouldReturnNotFoundWhenTheAttachmentDoesNotExist() throws Exception {
            when(attachmentService.getAttachment(workspaceId, projectId, taskId, attachmentId))
                    .thenThrow(new AttachmentNotFoundException("Attachment not found with id " + attachmentId));

            mockMvc.perform(get(ITEM, workspaceId, projectId, taskId, attachmentId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @WithMockUser
        void shouldReturnConflictWhenTheAttachmentIsNotReady() throws Exception {
            when(attachmentService.getAttachment(workspaceId, projectId, taskId, attachmentId))
                    .thenThrow(new ConflictException("Attachment not ready with id " + attachmentId));

            mockMvc.perform(get(ITEM, workspaceId, projectId, taskId, attachmentId))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }

    @Nested
    @DisplayName("DELETE /attachments/{attachmentId}")
    class DeleteAttachment {

        @Test
        @WithMockUser
        void shouldReturnTheDeletionEnvelope() throws Exception {
            RemoveAttachmentResponseDto response = RemoveAttachmentResponseDto.builder()
                    .attachmentId(attachmentId)
                    .taskId(taskId)
                    .build();
            when(attachmentService.deleteAttachment(workspaceId, projectId, taskId, attachmentId))
                    .thenReturn(response);

            mockMvc.perform(delete(ITEM, workspaceId, projectId, taskId, attachmentId).with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Attachment deletion initialized"))
                    .andExpect(jsonPath("$.data.attachmentId").value(attachmentId.toString()))
                    .andExpect(jsonPath("$.data.taskId").value(taskId.toString()));
        }

        @Test
        @WithMockUser
        void shouldReturnNotFoundWhenTheAttachmentDoesNotExist() throws Exception {
            when(attachmentService.deleteAttachment(workspaceId, projectId, taskId, attachmentId))
                    .thenThrow(new AttachmentNotFoundException("Attachment not found with id " + attachmentId));

            mockMvc.perform(delete(ITEM, workspaceId, projectId, taskId, attachmentId).with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }
}
