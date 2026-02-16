package ao.gov.embaixada.gpj.controller;

import ao.gov.embaixada.commons.dto.ApiResponse;
import ao.gov.embaixada.commons.dto.PagedResponse;
import ao.gov.embaixada.gpj.dto.MilestoneCreateRequest;
import ao.gov.embaixada.gpj.dto.MilestoneResponse;
import ao.gov.embaixada.gpj.service.MilestoneService;
import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class MilestoneController {

    private final MilestoneService milestoneService;

    public MilestoneController(MilestoneService milestoneService) {
        this.milestoneService = milestoneService;
    }

    @PostMapping("/api/sprints/{sprintId}/milestones")
    public ResponseEntity<ApiResponse<MilestoneResponse>> create(
            @PathVariable UUID sprintId,
            @Valid @RequestBody MilestoneCreateRequest request) {
        MilestoneResponse response = milestoneService.create(sprintId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @GetMapping("/api/sprints/{sprintId}/milestones")
    public ResponseEntity<ApiResponse<PagedResponse<MilestoneResponse>>> findBySprintId(
            @PathVariable UUID sprintId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                PagedResponse.of(milestoneService.findBySprintId(sprintId, pageable))));
    }

    @PutMapping("/api/milestones/{id}")
    public ResponseEntity<ApiResponse<MilestoneResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody MilestoneCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(milestoneService.update(id, request)));
    }

    @PatchMapping("/api/milestones/{id}/complete")
    public ResponseEntity<ApiResponse<MilestoneResponse>> complete(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(milestoneService.complete(id)));
    }

    @PatchMapping("/api/milestones/{id}/missed")
    public ResponseEntity<ApiResponse<MilestoneResponse>> markMissed(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(milestoneService.markMissed(id)));
    }

    @DeleteMapping("/api/milestones/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        milestoneService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
