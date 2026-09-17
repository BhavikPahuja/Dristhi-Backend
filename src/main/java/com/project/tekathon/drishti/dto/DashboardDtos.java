package com.project.tekathon.drishti.dto;

import java.util.List;

public final class DashboardDtos {

    private DashboardDtos() {
    }

    public record DashboardSummaryResponse(
            long totalCases,
            long totalPersons,
            long totalVehicles,
            long totalPhones,
            long totalAccounts,
            long totalTransactions,
            long totalCalls,
            long totalRelationships,
            long totalAnomalies) {
    }

    public record DashboardRecentCase(String caseId, String title, String status, String createdAt) {
    }

    public record DashboardStatisticsResponse(List<String> labels, List<Long> values) {
    }
}
