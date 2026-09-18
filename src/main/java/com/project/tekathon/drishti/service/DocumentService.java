package com.project.tekathon.drishti.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.project.tekathon.drishti.client.NlpServiceClient;
import com.project.tekathon.drishti.dto.DocumentDtos.DocumentListItemResponse;
import com.project.tekathon.drishti.dto.DocumentDtos.DocumentResponse;
import com.project.tekathon.drishti.dto.DocumentDtos.UploadDocumentResponse;
import com.project.tekathon.drishti.dto.IntegrationDtos.NlpExtractRequest;
import com.project.tekathon.drishti.dto.IntegrationDtos.NlpExtractionResponse;
import com.project.tekathon.drishti.entity.DocumentEntity;
import com.project.tekathon.drishti.exception.BadRequestException;
import com.project.tekathon.drishti.exception.ResourceNotFoundException;
import com.project.tekathon.drishti.repository.DocumentRepository;
import com.project.tekathon.drishti.util.IdGenerator;
import com.project.tekathon.drishti.util.TextExtractionUtils;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final CaseService caseService;
    private final InvestigationDataService investigationDataService;
    private final NetworkService networkService;
    private final NlpServiceClient nlpServiceClient;
    private final ObjectMapper objectMapper;

    public UploadDocumentResponse upload(String caseId, MultipartFile file, String documentType, String title, String source, String language) {
        caseService.findCase(caseId);
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Document file is required");
        }
        String extractedText = TextExtractionUtils.extractText(file);
        DocumentEntity document = DocumentEntity.builder()
                .documentId(IdGenerator.next("DOC", documentRepository.count() + 1))
                .caseId(caseId)
                .documentType(documentType == null ? "OTHER" : documentType)
                .title(title == null || title.isBlank() ? file.getOriginalFilename() : title)
                .text(extractedText)
                .source(source == null || source.isBlank() ? "UPLOAD" : source)
                .language(language == null || language.isBlank() ? "en" : language)
                .fileName(file.getOriginalFilename())
                .contentType(file.getContentType())
                .build();
        DocumentEntity saved = documentRepository.save(document);
        networkService.syncDocument(saved);

        NlpExtractionResponse nlpResult;
        try {
            nlpResult = nlpServiceClient.extract(new NlpExtractRequest(
                    saved.getDocumentId(),
                    caseId,
                    saved.getDocumentType(),
                    saved.getLanguage(),
                    extractedText,
                    Map.of(
                            "source", saved.getSource(),
                            "createdAt", saved.getCreatedAt() == null ? Instant.now().toString() : saved.getCreatedAt().toString())));
            saveNlpResult(saved, nlpResult);
            investigationDataService.applyNlpExtraction(caseId, saved.getDocumentId(), nlpResult.entities(), nlpResult.relationships(), nlpResult.events());
        } catch (Exception ex) {
            log.warn("NLP extraction failed or timed out for document {}: {}. Proceeding without NLP enhancement.", saved.getDocumentId(), ex.getMessage());
            nlpResult = new NlpExtractionResponse(saved.getDocumentId(), caseId, List.of(), List.of(), List.of());
        }

        return new UploadDocumentResponse(saved.getDocumentId(), saved.getCaseId(), saved.getDocumentType(),
                saved.getTitle(), saved.getSource(), saved.getCreatedAt(), nlpResult);
    }

    public List<DocumentListItemResponse> listByCase(String caseId) {
        caseService.findCase(caseId);
        return documentRepository.findByCaseIdOrderByCreatedAtDesc(caseId).stream()
                .map(document -> new DocumentListItemResponse(document.getDocumentId(), document.getCaseId(),
                        document.getDocumentType(), document.getTitle(), document.getSource(), document.getCreatedAt()))
                .toList();
    }

    public DocumentResponse get(String documentId) {
        DocumentEntity entity = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document " + documentId + " not found"));
        return new DocumentResponse(entity.getDocumentId(), entity.getCaseId(), entity.getDocumentType(), entity.getTitle(),
                entity.getText(), entity.getSource(), entity.getLanguage(), entity.getFileName(), entity.getContentType(),
                entity.getCreatedAt(), entity.getUpdatedAt());
    }

    public void delete(String documentId) {
        DocumentEntity entity = documentRepository.findById(documentId)
                .orElseThrow(() -> new ResourceNotFoundException("Document " + documentId + " not found"));
        documentRepository.delete(entity);
    }

    private void saveNlpResult(DocumentEntity document, NlpExtractionResponse nlpResult) {
        try {
            document.setNlpResultJson(objectMapper.writeValueAsString(nlpResult));
            documentRepository.save(document);
        } catch (JsonProcessingException ex) {
            log.warn("Could not serialize NLP result for {}", document.getDocumentId(), ex);
        }
    }
}
