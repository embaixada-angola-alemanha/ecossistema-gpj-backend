package ao.gov.embaixada.gpj.service;

import ao.gov.embaixada.gpj.dto.BlockerCreateRequest;
import ao.gov.embaixada.gpj.dto.BlockerResponse;
import ao.gov.embaixada.gpj.dto.TaskCreateRequest;
import ao.gov.embaixada.gpj.dto.TaskResponse;
import ao.gov.embaixada.gpj.enums.BlockerStatus;
import ao.gov.embaixada.gpj.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class BlockerServiceTest {

    @Autowired
    private BlockerService blockerService;

    @Autowired
    private TaskService taskService;

    private TaskResponse createTask() {
        return taskService.create(new TaskCreateRequest(
                "Test Task", null, null, null, 10.0, null));
    }

    @Test
    void create_shouldReturnBlockerWithOpenStatus() {
        TaskResponse task = createTask();
        BlockerCreateRequest request = new BlockerCreateRequest(
                "Database connection issue", "Cannot connect to DB", "HIGH");

        BlockerResponse response = blockerService.create(task.id(), request);

        assertNotNull(response.id());
        assertEquals("Database connection issue", response.title());
        assertEquals(BlockerStatus.OPEN, response.status());
        assertEquals("HIGH", response.severity());
        assertEquals(task.id(), response.taskId());
    }

    @Test
    void create_withDefaultSeverity_shouldUseMedium() {
        TaskResponse task = createTask();
        BlockerCreateRequest request = new BlockerCreateRequest("Blocker", null, null);

        BlockerResponse response = blockerService.create(task.id(), request);

        assertEquals("MEDIUM", response.severity());
    }

    @Test
    void findByTaskId_shouldReturnPagedResults() {
        TaskResponse task = createTask();
        blockerService.create(task.id(), new BlockerCreateRequest("B1", null, "LOW"));
        blockerService.create(task.id(), new BlockerCreateRequest("B2", null, "HIGH"));

        Page<BlockerResponse> page = blockerService.findByTaskId(task.id(), PageRequest.of(0, 10));

        assertEquals(2, page.getTotalElements());
    }

    @Test
    void resolve_shouldSetResolvedStatusAndResolution() {
        TaskResponse task = createTask();
        BlockerResponse created = blockerService.create(task.id(),
                new BlockerCreateRequest("Blocker", null, "HIGH"));

        BlockerResponse resolved = blockerService.resolve(created.id(), "Fixed the connection string");

        assertEquals(BlockerStatus.RESOLVED, resolved.status());
        assertEquals("Fixed the connection string", resolved.resolution());
        assertNotNull(resolved.resolvedAt());
    }

    @Test
    void delete_shouldSucceed() {
        TaskResponse task = createTask();
        BlockerResponse created = blockerService.create(task.id(),
                new BlockerCreateRequest("Blocker", null, "LOW"));

        blockerService.delete(created.id());

        Page<BlockerResponse> page = blockerService.findByTaskId(task.id(), PageRequest.of(0, 10));
        assertEquals(0, page.getTotalElements());
    }

    @Test
    void delete_notFound_shouldThrow() {
        assertThrows(ResourceNotFoundException.class,
                () -> blockerService.delete(java.util.UUID.randomUUID()));
    }
}
