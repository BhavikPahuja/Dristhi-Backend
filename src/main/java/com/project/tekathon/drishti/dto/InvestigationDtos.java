package com.project.tekathon.drishti.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

public final class InvestigationDtos {

    private InvestigationDtos() {
    }

    public record CreateCdrRequest(
            @NotBlank String callerPhoneId,
            @NotBlank String receiverPhoneId,
            @NotNull Instant timestamp,
            Integer durationSeconds,
            @NotBlank String caseId) {
    }

    public record CdrResponse(
            String cdrId,
            String callerPhoneId,
            String receiverPhoneId,
            Instant timestamp,
            Integer durationSeconds,
            String caseId) {
    }

    public record CreateTransactionRequest(
            @NotBlank String sourceAccountId,
            @NotBlank String destinationAccountId,
            @NotNull BigDecimal amount,
            @NotBlank String currency,
            @NotNull Instant timestamp,
            String description,
            @NotBlank String caseId) {
    }

    public record TransactionResponse(
            String transactionId,
            String sourceAccountId,
            String destinationAccountId,
            BigDecimal amount,
            String currency,
            Instant timestamp,
            String description,
            String caseId) {
    }

    public record CreateVehicleRequest(
            @NotBlank String registrationNumber,
            String make,
            String model,
            String ownerPersonId) {
    }

    public record VehicleResponse(
            String vehicleId,
            String registrationNumber,
            String make,
            String model,
            String ownerPersonId) {
    }

    public record CreateLocationRequest(
            @NotBlank String name,
            String address,
            Double latitude,
            Double longitude) {
    }

    public record LocationResponse(String locationId, String name, String address, Double latitude, Double longitude) {
    }

    public record CreateSurveillanceRequest(
            @NotBlank String caseId,
            String description,
            @NotNull Instant timestamp,
            String locationId,
            String vehicleId,
            String personId,
            String source) {
    }

    public record SurveillanceResponse(
            String reportId,
            String caseId,
            String description,
            Instant timestamp,
            String locationId,
            String vehicleId,
            String personId,
            String source) {
    }

    public record CreateEventRequest(
            @NotBlank String caseId,
            @NotBlank String eventType,
            @NotNull Instant timestamp,
            String locationId,
            String description,
            String sourceDocumentId,
            String entityId) {
    }

    public record EventResponse(
            String eventId,
            String caseId,
            String eventType,
            Instant timestamp,
            String locationId,
            String description,
            String sourceDocumentId,
            String entityId,
            String source) {
    }

    public record LocationEventRequest(
            @NotBlank String personId,
            @NotBlank String locationId,
            @NotNull Instant timestamp,
            String source) {
    }

    public record CreateEvidenceRequest(
            @NotBlank String caseId,
            @NotBlank String type,
            String description,
            String sourceDocumentId,
            String entityId,
            String relationshipId,
            String sourceSpanText) {
    }

    public record EvidenceResponse(
            String evidenceId,
            String caseId,
            String type,
            String description,
            String sourceDocumentId,
            String entityId,
            String relationshipId,
            String sourceSpanText) {
    }

    public record RelationshipResponse(
            String relationshipId,
            String caseId,
            String sourceId,
            String targetId,
            String relationship,
            Double confidence,
            Instant firstObserved,
            Instant lastObserved,
            String supportText,
            List<String> evidenceIds,
            List<String> sourceDocumentIds) {
    }
}
