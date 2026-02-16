package ao.gov.embaixada.gpj.service;

import ao.gov.embaixada.gpj.dto.BlockerCreateRequest;
import ao.gov.embaixada.gpj.dto.BurndownResponse;
import ao.gov.embaixada.gpj.dto.MilestoneCreateRequest;
import ao.gov.embaixada.gpj.dto.ProjectReportResponse;
import ao.gov.embaixada.gpj.dto.SprintCreateRequest;
import ao.gov.embaixada.gpj.dto.SprintResponse;
import ao.gov.embaixada.gpj.dto.TaskCreateRequest;
import ao.gov.embaixada.gpj.dto.TaskResponse;
import ao.gov.embaixada.gpj.dto.TimeLogCreateRequest;
import ao.gov.embaixada.gpj.dto.VelocityResponse;
import ao.gov.embaixada.gpj.enums.SprintStatus;
import ao.gov.embaixada.gpj.enums.TaskPriority;
import ao.gov.embaixada.gpj.enums.TaskStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DashboardServiceTest {

    @Autowired
    private DashboardService dashboardService;

    @Autowired
    private SprintService sprintService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private TimeLogService timeLogService;

    @Autowired
    private BlockerService blockerService;

    @Autowired
    private MilestoneService milestoneService;

    @Test
    void getBurndown_shouldReturnBurndownData() {
        SprintResponse sprint = sprintService.create(new SprintCreateRequest(
                "Sprint 1", null,
                LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 15), 40.0));
        taskService.create(new TaskCreateRequest(
                "Task 1", null, TaskPriority.HIGH, null, 20.0, sprint.id()));

        BurndownResponse burndown = dashboardService.getBurndown(sprint.id());

        assertNotNull(burndown);
        assertEquals(sprint.id(), burndown.sprintId());
        assertEquals(20.0, burndown.totalEstimatedHours());
        assertFalse(burndown.points().isEmpty());
    }

    @Test
    void getVelocity_shouldReturnVelocityForSprint() {
        SprintResponse sprint = sprintService.create(new SprintCreateRequest(
                "Sprint 1", null,
                LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 15), 40.0));
        TaskResponse task = taskService.create(new TaskCreateRequest(
                "Task 1", null, TaskPriority.HIGH, null, 10.0, sprint.id()));
        // Move task to DONE
        taskService.updateStatus(task.id(), TaskStatus.TODO);
        taskService.updateStatus(task.id(), TaskStatus.IN_PROGRESS);
        taskService.updateStatus(task.id(), TaskStatus.IN_REVIEW);
        taskService.updateStatus(task.id(), TaskStatus.DONE);

        VelocityResponse velocity = dashboardService.getVelocity(sprint.id());

        assertNotNull(velocity);
        assertEquals(1, velocity.completedTasks());
        assertEquals(10.0, velocity.completedHours());
        assertEquals(1, velocity.totalTasks());
    }

    @Test
    void getVelocityHistory_shouldReturnCompletedSprints() {
        SprintResponse sprint = sprintService.create(new SprintCreateRequest(
                "Sprint 1", null,
                LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 15), 40.0));
        sprintService.updateStatus(sprint.id(), SprintStatus.ACTIVE);
        sprintService.updateStatus(sprint.id(), SprintStatus.COMPLETED);

        List<VelocityResponse> history = dashboardService.getVelocityHistory();

        assertEquals(1, history.size());
        assertEquals("Sprint 1", history.get(0).sprintTitle());
    }

    @Test
    void getProjectReport_shouldAggregateAllMetrics() {
        // Create sprint with task and blocker
        SprintResponse sprint = sprintService.create(new SprintCreateRequest(
                "Sprint 1", null,
                LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 15), 40.0));
        TaskResponse task = taskService.create(new TaskCreateRequest(
                "Task", null, TaskPriority.MEDIUM, null, 8.0, sprint.id()));
        blockerService.create(task.id(),
                new BlockerCreateRequest("Blocker", null, "HIGH"));
        milestoneService.create(sprint.id(),
                new MilestoneCreateRequest("Milestone", null, LocalDate.of(2026, 3, 10)));

        ProjectReportResponse report = dashboardService.getProjectReport();

        assertNotNull(report);
        assertEquals(1, report.totalSprints());
        assertEquals(1, report.totalTasks());
        assertEquals(1, report.activeBlockers());
        assertEquals(1, report.totalMilestones());
        assertEquals(0, report.completedMilestones());
        assertEquals(8.0, report.totalHoursPlanned());
    }

    @Test
    void getProjectReport_withResolvedBlockerAndCompletedMilestone() {
        SprintResponse sprint = sprintService.create(new SprintCreateRequest(
                "Sprint 1", null,
                LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 15), 40.0));
        TaskResponse task = taskService.create(new TaskCreateRequest(
                "Task", null, TaskPriority.MEDIUM, null, 8.0, sprint.id()));

        // Create and resolve blocker
        var blocker = blockerService.create(task.id(),
                new BlockerCreateRequest("Blocker", null, "HIGH"));
        blockerService.resolve(blocker.id(), "Fixed");

        // Create and complete milestone
        var milestone = milestoneService.create(sprint.id(),
                new MilestoneCreateRequest("Milestone", null, LocalDate.of(2026, 3, 10)));
        milestoneService.complete(milestone.id());

        ProjectReportResponse report = dashboardService.getProjectReport();

        assertEquals(0, report.activeBlockers());
        assertEquals(1, report.completedMilestones());
    }
}
