package com.project.tekathon.drishti.repository;

import com.project.tekathon.drishti.entity.MlCommunityEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MlCommunityRepository extends JpaRepository<MlCommunityEntity, String> {

    List<MlCommunityEntity> findByCaseIdOrderByCreatedAtDesc(String caseId);
}
