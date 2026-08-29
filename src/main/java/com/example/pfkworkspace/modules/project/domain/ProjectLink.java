package com.example.pfkworkspace.modules.project.domain;


import com.example.pfkworkspace.common.persistence.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "project_links",
        indexes = {
                @Index(name = "project_links_project_id_idx", columnList = "project_id")
        }
)
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProjectLink extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @Column(name = "label", nullable = false)
    private String label;

    @Column(name = "url", nullable = false)
    private String url;

    @Column(name = "icon")
    private String icon;
}
