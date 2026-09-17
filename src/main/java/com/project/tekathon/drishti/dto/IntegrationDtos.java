package com.project.tekathon.drishti.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public final class IntegrationDtos {

    private IntegrationDtos() {
    }

    public record NlpExtractRequest(
            @NotBlank String documentId,
            @NotBlank String caseId,
            @NotBlank String documentType,
            @NotBlank String language,
            @NotBlank String text,
            Map<String, Object> metadata) {
    }

    public record NlpExtractionEvidence(String text, Integer start, Integer end) {
    }

    public record NlpEntityResult(
            String mention,
            String type,
            String canonicalId,
            Double confidence,
            NlpExtractionEvidence evidence) {
    }

    public record NlpRelationshipResult(
            String sourceId,
            String relation,
            String targetId,
            Double confidence,
            Map<String, Object> evidence) {
    }

    public record NlpEventResult(
            String type,
            LocalDate date,
            String locationId,
            List<String> participants) {
    }

    public record NlpExtractionResponse(
            String documentId,
            String caseId,
            List<NlpEntityResult> entities,
            List<NlpRelationshipResult> relationships,
            List<NlpEventResult> events) {
    }

    public record EntityResolutionRequest(
            @NotNull Map<String, Object> entity,
            @NotEmpty List<Map<String, Object>> candidateEntities) {
    }

    public record EntityResolutionResponse(Boolean resolved, String canonicalId, Double confidence, String status) {
    }

    public record MlAnalyzeRequest(
            @NotBlank String caseId,
            @NotEmpty List<String> entityIds,
            @NotEmpty List<String> analysisTypes) {
    }

    public record MlAnomalyResult(String entityId, String anomalyType, Double anomalyScore, String description) {
    }

    public record MlKeyActorResult(String entityId, Double centralityScore, String role) {
    }

    public record MlCommunityResult(String communityId, List<String> memberIds) {
    }

    public record MlPotentialLinkResult(String sourceId, String targetId, String predictedRelationship, Double probability) {
    }

    public record MlAnalyzeResponse(
            String caseId,
            List<MlAnomalyResult> anomalies,
            List<MlKeyActorResult> keyActors,
            List<MlCommunityResult> communities,
            List<MlPotentialLinkResult> potentialLinks) {
    }

    public record AgentInvestigateRequest(
            @NotBlank String sessionId,
            @NotBlank String caseId,
            @NotBlank String query) {
    }

    public record AgentFindingResult(String type, String description) {
    }

    public record AgentEvidenceResult(String documentId, String description) {
    }

    public record AgentInvestigateResponse(
            String sessionId,
            String caseId,
            String answer,
            List<AgentFindingResult> findings,
            List<AgentEvidenceResult> evidence,
            List<String> entities,
            List<String> relatedCases) {
    }

    public record AgentSessionResponse(String sessionId, String caseId, List<Map<String, Object>> messages) {
    }
}
