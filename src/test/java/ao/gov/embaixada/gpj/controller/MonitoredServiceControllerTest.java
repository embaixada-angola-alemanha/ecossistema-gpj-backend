package ao.gov.embaixada.gpj.controller;

import ao.gov.embaixada.gpj.dto.MonitoredServiceCreateRequest;
import ao.gov.embaixada.gpj.dto.MonitoredServiceResponse;
import ao.gov.embaixada.gpj.dto.MonitoredServiceUpdateRequest;
import ao.gov.embaixada.gpj.dto.UptimeResponse;
import ao.gov.embaixada.gpj.exception.GlobalExceptionHandler;
import ao.gov.embaixada.gpj.exception.ResourceNotFoundException;
import ao.gov.embaixada.gpj.service.HealthCheckService;
import ao.gov.embaixada.gpj.service.MonitoredServiceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MonitoredServiceController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@ActiveProfiles("test")
class MonitoredServiceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MonitoredServiceService serviceService;

    @MockitoBean
    private HealthCheckService healthCheckService;

    private MonitoredServiceResponse createResponse(UUID id, String name) {
        return new MonitoredServiceResponse(
                id, name, name + " Display", "BACKEND", "UP",
                Instant.now(), 50L, 0, Instant.now());
    }

    @Test
    void findAll_shouldReturn200() throws Exception {
        UUID id = UUID.randomUUID();
        when(serviceService.findAll()).thenReturn(List.of(createResponse(id, "sgc-backend")));

        mockMvc.perform(get("/api/services"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].name").value("sgc-backend"));
    }

    @Test
    void findById_shouldReturn200() throws Exception {
        UUID id = UUID.randomUUID();
        when(serviceService.findById(id)).thenReturn(createResponse(id, "sgc-backend"));

        mockMvc.perform(get("/api/services/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("sgc-backend"));
    }

    @Test
    void findById_notFound_shouldReturn404() throws Exception {
        UUID id = UUID.randomUUID();
        when(serviceService.findById(id)).thenThrow(new ResourceNotFoundException("MonitoredService", id));

        mockMvc.perform(get("/api/services/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void create_shouldReturn201() throws Exception {
        UUID id = UUID.randomUUID();
        MonitoredServiceCreateRequest request = new MonitoredServiceCreateRequest(
                "sgc-backend", "SGC Backend", "BACKEND", "http://localhost:8081/health", null);

        when(serviceService.create(any())).thenReturn(createResponse(id, "sgc-backend"));

        mockMvc.perform(post("/api/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.name").value("sgc-backend"));
    }

    @Test
    void create_missingName_shouldReturn400() throws Exception {
        MonitoredServiceCreateRequest request = new MonitoredServiceCreateRequest(
                "", "SGC Backend", "BACKEND", null, null);

        mockMvc.perform(post("/api/services")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void update_shouldReturn200() throws Exception {
        UUID id = UUID.randomUUID();
        MonitoredServiceUpdateRequest request = new MonitoredServiceUpdateRequest(
                "Updated Name", null, null);

        when(serviceService.update(eq(id), any())).thenReturn(createResponse(id, "sgc-backend"));

        mockMvc.perform(put("/api/services/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void delete_shouldReturn200() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(serviceService).delete(id);

        mockMvc.perform(delete("/api/services/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void delete_notFound_shouldReturn404() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new ResourceNotFoundException("MonitoredService", id)).when(serviceService).delete(id);

        mockMvc.perform(delete("/api/services/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void getUptime_shouldReturn200() throws Exception {
        UUID id = UUID.randomUUID();
        UptimeResponse uptime = new UptimeResponse(id, "SGC Backend", 99.9, 99.5, 99.0);

        when(healthCheckService.calculateUptime(id)).thenReturn(uptime);

        mockMvc.perform(get("/api/services/{id}/uptime", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.uptime24h").value(99.9));
    }
}
