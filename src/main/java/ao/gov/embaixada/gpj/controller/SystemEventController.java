package ao.gov.embaixada.gpj.controller;

import ao.gov.embaixada.commons.dto.ApiResponse;
import ao.gov.embaixada.commons.dto.PagedResponse;
import ao.gov.embaixada.gpj.dto.SystemEventResponse;
import ao.gov.embaixada.gpj.service.SystemEventService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/events")
public class SystemEventController {

    private final SystemEventService systemEventService;

    public SystemEventController(SystemEventService systemEventService) {
        this.systemEventService = systemEventService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('GOP-ADMIN', 'GOP-OPERATOR', 'GOP-VIEWER')")
    public ResponseEntity<ApiResponse<PagedResponse<SystemEventResponse>>> findAll(
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String eventType,
            Pageable pageable) {
        Page<SystemEventResponse> page = systemEventService.findAll(source, eventType, pageable);
        return ResponseEntity.ok(ApiResponse.success(PagedResponse.of(page)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('GOP-ADMIN', 'GOP-OPERATOR', 'GOP-VIEWER')")
    public ResponseEntity<ApiResponse<SystemEventResponse>> findById(@PathVariable UUID id) {
        SystemEventResponse event = systemEventService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(event));
    }

    @GetMapping("/stats/by-source")
    @PreAuthorize("hasAnyRole('GOP-ADMIN', 'GOP-OPERATOR', 'GOP-VIEWER')")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getEventsBySource(
            @RequestParam(defaultValue = "24") int hours) {
        Instant since = Instant.now().minus(hours, ChronoUnit.HOURS);
        List<Object[]> results = systemEventService.countBySourceSince(since);
        Map<String, Long> sourceMap = results.stream()
                .collect(Collectors.toMap(
                        row -> (String) row[0],
                        row -> (Long) row[1]
                ));
        return ResponseEntity.ok(ApiResponse.success(sourceMap));
    }

    @GetMapping("/stats/count-today")
    @PreAuthorize("hasAnyRole('GOP-ADMIN', 'GOP-OPERATOR', 'GOP-VIEWER')")
    public ResponseEntity<ApiResponse<Long>> countToday() {
        long count = systemEventService.countToday();
        return ResponseEntity.ok(ApiResponse.success(count));
    }
}
