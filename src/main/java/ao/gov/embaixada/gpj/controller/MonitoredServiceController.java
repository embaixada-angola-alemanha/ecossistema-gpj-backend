package ao.gov.embaixada.gpj.controller;

import ao.gov.embaixada.commons.dto.ApiResponse;
import ao.gov.embaixada.commons.dto.PagedResponse;
import ao.gov.embaixada.gpj.dto.HealthCheckLogResponse;
import ao.gov.embaixada.gpj.dto.MonitoredServiceCreateRequest;
import ao.gov.embaixada.gpj.dto.MonitoredServiceResponse;
import ao.gov.embaixada.gpj.dto.MonitoredServiceUpdateRequest;
import ao.gov.embaixada.gpj.dto.UptimeResponse;
import ao.gov.embaixada.gpj.service.HealthCheckService;
import ao.gov.embaixada.gpj.service.MonitoredServiceService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/services")
public class MonitoredServiceController {

    private final MonitoredServiceService serviceService;
    private final HealthCheckService healthCheckService;

    public MonitoredServiceController(MonitoredServiceService serviceService,
                                      HealthCheckService healthCheckService) {
        this.serviceService = serviceService;
        this.healthCheckService = healthCheckService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('GOP-ADMIN', 'GOP-OPERATOR', 'GOP-VIEWER')")
    public ResponseEntity<ApiResponse<List<MonitoredServiceResponse>>> findAll() {
        List<MonitoredServiceResponse> services = serviceService.findAll();
        return ResponseEntity.ok(ApiResponse.success(services));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('GOP-ADMIN', 'GOP-OPERATOR', 'GOP-VIEWER')")
    public ResponseEntity<ApiResponse<MonitoredServiceResponse>> findById(@PathVariable UUID id) {
        MonitoredServiceResponse service = serviceService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(service));
    }

    @GetMapping("/{id}/health-history")
    @PreAuthorize("hasAnyRole('GOP-ADMIN', 'GOP-OPERATOR', 'GOP-VIEWER')")
    public ResponseEntity<ApiResponse<PagedResponse<HealthCheckLogResponse>>> getHealthHistory(
            @PathVariable UUID id, Pageable pageable) {
        Page<HealthCheckLogResponse> page = healthCheckService.getHealthHistory(id, pageable);
        return ResponseEntity.ok(ApiResponse.success(PagedResponse.of(page)));
    }

    @GetMapping("/{id}/uptime")
    @PreAuthorize("hasAnyRole('GOP-ADMIN', 'GOP-OPERATOR', 'GOP-VIEWER')")
    public ResponseEntity<ApiResponse<UptimeResponse>> getUptime(@PathVariable UUID id) {
        UptimeResponse uptime = healthCheckService.calculateUptime(id);
        return ResponseEntity.ok(ApiResponse.success(uptime));
    }

    @PostMapping
    @PreAuthorize("hasRole('GOP-ADMIN')")
    public ResponseEntity<ApiResponse<MonitoredServiceResponse>> create(
            @Valid @RequestBody MonitoredServiceCreateRequest request) {
        MonitoredServiceResponse created = serviceService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Service created", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('GOP-ADMIN')")
    public ResponseEntity<ApiResponse<MonitoredServiceResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody MonitoredServiceUpdateRequest request) {
        MonitoredServiceResponse updated = serviceService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Service updated", updated));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('GOP-ADMIN')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        serviceService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Service deleted", null));
    }
}
