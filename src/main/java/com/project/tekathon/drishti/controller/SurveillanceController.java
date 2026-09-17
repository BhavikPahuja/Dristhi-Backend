package com.project.tekathon.drishti.controller;

import com.project.tekathon.drishti.dto.InvestigationDtos.CreateSurveillanceRequest;
import com.project.tekathon.drishti.dto.InvestigationDtos.SurveillanceResponse;
import com.project.tekathon.drishti.service.InvestigationDataService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SurveillanceController {

    private final InvestigationDataService investigationDataService;

    @PostMapping("/surveillance")
    public ResponseEntity<SurveillanceResponse> create(@RequestBody CreateSurveillanceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(investigationDataService.createSurveillance(request));
    }

    @GetMapping("/surveillance/{reportId}")
    public SurveillanceResponse get(@PathVariable String reportId) {
        return investigationDataService.getSurveillance(reportId);
    }
}
