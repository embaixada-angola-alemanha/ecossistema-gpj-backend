package ao.gov.embaixada.gpj.controller;

import ao.gov.embaixada.commons.dto.ApiResponse;
import ao.gov.embaixada.commons.dto.PagedResponse;
import ao.gov.embaixada.gpj.dto.MaintenanceCreateRequest;
import ao.gov.embaixada.gpj.dto.MaintenanceResponse;
import ao.gov.embaixada.gpj.dto.MaintenanceUpdateRequest;
import ao.gov.embaixada.gpj.enums.MaintenanceStatus;
import ao.gov.embaixada.gpj.service.MaintenanceWindowService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.UUID;

@RestController
@RequestMapping("/api/maintenance")
public class MaintenanceController {

    private final MaintenanceWindowService maintenanceService;

    public MaintenanceController(MaintenanceWindowService maintenanceService) {
        this.maintenanceService = maintenanceService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('GOP-ADMIN', 'GOP-OPERATOR', 'GOP-VIEWER')")
    public ResponseEntity<ApiResponse<PagedResponse<MaintenanceResponse>>> findAll(
            @RequestParam(required = false) MaintenanceStatus status,
            Pageable pageable) {
        Page<MaintenanceResponse> page = maintenanceService.findAll(status, pageable);
        return ResponseEntity.ok(ApiResponse.success(PagedResponse.of(page)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('GOP-ADMIN', 'GOP-OPERATOR', 'GOP-VIEWER')")
    public ResponseEntity<ApiResponse<MaintenanceResponse>> findById(@PathVariable UUID id) {
        MaintenanceResponse maintenance = maintenanceService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(maintenance));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('GOP-ADMIN', 'GOP-OPERATOR')")
    public ResponseEntity<ApiResponse<MaintenanceResponse>> create(
            @Valid @RequestBody MaintenanceCreateRequest request,
            Principal principal) {
        String createdByUser = principal != null ? principal.getName() : "unknown";
        MaintenanceResponse created = maintenanceService.create(request, createdByUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Maintenance window created", created));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('GOP-ADMIN', 'GOP-OPERATOR')")
    public ResponseEntity<ApiResponse<MaintenanceResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody MaintenanceUpdateRequest request) {
        MaintenanceResponse updated = maintenanceService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Maintenance window updated", updated));
    }

    @PostMapping("/{id}/start")
    @PreAuthorize("hasAnyRole('GOP-ADMIN', 'GOP-OPERATOR')")
    public ResponseEntity<ApiResponse<MaintenanceResponse>> start(@PathVariable UUID id) {
        MaintenanceResponse started = maintenanceService.start(id);
        return ResponseEntity.ok(ApiResponse.success("Maintenance started", started));
    }

    @PostMapping("/{id}/complete")
    @PreAuthorize("hasAnyRole('GOP-ADMIN', 'GOP-OPERATOR')")
    public ResponseEntity<ApiResponse<MaintenanceResponse>> complete(@PathVariable UUID id) {
        MaintenanceResponse completed = maintenanceService.complete(id);
        return ResponseEntity.ok(ApiResponse.success("Maintenance completed", completed));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('GOP-ADMIN', 'GOP-OPERATOR')")
    public ResponseEntity<ApiResponse<MaintenanceResponse>> cancel(@PathVariable UUID id) {
        MaintenanceResponse cancelled = maintenanceService.cancel(id);
        return ResponseEntity.ok(ApiResponse.success("Maintenance cancelled", cancelled));
    }
}
