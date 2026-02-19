package ao.gov.embaixada.gpj.repository;

import ao.gov.embaixada.gpj.entity.IncidentUpdate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IncidentUpdateRepository extends JpaRepository<IncidentUpdate, UUID> {

    List<IncidentUpdate> findByIncidentIdOrderByCreatedAtAsc(UUID incidentId);
}
