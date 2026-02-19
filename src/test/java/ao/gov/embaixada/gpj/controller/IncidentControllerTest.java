package ao.gov.embaixada.gpj.controller;

import ao.gov.embaixada.gpj.dto.IncidentCreateRequest;
import ao.gov.embaixada.gpj.dto.IncidentResolveRequest;
import ao.gov.embaixada.gpj.dto.IncidentResponse;
import ao.gov.embaixada.gpj.dto.IncidentUpdateRequest;
import ao.gov.embaixada.gpj.exception.GlobalExceptionHandler;
import ao.gov.embaixada.gpj.exception.InvalidStateTransitionException;
import ao.gov.embaixada.gpj.exception.ResourceNotFoundException;
import ao.gov.embaixada.gpj.service.IncidentService;
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
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(IncidentController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@ActiveProfiles("test")
class IncidentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private IncidentService incidentService;

    private IncidentResponse createResponse(UUID id, String status) {
        return new IncidentResponse(
                id, "Test Incident", "Description", "P2", status,
                List.of(), "admin", null, null, null, null,
                List.of(), Instant.now(), Instant.now());
    }

    @Test
    void findAll_shouldReturn200() throws Exception {
        Page<IncidentResponse> page = new PageImpl<>(
                List.of(createResponse(UUID.randomUUID(), "OPEN")));

        when(incidentService.findAll(isNull(), isNull(), any())).thenReturn(page);

        mockMvc.perform(get("/api/incidents"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(1));
    }

    @Test
    void findById_shouldReturn200() throws Exception {
        UUID id = UUID.randomUUID();
        when(incidentService.findById(id)).thenReturn(createResponse(id, "OPEN"));

        mockMvc.perform(get("/api/incidents/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Test Incident"))
                .andExpect(jsonPath("$.data.status").value("OPEN"));
    }

    @Test
    void findById_notFound_shouldReturn404() throws Exception {
        UUID id = UUID.randomUUID();
        when(incidentService.findById(id)).thenThrow(new ResourceNotFoundException("Incident", id));

        mockMvc.perform(get("/api/incidents/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void create_shouldReturn201() throws Exception {
        UUID id = UUID.randomUUID();
        IncidentCreateRequest request = new IncidentCreateRequest(
                "Service Down", "SGC is down", "P2", null, null);

        when(incidentService.create(any(), anyString())).thenReturn(createResponse(id, "OPEN"));

        mockMvc.perform(post("/api/incidents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("OPEN"));
    }

    @Test
    void create_missingTitle_shouldReturn400() throws Exception {
        IncidentCreateRequest request = new IncidentCreateRequest(
                "", "desc", "P2", null, null);

        mockMvc.perform(post("/api/incidents")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void updateStatus_shouldReturn200() throws Exception {
        UUID id = UUID.randomUUID();
        when(incidentService.updateStatus(eq(id), any())).thenReturn(createResponse(id, "INVESTIGATING"));

        mockMvc.perform(patch("/api/incidents/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "INVESTIGATING"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void updateStatus_invalidTransition_shouldReturn409() throws Exception {
        UUID id = UUID.randomUUID();
        when(incidentService.updateStatus(eq(id), any()))
                .thenThrow(new InvalidStateTransitionException("Incident", "OPEN", "RESOLVED"));

        mockMvc.perform(patch("/api/incidents/{id}/status", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("status", "RESOLVED"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void addUpdate_shouldReturn200() throws Exception {
        UUID id = UUID.randomUUID();
        IncidentUpdateRequest request = new IncidentUpdateRequest("Investigating the issue");

        when(incidentService.addUpdate(eq(id), any(), anyString())).thenReturn(createResponse(id, "INVESTIGATING"));

        mockMvc.perform(post("/api/incidents/{id}/updates", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void resolve_shouldReturn200() throws Exception {
        UUID id = UUID.randomUUID();
        IncidentResolveRequest request = new IncidentResolveRequest("DB pool exhausted", "Increased pool size");

        when(incidentService.resolve(eq(id), any())).thenReturn(createResponse(id, "RESOLVED"));

        mockMvc.perform(post("/api/incidents/{id}/resolve", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void resolve_missingResolution_shouldReturn400() throws Exception {
        UUID id = UUID.randomUUID();
        IncidentResolveRequest request = new IncidentResolveRequest("cause", "");

        mockMvc.perform(post("/api/incidents/{id}/resolve", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void close_shouldReturn200() throws Exception {
        UUID id = UUID.randomUUID();
        doNothing().when(incidentService).close(id);

        mockMvc.perform(post("/api/incidents/{id}/close", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void close_notFound_shouldReturn404() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new ResourceNotFoundException("Incident", id)).when(incidentService).close(id);

        mockMvc.perform(post("/api/incidents/{id}/close", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }
}
