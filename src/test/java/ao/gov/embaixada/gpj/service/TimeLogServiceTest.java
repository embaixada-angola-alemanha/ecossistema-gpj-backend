package ao.gov.embaixada.gpj.service;

import ao.gov.embaixada.gpj.dto.TaskCreateRequest;
import ao.gov.embaixada.gpj.dto.TaskResponse;
import ao.gov.embaixada.gpj.dto.TimeLogCreateRequest;
import ao.gov.embaixada.gpj.dto.TimeLogResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TimeLogServiceTest {

    @Autowired
    private TimeLogService timeLogService;

    @Autowired
    private TaskService taskService;

    @Test
    void create_shouldReturnTimeLog() {
        TaskResponse task = taskService.create(
                new TaskCreateRequest("Task", null, null, null, 10.0, null));
        TimeLogCreateRequest request = new TimeLogCreateRequest(
                2.5, "Worked on implementation", LocalDate.of(2026, 3, 1));

        TimeLogResponse response = timeLogService.create(task.id(), request, "officer");

        assertNotNull(response.id());
        assertEquals(task.id(), response.taskId());
        assertEquals(2.5, response.hours());
        assertEquals("officer", response.userId());
    }

    @Test
    void create_shouldUpdateTaskConsumedHours() {
        TaskResponse task = taskService.create(
                new TaskCreateRequest("Task", null, null, null, 10.0, null));

        timeLogService.create(task.id(),
                new TimeLogCreateRequest(2.0, "Session 1", LocalDate.now()), "user1");
        timeLogService.create(task.id(),
                new TimeLogCreateRequest(3.0, "Session 2", LocalDate.now()), "user1");

        TaskResponse updated = taskService.findById(task.id());
        assertEquals(5.0, updated.consumedHours());
    }

    @Test
    void findByTaskId_shouldReturnPagedResults() {
        TaskResponse task = taskService.create(
                new TaskCreateRequest("Task", null, null, null, 0.0, null));
        timeLogService.create(task.id(),
                new TimeLogCreateRequest(1.0, "Log 1", LocalDate.now()), "user1");
        timeLogService.create(task.id(),
                new TimeLogCreateRequest(2.0, "Log 2", LocalDate.now()), "user1");

        Page<TimeLogResponse> page = timeLogService.findByTaskId(task.id(), PageRequest.of(0, 10));

        assertEquals(2, page.getTotalElements());
    }
}
