package com.example.pfkworkspace.modules.email.domain;

import com.example.pfkworkspace.common.persistence.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.Map;

@Entity
@Table(name = "email_outbox")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class EmailOutbox extends BaseEntity {

    @Column(name = "recipient", nullable = false)
    private String recipient;

    @Column(name = "email_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private EmailType emailType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private Map<String, String> payload;

    @Column(name = "status", nullable = false)
    @Enumerated(EnumType.STRING)
    private EmailOutboxStatus status;

    @Column(name = "attempts")
    private Integer attempts;

    @Column(name = "last_error")
    private String lastError;
}
