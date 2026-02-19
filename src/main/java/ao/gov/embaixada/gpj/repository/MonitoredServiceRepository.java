package ao.gov.embaixada.gpj.repository;

import ao.gov.embaixada.gpj.entity.MonitoredService;
import ao.gov.embaixada.gpj.enums.ServiceStatus;
import ao.gov.embaixada.gpj.enums.ServiceType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MonitoredServiceRepository extends JpaRepository<MonitoredService, UUID> {

    List<MonitoredService> findByStatus(ServiceStatus status);

    List<MonitoredService> findByType(ServiceType type);

    Optional<MonitoredService> findByName(String name);

    long countByStatus(ServiceStatus status);
}
