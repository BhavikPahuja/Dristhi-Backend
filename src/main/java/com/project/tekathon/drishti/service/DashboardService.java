package com.project.tekathon.drishti.service;

import com.project.tekathon.drishti.dto.CaseDtos.CaseListItemResponse;
import com.project.tekathon.drishti.dto.DashboardDtos.DashboardSummaryResponse;
import com.project.tekathon.drishti.repository.MlAnomalyRepository;
import java.util.LinkedHashMap;
import com.project.tekathon.drishti.repository.AccountRepository;
import com.project.tekathon.drishti.repository.CaseRepository;
import com.project.tekathon.drishti.repository.CdrRepository;
import com.project.tekathon.drishti.repository.PersonRepository;
import com.project.tekathon.drishti.repository.PhoneRepository;
import com.project.tekathon.drishti.repository.RelationshipRepository;
import com.project.tekathon.drishti.repository.TransactionRepository;
import com.project.tekathon.drishti.repository.VehicleRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final CaseRepository caseRepository;
    private final PersonRepository personRepository;
    private final VehicleRepository vehicleRepository;
    private final PhoneRepository phoneRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final CdrRepository cdrRepository;
    private final RelationshipRepository relationshipRepository;
    private final MlAnomalyRepository anomalyRepository;
    private final CaseService caseService;

    public DashboardSummaryResponse summary() {
        return new DashboardSummaryResponse(
                caseRepository.count(),
                personRepository.count(),
                vehicleRepository.count(),
                phoneRepository.count(),
                accountRepository.count(),
                transactionRepository.count(),
                cdrRepository.count(),
                relationshipRepository.count(),
                anomalyRepository.count());
    }

    public List<CaseListItemResponse> recentCases() {
        return caseRepository.findAll().stream()
                .sorted((left, right) -> right.getCreatedAt().compareTo(left.getCreatedAt()))
                .limit(10)
                .map(caseEntity -> new CaseListItemResponse(caseEntity.getCaseId(), caseEntity.getTitle(), caseEntity.getStatus(), caseEntity.getCreatedAt()))
                .toList();
    }

    public List<java.util.Map<String, Object>> anomalies() {
        return anomalyRepository.findAll().stream().map(anomaly -> {
            java.util.Map<String, Object> item = new LinkedHashMap<>();
            item.put("anomalyId", anomaly.getAnomalyId());
            item.put("caseId", anomaly.getCaseId());
            item.put("entityId", anomaly.getEntityId());
            item.put("anomalyType", anomaly.getAnomalyType());
            item.put("anomalyScore", anomaly.getAnomalyScore());
            item.put("description", anomaly.getDescription());
            return item;
        }).toList();
    }

    public java.util.Map<String, Object> networkStatistics() {
        java.util.Map<String, Object> stats = new LinkedHashMap<>();
        stats.put("relationships", relationshipRepository.count());
        stats.put("people", personRepository.count());
        stats.put("phones", phoneRepository.count());
        stats.put("vehicles", vehicleRepository.count());
        stats.put("accounts", accountRepository.count());
        return stats;
    }
}
