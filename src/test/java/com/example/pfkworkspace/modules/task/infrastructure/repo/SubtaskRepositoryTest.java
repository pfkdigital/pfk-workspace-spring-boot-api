package com.example.pfkworkspace.modules.task.infrastructure.repo;

import com.example.pfkworkspace.TestcontainersConfiguration;
import com.example.pfkworkspace.config.PersistenceConfig;
import com.example.pfkworkspace.modules.project.domain.Project;
import com.example.pfkworkspace.modules.project.domain.ProjectStatus;
import com.example.pfkworkspace.modules.project.infrastructure.repo.ProjectRepository;
import com.example.pfkworkspace.modules.task.domain.Subtask;
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

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfiguration.class, PersistenceConfig.class})
class SubtaskRepositoryTest {

    @Autowired
    private SubtaskRepository subtaskRepository;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private WorkspaceRepository workspaceRepository;
    @Autowired
    private UserRepository userRepository;

    private Task taskA;
    private Task taskB;
    private Subtask subtaskA1;

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

        Workspace workspace = workspaceRepository.save(Workspace.builder().name("Workspace").owner(owner).build());
        Project project = projectRepository.save(
                Project.builder().name("Project").status(ProjectStatus.ACTIVE).workspace(workspace).build());

        taskA = taskRepository.save(
                Task.builder().project(project).workspace(workspace).title("Task A")
                        .status(TaskStatus.TODO).priority(TaskPriority.MEDIUM).build());
        taskB = taskRepository.save(
                Task.builder().project(project).workspace(workspace).title("Task B")
                        .status(TaskStatus.TODO).priority(TaskPriority.MEDIUM).build());

        subtaskA1 = subtaskRepository.save(Subtask.builder().task(taskA).title("Subtask A1").done(false).build());
        subtaskRepository.save(Subtask.builder().task(taskB).title("Subtask B1").done(false).build());
    }

    @Test
    void findByIdAndTaskId_WhenSubtaskBelongsToTask_ShouldReturnSubtask() {
        Optional<Subtask> result = subtaskRepository.findByIdAndTaskId(subtaskA1.getId(), taskA.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getTitle()).isEqualTo("Subtask A1");
    }

    @Test
    void findByIdAndTaskId_WhenSubtaskBelongsToDifferentTask_ShouldReturnEmpty() {
        Optional<Subtask> result = subtaskRepository.findByIdAndTaskId(subtaskA1.getId(), taskB.getId());

        assertThat(result).isEmpty();
    }

    @Test
    void findByIdAndTaskId_WhenSubtaskDoesNotExist_ShouldReturnEmpty() {
        Optional<Subtask> result = subtaskRepository.findByIdAndTaskId(UUID.randomUUID(), taskA.getId());

        assertThat(result).isEmpty();
    }
}
