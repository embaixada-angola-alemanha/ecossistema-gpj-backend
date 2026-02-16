package ao.gov.embaixada.gpj.controller;

import ao.gov.embaixada.commons.dto.ApiResponse;
import ao.gov.embaixada.commons.dto.PagedResponse;
import ao.gov.embaixada.gpj.dto.BlockerCreateRequest;
import ao.gov.embaixada.gpj.dto.BlockerResponse;
import ao.gov.embaixada.gpj.service.BlockerService;
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

import java.util.Map;
import java.util.UUID;

@RestController
public class BlockerController {

    private final BlockerService blockerService;

    public BlockerController(BlockerService blockerService) {
        this.blockerService = blockerService;
    }

    @PostMapping("/api/tasks/{taskId}/blockers")
    public ResponseEntity<ApiResponse<BlockerResponse>> create(
            @PathVariable UUID taskId,
            @Valid @RequestBody BlockerCreateRequest request) {
        BlockerResponse response = blockerService.create(taskId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @GetMapping("/api/tasks/{taskId}/blockers")
    public ResponseEntity<ApiResponse<PagedResponse<BlockerResponse>>> findByTaskId(
            @PathVariable UUID taskId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(
                PagedResponse.of(blockerService.findByTaskId(taskId, pageable))));
    }

    @PutMapping("/api/blockers/{id}")
    public ResponseEntity<ApiResponse<BlockerResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody BlockerCreateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(blockerService.update(id, request)));
    }

    @PatchMapping("/api/blockers/{id}/resolve")
    public ResponseEntity<ApiResponse<BlockerResponse>> resolve(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        String resolution = body.getOrDefault("resolution", "");
        return ResponseEntity.ok(ApiResponse.success(blockerService.resolve(id, resolution)));
    }

    @DeleteMapping("/api/blockers/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        blockerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
