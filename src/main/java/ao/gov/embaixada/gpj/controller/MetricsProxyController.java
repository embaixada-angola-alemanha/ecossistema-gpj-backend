package ao.gov.embaixada.gpj.controller;

import ao.gov.embaixada.commons.dto.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import java.time.Instant;

@RestController
@RequestMapping("/api/metrics")
public class MetricsProxyController {

    private static final Logger log = LoggerFactory.getLogger(MetricsProxyController.class);

    private final RestClient prometheusRestClient;

    public MetricsProxyController(@Qualifier("prometheusRestClient") RestClient prometheusRestClient) {
        this.prometheusRestClient = prometheusRestClient;
    }

    @GetMapping("/query")
    @PreAuthorize("hasAnyRole('GOP-ADMIN', 'GOP-OPERATOR', 'GOP-VIEWER')")
    public ResponseEntity<ApiResponse<String>> queryPrometheus(
            @RequestParam String query,
            @RequestParam(defaultValue = "1h") String period) {
        try {
            String result = prometheusRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/query")
                            .queryParam("query", query)
                            .queryParam("time", Instant.now().getEpochSecond())
                            .build())
                    .retrieve()
                    .body(String.class);

            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception ex) {
            log.error("Prometheus query failed: query={}, error={}", query, ex.getMessage());
            return ResponseEntity.ok(ApiResponse.error("Prometheus query failed: " + ex.getMessage()));
        }
    }

    @GetMapping("/query-range")
    @PreAuthorize("hasAnyRole('GOP-ADMIN', 'GOP-OPERATOR', 'GOP-VIEWER')")
    public ResponseEntity<ApiResponse<String>> queryRange(
            @RequestParam String query,
            @RequestParam(required = false) String start,
            @RequestParam(required = false) String end,
            @RequestParam(defaultValue = "60") String step) {
        try {
            String effectiveStart = start != null ? start
                    : String.valueOf(Instant.now().minusSeconds(3600).getEpochSecond());
            String effectiveEnd = end != null ? end
                    : String.valueOf(Instant.now().getEpochSecond());

            String result = prometheusRestClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/api/v1/query_range")
                            .queryParam("query", query)
                            .queryParam("start", effectiveStart)
                            .queryParam("end", effectiveEnd)
                            .queryParam("step", step)
                            .build())
                    .retrieve()
                    .body(String.class);

            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception ex) {
            log.error("Prometheus range query failed: query={}, error={}", query, ex.getMessage());
            return ResponseEntity.ok(ApiResponse.error("Prometheus range query failed: " + ex.getMessage()));
        }
    }
}
