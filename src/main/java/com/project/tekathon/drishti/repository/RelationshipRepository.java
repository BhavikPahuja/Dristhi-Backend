package com.project.tekathon.drishti.repository;

import com.project.tekathon.drishti.entity.RelationshipEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RelationshipRepository extends JpaRepository<RelationshipEntity, String> {

    List<RelationshipEntity> findByCaseIdOrderByCreatedAtDesc(String caseId);

    List<RelationshipEntity> findBySourceIdOrTargetId(String sourceId, String targetId);
}
