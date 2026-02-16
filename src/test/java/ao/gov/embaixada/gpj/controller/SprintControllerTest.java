package ao.gov.embaixada.gpj.controller;

import ao.gov.embaixada.gpj.dto.SprintResponse;
import ao.gov.embaixada.gpj.enums.SprintStatus;
import ao.gov.embaixada.gpj.service.SprintService;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SprintController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class SprintControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SprintService sprintService;

    @TestConfiguration
    static class Config {
        @Bean
        public SprintService sprintService() {
            return mock(SprintService.class);
        }
    }

    private SprintResponse sampleResponse() {
        return new SprintResponse(
                UUID.randomUUID(), "Sprint 1", "Desc",
                LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 15),
                SprintStatus.PLANNING, 40.0, 0,
                Instant.now(), Instant.now(), "admin");
    }

    @Test
    void create_shouldReturn201() throws Exception {
        when(sprintService.create(any())).thenReturn(sampleResponse());

        mockMvc.perform(post("/api/sprints")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Sprint 1","description":"Desc","capacityHours":40.0}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Sprint 1"));
    }

    @Test
    void findById_shouldReturn200() throws Exception {
        SprintResponse response = sampleResponse();
        when(sprintService.findById(any())).thenReturn(response);

        mockMvc.perform(get("/api/sprints/" + response.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.title").value("Sprint 1"));
    }

    @Test
    void findAll_shouldReturn200() throws Exception {
        Page<SprintResponse> page = new PageImpl<>(List.of(sampleResponse()));
        when(sprintService.findAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/sprints"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }
}
