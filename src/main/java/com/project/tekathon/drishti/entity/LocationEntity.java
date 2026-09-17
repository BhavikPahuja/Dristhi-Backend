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
@Table(name = "locations")
public class LocationEntity extends BaseEntity {

    @Id
    @Column(nullable = false, updatable = false, length = 32)
    private String locationId;

    @Column(nullable = false)
    private String name;

    @Column(length = 2000)
    private String address;

    private Double latitude;

    private Double longitude;
}
