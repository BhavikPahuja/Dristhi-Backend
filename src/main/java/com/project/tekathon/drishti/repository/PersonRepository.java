package com.project.tekathon.drishti.repository;

import com.project.tekathon.drishti.entity.PersonEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonRepository extends JpaRepository<PersonEntity, String> {

    List<PersonEntity> findByNameContainingIgnoreCase(String name);
}
