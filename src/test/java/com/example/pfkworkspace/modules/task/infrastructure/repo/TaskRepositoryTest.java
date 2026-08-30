package com.example.pfkworkspace.modules.task.infrastructure.repo;

import com.example.pfkworkspace.TestcontainersConfiguration;
import com.example.pfkworkspace.config.PersistenceConfig;
import com.example.pfkworkspace.modules.project.domain.Project;
import com.example.pfkworkspace.modules.project.domain.ProjectStatus;
import com.example.pfkworkspace.modules.project.infrastructure.repo.ProjectRepository;
import com.example.pfkworkspace.modules.task.domain.Task;
import com.example.pfkworkspace.modules.task.domain.TaskPriority;
import com.example.pfkworkspace.modules.task.domain.TaskStatus;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfiguration.class, PersistenceConfig.class})
class TaskRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private WorkspaceRepository workspaceRepository;
    @Autowired
    private UserRepository userRepository;

    private Workspace workspace;
    private Project projectA;
    private Project projectB;
    private User assignee;

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

        assignee = userRepository.save(
                User.builder()
                        .email("assignee-" + UUID.randomUUID() + "@example.com")
                        .username("assignee-" + UUID.randomUUID())
                        .firstName("Assignee")
                        .lastName("User")
                        .passwordHash("hashed")
                        .build());

        workspace = workspaceRepository.save(Workspace.builder().name("Workspace").owner(owner).build());

        projectA = projectRepository.save(
                Project.builder().name("Project A").status(ProjectStatus.ACTIVE).workspace(workspace).build());
        projectB = projectRepository.save(
                Project.builder().name("Project B").status(ProjectStatus.ACTIVE).workspace(workspace).build());

        taskRepository.save(newTask(projectA, "Task A1", TaskStatus.TODO));
        taskRepository.save(newTask(projectA, "Task A2", TaskStatus.DONE));
        taskRepository.save(newTask(projectB, "Task B1", TaskStatus.TODO));
    }

    private Task newTask(Project project, String title, TaskStatus status) {
        return Task.builder()
                .project(project)
                .workspace(workspace)
                .title(title)
                .description("Description")
                .status(status)
                .priority(TaskPriority.MEDIUM)
                .assignee(assignee)
                .dueDate(LocalDate.now().plusDays(7))
                .build();
    }

    @Test
    void findAllByProjectId_ShouldReturnOnlyTasksForThatProject() {
        Page<Task> results = taskRepository.findAllByProjectId(projectA.getId(), PageRequest.of(0, 10));

        assertThat(results.getContent()).hasSize(2);
        assertThat(results.getContent()).extracting(Task::getTitle)
                .containsExactlyInAnyOrder("Task A1", "Task A2");
    }

    @Test
    void findAllByProjectId_WhenProjectHasNoTasks_ShouldReturnEmptyPage() {
        Project emptyProject = projectRepository.save(
                Project.builder().name("Empty").status(ProjectStatus.ACTIVE).workspace(workspace).build());

        Page<Task> results = taskRepository.findAllByProjectId(emptyProject.getId(), PageRequest.of(0, 10));

        assertThat(results.getContent()).isEmpty();
    }

    @Test
    void findByIdAndProjectId_WhenTaskBelongsToProject_ShouldReturnTask() {
        Task task = taskRepository.findAllByProjectId(projectA.getId(), PageRequest.of(0, 10))
                .getContent().get(0);

        Optional<Task> result = taskRepository.findByIdAndProjectId(task.getId(), projectA.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(task.getId());
    }

    @Test
    void findByIdAndProjectId_WhenTaskBelongsToDifferentProject_ShouldReturnEmpty() {
        Task task = taskRepository.findAllByProjectId(projectA.getId(), PageRequest.of(0, 10))
                .getContent().get(0);

        Optional<Task> result = taskRepository.findByIdAndProjectId(task.getId(), projectB.getId());

        assertThat(result).isEmpty();
    }

    @Test
    void countByProjectIdGroupByStatus_ShouldGroupTasksByStatus() {
        List<TaskRepository.TaskStatusCount> counts = taskRepository.countByProjectIdGroupByStatus(projectA.getId());

        assertThat(counts).hasSize(2);
        assertThat(counts)
                .filteredOn(count -> count.getStatus() == TaskStatus.TODO)
                .extracting(TaskRepository.TaskStatusCount::getCount)
                .containsExactly(1L);
        assertThat(counts)
                .filteredOn(count -> count.getStatus() == TaskStatus.DONE)
                .extracting(TaskRepository.TaskStatusCount::getCount)
                .containsExactly(1L);
    }
}
