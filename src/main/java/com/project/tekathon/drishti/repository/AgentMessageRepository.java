package com.project.tekathon.drishti.repository;

import com.project.tekathon.drishti.entity.AgentMessageEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AgentMessageRepository extends JpaRepository<AgentMessageEntity, String> {

    List<AgentMessageEntity> findBySessionIdOrderByCreatedAtAsc(String sessionId);
}
