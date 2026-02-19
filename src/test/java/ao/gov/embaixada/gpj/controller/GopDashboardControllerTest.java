package ao.gov.embaixada.gpj.controller;

import ao.gov.embaixada.gpj.dto.GopDashboardResponse;
import ao.gov.embaixada.gpj.dto.UptimeResponse;
import ao.gov.embaixada.gpj.exception.GlobalExceptionHandler;
import ao.gov.embaixada.gpj.service.GopDashboardService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GopDashboardController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@ActiveProfiles("test")
class GopDashboardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private GopDashboardService dashboardService;

    @Test
    void getDashboard_shouldReturn200() throws Exception {
        GopDashboardResponse response = new GopDashboardResponse(
                5L, 1L, 0L, 2L, 1L, List.of(), List.of(), 10L);

        when(dashboardService.getDashboard()).thenReturn(response);

        mockMvc.perform(get("/api/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.servicesUp").value(5))
                .andExpect(jsonPath("$.data.servicesDown").value(1))
                .andExpect(jsonPath("$.data.activeIncidents").value(2))
                .andExpect(jsonPath("$.data.p1Incidents").value(1))
                .andExpect(jsonPath("$.data.eventsToday").value(10));
    }

    @Test
    void getUptimeAll_shouldReturn200() throws Exception {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        List<UptimeResponse> uptimes = List.of(
                new UptimeResponse(id1, "SGC Backend", 99.9, 99.5, 99.0),
                new UptimeResponse(id2, "SI Backend", 100.0, 100.0, 100.0));

        when(dashboardService.getUptimeAll()).thenReturn(uptimes);

        mockMvc.perform(get("/api/dashboard/uptime"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].serviceName").value("SGC Backend"))
                .andExpect(jsonPath("$.data[0].uptime24h").value(99.9));
    }
}
