package com.project.tekathon.drishti.repository;

import com.project.tekathon.drishti.entity.InvestigationEventEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<InvestigationEventEntity, String> {

    List<InvestigationEventEntity> findByCaseIdOrderByTimestampDesc(String caseId);

    List<InvestigationEventEntity> findByEntityIdOrderByTimestampDesc(String entityId);

    List<InvestigationEventEntity> findBySourceDocumentIdOrderByTimestampDesc(String sourceDocumentId);
}
