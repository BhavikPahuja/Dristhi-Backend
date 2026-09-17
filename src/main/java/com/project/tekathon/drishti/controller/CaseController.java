package com.project.tekathon.drishti.controller;

import com.project.tekathon.drishti.dto.CaseDtos.CaseListItemResponse;
import com.project.tekathon.drishti.dto.CaseDtos.CaseResponse;
import com.project.tekathon.drishti.dto.CaseDtos.CreateCaseRequest;
import com.project.tekathon.drishti.dto.CaseDtos.UpdateCaseRequest;
import com.project.tekathon.drishti.dto.InvestigationDtos.SurveillanceResponse;
import com.project.tekathon.drishti.service.CaseService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/cases")
@RequiredArgsConstructor
public class CaseController {

    private final CaseService caseService;

    @PostMapping
    public ResponseEntity<CaseResponse> create(@RequestBody CreateCaseRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(caseService.create(request));
    }

    @GetMapping
    public List<CaseListItemResponse> list() {
        return caseService.list();
    }

    @GetMapping("/{caseId}")
    public CaseResponse get(@PathVariable String caseId) {
        return caseService.get(caseId);
    }

    @PutMapping("/{caseId}")
    public CaseResponse update(@PathVariable String caseId, @RequestBody UpdateCaseRequest request) {
        return caseService.update(caseId, request);
    }

    @DeleteMapping("/{caseId}")
    public ResponseEntity<Void> delete(@PathVariable String caseId) {
        caseService.delete(caseId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{caseId}/surveillance")
    public List<SurveillanceResponse> surveillance(@PathVariable String caseId) {
        return caseService.surveillance(caseId);
    }

}
