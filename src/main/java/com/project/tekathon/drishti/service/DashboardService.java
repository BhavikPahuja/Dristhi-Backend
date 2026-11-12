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
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
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
                count("cases", caseRepository::count),
                count("persons", personRepository::count),
                count("vehicles", vehicleRepository::count),
                count("phones", phoneRepository::count),
                count("accounts", accountRepository::count),
                count("transactions", transactionRepository::count),
                count("calls", cdrRepository::count),
                count("relationships", relationshipRepository::count),
                count("anomalies", anomalyRepository::count));
    }

    public List<CaseListItemResponse> recentCases() {
        return caseRepository.findAll().stream()
                .sorted((left, right) -> right.getCreatedAt().compareTo(left.getCreatedAt()))
                .limit(10)
                .map(caseEntity -> new CaseListItemResponse(caseEntity.getCaseId(), caseEntity.getTitle(),
                        caseEntity.getStatus(), caseEntity.getCreatedAt()))
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
        stats.put("relationships", count("relationships", relationshipRepository::count));
        stats.put("people", count("people", personRepository::count));
        stats.put("phones", count("phones", phoneRepository::count));
        stats.put("vehicles", count("vehicles", vehicleRepository::count));
        stats.put("accounts", count("accounts", accountRepository::count));
        return stats;
    }

    private long count(String resource, java.util.function.LongSupplier query) {
        try {
            return query.getAsLong();
        } catch (DataAccessException ex) {
            log.error("Dashboard count failed resource={} returning zero rootCause={}", resource,
                    rootCause(ex).toString(), ex);
            return 0;
        }
    }

    private Throwable rootCause(Throwable throwable) {
        Throwable current = throwable;
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        return current;
    }
}
