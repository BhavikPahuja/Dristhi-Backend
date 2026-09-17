package com.project.tekathon.drishti.controller;

import com.project.tekathon.drishti.dto.InvestigationDtos.CdrResponse;
import com.project.tekathon.drishti.dto.InvestigationDtos.CreateCdrRequest;
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
@RequestMapping("/api/cdr")
@RequiredArgsConstructor
public class CdrController {

    private final InvestigationDataService investigationDataService;

    @PostMapping
    public ResponseEntity<CdrResponse> create(@RequestBody CreateCdrRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(investigationDataService.createCdr(request));
    }

    @PostMapping("/bulk")
    public List<CdrResponse> bulk(@RequestBody List<CreateCdrRequest> requests) {
        return investigationDataService.bulkCdr(requests);
    }

    @GetMapping("/{cdrId}")
    public CdrResponse get(@PathVariable String cdrId) {
        return investigationDataService.getCdr(cdrId);
    }
}
