package com.project.tekathon.drishti.repository;

import com.project.tekathon.drishti.entity.EvidenceEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EvidenceRepository extends JpaRepository<EvidenceEntity, String> {

    List<EvidenceEntity> findByCaseIdOrderByCreatedAtDesc(String caseId);

    List<EvidenceEntity> findByEntityIdOrderByCreatedAtDesc(String entityId);

    List<EvidenceEntity> findByRelationshipIdOrderByCreatedAtDesc(String relationshipId);
}
