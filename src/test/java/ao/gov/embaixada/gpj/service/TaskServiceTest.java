package ao.gov.embaixada.gpj.service;

import ao.gov.embaixada.gpj.dto.SprintCreateRequest;
import ao.gov.embaixada.gpj.dto.SprintResponse;
import ao.gov.embaixada.gpj.dto.TaskCreateRequest;
import ao.gov.embaixada.gpj.dto.TaskResponse;
import ao.gov.embaixada.gpj.enums.TaskPriority;
import ao.gov.embaixada.gpj.enums.TaskStatus;
import ao.gov.embaixada.gpj.exception.CapacityExceededException;
import ao.gov.embaixada.gpj.exception.CircularDependencyException;
import ao.gov.embaixada.gpj.exception.InvalidStateTransitionException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TaskServiceTest {

    @Autowired
    private TaskService taskService;

    @Autowired
    private SprintService sprintService;

    @Test
    void create_shouldReturnTaskWithBacklogStatus() {
        TaskCreateRequest request = new TaskCreateRequest(
                "Task 1", "Description", TaskPriority.HIGH, "officer", 8.0, null);

        TaskResponse response = taskService.create(request);

        assertNotNull(response.id());
        assertEquals("Task 1", response.title());
        assertEquals(TaskStatus.BACKLOG, response.status());
        assertEquals(TaskPriority.HIGH, response.priority());
    }

    @Test
    void create_withSprint_shouldAssignToSprint() {
        SprintResponse sprint = sprintService.create(
                new SprintCreateRequest("S1", null, null, null, 100.0));
        TaskCreateRequest request = new TaskCreateRequest(
                "Task", null, TaskPriority.MEDIUM, null, 10.0, sprint.id());

        TaskResponse response = taskService.create(request);

        assertEquals(sprint.id(), response.sprintId());
    }

    @Test
    void create_exceedingCapacity_shouldThrow() {
        SprintResponse sprint = sprintService.create(
                new SprintCreateRequest("S1", null, null, null, 10.0));
        taskService.create(new TaskCreateRequest(
                "Task 1", null, TaskPriority.MEDIUM, null, 8.0, sprint.id()));

        assertThrows(CapacityExceededException.class, () ->
                taskService.create(new TaskCreateRequest(
                        "Task 2", null, TaskPriority.MEDIUM, null, 5.0, sprint.id())));
    }

    @Test
    void updateStatus_backlogToTodo_shouldSucceed() {
        TaskResponse created = taskService.create(
                new TaskCreateRequest("Task", null, null, null, 0.0, null));

        TaskResponse updated = taskService.updateStatus(created.id(), TaskStatus.TODO);

        assertEquals(TaskStatus.TODO, updated.status());
    }

    @Test
    void updateStatus_toDone_shouldSet100Percent() {
        TaskResponse created = taskService.create(
                new TaskCreateRequest("Task", null, null, null, 0.0, null));
        taskService.updateStatus(created.id(), TaskStatus.TODO);
        taskService.updateStatus(created.id(), TaskStatus.IN_PROGRESS);
        taskService.updateStatus(created.id(), TaskStatus.IN_REVIEW);

        TaskResponse done = taskService.updateStatus(created.id(), TaskStatus.DONE);

        assertEquals(100, done.progressPct());
    }

    @Test
    void updateStatus_backlogToDone_shouldThrow() {
        TaskResponse created = taskService.create(
                new TaskCreateRequest("Task", null, null, null, 0.0, null));

        assertThrows(InvalidStateTransitionException.class,
                () -> taskService.updateStatus(created.id(), TaskStatus.DONE));
    }

    @Test
    void addDependency_shouldSucceed() {
        TaskResponse task1 = taskService.create(
                new TaskCreateRequest("Task 1", null, null, null, 0.0, null));
        TaskResponse task2 = taskService.create(
                new TaskCreateRequest("Task 2", null, null, null, 0.0, null));

        TaskResponse updated = taskService.addDependency(task2.id(), task1.id());

        assertTrue(updated.dependencyIds().contains(task1.id()));
    }

    @Test
    void addDependency_circular_shouldThrow() {
        TaskResponse task1 = taskService.create(
                new TaskCreateRequest("Task 1", null, null, null, 0.0, null));
        TaskResponse task2 = taskService.create(
                new TaskCreateRequest("Task 2", null, null, null, 0.0, null));

        taskService.addDependency(task2.id(), task1.id());

        assertThrows(CircularDependencyException.class,
                () -> taskService.addDependency(task1.id(), task2.id()));
    }

    @Test
    void removeDependency_shouldSucceed() {
        TaskResponse task1 = taskService.create(
                new TaskCreateRequest("Task 1", null, null, null, 0.0, null));
        TaskResponse task2 = taskService.create(
                new TaskCreateRequest("Task 2", null, null, null, 0.0, null));
        taskService.addDependency(task2.id(), task1.id());

        TaskResponse updated = taskService.removeDependency(task2.id(), task1.id());

        assertTrue(updated.dependencyIds().isEmpty());
    }
}
