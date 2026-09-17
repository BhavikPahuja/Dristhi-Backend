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
@Table(name = "vehicles")
public class VehicleEntity extends BaseEntity {

    @Id
    @Column(nullable = false, updatable = false, length = 32)
    private String vehicleId;

    @Column(nullable = false, unique = true)
    private String registrationNumber;

    private String make;

    private String model;

    private String ownerPersonId;
}
