package com.example.pfkworkspace.modules.task.infrastructure.repo;

import com.example.pfkworkspace.TestcontainersConfiguration;
import com.example.pfkworkspace.config.PersistenceConfig;
import com.example.pfkworkspace.modules.project.domain.Project;
import com.example.pfkworkspace.modules.project.domain.ProjectStatus;
import com.example.pfkworkspace.modules.project.infrastructure.repo.ProjectRepository;
import com.example.pfkworkspace.modules.task.domain.Comment;
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
class CommentRepositoryTest {

    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private TaskRepository taskRepository;
    @Autowired
    private ProjectRepository projectRepository;
    @Autowired
    private WorkspaceRepository workspaceRepository;
    @Autowired
    private UserRepository userRepository;

    private Task task;
    private User author;

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

        author = userRepository.save(
                User.builder()
                        .email("author-" + UUID.randomUUID() + "@example.com")
                        .username("author-" + UUID.randomUUID())
                        .firstName("Author")
                        .lastName("User")
                        .passwordHash("hashed")
                        .build());

        Workspace workspace = workspaceRepository.save(Workspace.builder().name("Workspace").owner(owner).build());
        Project project = projectRepository.save(
                Project.builder().name("Project").status(ProjectStatus.ACTIVE).workspace(workspace).build());

        task = taskRepository.save(
                Task.builder().project(project).workspace(workspace).title("Task")
                        .status(TaskStatus.TODO).priority(TaskPriority.MEDIUM).build());
    }

    @Test
    void save_ShouldPersistCommentWithAssociationsAndAuditFields() {
        Comment comment = commentRepository.save(Comment.builder().task(task).author(author).body("Hello").build());

        Optional<Comment> result = commentRepository.findById(comment.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getBody()).isEqualTo("Hello");
        assertThat(result.get().getTask().getId()).isEqualTo(task.getId());
        assertThat(result.get().getAuthor().getId()).isEqualTo(author.getId());
        assertThat(result.get().getCreatedAt()).isNotNull();
        assertThat(result.get().getUpdatedAt()).isNotNull();
    }

    @Test
    void findById_WhenCommentDoesNotExist_ShouldReturnEmpty() {
        Optional<Comment> result = commentRepository.findById(UUID.randomUUID());

        assertThat(result).isEmpty();
    }

    @Test
    void delete_ShouldRemoveComment() {
        Comment comment = commentRepository.save(Comment.builder().task(task).author(author).body("Hello").build());

        commentRepository.delete(comment);

        assertThat(commentRepository.findById(comment.getId())).isEmpty();
    }
}
