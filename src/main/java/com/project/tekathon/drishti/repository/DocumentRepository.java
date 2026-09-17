package com.project.tekathon.drishti.repository;

import com.project.tekathon.drishti.entity.DocumentEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<DocumentEntity, String> {

    List<DocumentEntity> findByCaseIdOrderByCreatedAtDesc(String caseId);
}
