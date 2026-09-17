package com.project.tekathon.drishti.repository;

import com.project.tekathon.drishti.entity.MlPotentialLinkEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MlPotentialLinkRepository extends JpaRepository<MlPotentialLinkEntity, String> {

    List<MlPotentialLinkEntity> findByCaseIdOrderByCreatedAtDesc(String caseId);
}
