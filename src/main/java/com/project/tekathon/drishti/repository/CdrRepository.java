package com.project.tekathon.drishti.repository;

import com.project.tekathon.drishti.entity.CdrEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CdrRepository extends JpaRepository<CdrEntity, String> {

    List<CdrEntity> findByCaseIdOrderByTimestampDesc(String caseId);

    List<CdrEntity> findByCallerPhoneIdOrReceiverPhoneIdOrderByTimestampDesc(String callerPhoneId, String receiverPhoneId);
}
