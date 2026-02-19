package ao.gov.embaixada.gpj.controller;

import ao.gov.embaixada.gpj.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestClient;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MetricsProxyController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@ActiveProfiles("test")
class MetricsProxyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean(name = "prometheusRestClient")
    private RestClient prometheusRestClient;

    @Test
    void queryPrometheus_success_shouldReturn200() throws Exception {
        RestClient.RequestHeadersUriSpec uriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.RequestHeadersSpec headersSpec = mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        when(prometheusRestClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(any(java.util.function.Function.class))).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(String.class)).thenReturn("{\"status\":\"success\"}");

        mockMvc.perform(get("/api/metrics/query")
                        .param("query", "up"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("{\"status\":\"success\"}"));
    }

    @Test
    void queryPrometheus_failure_shouldReturnError() throws Exception {
        when(prometheusRestClient.get()).thenThrow(new RuntimeException("Connection refused"));

        mockMvc.perform(get("/api/metrics/query")
                        .param("query", "up"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Prometheus query failed: Connection refused"));
    }

    @Test
    void queryRange_success_shouldReturn200() throws Exception {
        RestClient.RequestHeadersUriSpec uriSpec = mock(RestClient.RequestHeadersUriSpec.class);
        RestClient.RequestHeadersSpec headersSpec = mock(RestClient.RequestHeadersSpec.class);
        RestClient.ResponseSpec responseSpec = mock(RestClient.ResponseSpec.class);

        when(prometheusRestClient.get()).thenReturn(uriSpec);
        when(uriSpec.uri(any(java.util.function.Function.class))).thenReturn(headersSpec);
        when(headersSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.body(String.class)).thenReturn("{\"status\":\"success\",\"data\":{}}");

        mockMvc.perform(get("/api/metrics/query-range")
                        .param("query", "rate(http_requests_total[5m])"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    void queryRange_failure_shouldReturnError() throws Exception {
        when(prometheusRestClient.get()).thenThrow(new RuntimeException("Timeout"));

        mockMvc.perform(get("/api/metrics/query-range")
                        .param("query", "rate(http_requests_total[5m])"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Prometheus range query failed: Timeout"));
    }
}
