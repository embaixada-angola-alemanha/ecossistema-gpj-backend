package ao.gov.embaixada.gpj.controller;

import ao.gov.embaixada.gpj.dto.MilestoneResponse;
import ao.gov.embaixada.gpj.enums.MilestoneStatus;
import ao.gov.embaixada.gpj.service.MilestoneService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MilestoneController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class MilestoneControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MilestoneService milestoneService;

    @TestConfiguration
    static class Config {
        @Bean
        public MilestoneService milestoneService() {
            return mock(MilestoneService.class);
        }
    }

    private static final UUID SPRINT_ID = UUID.randomUUID();

    private MilestoneResponse sampleResponse() {
        return new MilestoneResponse(
                UUID.randomUUID(), SPRINT_ID, "Sprint 1",
                "MVP Release", "First release",
                LocalDate.of(2026, 3, 10),
                MilestoneStatus.PENDING, null,
                Instant.now(), "admin");
    }

    @Test
    void create_shouldReturn201() throws Exception {
        when(milestoneService.create(eq(SPRINT_ID), any())).thenReturn(sampleResponse());

        mockMvc.perform(post("/api/sprints/" + SPRINT_ID + "/milestones")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"MVP Release","targetDate":"2026-03-10"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value("MVP Release"));
    }

    @Test
    void findBySprintId_shouldReturn200() throws Exception {
        Page<MilestoneResponse> page = new PageImpl<>(List.of(sampleResponse()));
        when(milestoneService.findBySprintId(eq(SPRINT_ID), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/sprints/" + SPRINT_ID + "/milestones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    void complete_shouldReturn200() throws Exception {
        MilestoneResponse completed = new MilestoneResponse(
                UUID.randomUUID(), SPRINT_ID, "Sprint 1",
                "MVP Release", "First release",
                LocalDate.of(2026, 3, 10),
                MilestoneStatus.COMPLETED, Instant.now(),
                Instant.now(), "admin");
        when(milestoneService.complete(any())).thenReturn(completed);

        mockMvc.perform(patch("/api/milestones/" + UUID.randomUUID() + "/complete"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("COMPLETED"));
    }

    @Test
    void markMissed_shouldReturn200() throws Exception {
        MilestoneResponse missed = new MilestoneResponse(
                UUID.randomUUID(), SPRINT_ID, "Sprint 1",
                "MVP Release", "First release",
                LocalDate.of(2026, 3, 10),
                MilestoneStatus.MISSED, null,
                Instant.now(), "admin");
        when(milestoneService.markMissed(any())).thenReturn(missed);

        mockMvc.perform(patch("/api/milestones/" + UUID.randomUUID() + "/missed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("MISSED"));
    }
}
