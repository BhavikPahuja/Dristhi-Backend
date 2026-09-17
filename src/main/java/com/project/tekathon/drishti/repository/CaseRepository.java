package com.project.tekathon.drishti.repository;

import com.project.tekathon.drishti.entity.CaseEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CaseRepository extends JpaRepository<CaseEntity, String> {
}
