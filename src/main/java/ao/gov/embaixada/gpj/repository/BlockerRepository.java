package ao.gov.embaixada.gpj.repository;

import ao.gov.embaixada.gpj.entity.Blocker;
import ao.gov.embaixada.gpj.enums.BlockerStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BlockerRepository extends JpaRepository<Blocker, UUID> {

    Page<Blocker> findByTaskId(UUID taskId, Pageable pageable);

    long countByStatus(BlockerStatus status);

    long countByTaskId(UUID taskId);
}
