package ao.gov.embaixada.gpj.repository;

import ao.gov.embaixada.gpj.entity.HealthCheckLog;
import ao.gov.embaixada.gpj.enums.ServiceStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Repository
public interface HealthCheckLogRepository extends JpaRepository<HealthCheckLog, UUID> {

    Page<HealthCheckLog> findByServiceIdOrderByCheckedAtDesc(UUID serviceId, Pageable pageable);

    long countByServiceIdAndStatusAndCheckedAtAfter(UUID serviceId, ServiceStatus status, Instant after);

    long countByServiceIdAndCheckedAtAfter(UUID serviceId, Instant after);

    @Modifying
    @Transactional
    void deleteByCheckedAtBefore(Instant before);
}
