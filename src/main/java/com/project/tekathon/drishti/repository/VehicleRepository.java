package com.project.tekathon.drishti.repository;

import com.project.tekathon.drishti.entity.VehicleEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VehicleRepository extends JpaRepository<VehicleEntity, String> {

    Optional<VehicleEntity> findByRegistrationNumber(String registrationNumber);

    List<VehicleEntity> findByOwnerPersonId(String ownerPersonId);
}
