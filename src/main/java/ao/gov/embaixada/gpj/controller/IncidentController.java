package ao.gov.embaixada.gpj.controller;

import ao.gov.embaixada.commons.dto.ApiResponse;
import ao.gov.embaixada.commons.dto.PagedResponse;
import ao.gov.embaixada.gpj.dto.IncidentCreateRequest;
import ao.gov.embaixada.gpj.dto.IncidentResolveRequest;
import ao.gov.embaixada.gpj.dto.IncidentResponse;
import ao.gov.embaixada.gpj.dto.IncidentUpdateRequest;
import ao.gov.embaixada.gpj.enums.IncidentSeverity;
import ao.gov.embaixada.gpj.enums.IncidentStatus;
import ao.gov.embaixada.gpj.service.IncidentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/incidents")
public class IncidentController {

    private final IncidentService incidentService;

    public IncidentController(IncidentService incidentService) {
        this.incidentService = incidentService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('GOP-ADMIN', 'GOP-OPERATOR', 'GOP-VIEWER')")
    public ResponseEntity<ApiResponse<PagedResponse<IncidentResponse>>> findAll(
            @RequestParam(required = false) IncidentStatus status,
            @RequestParam(required = false) IncidentSeverity severity,
            Pageable pageable) {
        Page<IncidentResponse> page = incidentService.findAll(status, severity, pageable);
        return ResponseEntity.ok(ApiResponse.success(PagedResponse.of(page)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('GOP-ADMIN', 'GOP-OPERATOR', 'GOP-VIEWER')")
    public ResponseEntity<ApiResponse<IncidentResponse>> findById(@PathVariable UUID id) {
        IncidentResponse incident = incidentService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(incident));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('GOP-ADMIN', 'GOP-OPERATOR')")
    public ResponseEntity<ApiResponse<IncidentResponse>> create(
            @Valid @RequestBody IncidentCreateRequest request,
            Principal principal) {
        String reportedBy = principal != null ? principal.getName() : "unknown";
        IncidentResponse created = incidentService.create(request, reportedBy);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Incident created", created));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('GOP-ADMIN', 'GOP-OPERATOR')")
    public ResponseEntity<ApiResponse<IncidentResponse>> updateStatus(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        String statusStr = body.get("status");
        IncidentStatus newStatus = IncidentStatus.valueOf(statusStr);
        IncidentResponse updated = incidentService.updateStatus(id, newStatus);
        return ResponseEntity.ok(ApiResponse.success("Incident status updated", updated));
    }

    @PostMapping("/{id}/updates")
    @PreAuthorize("hasAnyRole('GOP-ADMIN', 'GOP-OPERATOR')")
    public ResponseEntity<ApiResponse<IncidentResponse>> addUpdate(
            @PathVariable UUID id,
            @Valid @RequestBody IncidentUpdateRequest request,
            Principal principal) {
        String author = principal != null ? principal.getName() : "unknown";
        IncidentResponse updated = incidentService.addUpdate(id, request, author);
        return ResponseEntity.ok(ApiResponse.success("Update added", updated));
    }

    @PostMapping("/{id}/resolve")
    @PreAuthorize("hasAnyRole('GOP-ADMIN', 'GOP-OPERATOR')")
    public ResponseEntity<ApiResponse<IncidentResponse>> resolve(
            @PathVariable UUID id,
            @Valid @RequestBody IncidentResolveRequest request) {
        IncidentResponse resolved = incidentService.resolve(id, request);
        return ResponseEntity.ok(ApiResponse.success("Incident resolved", resolved));
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasAnyRole('GOP-ADMIN', 'GOP-OPERATOR')")
    public ResponseEntity<ApiResponse<Void>> close(@PathVariable UUID id) {
        incidentService.close(id);
        return ResponseEntity.ok(ApiResponse.success("Incident closed", null));
    }
}
