package ao.gov.embaixada.gpj.controller;

import ao.gov.embaixada.gpj.dto.BlockerResponse;
import ao.gov.embaixada.gpj.enums.BlockerStatus;
import ao.gov.embaixada.gpj.service.BlockerService;
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

@WebMvcTest(BlockerController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class BlockerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BlockerService blockerService;

    @TestConfiguration
    static class Config {
        @Bean
        public BlockerService blockerService() {
            return mock(BlockerService.class);
        }
    }

    private static final UUID TASK_ID = UUID.randomUUID();

    private BlockerResponse sampleResponse() {
        return new BlockerResponse(
                UUID.randomUUID(), TASK_ID, "Task 1",
                "DB Issue", "Cannot connect", "HIGH",
                BlockerStatus.OPEN, null, null,
                Instant.now(), "admin");
    }

    @Test
    void create_shouldReturn201() throws Exception {
        when(blockerService.create(eq(TASK_ID), any())).thenReturn(sampleResponse());

        mockMvc.perform(post("/api/tasks/" + TASK_ID + "/blockers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"DB Issue","severity":"HIGH"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value("DB Issue"));
    }

    @Test
    void findByTaskId_shouldReturn200() throws Exception {
        Page<BlockerResponse> page = new PageImpl<>(List.of(sampleResponse()));
        when(blockerService.findByTaskId(eq(TASK_ID), any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/tasks/" + TASK_ID + "/blockers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    void resolve_shouldReturn200() throws Exception {
        BlockerResponse resolved = new BlockerResponse(
                UUID.randomUUID(), TASK_ID, "Task 1",
                "DB Issue", "Cannot connect", "HIGH",
                BlockerStatus.RESOLVED, "Fixed it", Instant.now(),
                Instant.now(), "admin");
        when(blockerService.resolve(any(), any())).thenReturn(resolved);

        mockMvc.perform(patch("/api/blockers/" + UUID.randomUUID() + "/resolve")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"resolution":"Fixed it"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("RESOLVED"));
    }
}
