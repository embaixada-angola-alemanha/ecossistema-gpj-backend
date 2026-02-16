package ao.gov.embaixada.gpj.controller;

import ao.gov.embaixada.gpj.dto.TaskResponse;
import ao.gov.embaixada.gpj.enums.TaskPriority;
import ao.gov.embaixada.gpj.enums.TaskStatus;
import ao.gov.embaixada.gpj.service.TaskService;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskService taskService;

    @TestConfiguration
    static class Config {
        @Bean
        public TaskService taskService() {
            return mock(TaskService.class);
        }
    }

    private TaskResponse sampleResponse() {
        return new TaskResponse(
                UUID.randomUUID(), "Task 1", "Desc",
                TaskStatus.BACKLOG, TaskPriority.HIGH, "officer",
                8.0, 0.0, 0, null, null, List.of(),
                Instant.now(), Instant.now(), "admin");
    }

    @Test
    void create_shouldReturn201() throws Exception {
        when(taskService.create(any())).thenReturn(sampleResponse());

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Task 1","priority":"HIGH","estimatedHours":8.0}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.title").value("Task 1"));
    }

    @Test
    void findAll_shouldReturn200() throws Exception {
        Page<TaskResponse> page = new PageImpl<>(List.of(sampleResponse()));
        when(taskService.findAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.totalElements").value(1));
    }

    @Test
    void findById_shouldReturn200() throws Exception {
        TaskResponse response = sampleResponse();
        when(taskService.findById(any())).thenReturn(response);

        mockMvc.perform(get("/api/tasks/" + response.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.status").value("BACKLOG"));
    }
}
