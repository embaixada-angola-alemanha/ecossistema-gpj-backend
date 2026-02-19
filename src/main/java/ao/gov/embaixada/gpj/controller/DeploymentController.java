package ao.gov.embaixada.gpj.controller;

import ao.gov.embaixada.commons.dto.ApiResponse;
import ao.gov.embaixada.commons.dto.PagedResponse;
import ao.gov.embaixada.gpj.dto.DeploymentCreateRequest;
import ao.gov.embaixada.gpj.dto.DeploymentResponse;
import ao.gov.embaixada.gpj.enums.DeploymentEnvironment;
import ao.gov.embaixada.gpj.service.DeploymentService;
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
@RequestMapping("/api/deployments")
public class DeploymentController {

    private final DeploymentService deploymentService;

    public DeploymentController(DeploymentService deploymentService) {
        this.deploymentService = deploymentService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('GOP-ADMIN', 'GOP-OPERATOR', 'GOP-VIEWER')")
    public ResponseEntity<ApiResponse<PagedResponse<DeploymentResponse>>> findAll(
            @RequestParam(required = false) UUID serviceId,
            @RequestParam(required = false) DeploymentEnvironment environment,
            Pageable pageable) {
        Page<DeploymentResponse> page = deploymentService.findAll(serviceId, environment, pageable);
        return ResponseEntity.ok(ApiResponse.success(PagedResponse.of(page)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('GOP-ADMIN', 'GOP-OPERATOR', 'GOP-VIEWER')")
    public ResponseEntity<ApiResponse<DeploymentResponse>> findById(@PathVariable UUID id) {
        DeploymentResponse deployment = deploymentService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(deployment));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('GOP-ADMIN', 'GOP-OPERATOR')")
    public ResponseEntity<ApiResponse<DeploymentResponse>> create(
            @Valid @RequestBody DeploymentCreateRequest request,
            Principal principal) {
        String deployedBy = principal != null ? principal.getName() : "unknown";
        DeploymentResponse created = deploymentService.create(request, deployedBy);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Deployment created", created));
    }
}
