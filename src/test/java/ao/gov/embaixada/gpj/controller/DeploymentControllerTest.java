package ao.gov.embaixada.gpj.controller;

import ao.gov.embaixada.gpj.dto.DeploymentCreateRequest;
import ao.gov.embaixada.gpj.dto.DeploymentResponse;
import ao.gov.embaixada.gpj.exception.GlobalExceptionHandler;
import ao.gov.embaixada.gpj.exception.ResourceNotFoundException;
import ao.gov.embaixada.gpj.service.DeploymentService;
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
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DeploymentController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@ActiveProfiles("test")
class DeploymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private DeploymentService deploymentService;

    private DeploymentResponse createResponse(UUID id) {
        return new DeploymentResponse(
                id, UUID.randomUUID(), "SGC Backend", "1.0.0", "abc123",
                "PRODUCTION", "admin", Instant.now(), "SUCCESS", null, Instant.now());
    }

    @Test
    void findAll_shouldReturn200() throws Exception {
        Page<DeploymentResponse> page = new PageImpl<>(List.of(createResponse(UUID.randomUUID())));

        when(deploymentService.findAll(isNull(), isNull(), any())).thenReturn(page);

        mockMvc.perform(get("/api/deployments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.content").isArray())
                .andExpect(jsonPath("$.data.content.length()").value(1));
    }

    @Test
    void findById_shouldReturn200() throws Exception {
        UUID id = UUID.randomUUID();
        when(deploymentService.findById(id)).thenReturn(createResponse(id));

        mockMvc.perform(get("/api/deployments/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.versionTag").value("1.0.0"));
    }

    @Test
    void findById_notFound_shouldReturn404() throws Exception {
        UUID id = UUID.randomUUID();
        when(deploymentService.findById(id)).thenThrow(new ResourceNotFoundException("Deployment", id));

        mockMvc.perform(get("/api/deployments/{id}", id))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void create_shouldReturn201() throws Exception {
        UUID serviceId = UUID.randomUUID();
        UUID deployId = UUID.randomUUID();
        DeploymentCreateRequest request = new DeploymentCreateRequest(
                serviceId, "1.0.0", "abc123", "PRODUCTION", "Initial deployment");

        when(deploymentService.create(any(), anyString())).thenReturn(createResponse(deployId));

        mockMvc.perform(post("/api/deployments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.versionTag").value("1.0.0"));
    }

    @Test
    void create_missingVersionTag_shouldReturn400() throws Exception {
        UUID serviceId = UUID.randomUUID();
        DeploymentCreateRequest request = new DeploymentCreateRequest(
                serviceId, "", null, "PRODUCTION", null);

        mockMvc.perform(post("/api/deployments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    void create_missingServiceId_shouldReturn400() throws Exception {
        // Construct JSON manually to ensure null serviceId
        String json = "{\"serviceId\":null,\"versionTag\":\"1.0.0\",\"environment\":\"PRODUCTION\"}";

        mockMvc.perform(post("/api/deployments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }
}
