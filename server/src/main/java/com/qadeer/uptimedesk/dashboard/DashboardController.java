package com.qadeer.uptimedesk.dashboard;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardSummaryService dashboardSummaryService;

    public DashboardController(DashboardSummaryService dashboardSummaryService) {
        this.dashboardSummaryService = dashboardSummaryService;
    }

    @GetMapping("/summary")
    DashboardSummaryResponse getSummary() {
        return dashboardSummaryService.getSummary();
    }
}
