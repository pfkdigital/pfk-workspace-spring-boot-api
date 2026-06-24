package com.example.pfkworkspace.config;

import com.example.pfkworkspace.modules.user.domain.User;
import com.example.pfkworkspace.modules.user.infrastructure.repo.UserRepository;
import com.example.pfkworkspace.modules.workspace.domain.Workspace;
import com.example.pfkworkspace.modules.workspace.domain.WorkspaceMember;
import com.example.pfkworkspace.modules.workspace.domain.WorkspaceRole;
import com.example.pfkworkspace.modules.workspace.infrastructure.repo.WorkspaceRepository;
import java.time.Instant;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Profile("local")
@RequiredArgsConstructor
@Slf4j
public class DataSeeder implements ApplicationRunner {

  private final UserRepository userRepository;
  private final WorkspaceRepository workspaceRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  @Transactional
  public void run(ApplicationArguments args) {
    if (userRepository.existsByUsername("alice")) {
      log.debug("Seed data already present, skipping");
      return;
    }

    User alice = createUser("alice", "alice@example.com", "Alice", "Johnson");
    User bob = createUser("bob", "bob@example.com", "Bob", "Smith");
    User charlie = createUser("charlie", "charlie@example.com", "Charlie", "Brown");
    User diana = createUser("diana", "diana@example.com", "Diana", "Prince");

    userRepository.save(alice);
    userRepository.save(bob);
    userRepository.save(charlie);
    userRepository.save(diana);

    seedWorkspace(
        "PFK Digital",
        "Main company workspace for the PFK Digital team",
        alice,
        new MemberEntry(bob, WorkspaceRole.ADMIN),
        new MemberEntry(charlie, WorkspaceRole.MEMBER),
        new MemberEntry(diana, WorkspaceRole.MEMBER));

    seedWorkspace(
        "Side Project",
        "Experimental workspace for internal tooling",
        bob,
        new MemberEntry(alice, WorkspaceRole.ADMIN),
        new MemberEntry(charlie, WorkspaceRole.MEMBER));

    seedWorkspace(
        "Design System",
        "Shared design system workspace",
        diana,
        new MemberEntry(alice, WorkspaceRole.MEMBER));

    log.info("Seed data created: 4 users, 3 workspaces");
  }

  private User createUser(String username, String email, String firstName, String lastName) {
    User user = User.builder()
        .username(username)
        .email(email)
        .firstName(firstName)
        .lastName(lastName)
        .passwordHash(passwordEncoder.encode("Password123!"))
        .roles(Set.of("ROLE_USER"))
        .build();
    user.markEmailVerified();
    return user;
  }

  private void seedWorkspace(String name, String description, User owner, MemberEntry... members) {
    Workspace workspace = workspaceRepository.save(
        Workspace.builder()
            .name(name)
            .description(description)
            .owner(owner)
            .build());

    workspace.addWorkspaceMember(WorkspaceMember.builder()
        .user(owner)
        .role(WorkspaceRole.OWNER)
        .joinedAt(Instant.now())
        .build());

    for (MemberEntry entry : members) {
      workspace.addWorkspaceMember(WorkspaceMember.builder()
          .user(entry.user())
          .role(entry.role())
          .joinedAt(Instant.now())
          .build());
    }
  }

  private record MemberEntry(User user, WorkspaceRole role) {}
}
