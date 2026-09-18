package com.project.tekathon.drishti.controller;

import com.project.tekathon.drishti.dto.DocumentDtos.DocumentListItemResponse;
import com.project.tekathon.drishti.dto.DocumentDtos.DocumentResponse;
import com.project.tekathon.drishti.dto.DocumentDtos.UploadDocumentResponse;
import com.project.tekathon.drishti.service.DocumentService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DocumentController {

    private final DocumentService documentService;

    @PostMapping(value = "/cases/{caseId}/documents/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadDocumentResponse> upload(
            @PathVariable String caseId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "documentType", required = false) String documentType,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "source", required = false) String source,
            @RequestParam(value = "language", required = false) String language) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(documentService.upload(caseId, file, documentType, title, source, language));
    }

    @PostMapping(value = "/documents/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadDocumentResponse> uploadDirect(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "caseId", required = false) String caseId,
            @RequestParam(value = "documentType", required = false) String documentType,
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "source", required = false) String source,
            @RequestParam(value = "language", required = false) String language) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(documentService.upload(caseId, file, documentType, title, source, language));
    }

    @GetMapping("/cases/{caseId}/documents")
    public List<DocumentListItemResponse> listCaseDocuments(@PathVariable String caseId) {
        return documentService.listByCase(caseId);
    }

    @GetMapping("/documents/{documentId}")
    public DocumentResponse get(@PathVariable String documentId) {
        return documentService.get(documentId);
    }

    @DeleteMapping("/documents/{documentId}")
    public ResponseEntity<Void> delete(@PathVariable String documentId) {
        documentService.delete(documentId);
        return ResponseEntity.noContent().build();
    }
}
