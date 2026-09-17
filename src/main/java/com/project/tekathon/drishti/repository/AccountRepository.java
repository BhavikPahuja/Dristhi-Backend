package com.project.tekathon.drishti.repository;

import com.project.tekathon.drishti.entity.AccountEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountRepository extends JpaRepository<AccountEntity, String> {

    Optional<AccountEntity> findByAccountNumber(String accountNumber);

    List<AccountEntity> findByOwnerPersonId(String ownerPersonId);
}
