package ao.gov.embaixada.gpj.service;

import ao.gov.embaixada.gpj.dto.SprintCreateRequest;
import ao.gov.embaixada.gpj.dto.SprintResponse;
import ao.gov.embaixada.gpj.enums.SprintStatus;
import ao.gov.embaixada.gpj.exception.InvalidStateTransitionException;
import ao.gov.embaixada.gpj.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class SprintServiceTest {

    @Autowired
    private SprintService sprintService;

    @Test
    void create_shouldReturnSprintWithPlanningStatus() {
        SprintCreateRequest request = new SprintCreateRequest(
                "Sprint 1", "First sprint",
                LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 15), 40.0);

        SprintResponse response = sprintService.create(request);

        assertNotNull(response.id());
        assertEquals("Sprint 1", response.title());
        assertEquals(SprintStatus.PLANNING, response.status());
        assertEquals(40.0, response.capacityHours());
    }

    @Test
    void findById_shouldReturnSprint() {
        SprintCreateRequest request = new SprintCreateRequest(
                "Sprint 2", null, null, null, 20.0);
        SprintResponse created = sprintService.create(request);

        SprintResponse found = sprintService.findById(created.id());

        assertEquals(created.id(), found.id());
        assertEquals("Sprint 2", found.title());
    }

    @Test
    void findById_shouldThrowWhenNotFound() {
        assertThrows(ResourceNotFoundException.class,
                () -> sprintService.findById(UUID.randomUUID()));
    }

    @Test
    void findAll_shouldReturnPagedResults() {
        sprintService.create(new SprintCreateRequest("S1", null, null, null, 0.0));
        sprintService.create(new SprintCreateRequest("S2", null, null, null, 0.0));

        Page<SprintResponse> page = sprintService.findAll(PageRequest.of(0, 10));

        assertEquals(2, page.getTotalElements());
    }

    @Test
    void updateStatus_planningToActive_shouldSucceed() {
        SprintResponse created = sprintService.create(
                new SprintCreateRequest("Sprint", null, null, null, 0.0));

        SprintResponse updated = sprintService.updateStatus(created.id(), SprintStatus.ACTIVE);

        assertEquals(SprintStatus.ACTIVE, updated.status());
    }

    @Test
    void updateStatus_activeToCompleted_shouldSucceed() {
        SprintResponse created = sprintService.create(
                new SprintCreateRequest("Sprint", null, null, null, 0.0));
        sprintService.updateStatus(created.id(), SprintStatus.ACTIVE);

        SprintResponse updated = sprintService.updateStatus(created.id(), SprintStatus.COMPLETED);

        assertEquals(SprintStatus.COMPLETED, updated.status());
    }

    @Test
    void updateStatus_planningToCompleted_shouldThrow() {
        SprintResponse created = sprintService.create(
                new SprintCreateRequest("Sprint", null, null, null, 0.0));

        assertThrows(InvalidStateTransitionException.class,
                () -> sprintService.updateStatus(created.id(), SprintStatus.COMPLETED));
    }

    @Test
    void delete_shouldSucceed() {
        SprintResponse created = sprintService.create(
                new SprintCreateRequest("Sprint", null, null, null, 0.0));

        sprintService.delete(created.id());

        assertThrows(ResourceNotFoundException.class,
                () -> sprintService.findById(created.id()));
    }
}
