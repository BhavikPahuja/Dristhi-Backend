package com.project.tekathon.drishti.repository;

import com.project.tekathon.drishti.entity.MlKeyActorEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MlKeyActorRepository extends JpaRepository<MlKeyActorEntity, String> {

    List<MlKeyActorEntity> findByCaseIdOrderByCreatedAtDesc(String caseId);
}
