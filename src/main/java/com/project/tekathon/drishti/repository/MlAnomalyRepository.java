package com.project.tekathon.drishti.repository;

import com.project.tekathon.drishti.entity.MlAnomalyEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MlAnomalyRepository extends JpaRepository<MlAnomalyEntity, String> {

    List<MlAnomalyEntity> findByCaseIdOrderByCreatedAtDesc(String caseId);

    List<MlAnomalyEntity> findByEntityIdOrderByCreatedAtDesc(String entityId);
}
