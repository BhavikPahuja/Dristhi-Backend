package com.project.tekathon.drishti.controller;

import com.project.tekathon.drishti.dto.DashboardDtos.DashboardSummaryResponse;
import com.project.tekathon.drishti.service.DashboardService;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @GetMapping("/summary")
    public DashboardSummaryResponse summary() {
        return dashboardService.summary();
    }

    @GetMapping("/recent-cases")
    public List<?> recentCases() {
        return dashboardService.recentCases();
    }

    @GetMapping("/anomalies")
    public List<Map<String, Object>> anomalies() {
        return dashboardService.anomalies();
    }

    @GetMapping("/network-statistics")
    public Map<String, Object> networkStatistics() {
        return dashboardService.networkStatistics();
    }
}
