package ao.gov.embaixada.gpj.repository;

import ao.gov.embaixada.gpj.entity.Deployment;
import ao.gov.embaixada.gpj.enums.DeploymentEnvironment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DeploymentRepository extends JpaRepository<Deployment, UUID> {

    Page<Deployment> findByServiceId(UUID serviceId, Pageable pageable);

    Page<Deployment> findByEnvironment(DeploymentEnvironment environment, Pageable pageable);

    List<Deployment> findTop10ByOrderByDeployedAtDesc();
}
