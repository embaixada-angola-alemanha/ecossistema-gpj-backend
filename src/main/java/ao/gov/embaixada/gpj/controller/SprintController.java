package ao.gov.embaixada.gpj.controller;

import ao.gov.embaixada.commons.dto.ApiResponse;
import ao.gov.embaixada.commons.dto.PagedResponse;
import ao.gov.embaixada.gpj.dto.SprintCreateRequest;
import ao.gov.embaixada.gpj.dto.SprintResponse;
import ao.gov.embaixada.gpj.dto.SprintUpdateRequest;
import ao.gov.embaixada.gpj.enums.SprintStatus;
import ao.gov.embaixada.gpj.service.SprintService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/sprints")
public class SprintController {

    private final SprintService sprintService;

    public SprintController(SprintService sprintService) {
        this.sprintService = sprintService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SprintResponse>> create(@Valid @RequestBody SprintCreateRequest request) {
        SprintResponse response = sprintService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SprintResponse>> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(sprintService.findById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<SprintResponse>>> findAll(
            @RequestParam(required = false) SprintStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<SprintResponse> page;
        if (status != null) {
            page = sprintService.findByStatus(status, pageable);
        } else {
            page = sprintService.findAll(pageable);
        }
        return ResponseEntity.ok(ApiResponse.success(PagedResponse.of(page)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SprintResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody SprintUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(sprintService.update(id, request)));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<SprintResponse>> updateStatus(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        SprintStatus newStatus = SprintStatus.valueOf(body.get("status"));
        return ResponseEntity.ok(ApiResponse.success(sprintService.updateStatus(id, newStatus)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        sprintService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
