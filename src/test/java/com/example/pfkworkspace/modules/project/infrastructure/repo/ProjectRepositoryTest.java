package com.example.pfkworkspace.modules.project.infrastructure.repo;

import com.example.pfkworkspace.TestcontainersConfiguration;
import com.example.pfkworkspace.config.PersistenceConfig;
import com.example.pfkworkspace.modules.project.domain.Project;
import com.example.pfkworkspace.modules.project.domain.ProjectStatus;
import com.example.pfkworkspace.modules.user.domain.User;
import com.example.pfkworkspace.modules.user.infrastructure.repo.UserRepository;
import com.example.pfkworkspace.modules.workspace.domain.Workspace;
import com.example.pfkworkspace.modules.workspace.infrastructure.repo.WorkspaceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfiguration.class, PersistenceConfig.class})
class ProjectRepositoryTest {

    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private WorkspaceRepository workspaceRepository;
    @Autowired
    private UserRepository userRepository;

    private Workspace workspaceA;
    private Workspace workspaceB;

    @BeforeEach
    void setUp() {
        User owner = userRepository.save(
                User.builder()
                        .email("owner-" + UUID.randomUUID() + "@example.com")
                        .username("owner-" + UUID.randomUUID())
                        .firstName("Owner")
                        .lastName("User")
                        .passwordHash("hashed")
                        .build());

        workspaceA = workspaceRepository.save(
                Workspace.builder().name("Workspace A").owner(owner).build());
        workspaceB = workspaceRepository.save(
                Workspace.builder().name("Workspace B").owner(owner).build());

        projectRepository.save(
                Project.builder().name("Project A1").status(ProjectStatus.ACTIVE).workspace(workspaceA).build());
        projectRepository.save(
                Project.builder().name("Project A2").status(ProjectStatus.ACTIVE).workspace(workspaceA).build());
        projectRepository.save(
                Project.builder().name("Project B1").status(ProjectStatus.ACTIVE).workspace(workspaceB).build());
    }

    @Test
    void findByWorkspaceId_ShouldReturnOnlyProjectsForThatWorkspace() {
        List<Project> results = projectRepository.findByWorkspaceId(workspaceA.getId());

        assertThat(results).hasSize(2);
        assertThat(results).extracting(Project::getName)
                .containsExactlyInAnyOrder("Project A1", "Project A2");
        assertThat(results).allMatch(project -> project.getWorkspace().getId().equals(workspaceA.getId()));
    }

    @Test
    void findByWorkspaceId_WhenWorkspaceHasNoProjects_ShouldReturnEmptyList() {
        Workspace emptyWorkspace = workspaceRepository.save(
                Workspace.builder().name("Empty Workspace").owner(workspaceA.getOwner()).build());

        List<Project> results = projectRepository.findByWorkspaceId(emptyWorkspace.getId());

        assertThat(results).isEmpty();
    }
}
