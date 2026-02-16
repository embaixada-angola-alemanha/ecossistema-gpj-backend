package ao.gov.embaixada.gpj.repository;

import ao.gov.embaixada.gpj.entity.Task;
import ao.gov.embaixada.gpj.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {

    Page<Task> findBySprintId(UUID sprintId, Pageable pageable);

    Page<Task> findByStatus(TaskStatus status, Pageable pageable);

    Page<Task> findByAssignee(String assignee, Pageable pageable);

    Page<Task> findBySprintIdAndStatus(UUID sprintId, TaskStatus status, Pageable pageable);

    long countByStatus(TaskStatus status);

    long countBySprintId(UUID sprintId);

    long countBySprintIdAndStatus(UUID sprintId, TaskStatus status);

    @Query("SELECT COALESCE(SUM(t.estimatedHours), 0) FROM Task t WHERE t.sprint.id = :sprintId")
    double sumEstimatedHoursBySprintId(@Param("sprintId") UUID sprintId);

    @Query("SELECT COALESCE(SUM(t.consumedHours), 0) FROM Task t WHERE t.sprint.id = :sprintId")
    double sumConsumedHoursBySprintId(@Param("sprintId") UUID sprintId);

    @Query("SELECT COALESCE(SUM(t.estimatedHours), 0) FROM Task t WHERE t.sprint.id = :sprintId AND t.status = 'DONE'")
    double sumEstimatedHoursOfCompletedBySprintId(@Param("sprintId") UUID sprintId);

    @Query("SELECT COALESCE(SUM(t.estimatedHours), 0) FROM Task t")
    double sumAllEstimatedHours();

    @Query("SELECT COALESCE(SUM(t.consumedHours), 0) FROM Task t")
    double sumAllConsumedHours();
}
