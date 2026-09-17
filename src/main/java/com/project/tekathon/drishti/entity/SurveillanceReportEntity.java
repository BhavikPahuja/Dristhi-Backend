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
@Table(name = "surveillance_reports")
public class SurveillanceReportEntity extends BaseEntity {

    @Id
    @Column(nullable = false, updatable = false, length = 32)
    private String reportId;

    @Column(nullable = false)
    private String caseId;

    @Column(length = 4000)
    private String description;

    @Column(nullable = false)
    private Instant timestamp;

    private String locationId;

    private String vehicleId;

    private String personId;

    private String source;
}
