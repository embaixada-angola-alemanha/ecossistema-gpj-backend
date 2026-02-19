package ao.gov.embaixada.gpj.repository;

import ao.gov.embaixada.gpj.entity.MaintenanceWindow;
import ao.gov.embaixada.gpj.enums.MaintenanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface MaintenanceWindowRepository extends JpaRepository<MaintenanceWindow, UUID> {

    List<MaintenanceWindow> findByStatus(MaintenanceStatus status);

    List<MaintenanceWindow> findByScheduledStartBetween(Instant start, Instant end);

    List<MaintenanceWindow> findByStatusIn(List<MaintenanceStatus> statuses);
}
