package com.example.pfkworkspace.modules.label.domain;

import com.example.pfkworkspace.common.persistence.BaseEntity;
import com.example.pfkworkspace.modules.project.domain.Project;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "labels",
    indexes = {@Index(name = "labels_project_id_idx", columnList = "project_id")},
    uniqueConstraints = {
      @UniqueConstraint(
          name = "uk_labels_project_id_name",
          columnNames = {"project_id", "name"})
    })
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Label extends BaseEntity {

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "project_id", nullable = false)
  private Project project;

  @Column(name = "name", nullable = false)
  private String name;

  @Column(name = "color", nullable = false)
  private String color;
}
