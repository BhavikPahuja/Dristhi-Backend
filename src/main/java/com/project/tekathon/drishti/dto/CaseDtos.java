package com.project.tekathon.drishti.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public final class CaseDtos {

    private CaseDtos() {
    }

    public record CreateCaseRequest(
            @NotBlank @Size(max = 255) String title,
            @Size(max = 4000) String description,
            @NotBlank @Size(max = 32) String status) {
    }

    public record UpdateCaseRequest(
            @NotBlank @Size(max = 255) String title,
            @Size(max = 4000) String description,
            @NotBlank @Size(max = 32) String status) {
    }

    public record CaseResponse(String caseId, String title, String description, String status, Instant createdAt, Instant updatedAt) {
    }

    public record CaseListItemResponse(String caseId, String title, String status, Instant createdAt) {
    }
}
