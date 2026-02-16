package ao.gov.embaixada.gpj.service;

import ao.gov.embaixada.gpj.dto.MilestoneCreateRequest;
import ao.gov.embaixada.gpj.dto.MilestoneResponse;
import ao.gov.embaixada.gpj.dto.SprintCreateRequest;
import ao.gov.embaixada.gpj.dto.SprintResponse;
import ao.gov.embaixada.gpj.enums.MilestoneStatus;
import ao.gov.embaixada.gpj.exception.ResourceNotFoundException;
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
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MilestoneServiceTest {

    @Autowired
    private MilestoneService milestoneService;

    @Autowired
    private SprintService sprintService;

    private SprintResponse createSprint() {
        return sprintService.create(new SprintCreateRequest(
                "Sprint 1", null, LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 15), 40.0));
    }

    @Test
    void create_shouldReturnMilestoneWithPendingStatus() {
        SprintResponse sprint = createSprint();
        MilestoneCreateRequest request = new MilestoneCreateRequest(
                "MVP Release", "First release", LocalDate.of(2026, 3, 10));

        MilestoneResponse response = milestoneService.create(sprint.id(), request);

        assertNotNull(response.id());
        assertEquals("MVP Release", response.title());
        assertEquals(MilestoneStatus.PENDING, response.status());
        assertEquals(sprint.id(), response.sprintId());
    }

    @Test
    void findBySprintId_shouldReturnPagedResults() {
        SprintResponse sprint = createSprint();
        milestoneService.create(sprint.id(),
                new MilestoneCreateRequest("M1", null, LocalDate.of(2026, 3, 5)));
        milestoneService.create(sprint.id(),
                new MilestoneCreateRequest("M2", null, LocalDate.of(2026, 3, 10)));

        Page<MilestoneResponse> page = milestoneService.findBySprintId(sprint.id(), PageRequest.of(0, 10));

        assertEquals(2, page.getTotalElements());
    }

    @Test
    void complete_shouldSetCompletedStatusAndTimestamp() {
        SprintResponse sprint = createSprint();
        MilestoneResponse created = milestoneService.create(sprint.id(),
                new MilestoneCreateRequest("Milestone", null, LocalDate.of(2026, 3, 10)));

        MilestoneResponse completed = milestoneService.complete(created.id());

        assertEquals(MilestoneStatus.COMPLETED, completed.status());
        assertNotNull(completed.completedAt());
    }

    @Test
    void markMissed_shouldSetMissedStatus() {
        SprintResponse sprint = createSprint();
        MilestoneResponse created = milestoneService.create(sprint.id(),
                new MilestoneCreateRequest("Milestone", null, LocalDate.of(2026, 3, 10)));

        MilestoneResponse missed = milestoneService.markMissed(created.id());

        assertEquals(MilestoneStatus.MISSED, missed.status());
    }

    @Test
    void delete_shouldSucceed() {
        SprintResponse sprint = createSprint();
        MilestoneResponse created = milestoneService.create(sprint.id(),
                new MilestoneCreateRequest("Milestone", null, LocalDate.of(2026, 3, 10)));

        milestoneService.delete(created.id());

        Page<MilestoneResponse> page = milestoneService.findBySprintId(sprint.id(), PageRequest.of(0, 10));
        assertEquals(0, page.getTotalElements());
    }

    @Test
    void delete_notFound_shouldThrow() {
        assertThrows(ResourceNotFoundException.class,
                () -> milestoneService.delete(java.util.UUID.randomUUID()));
    }
}
