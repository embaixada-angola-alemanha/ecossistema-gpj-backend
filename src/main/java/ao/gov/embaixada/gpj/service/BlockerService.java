package ao.gov.embaixada.gpj.service;

import ao.gov.embaixada.gpj.dto.BlockerCreateRequest;
import ao.gov.embaixada.gpj.dto.BlockerResponse;
import ao.gov.embaixada.gpj.entity.Blocker;
import ao.gov.embaixada.gpj.entity.Task;
import ao.gov.embaixada.gpj.enums.BlockerStatus;
import ao.gov.embaixada.gpj.exception.ResourceNotFoundException;
import ao.gov.embaixada.gpj.mapper.BlockerMapper;
import ao.gov.embaixada.gpj.repository.BlockerRepository;
import ao.gov.embaixada.gpj.repository.TaskRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@Transactional
public class BlockerService {

    private final BlockerRepository blockerRepository;
    private final TaskRepository taskRepository;
    private final BlockerMapper blockerMapper;

    public BlockerService(BlockerRepository blockerRepository,
                          TaskRepository taskRepository,
                          BlockerMapper blockerMapper) {
        this.blockerRepository = blockerRepository;
        this.taskRepository = taskRepository;
        this.blockerMapper = blockerMapper;
    }

    public BlockerResponse create(UUID taskId, BlockerCreateRequest request) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Task", taskId));

        Blocker blocker = blockerMapper.toEntity(request);
        blocker.setTask(task);
        blocker.setStatus(BlockerStatus.OPEN);
        if (blocker.getSeverity() == null || blocker.getSeverity().isBlank()) {
            blocker.setSeverity("MEDIUM");
        }
        blocker = blockerRepository.save(blocker);
        return blockerMapper.toResponse(blocker);
    }

    @Transactional(readOnly = true)
    public Page<BlockerResponse> findByTaskId(UUID taskId, Pageable pageable) {
        if (!taskRepository.existsById(taskId)) {
            throw new ResourceNotFoundException("Task", taskId);
        }
        return blockerRepository.findByTaskId(taskId, pageable)
                .map(blockerMapper::toResponse);
    }

    public BlockerResponse update(UUID id, BlockerCreateRequest request) {
        Blocker blocker = blockerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blocker", id));
        blockerMapper.updateEntity(request, blocker);
        blocker = blockerRepository.save(blocker);
        return blockerMapper.toResponse(blocker);
    }

    public BlockerResponse resolve(UUID id, String resolution) {
        Blocker blocker = blockerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Blocker", id));
        blocker.setStatus(BlockerStatus.RESOLVED);
        blocker.setResolution(resolution);
        blocker.setResolvedAt(Instant.now());
        blocker = blockerRepository.save(blocker);
        return blockerMapper.toResponse(blocker);
    }

    public void delete(UUID id) {
        if (!blockerRepository.existsById(id)) {
            throw new ResourceNotFoundException("Blocker", id);
        }
        blockerRepository.deleteById(id);
    }
}
