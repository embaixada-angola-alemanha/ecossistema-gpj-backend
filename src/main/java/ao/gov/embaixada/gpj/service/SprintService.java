package ao.gov.embaixada.gpj.service;

import ao.gov.embaixada.gpj.dto.SprintCreateRequest;
import ao.gov.embaixada.gpj.dto.SprintResponse;
import ao.gov.embaixada.gpj.dto.SprintUpdateRequest;
import ao.gov.embaixada.gpj.entity.Sprint;
import ao.gov.embaixada.gpj.enums.SprintStatus;
import ao.gov.embaixada.gpj.exception.ResourceNotFoundException;
import ao.gov.embaixada.gpj.mapper.SprintMapper;
import ao.gov.embaixada.gpj.repository.SprintRepository;
import ao.gov.embaixada.gpj.statemachine.SprintStateMachine;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class SprintService {

    private final SprintRepository sprintRepository;
    private final SprintMapper sprintMapper;
    private final SprintStateMachine stateMachine;

    public SprintService(SprintRepository sprintRepository,
                         SprintMapper sprintMapper,
                         SprintStateMachine stateMachine) {
        this.sprintRepository = sprintRepository;
        this.sprintMapper = sprintMapper;
        this.stateMachine = stateMachine;
    }

    public SprintResponse create(SprintCreateRequest request) {
        Sprint sprint = sprintMapper.toEntity(request);
        sprint.setStatus(SprintStatus.PLANNING);
        sprint = sprintRepository.save(sprint);
        return sprintMapper.toResponse(sprint);
    }

    @Transactional(readOnly = true)
    public SprintResponse findById(UUID id) {
        Sprint sprint = sprintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint", id));
        return sprintMapper.toResponse(sprint);
    }

    @Transactional(readOnly = true)
    public Page<SprintResponse> findAll(Pageable pageable) {
        return sprintRepository.findAll(pageable)
                .map(sprintMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<SprintResponse> findByStatus(SprintStatus status, Pageable pageable) {
        return sprintRepository.findByStatus(status, pageable)
                .map(sprintMapper::toResponse);
    }

    public SprintResponse update(UUID id, SprintUpdateRequest request) {
        Sprint sprint = sprintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint", id));
        sprintMapper.updateEntity(request, sprint);
        sprint = sprintRepository.save(sprint);
        return sprintMapper.toResponse(sprint);
    }

    public SprintResponse updateStatus(UUID id, SprintStatus newStatus) {
        Sprint sprint = sprintRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint", id));
        stateMachine.validateTransition(sprint.getStatus(), newStatus);
        sprint.setStatus(newStatus);
        sprint = sprintRepository.save(sprint);
        return sprintMapper.toResponse(sprint);
    }

    public void delete(UUID id) {
        if (!sprintRepository.existsById(id)) {
            throw new ResourceNotFoundException("Sprint", id);
        }
        sprintRepository.deleteById(id);
    }
}
