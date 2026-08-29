package com.example.pfkworkspace.modules.project.application.impl;

import com.example.pfkworkspace.modules.auth.application.UserContextService;
import com.example.pfkworkspace.modules.project.api.dto.*;
import com.example.pfkworkspace.modules.project.api.exception.ProjectNotFoundException;
import com.example.pfkworkspace.modules.project.application.mapper.ProjectMapper;
import com.example.pfkworkspace.modules.project.domain.Project;
import com.example.pfkworkspace.modules.project.domain.ProjectStatus;
import com.example.pfkworkspace.modules.project.infrastructure.repo.ProjectRepository;
import com.example.pfkworkspace.modules.task.infrastructure.repo.TaskRepository;
import com.example.pfkworkspace.modules.user.domain.User;
import com.example.pfkworkspace.modules.workspace.api.exception.WorkspaceNotFoundException;
import com.example.pfkworkspace.modules.workspace.domain.Workspace;
import com.example.pfkworkspace.modules.workspace.infrastructure.repo.WorkspaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceImplTest {

    @Mock
    private TaskRepository taskRepository;
    @Mock
    private UserContextService userContextService;
    @Mock
    private WorkspaceRepository workspaceRepository;
    @Mock
    private ProjectRepository projectRepository;
    @Mock
    private ProjectMapper projectMapper;

    @InjectMocks
    private ProjectServiceImpl projectService;

    private User currentUser;
    private UUID workspaceId;
    private Workspace workspace;
    private UUID projectId;

    @BeforeEach
    void setUp() {
        currentUser = User.builder()
                .email("owner@example.com")
                .username("owner")
                .firstName("Owner")
                .lastName("User")
                .build();
        currentUser.setId(UUID.randomUUID());

        workspaceId = UUID.randomUUID();
        workspace = Workspace.builder().name("Test Workspace").owner(currentUser).build();
        workspace.setId(workspaceId);

        projectId = UUID.randomUUID();
    }

    private Project projectInWorkspace(UUID ownerWorkspaceId) {
        Workspace projectWorkspace = Workspace.builder().name("Some Workspace").build();
        projectWorkspace.setId(ownerWorkspaceId);

        Project project = Project.builder()
                .name("Test Project")
                .status(ProjectStatus.ACTIVE)
                .workspace(projectWorkspace)
                .build();
        project.setId(projectId);
        return project;
    }

    @Test
    void createProject_WhenWorkspaceNotFound_ShouldThrow() {
        CreateProjectDtoRequest request = new CreateProjectDtoRequest("New Project", null, null, null, null);
        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.createProject(workspaceId, request))
                .isInstanceOf(WorkspaceNotFoundException.class);
    }

    @Test
    void createProject_WhenValid_ShouldCreateSuccessfully() {
        CreateProjectDtoRequest request = new CreateProjectDtoRequest(
                "New Project", "Description", "#10B981", LocalDate.of(2026, 1, 1), LocalDate.of(2026, 6, 1));

        when(userContextService.getCurrentUser()).thenReturn(currentUser);
        when(workspaceRepository.findById(workspaceId)).thenReturn(Optional.of(workspace));
        when(projectRepository.save(any(Project.class))).thenAnswer(invocation -> {
            Project project = invocation.getArgument(0);
            project.setId(projectId);
            return project;
        });

        CreateProjectResponseDto response = projectService.createProject(workspaceId, request);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(projectId);
        assertThat(response.getName()).isEqualTo("New Project");
        assertThat(response.getStatus()).isEqualTo(ProjectStatus.ACTIVE);
        assertThat(workspace.getProjects()).hasSize(1);
        verify(projectRepository).save(any(Project.class));
    }

    @Test
    void getProjects_ShouldReturnMappedSummaries() {
        Project project = projectInWorkspace(workspaceId);
        ProjectResponseDto summary = ProjectResponseDto.builder().id(projectId).name("Test Project").build();

        when(projectRepository.findByWorkspaceId(workspaceId)).thenReturn(List.of(project));
        when(projectMapper.toSummaryDto(project)).thenReturn(summary);

        List<ProjectResponseDto> results = projectService.getProjects(workspaceId);

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("Test Project");
    }

    @Test
    void getProjectDetail_WhenProjectNotFound_ShouldThrow() {
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.getProjectDetail(workspaceId, projectId))
                .isInstanceOf(ProjectNotFoundException.class);
    }

    @Test
    void getProjectDetail_WhenProjectBelongsToDifferentWorkspace_ShouldThrow() {
        Project project = projectInWorkspace(UUID.randomUUID());
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        assertThatThrownBy(() -> projectService.getProjectDetail(workspaceId, projectId))
                .isInstanceOf(ProjectNotFoundException.class);

        verifyNoInteractions(taskRepository);
    }

    @Test
    void getProjectDetail_WhenValid_ShouldReturnDetail() {
        Project project = projectInWorkspace(workspaceId);
        List<TaskRepository.TaskStatusCount> statusCounts = List.of();
        ProjectDetailResponseDto detail = ProjectDetailResponseDto.builder().id(projectId).build();

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(taskRepository.countByProjectIdGroupByStatus(projectId)).thenReturn(statusCounts);
        when(projectMapper.toDetailDto(project, statusCounts)).thenReturn(detail);

        ProjectDetailResponseDto result = projectService.getProjectDetail(workspaceId, projectId);

        assertThat(result).isEqualTo(detail);
    }

    @Test
    void updateProject_WhenProjectNotFound_ShouldThrow() {
        UpdateProjectRequestDto request = new UpdateProjectRequestDto("Updated", null, null, null, null, null);
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.updateProject(workspaceId, projectId, request))
                .isInstanceOf(ProjectNotFoundException.class);
    }

    @Test
    void updateProject_WhenProjectBelongsToDifferentWorkspace_ShouldThrow() {
        Project project = projectInWorkspace(UUID.randomUUID());
        UpdateProjectRequestDto request = new UpdateProjectRequestDto("Updated", null, null, null, null, null);
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        assertThatThrownBy(() -> projectService.updateProject(workspaceId, projectId, request))
                .isInstanceOf(ProjectNotFoundException.class);

        verify(projectRepository, never()).save(any());
    }

    @Test
    void updateProject_WhenValid_ShouldUpdateSuccessfully() {
        Project project = projectInWorkspace(workspaceId);
        UpdateProjectRequestDto request = new UpdateProjectRequestDto(
                "Updated Name", "Updated Desc", "#3B82F6", ProjectStatus.COMPLETED,
                LocalDate.of(2026, 2, 1), LocalDate.of(2026, 8, 1));

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectRepository.save(project)).thenReturn(project);

        UpdateProjectResponseDto response = projectService.updateProject(workspaceId, projectId, request);

        assertThat(response.getName()).isEqualTo("Updated Name");
        assertThat(response.getStatus()).isEqualTo(ProjectStatus.COMPLETED);
        assertThat(project.getDescription()).isEqualTo("Updated Desc");
    }

    @Test
    void deleteProject_WhenProjectNotFound_ShouldThrow() {
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.deleteProject(workspaceId, projectId))
                .isInstanceOf(ProjectNotFoundException.class);
    }

    @Test
    void deleteProject_WhenProjectBelongsToDifferentWorkspace_ShouldThrow() {
        Project project = projectInWorkspace(UUID.randomUUID());
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        assertThatThrownBy(() -> projectService.deleteProject(workspaceId, projectId))
                .isInstanceOf(ProjectNotFoundException.class);

        verify(projectRepository, never()).delete(any());
    }

    @Test
    void deleteProject_WhenValid_ShouldDelete() {
        Project project = projectInWorkspace(workspaceId);
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        projectService.deleteProject(workspaceId, projectId);

        verify(projectRepository).delete(project);
    }

    @Test
    void archiveProject_WhenProjectNotFound_ShouldThrow() {
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.archiveProject(workspaceId, projectId))
                .isInstanceOf(ProjectNotFoundException.class);
    }

    @Test
    void archiveProject_WhenProjectBelongsToDifferentWorkspace_ShouldThrow() {
        Project project = projectInWorkspace(UUID.randomUUID());
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));

        assertThatThrownBy(() -> projectService.archiveProject(workspaceId, projectId))
                .isInstanceOf(ProjectNotFoundException.class);

        verify(projectRepository, never()).save(any());
    }

    @Test
    void archiveProject_WhenValid_ShouldArchive() {
        Project project = projectInWorkspace(workspaceId);
        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(projectRepository.save(project)).thenReturn(project);

        ArchiveProjectResponseDto response = projectService.archiveProject(workspaceId, projectId);

        assertThat(response.getStatus()).isEqualTo(ProjectStatus.ARCHIVED);
        assertThat(response.getArchivedAt()).isNotNull();
        assertThat(project.getStatus()).isEqualTo(ProjectStatus.ARCHIVED);
    }
}
