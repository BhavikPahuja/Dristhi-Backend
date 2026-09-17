package com.project.tekathon.drishti.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
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
@Table(name = "relationships")
public class RelationshipEntity extends BaseEntity {

    @Id
    @Column(nullable = false, updatable = false, length = 32)
    private String relationshipId;

    @Column(nullable = false)
    private String caseId;

    @Column(nullable = false)
    private String sourceId;

    @Column(nullable = false)
    private String targetId;

    @Column(nullable = false)
    private String relationship;

    private Double confidence;

    private Instant firstObserved;

    private Instant lastObserved;

    @Column(length = 4000)
    private String supportText;

    @Column(length = 4000)
    private String evidenceIdsJson;

    @Column(length = 4000)
    private String sourceDocumentIdsJson;
}
