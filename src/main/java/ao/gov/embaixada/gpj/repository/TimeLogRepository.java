package ao.gov.embaixada.gpj.repository;

import ao.gov.embaixada.gpj.entity.TimeLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TimeLogRepository extends JpaRepository<TimeLog, UUID> {

    Page<TimeLog> findByTaskId(UUID taskId, Pageable pageable);

    @Query("SELECT COALESCE(SUM(tl.hours), 0) FROM TimeLog tl WHERE tl.task.id = :taskId")
    double sumHoursByTaskId(UUID taskId);

    @Query("SELECT COALESCE(SUM(tl.hours), 0) FROM TimeLog tl")
    double sumAllHours();
}
