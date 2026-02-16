package ao.gov.embaixada.gpj.repository;

import ao.gov.embaixada.gpj.entity.Milestone;
import ao.gov.embaixada.gpj.enums.MilestoneStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface MilestoneRepository extends JpaRepository<Milestone, UUID> {

    Page<Milestone> findBySprintId(UUID sprintId, Pageable pageable);

    long countByStatus(MilestoneStatus status);

    long countBySprintId(UUID sprintId);
}
