package com.project.tekathon.drishti.repository;

import com.project.tekathon.drishti.entity.TransactionEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository<TransactionEntity, String> {

    List<TransactionEntity> findByCaseIdOrderByTimestampDesc(String caseId);

    List<TransactionEntity> findBySourceAccountIdOrDestinationAccountIdOrderByTimestampDesc(String sourceAccountId, String destinationAccountId);
}
