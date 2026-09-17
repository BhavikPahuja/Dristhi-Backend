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
@Table(name = "evidence")
public class EvidenceEntity extends BaseEntity {

    @Id
    @Column(nullable = false, updatable = false, length = 32)
    private String evidenceId;

    @Column(nullable = false)
    private String caseId;

    @Column(nullable = false)
    private String type;

    @Column(length = 4000)
    private String description;

    private String sourceDocumentId;

    private String entityId;

    private String relationshipId;

    @Column(length = 4000)
    private String sourceSpanText;
}
