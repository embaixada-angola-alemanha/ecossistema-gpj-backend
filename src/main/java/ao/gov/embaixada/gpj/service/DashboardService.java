package ao.gov.embaixada.gpj.service;

import ao.gov.embaixada.gpj.dto.CapacityResponse;
import ao.gov.embaixada.gpj.dto.DashboardResponse;
import ao.gov.embaixada.gpj.entity.Sprint;
import ao.gov.embaixada.gpj.enums.SprintStatus;
import ao.gov.embaixada.gpj.enums.TaskStatus;
import ao.gov.embaixada.gpj.repository.SprintRepository;
import ao.gov.embaixada.gpj.repository.TaskRepository;
import ao.gov.embaixada.gpj.repository.TimeLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    private final SprintRepository sprintRepository;
    private final TaskRepository taskRepository;
    private final TimeLogRepository timeLogRepository;

    public DashboardService(SprintRepository sprintRepository,
                            TaskRepository taskRepository,
                            TimeLogRepository timeLogRepository) {
        this.sprintRepository = sprintRepository;
        this.taskRepository = taskRepository;
        this.timeLogRepository = timeLogRepository;
    }

    public DashboardResponse getDashboard() {
        long totalSprints = sprintRepository.count();
        long totalTasks = taskRepository.count();
        long totalTimeLogs = timeLogRepository.count();
        double totalHoursLogged = timeLogRepository.sumAllHours();

        Map<String, Long> tasksByStatus = new LinkedHashMap<>();
        for (TaskStatus status : TaskStatus.values()) {
            tasksByStatus.put(status.name(), taskRepository.countByStatus(status));
        }

        Map<String, Long> sprintsByStatus = new LinkedHashMap<>();
        for (SprintStatus status : SprintStatus.values()) {
            sprintsByStatus.put(status.name(), sprintRepository.countByStatus(status));
        }

        return new DashboardResponse(
                totalSprints, totalTasks, totalTimeLogs, totalHoursLogged,
                tasksByStatus, sprintsByStatus);
    }

    public List<CapacityResponse> getCapacity() {
        List<Sprint> activeSprints = sprintRepository.findByStatus(SprintStatus.ACTIVE);
        if (activeSprints.isEmpty()) {
            activeSprints = sprintRepository.findByStatus(SprintStatus.PLANNING);
        }

        return activeSprints.stream()
                .map(this::buildCapacity)
                .collect(Collectors.toList());
    }

    private CapacityResponse buildCapacity(Sprint sprint) {
        double allocated = taskRepository.sumEstimatedHoursBySprintId(sprint.getId());
        double consumed = taskRepository.sumConsumedHoursBySprintId(sprint.getId());
        double capacity = sprint.getCapacityHours() != null ? sprint.getCapacityHours() : 0;
        double remaining = Math.max(0, capacity - allocated);
        double utilization = capacity > 0 ? (allocated / capacity) * 100 : 0;

        return new CapacityResponse(
                sprint.getId(),
                sprint.getTitle(),
                capacity,
                allocated,
                remaining,
                consumed,
                Math.round(utilization * 10.0) / 10.0);
    }
}
