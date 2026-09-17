package com.project.tekathon.drishti.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "investigation_sessions")
public class InvestigationSessionEntity extends BaseEntity {

    @Id
    @Column(nullable = false, updatable = false, length = 32)
    private String sessionId;

    private String caseId;

    @Column(length = 4000)
    private String query;

    @Column(length = 4000)
    private String answer;
}
