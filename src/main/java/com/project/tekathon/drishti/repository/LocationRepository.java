package com.project.tekathon.drishti.repository;

import com.project.tekathon.drishti.entity.LocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<LocationEntity, String> {
}
