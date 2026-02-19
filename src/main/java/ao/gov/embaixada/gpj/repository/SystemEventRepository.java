package ao.gov.embaixada.gpj.repository;

import ao.gov.embaixada.gpj.entity.SystemEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface SystemEventRepository extends JpaRepository<SystemEvent, UUID> {

    Page<SystemEvent> findBySource(String source, Pageable pageable);

    Page<SystemEvent> findByEventType(String eventType, Pageable pageable);

    boolean existsByEventId(UUID eventId);

    Page<SystemEvent> findAllByOrderByTimestampDesc(Pageable pageable);

    @Query("SELECT e.source, COUNT(e) FROM SystemEvent e WHERE e.timestamp > :since GROUP BY e.source")
    List<Object[]> countBySourceSince(@Param("since") Instant since);

    @Query("SELECT COUNT(e) FROM SystemEvent e WHERE e.timestamp > :since")
    long countSince(@Param("since") Instant since);
}
