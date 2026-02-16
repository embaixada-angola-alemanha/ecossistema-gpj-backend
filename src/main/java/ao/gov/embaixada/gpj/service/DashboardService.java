package ao.gov.embaixada.gpj.service;

import ao.gov.embaixada.gpj.dto.BurndownPoint;
import ao.gov.embaixada.gpj.dto.BurndownResponse;
import ao.gov.embaixada.gpj.dto.CapacityResponse;
import ao.gov.embaixada.gpj.dto.DashboardResponse;
import ao.gov.embaixada.gpj.dto.ProjectReportResponse;
import ao.gov.embaixada.gpj.dto.VelocityResponse;
import ao.gov.embaixada.gpj.entity.Sprint;
import ao.gov.embaixada.gpj.enums.BlockerStatus;
import ao.gov.embaixada.gpj.enums.MilestoneStatus;
import ao.gov.embaixada.gpj.enums.SprintStatus;
import ao.gov.embaixada.gpj.enums.TaskStatus;
import ao.gov.embaixada.gpj.exception.ResourceNotFoundException;
import ao.gov.embaixada.gpj.repository.BlockerRepository;
import ao.gov.embaixada.gpj.repository.MilestoneRepository;
import ao.gov.embaixada.gpj.repository.SprintRepository;
import ao.gov.embaixada.gpj.repository.TaskRepository;
import ao.gov.embaixada.gpj.repository.TimeLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
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
    private final BlockerRepository blockerRepository;
    private final MilestoneRepository milestoneRepository;

    public DashboardService(SprintRepository sprintRepository,
                            TaskRepository taskRepository,
                            TimeLogRepository timeLogRepository,
                            BlockerRepository blockerRepository,
                            MilestoneRepository milestoneRepository) {
        this.sprintRepository = sprintRepository;
        this.taskRepository = taskRepository;
        this.timeLogRepository = timeLogRepository;
        this.blockerRepository = blockerRepository;
        this.milestoneRepository = milestoneRepository;
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

    public BurndownResponse getBurndown(java.util.UUID sprintId) {
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint", sprintId));

        double totalEstimated = taskRepository.sumEstimatedHoursBySprintId(sprintId);
        LocalDate startDate = sprint.getStartDate() != null ? sprint.getStartDate() : LocalDate.now();
        LocalDate endDate = sprint.getEndDate() != null ? sprint.getEndDate() : startDate.plusDays(14);
        LocalDate today = LocalDate.now();
        LocalDate effectiveEnd;
        if (today.isBefore(startDate)) {
            effectiveEnd = startDate;
        } else if (today.isBefore(endDate)) {
            effectiveEnd = today;
        } else {
            effectiveEnd = endDate;
        }

        long totalDays = ChronoUnit.DAYS.between(startDate, endDate);
        if (totalDays <= 0) totalDays = 1;

        double dailyIdealBurn = totalEstimated / totalDays;

        List<BurndownPoint> points = new ArrayList<>();
        for (LocalDate date = startDate; !date.isAfter(effectiveEnd); date = date.plusDays(1)) {
            double consumed = timeLogRepository.sumHoursBySprintIdAndDateBefore(sprintId, date);
            double remaining = Math.max(0, totalEstimated - consumed);
            long dayIndex = ChronoUnit.DAYS.between(startDate, date);
            double ideal = Math.max(0, totalEstimated - (dailyIdealBurn * dayIndex));
            points.add(new BurndownPoint(date, remaining, Math.round(ideal * 10.0) / 10.0));
        }

        return new BurndownResponse(
                sprintId, sprint.getTitle(), startDate, endDate,
                totalEstimated, points);
    }

    public VelocityResponse getVelocity(java.util.UUID sprintId) {
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint", sprintId));
        return buildVelocity(sprint);
    }

    public List<VelocityResponse> getVelocityHistory() {
        List<Sprint> completedSprints = sprintRepository.findByStatus(SprintStatus.COMPLETED);
        return completedSprints.stream()
                .map(this::buildVelocity)
                .collect(Collectors.toList());
    }

    public ProjectReportResponse getProjectReport() {
        long totalSprints = sprintRepository.count();
        long activeSprints = sprintRepository.countByStatus(SprintStatus.ACTIVE);
        long totalTasks = taskRepository.count();

        Map<String, Long> tasksByStatus = new LinkedHashMap<>();
        for (TaskStatus status : TaskStatus.values()) {
            tasksByStatus.put(status.name(), taskRepository.countByStatus(status));
        }

        double totalHoursPlanned = taskRepository.sumAllEstimatedHours();
        double totalHoursConsumed = taskRepository.sumAllConsumedHours();
        double overallProgress = totalHoursPlanned > 0
                ? Math.round((totalHoursConsumed / totalHoursPlanned) * 1000.0) / 10.0
                : 0;

        long activeBlockers = blockerRepository.countByStatus(BlockerStatus.OPEN)
                + blockerRepository.countByStatus(BlockerStatus.IN_PROGRESS);
        long completedMilestones = milestoneRepository.countByStatus(MilestoneStatus.COMPLETED);
        long totalMilestones = milestoneRepository.count();

        List<VelocityResponse> velocityHistory = getVelocityHistory();
        List<CapacityResponse> capacityUtilization = getCapacity();

        return new ProjectReportResponse(
                totalSprints, activeSprints, totalTasks, tasksByStatus,
                totalHoursPlanned, totalHoursConsumed, overallProgress,
                activeBlockers, completedMilestones, totalMilestones,
                velocityHistory, capacityUtilization);
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

    private VelocityResponse buildVelocity(Sprint sprint) {
        long totalTasks = taskRepository.countBySprintId(sprint.getId());
        long completedTasks = taskRepository.countBySprintIdAndStatus(sprint.getId(), TaskStatus.DONE);
        double completedHours = taskRepository.sumEstimatedHoursOfCompletedBySprintId(sprint.getId());

        LocalDate start = sprint.getStartDate() != null ? sprint.getStartDate() : LocalDate.now();
        LocalDate end = sprint.getEndDate() != null ? sprint.getEndDate() : start.plusDays(14);
        long durationDays = Math.max(1, ChronoUnit.DAYS.between(start, end));

        double tasksPerDay = (double) completedTasks / durationDays;
        double hoursPerDay = completedHours / durationDays;

        return new VelocityResponse(
                sprint.getId(),
                sprint.getTitle(),
                completedTasks,
                completedHours,
                totalTasks,
                durationDays,
                Math.round(tasksPerDay * 100.0) / 100.0,
                Math.round(hoursPerDay * 100.0) / 100.0);
    }
}
