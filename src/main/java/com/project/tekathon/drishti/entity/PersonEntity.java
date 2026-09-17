package com.project.tekathon.drishti.entity;

import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.JoinColumn;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
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
@Table(name = "persons")
public class PersonEntity extends BaseEntity {

    @Id
    @Column(nullable = false, updatable = false, length = 32)
    private String personId;

    @Column(nullable = false)
    private String name;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "person_aliases", joinColumns = @JoinColumn(name = "person_id"))
    @Column(name = "alias")
    @Builder.Default
    private List<String> aliases = new ArrayList<>();

    private Integer age;

    private LocalDate dateOfBirth;

    @Column(length = 2000)
    private String address;

    @Column(nullable = false)
    private String status;

    @Column(length = 4000)
    private String notes;
}
