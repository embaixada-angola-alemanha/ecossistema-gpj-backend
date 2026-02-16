package ao.gov.embaixada.gpj.service;

import ao.gov.embaixada.gpj.dto.TaskCreateRequest;
import ao.gov.embaixada.gpj.dto.TaskResponse;
import ao.gov.embaixada.gpj.dto.TaskUpdateRequest;
import ao.gov.embaixada.gpj.entity.Sprint;
import ao.gov.embaixada.gpj.entity.Task;
import ao.gov.embaixada.gpj.enums.TaskStatus;
import ao.gov.embaixada.gpj.exception.CapacityExceededException;
import ao.gov.embaixada.gpj.exception.CircularDependencyException;
import ao.gov.embaixada.gpj.exception.ResourceNotFoundException;
import ao.gov.embaixada.gpj.mapper.TaskMapper;
import ao.gov.embaixada.gpj.repository.SprintRepository;
import ao.gov.embaixada.gpj.repository.TaskRepository;
import ao.gov.embaixada.gpj.statemachine.TaskStateMachine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@Transactional
public class TaskService {

    private final TaskRepository taskRepository;
    private final SprintRepository sprintRepository;
    private final TaskMapper taskMapper;
    private final TaskStateMachine stateMachine;

    public TaskService(TaskRepository taskRepository,
                       SprintRepository sprintRepository,
                       TaskMapper taskMapper,
                       TaskStateMachine stateMachine) {
        this.taskRepository = taskRepository;
        this.sprintRepository = sprintRepository;
        this.taskMapper = taskMapper;
        this.stateMachine = stateMachine;
    }

    public TaskResponse create(TaskCreateRequest request) {
        Task task = taskMapper.toEntity(request);
        task.setStatus(TaskStatus.BACKLOG);
        if (task.getPriority() == null) {
            task.setPriority(ao.gov.embaixada.gpj.enums.TaskPriority.MEDIUM);
        }

        if (request.sprintId() != null) {
            Sprint sprint = sprintRepository.findById(request.sprintId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sprint", request.sprintId()));
            checkCapacity(sprint, request.estimatedHours() != null ? request.estimatedHours() : 0);
            task.setSprint(sprint);
        }

        task = taskRepository.save(task);
        return taskMapper.toResponse(task);
    }

    @Transactional(readOnly = true)
    public TaskResponse findById(UUID id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", id));
        return taskMapper.toResponse(task);
    }

    @Transactional(readOnly = true)
    public Page<TaskResponse> findAll(Pageable pageable) {
        return taskRepository.findAll(pageable)
                .map(taskMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<TaskResponse> findBySprintId(UUID sprintId, Pageable pageable) {
        return taskRepository.findBySprintId(sprintId, pageable)
                .map(taskMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<TaskResponse> findByStatus(TaskStatus status, Pageable pageable) {
        return taskRepository.findByStatus(status, pageable)
                .map(taskMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<TaskResponse> findByAssignee(String assignee, Pageable pageable) {
        return taskRepository.findByAssignee(assignee, pageable)
                .map(taskMapper::toResponse);
    }

    public TaskResponse update(UUID id, TaskUpdateRequest request) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", id));

        if (request.sprintId() != null && (task.getSprint() == null || !request.sprintId().equals(task.getSprint().getId()))) {
            Sprint sprint = sprintRepository.findById(request.sprintId())
                    .orElseThrow(() -> new ResourceNotFoundException("Sprint", request.sprintId()));
            double newEstimate = request.estimatedHours() != null ? request.estimatedHours() : 0;
            checkCapacity(sprint, newEstimate);
            task.setSprint(sprint);
        }

        taskMapper.updateEntity(request, task);
        task = taskRepository.save(task);
        return taskMapper.toResponse(task);
    }

    public TaskResponse updateStatus(UUID id, TaskStatus newStatus) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", id));
        stateMachine.validateTransition(task.getStatus(), newStatus);
        task.setStatus(newStatus);
        if (newStatus == TaskStatus.DONE) {
            task.setProgressPct(100);
        }
        task = taskRepository.save(task);
        return taskMapper.toResponse(task);
    }

    public TaskResponse addDependency(UUID taskId, UUID dependsOnId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", taskId));
        Task dependency = taskRepository.findById(dependsOnId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", dependsOnId));

        task.getDependencies().add(dependency);

        if (hasCircularDependency(task, new HashSet<>())) {
            task.getDependencies().remove(dependency);
            throw new CircularDependencyException();
        }

        task = taskRepository.save(task);
        return taskMapper.toResponse(task);
    }

    public TaskResponse removeDependency(UUID taskId, UUID dependsOnId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", taskId));
        task.getDependencies().removeIf(d -> d.getId().equals(dependsOnId));
        task = taskRepository.save(task);
        return taskMapper.toResponse(task);
    }

    public void delete(UUID id) {
        if (!taskRepository.existsById(id)) {
            throw new ResourceNotFoundException("Task", id);
        }
        taskRepository.deleteById(id);
    }

    private boolean hasCircularDependency(Task task, Set<UUID> visited) {
        if (visited.contains(task.getId())) {
            return true;
        }
        visited.add(task.getId());
        for (Task dep : task.getDependencies()) {
            Task loaded = taskRepository.findById(dep.getId()).orElse(dep);
            if (hasCircularDependency(loaded, new HashSet<>(visited))) {
                return true;
            }
        }
        return false;
    }

    private void checkCapacity(Sprint sprint, double additionalHours) {
        if (sprint.getCapacityHours() != null && sprint.getCapacityHours() > 0) {
            double allocated = taskRepository.sumEstimatedHoursBySprintId(sprint.getId());
            if (allocated + additionalHours > sprint.getCapacityHours()) {
                throw new CapacityExceededException(sprint.getCapacityHours(), allocated + additionalHours);
            }
        }
    }
}
