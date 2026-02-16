package ao.gov.embaixada.gpj.controller;

import ao.gov.embaixada.commons.dto.ApiResponse;
import ao.gov.embaixada.commons.dto.PagedResponse;
import ao.gov.embaixada.gpj.dto.TaskCreateRequest;
import ao.gov.embaixada.gpj.dto.TaskDependencyRequest;
import ao.gov.embaixada.gpj.dto.TaskResponse;
import ao.gov.embaixada.gpj.dto.TaskUpdateRequest;
import ao.gov.embaixada.gpj.enums.TaskStatus;
import ao.gov.embaixada.gpj.service.TaskService;
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
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService) {
        this.taskService = taskService;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<TaskResponse>> create(@Valid @RequestBody TaskCreateRequest request) {
        TaskResponse response = taskService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskResponse>> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(taskService.findById(id)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagedResponse<TaskResponse>>> findAll(
            @RequestParam(required = false) UUID sprintId,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) String assignee,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<TaskResponse> page;
        if (sprintId != null) {
            page = taskService.findBySprintId(sprintId, pageable);
        } else if (status != null) {
            page = taskService.findByStatus(status, pageable);
        } else if (assignee != null) {
            page = taskService.findByAssignee(assignee, pageable);
        } else {
            page = taskService.findAll(pageable);
        }
        return ResponseEntity.ok(ApiResponse.success(PagedResponse.of(page)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<TaskResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody TaskUpdateRequest request) {
        return ResponseEntity.ok(ApiResponse.success(taskService.update(id, request)));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<TaskResponse>> updateStatus(
            @PathVariable UUID id,
            @RequestBody Map<String, String> body) {
        TaskStatus newStatus = TaskStatus.valueOf(body.get("status"));
        return ResponseEntity.ok(ApiResponse.success(taskService.updateStatus(id, newStatus)));
    }

    @PostMapping("/{id}/dependencies")
    public ResponseEntity<ApiResponse<TaskResponse>> addDependency(
            @PathVariable UUID id,
            @Valid @RequestBody TaskDependencyRequest request) {
        return ResponseEntity.ok(ApiResponse.success(taskService.addDependency(id, request.dependsOnId())));
    }

    @DeleteMapping("/{id}/dependencies/{depId}")
    public ResponseEntity<ApiResponse<TaskResponse>> removeDependency(
            @PathVariable UUID id,
            @PathVariable UUID depId) {
        return ResponseEntity.ok(ApiResponse.success(taskService.removeDependency(id, depId)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        taskService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
