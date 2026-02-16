package ao.gov.embaixada.gpj.controller;

import ao.gov.embaixada.gpj.dto.CapacityResponse;
import ao.gov.embaixada.gpj.dto.DashboardResponse;
import ao.gov.embaixada.gpj.service.DashboardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(DashboardController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class DashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DashboardService dashboardService;

    @TestConfiguration
    static class Config {
        @Bean
        public DashboardService dashboardService() {
            return mock(DashboardService.class);
        }
    }

    @Test
    void getDashboard_shouldReturn200() throws Exception {
        DashboardResponse response = new DashboardResponse(
                3, 15, 42, 120.5,
                Map.of("BACKLOG", 5L, "IN_PROGRESS", 3L, "DONE", 7L),
                Map.of("PLANNING", 1L, "ACTIVE", 1L, "COMPLETED", 1L));
        when(dashboardService.getDashboard()).thenReturn(response);

        mockMvc.perform(get("/api/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalSprints").value(3))
                .andExpect(jsonPath("$.data.totalTasks").value(15))
                .andExpect(jsonPath("$.data.totalHoursLogged").value(120.5));
    }

    @Test
    void getCapacity_shouldReturn200() throws Exception {
        CapacityResponse cap = new CapacityResponse(
                UUID.randomUUID(), "Sprint 1", 40.0, 30.0, 10.0, 20.0, 75.0);
        when(dashboardService.getCapacity()).thenReturn(List.of(cap));

        mockMvc.perform(get("/api/dashboard/capacity"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].sprintTitle").value("Sprint 1"))
                .andExpect(jsonPath("$.data[0].utilizationPct").value(75.0));
    }
}
