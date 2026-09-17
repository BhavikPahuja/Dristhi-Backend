package com.project.tekathon.drishti.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public final class PersonDtos {

    private PersonDtos() {
    }

    public record PersonResponse(
            String personId,
            String name,
            List<String> aliases,
            Integer age,
            LocalDate dateOfBirth,
            String address,
            String status,
            Instant createdAt,
            Instant updatedAt) {
    }

    public record PersonSearchResponse(String personId, String name, List<String> aliases, Double confidence) {
    }

    public record PersonProfileResponse(
            String personId,
            String name,
            List<String> aliases,
            Integer age,
            LocalDate dateOfBirth,
            String address,
            List<String> phones,
            List<String> vehicles,
            List<String> accounts,
            List<String> relatedCases) {
    }

    public record PersonConnectionResponse(
            String sourceId,
            String relationship,
            String targetId,
            Double confidence,
            Instant firstObserved,
            Instant lastObserved) {
    }

    public record PersonTimelineItemResponse(
            String eventId,
            String type,
            Instant timestamp,
            String description,
            String sourceDocumentId) {
    }
}
