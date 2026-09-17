package com.project.tekathon.drishti.repository;

import com.project.tekathon.drishti.entity.PhoneEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PhoneRepository extends JpaRepository<PhoneEntity, String> {

    Optional<PhoneEntity> findByPhoneNumber(String phoneNumber);

    List<PhoneEntity> findByOwnerPersonId(String ownerPersonId);
}
