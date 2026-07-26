package com.qadeer.uptimedesk.incident;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface IncidentRepository extends JpaRepository<Incident, Long> {
    Optional<Incident> findFirstByMonitorIdAndStatusInOrderByOpenedAtDesc(
            Long monitorId,
            Collection<IncidentStatus> statuses
    );

    List<Incident> findTop20ByMonitorIdOrderByOpenedAtDesc(Long monitorId);

    List<Incident> findByStatusInOrderByOpenedAtDesc(Collection<IncidentStatus> statuses);
}
