package ao.gov.embaixada.gpj.repository;

import ao.gov.embaixada.gpj.entity.Incident;
import ao.gov.embaixada.gpj.entity.MonitoredService;
import ao.gov.embaixada.gpj.enums.IncidentSeverity;
import ao.gov.embaixada.gpj.enums.IncidentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface IncidentRepository extends JpaRepository<Incident, UUID> {

    Page<Incident> findByStatus(IncidentStatus status, Pageable pageable);

    Page<Incident> findBySeverity(IncidentSeverity severity, Pageable pageable);

    long countByStatusIn(List<IncidentStatus> statuses);

    List<Incident> findByStatusInAndAffectedServicesContaining(List<IncidentStatus> statuses, MonitoredService service);
}
