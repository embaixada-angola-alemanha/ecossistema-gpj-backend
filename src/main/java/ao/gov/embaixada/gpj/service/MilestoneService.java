package ao.gov.embaixada.gpj.service;

import ao.gov.embaixada.gpj.dto.MilestoneCreateRequest;
import ao.gov.embaixada.gpj.dto.MilestoneResponse;
import ao.gov.embaixada.gpj.entity.Milestone;
import ao.gov.embaixada.gpj.entity.Sprint;
import ao.gov.embaixada.gpj.enums.MilestoneStatus;
import ao.gov.embaixada.gpj.exception.ResourceNotFoundException;
import ao.gov.embaixada.gpj.mapper.MilestoneMapper;
import ao.gov.embaixada.gpj.repository.MilestoneRepository;
import ao.gov.embaixada.gpj.repository.SprintRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Service
@Transactional
public class MilestoneService {

    private final MilestoneRepository milestoneRepository;
    private final SprintRepository sprintRepository;
    private final MilestoneMapper milestoneMapper;

    public MilestoneService(MilestoneRepository milestoneRepository,
                            SprintRepository sprintRepository,
                            MilestoneMapper milestoneMapper) {
        this.milestoneRepository = milestoneRepository;
        this.sprintRepository = sprintRepository;
        this.milestoneMapper = milestoneMapper;
    }

    public MilestoneResponse create(UUID sprintId, MilestoneCreateRequest request) {
        Sprint sprint = sprintRepository.findById(sprintId)
                .orElseThrow(() -> new ResourceNotFoundException("Sprint", sprintId));

        Milestone milestone = milestoneMapper.toEntity(request);
        milestone.setSprint(sprint);
        milestone.setStatus(MilestoneStatus.PENDING);
        milestone = milestoneRepository.save(milestone);
        return milestoneMapper.toResponse(milestone);
    }

    @Transactional(readOnly = true)
    public Page<MilestoneResponse> findBySprintId(UUID sprintId, Pageable pageable) {
        if (!sprintRepository.existsById(sprintId)) {
            throw new ResourceNotFoundException("Sprint", sprintId);
        }
        return milestoneRepository.findBySprintId(sprintId, pageable)
                .map(milestoneMapper::toResponse);
    }

    public MilestoneResponse update(UUID id, MilestoneCreateRequest request) {
        Milestone milestone = milestoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Milestone", id));
        milestoneMapper.updateEntity(request, milestone);
        milestone = milestoneRepository.save(milestone);
        return milestoneMapper.toResponse(milestone);
    }

    public MilestoneResponse complete(UUID id) {
        Milestone milestone = milestoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Milestone", id));
        milestone.setStatus(MilestoneStatus.COMPLETED);
        milestone.setCompletedAt(Instant.now());
        milestone = milestoneRepository.save(milestone);
        return milestoneMapper.toResponse(milestone);
    }

    public MilestoneResponse markMissed(UUID id) {
        Milestone milestone = milestoneRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Milestone", id));
        milestone.setStatus(MilestoneStatus.MISSED);
        milestone = milestoneRepository.save(milestone);
        return milestoneMapper.toResponse(milestone);
    }

    public void delete(UUID id) {
        if (!milestoneRepository.existsById(id)) {
            throw new ResourceNotFoundException("Milestone", id);
        }
        milestoneRepository.deleteById(id);
    }
}
