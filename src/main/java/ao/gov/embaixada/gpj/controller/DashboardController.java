package ao.gov.embaixada.gpj.controller;

import ao.gov.embaixada.commons.dto.ApiResponse;
import ao.gov.embaixada.gpj.dto.BurndownResponse;
import ao.gov.embaixada.gpj.dto.CapacityResponse;
import ao.gov.embaixada.gpj.dto.DashboardResponse;
import ao.gov.embaixada.gpj.dto.ProjectReportResponse;
import ao.gov.embaixada.gpj.dto.VelocityResponse;
import ao.gov.embaixada.gpj.service.DashboardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<DashboardResponse>> getDashboard() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getDashboard()));
    }

    @GetMapping("/capacity")
    public ResponseEntity<ApiResponse<List<CapacityResponse>>> getCapacity() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getCapacity()));
    }

    @GetMapping("/burndown/{sprintId}")
    public ResponseEntity<ApiResponse<BurndownResponse>> getBurndown(@PathVariable UUID sprintId) {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getBurndown(sprintId)));
    }

    @GetMapping("/velocity/{sprintId}")
    public ResponseEntity<ApiResponse<VelocityResponse>> getVelocity(@PathVariable UUID sprintId) {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getVelocity(sprintId)));
    }

    @GetMapping("/velocity")
    public ResponseEntity<ApiResponse<List<VelocityResponse>>> getVelocityHistory() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getVelocityHistory()));
    }

    @GetMapping("/report")
    public ResponseEntity<ApiResponse<ProjectReportResponse>> getProjectReport() {
        return ResponseEntity.ok(ApiResponse.success(dashboardService.getProjectReport()));
    }
}
