package com.project.tekathon.drishti.dto;

import java.time.Instant;

public final class DocumentDtos {

    private DocumentDtos() {
    }

    public record DocumentResponse(
            String documentId,
            String caseId,
            String documentType,
            String title,
            String text,
            String source,
            String language,
            String fileName,
            String contentType,
            Instant createdAt,
            Instant updatedAt) {
    }

    public record DocumentListItemResponse(
            String documentId,
            String caseId,
            String documentType,
            String title,
            String source,
            Instant createdAt) {
    }

    public record UploadDocumentResponse(
            String documentId,
            String caseId,
            String documentType,
            String title,
            String source,
            Instant createdAt,
            Object nlpResult) {
    }
}
