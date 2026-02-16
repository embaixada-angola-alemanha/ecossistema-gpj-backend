package ao.gov.embaixada.gpj.service;

import ao.gov.embaixada.gpj.dto.TimeLogCreateRequest;
import ao.gov.embaixada.gpj.dto.TimeLogResponse;
import ao.gov.embaixada.gpj.entity.Task;
import ao.gov.embaixada.gpj.entity.TimeLog;
import ao.gov.embaixada.gpj.exception.ResourceNotFoundException;
import ao.gov.embaixada.gpj.mapper.TimeLogMapper;
import ao.gov.embaixada.gpj.repository.TaskRepository;
import ao.gov.embaixada.gpj.repository.TimeLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class TimeLogService {

    private final TimeLogRepository timeLogRepository;
    private final TaskRepository taskRepository;
    private final TimeLogMapper timeLogMapper;

    public TimeLogService(TimeLogRepository timeLogRepository,
                          TaskRepository taskRepository,
                          TimeLogMapper timeLogMapper) {
        this.timeLogRepository = timeLogRepository;
        this.taskRepository = taskRepository;
        this.timeLogMapper = timeLogMapper;
    }

    public TimeLogResponse create(UUID taskId, TimeLogCreateRequest request, String userId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", taskId));

        TimeLog timeLog = timeLogMapper.toEntity(request);
        timeLog.setTask(task);
        timeLog.setUserId(userId);
        timeLog = timeLogRepository.save(timeLog);

        // Update task consumed hours
        double totalHours = timeLogRepository.sumHoursByTaskId(taskId);
        task.setConsumedHours(totalHours);
        taskRepository.save(task);

        return timeLogMapper.toResponse(timeLog);
    }

    @Transactional(readOnly = true)
    public Page<TimeLogResponse> findByTaskId(UUID taskId, Pageable pageable) {
        if (!taskRepository.existsById(taskId)) {
            throw new ResourceNotFoundException("Task", taskId);
        }
        return timeLogRepository.findByTaskId(taskId, pageable)
                .map(timeLogMapper::toResponse);
    }
}
