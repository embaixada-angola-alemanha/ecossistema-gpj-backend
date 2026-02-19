package ao.gov.embaixada.gpj.controller;

import ao.gov.embaixada.gpj.dto.MaintenanceCreateRequest;
import ao.gov.embaixada.gpj.dto.MaintenanceResponse;
import ao.gov.embaixada.gpj.dto.MaintenanceUpdateRequest;
import ao.gov.embaixada.gpj.exception.GlobalExceptionHandler;
import ao.gov.embaixada.gpj.exception.InvalidStateTransitionException;
import ao.gov.embaixada.gpj.exception.ResourceNotFoundException;
import ao.gov.embaixada.gpj.service.MaintenanceWindowService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MaintenanceController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@ActiveProfiles("test")
class MaintenanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private MaintenanceWindowService maintenanceService;

    private MaintenanceResponse createResponse(UUID id, String status) {
        return new MaintenanceResponse(
                id, "DB Maintenance", "Database upgrade",
                Instant.now().plus(1, ChronoUnit.DAYS),
                Instant.now().plus(1, ChronoUnit.DAYS).plus(2, ChronoUnit.HOURS),
                null, null, status, List.of(), "admin", Instant.now());
    }

    @Test
    void findAll_shouldReturn200() throws Exception {
        Page<MaintenanceResponse> page = new PageImpl<>(
                List.of(createResponse(UUID.randomUUID(), "SCHEDULED")));

        when(maintenanceService.findAll(isNull(), any())).thenReturn(page);

        mockMvc.perform(get("/api/maintenance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(1));
    }

    @Test
    void findById_shouldReturn200() throws Exception {
        UUID id = UUID.randomUUID();
        when(maintenanceService.findById(id)).thenReturn(createResponse(id, "SCHEDULED"));

        mockMvc.perform(get("/api/maintenance/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("DB Maintenance"));
    }

    @Test
    void findById_notFound_shouldReturn404() throws Exception {
        UUID id = UUID.randomUUID();
        when(maintenanceService.findById(id)).thenThrow(new ResourceNotFoundException("MaintenanceWindow", id));

        mockMvc.perform(get("/api/maintenance/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void create_shouldReturn201() throws Exception {
        UUID id = UUID.randomUUID();
        Instant start = Instant.now().plus(1, ChronoUnit.DAYS);
        Instant end = start.plus(2, ChronoUnit.HOURS);
        MaintenanceCreateRequest request = new MaintenanceCreateRequest(
                "DB Maintenance", "Upgrade", start, end, null);

        when(maintenanceService.create(any(), anyString())).thenReturn(createResponse(id, "SCHEDULED"));

        mockMvc.perform(post("/api/maintenance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("SCHEDULED"));
    }

    @Test
    void create_missingTitle_shouldReturn400() throws Exception {
        Instant start = Instant.now().plus(1, ChronoUnit.DAYS);
        Instant end = start.plus(2, ChronoUnit.HOURS);
        MaintenanceCreateRequest request = new MaintenanceCreateRequest(
                "", null, start, end, null);

        mockMvc.perform(post("/api/maintenance")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void update_shouldReturn200() throws Exception {
        UUID id = UUID.randomUUID();
        MaintenanceUpdateRequest request = new MaintenanceUpdateRequest(
                "Updated Title", null, null, null, null);

        when(maintenanceService.update(eq(id), any())).thenReturn(createResponse(id, "SCHEDULED"));

        mockMvc.perform(put("/api/maintenance/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void start_shouldReturn200() throws Exception {
        UUID id = UUID.randomUUID();
        when(maintenanceService.start(id)).thenReturn(createResponse(id, "IN_PROGRESS"));

        mockMvc.perform(post("/api/maintenance/{id}/start", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void start_invalidTransition_shouldReturn409() throws Exception {
        UUID id = UUID.randomUUID();
        when(maintenanceService.start(id))
                .thenThrow(new InvalidStateTransitionException("MaintenanceWindow", "COMPLETED", "IN_PROGRESS"));

        mockMvc.perform(post("/api/maintenance/{id}/start", id))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void complete_shouldReturn200() throws Exception {
        UUID id = UUID.randomUUID();
        when(maintenanceService.complete(id)).thenReturn(createResponse(id, "COMPLETED"));

        mockMvc.perform(post("/api/maintenance/{id}/complete", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void cancel_shouldReturn200() throws Exception {
        UUID id = UUID.randomUUID();
        when(maintenanceService.cancel(id)).thenReturn(createResponse(id, "CANCELLED"));

        mockMvc.perform(post("/api/maintenance/{id}/cancel", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}
