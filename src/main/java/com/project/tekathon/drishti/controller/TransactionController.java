package com.project.tekathon.drishti.controller;

import com.project.tekathon.drishti.dto.InvestigationDtos.CreateTransactionRequest;
import com.project.tekathon.drishti.dto.InvestigationDtos.TransactionResponse;
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
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final InvestigationDataService investigationDataService;

    @PostMapping
    public ResponseEntity<TransactionResponse> create(@RequestBody CreateTransactionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(investigationDataService.createTransaction(request));
    }

    @PostMapping("/bulk")
    public List<TransactionResponse> bulk(@RequestBody List<CreateTransactionRequest> requests) {
        return investigationDataService.bulkTransactions(requests);
    }

    @GetMapping("/{transactionId}")
    public TransactionResponse get(@PathVariable String transactionId) {
        return investigationDataService.getTransaction(transactionId);
    }
}
