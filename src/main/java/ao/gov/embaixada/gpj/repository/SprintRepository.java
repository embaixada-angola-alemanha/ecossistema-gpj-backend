package ao.gov.embaixada.gpj.repository;

import ao.gov.embaixada.gpj.entity.Sprint;
import ao.gov.embaixada.gpj.enums.SprintStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SprintRepository extends JpaRepository<Sprint, UUID> {

    Page<Sprint> findByStatus(SprintStatus status, Pageable pageable);

    List<Sprint> findByStatus(SprintStatus status);

    long countByStatus(SprintStatus status);
}
