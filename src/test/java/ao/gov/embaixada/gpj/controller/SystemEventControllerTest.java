package ao.gov.embaixada.gpj.controller;

import ao.gov.embaixada.gpj.dto.SystemEventResponse;
import ao.gov.embaixada.gpj.exception.GlobalExceptionHandler;
import ao.gov.embaixada.gpj.exception.ResourceNotFoundException;
import ao.gov.embaixada.gpj.service.SystemEventService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SystemEventController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@ActiveProfiles("test")
class SystemEventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SystemEventService systemEventService;

    private SystemEventResponse createResponse(UUID id) {
        return new SystemEventResponse(
                id, UUID.randomUUID(), "SGC", "CIDADAO_CREATED",
                "Cidadao", "entity-1", Instant.now(), Instant.now());
    }

    @Test
    void findAll_shouldReturn200() throws Exception {
        Page<SystemEventResponse> page = new PageImpl<>(
                List.of(createResponse(UUID.randomUUID())));

        when(systemEventService.findAll(isNull(), isNull(), any())).thenReturn(page);

        mockMvc.perform(get("/api/events"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(1));
    }

    @Test
    void findAll_withSourceFilter_shouldReturn200() throws Exception {
        Page<SystemEventResponse> page = new PageImpl<>(
                List.of(createResponse(UUID.randomUUID())));

        when(systemEventService.findAll(eq("SGC"), isNull(), any())).thenReturn(page);

        mockMvc.perform(get("/api/events").param("source", "SGC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void findById_shouldReturn200() throws Exception {
        UUID id = UUID.randomUUID();
        when(systemEventService.findById(id)).thenReturn(createResponse(id));

        mockMvc.perform(get("/api/events/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.source").value("SGC"));
    }

    @Test
    void findById_notFound_shouldReturn404() throws Exception {
        UUID id = UUID.randomUUID();
        when(systemEventService.findById(id)).thenThrow(new ResourceNotFoundException("SystemEvent", id));

        mockMvc.perform(get("/api/events/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void getEventsBySource_shouldReturn200() throws Exception {
        List<Object[]> results = List.of(
                new Object[]{"SGC", 10L},
                new Object[]{"SI", 5L});

        when(systemEventService.countBySourceSince(any())).thenReturn(results);

        mockMvc.perform(get("/api/events/stats/by-source").param("hours", "24"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.SGC").value(10))
                .andExpect(jsonPath("$.data.SI").value(5));
    }

    @Test
    void countToday_shouldReturn200() throws Exception {
        when(systemEventService.countToday()).thenReturn(42L);

        mockMvc.perform(get("/api/events/stats/count-today"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value(42));
    }
}
