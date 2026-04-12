package com.example.pfkworkspace.modules.workspace.domain;

import com.example.pfkworkspace.common.persistence.BaseEntity;
import com.example.pfkworkspace.modules.user.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
    name = "workspaces",
    indexes = {@Index(name = "idx_workspaces_owner_user_id", columnList = "owner_user_id")})
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Workspace extends BaseEntity {
  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "description")
  private String description;

  @Column(name = "image_url")
  private String imageUrl;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "owner_user_id", nullable = false)
  private User owner;

  @OneToMany(mappedBy = "workspace", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<WorkspaceMember> workspaceMembers = new ArrayList<>();

  @OneToMany(mappedBy = "workspace", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<WorkspaceInvitation> workspaceInvitations = new ArrayList<>();

  public void addWorkspaceMember(WorkspaceMember workspaceMember) {
    workspaceMember.setWorkspace(this);
    workspaceMembers.add(workspaceMember);
  }

  public void addWorkspaceInvitation(WorkspaceInvitation workspaceInvitation) {
    workspaceInvitation.setWorkspace(this);
    workspaceInvitations.add(workspaceInvitation);
  }

  public void removeWorkspaceMember(WorkspaceMember workspaceMember) {
    workspaceMembers.remove(workspaceMember);
    workspaceMember.setWorkspace(null);
  }
}
