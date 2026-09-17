package com.project.tekathon.drishti.service;

import com.project.tekathon.drishti.client.AgentServiceClient;
import com.project.tekathon.drishti.client.MlServiceClient;
import com.project.tekathon.drishti.client.NlpServiceClient;
import com.project.tekathon.drishti.dto.IntegrationDtos.AgentInvestigateRequest;
import com.project.tekathon.drishti.dto.IntegrationDtos.AgentInvestigateResponse;
import com.project.tekathon.drishti.dto.IntegrationDtos.AgentSessionResponse;
import com.project.tekathon.drishti.dto.IntegrationDtos.EntityResolutionRequest;
import com.project.tekathon.drishti.dto.IntegrationDtos.EntityResolutionResponse;
import com.project.tekathon.drishti.dto.IntegrationDtos.MlAnomalyResult;
import com.project.tekathon.drishti.dto.IntegrationDtos.MlAnalyzeRequest;
import com.project.tekathon.drishti.dto.IntegrationDtos.MlAnalyzeResponse;
import com.project.tekathon.drishti.dto.IntegrationDtos.MlCommunityResult;
import com.project.tekathon.drishti.dto.IntegrationDtos.MlKeyActorResult;
import com.project.tekathon.drishti.dto.IntegrationDtos.MlPotentialLinkResult;
import com.project.tekathon.drishti.dto.IntegrationDtos.NlpExtractRequest;
import com.project.tekathon.drishti.dto.IntegrationDtos.NlpExtractionResponse;
import com.project.tekathon.drishti.entity.AgentMessageEntity;
import com.project.tekathon.drishti.entity.InvestigationSessionEntity;
import com.project.tekathon.drishti.entity.MlAnalysisEntity;
import com.project.tekathon.drishti.entity.MlAnomalyEntity;
import com.project.tekathon.drishti.entity.MlCommunityEntity;
import com.project.tekathon.drishti.entity.MlKeyActorEntity;
import com.project.tekathon.drishti.entity.MlPotentialLinkEntity;
import com.project.tekathon.drishti.repository.AgentMessageRepository;
import com.project.tekathon.drishti.repository.InvestigationSessionRepository;
import com.project.tekathon.drishti.repository.MlAnalysisRepository;
import com.project.tekathon.drishti.repository.MlAnomalyRepository;
import com.project.tekathon.drishti.repository.MlCommunityRepository;
import com.project.tekathon.drishti.repository.MlKeyActorRepository;
import com.project.tekathon.drishti.repository.MlPotentialLinkRepository;
import com.project.tekathon.drishti.util.IdGenerator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class IntegrationService {

    private final NlpServiceClient nlpServiceClient;
    private final MlServiceClient mlServiceClient;
    private final AgentServiceClient agentServiceClient;
    private final InvestigationSessionRepository sessionRepository;
    private final AgentMessageRepository messageRepository;
    private final MlAnalysisRepository mlAnalysisRepository;
    private final MlAnomalyRepository mlAnomalyRepository;
    private final MlKeyActorRepository mlKeyActorRepository;
    private final MlCommunityRepository mlCommunityRepository;
    private final MlPotentialLinkRepository mlPotentialLinkRepository;

    public NlpExtractionResponse extract(NlpExtractRequest request) {
        return nlpServiceClient.extract(request);
    }

    public EntityResolutionResponse resolve(EntityResolutionRequest request) {
        return nlpServiceClient.resolve(request);
    }

    public MlAnalyzeResponse analyze(MlAnalyzeRequest request) {
        MlAnalyzeResponse response = mlServiceClient.analyze(request);
        MlAnalysisEntity analysis = MlAnalysisEntity.builder()
                .analysisId(IdGenerator.next("MLA", mlAnalysisRepository.count() + 1))
                .caseId(response.caseId())
                .resultJson("{}")
                .build();
        mlAnalysisRepository.save(analysis);
        if (response.anomalies() != null) {
            response.anomalies().forEach(anomaly -> mlAnomalyRepository.save(MlAnomalyEntity.builder()
                    .anomalyId(IdGenerator.next("MLAN", mlAnomalyRepository.count() + 1))
                    .caseId(response.caseId())
                    .entityId(anomaly.entityId())
                    .anomalyType(anomaly.anomalyType())
                    .anomalyScore(anomaly.anomalyScore())
                    .description(anomaly.description())
                    .build()));
        }
        if (response.keyActors() != null) {
            response.keyActors().forEach(keyActor -> mlKeyActorRepository.save(MlKeyActorEntity.builder()
                    .keyActorId(IdGenerator.next("MLK", mlKeyActorRepository.count() + 1))
                    .caseId(response.caseId())
                    .entityId(keyActor.entityId())
                    .centralityScore(keyActor.centralityScore())
                    .role(keyActor.role())
                    .build()));
        }
        if (response.communities() != null) {
            response.communities().forEach(community -> mlCommunityRepository.save(MlCommunityEntity.builder()
                    .communityId(IdGenerator.next("MLC", mlCommunityRepository.count() + 1))
                    .caseId(response.caseId())
                    .memberIdsJson(community.memberIds() == null ? "[]" : community.memberIds().toString())
                    .build()));
        }
        if (response.potentialLinks() != null) {
            response.potentialLinks().forEach(link -> mlPotentialLinkRepository.save(MlPotentialLinkEntity.builder()
                    .linkId(IdGenerator.next("MLPL", mlPotentialLinkRepository.count() + 1))
                    .caseId(response.caseId())
                    .sourceId(link.sourceId())
                    .targetId(link.targetId())
                    .predictedRelationship(link.predictedRelationship())
                    .probability(link.probability())
                    .build()));
        }
        return response;
    }

    public List<MlAnomalyResult> anomalies() {
        return mlAnomalyRepository.findAll().stream()
                .map(anomaly -> new MlAnomalyResult(anomaly.getEntityId(), anomaly.getAnomalyType(), anomaly.getAnomalyScore(), anomaly.getDescription()))
                .toList();
    }

    public List<MlAnomalyResult> personAnomalies(String personId) {
        return mlAnomalyRepository.findByEntityIdOrderByCreatedAtDesc(personId).stream()
                .map(anomaly -> new MlAnomalyResult(anomaly.getEntityId(), anomaly.getAnomalyType(), anomaly.getAnomalyScore(), anomaly.getDescription()))
                .toList();
    }

    public List<MlKeyActorResult> keyActors() {
        return mlKeyActorRepository.findAll().stream()
                .map(keyActor -> new MlKeyActorResult(keyActor.getEntityId(), keyActor.getCentralityScore(), keyActor.getRole()))
                .toList();
    }

    public List<MlCommunityResult> communities() {
        return mlCommunityRepository.findAll().stream()
                .map(community -> new MlCommunityResult(community.getCommunityId(),
                        community.getMemberIdsJson() == null ? List.of() : List.of(community.getMemberIdsJson())))
                .toList();
    }

    public List<MlPotentialLinkResult> potentialLinks() {
        return mlPotentialLinkRepository.findAll().stream()
                .map(link -> new MlPotentialLinkResult(link.getSourceId(), link.getTargetId(), link.getPredictedRelationship(), link.getProbability()))
                .toList();
    }

    public AgentInvestigateResponse investigate(AgentInvestigateRequest request) {
        AgentInvestigateResponse response = agentServiceClient.investigate(request);
        InvestigationSessionEntity session = InvestigationSessionEntity.builder()
                .sessionId(request.sessionId())
                .caseId(request.caseId())
                .query(request.query())
                .answer(response.answer())
                .build();
        sessionRepository.save(session);
        messageRepository.save(AgentMessageEntity.builder()
                .messageId(IdGenerator.next("MSG", messageRepository.count() + 1))
                .sessionId(request.sessionId())
                .role("user")
                .content(request.query())
                .build());
        messageRepository.save(AgentMessageEntity.builder()
                .messageId(IdGenerator.next("MSG", messageRepository.count() + 1))
                .sessionId(request.sessionId())
                .role("assistant")
                .content(response.answer())
                .build());
        return response;
    }

    public AgentSessionResponse session(String sessionId) {
        List<Map<String, Object>> messages = new java.util.ArrayList<>();
        messageRepository.findBySessionIdOrderByCreatedAtAsc(sessionId).forEach(message -> {
            Map<String, Object> item = new HashMap<>();
            item.put("role", message.getRole());
            item.put("content", message.getContent());
            item.put("createdAt", message.getCreatedAt());
            messages.add(item);
        });
        return new AgentSessionResponse(sessionId, sessionRepository.findById(sessionId).map(InvestigationSessionEntity::getCaseId).orElse(null), messages);
    }
}
