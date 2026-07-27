package com.qadeer.uptimedesk.monitor;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MonitorRepository extends JpaRepository<Monitor, Long> {
    List<Monitor> findByActiveTrue();

    List<Monitor> findByOwnerExternalSubject(String externalSubject);

    Optional<Monitor> findByIdAndOwnerExternalSubject(Long id, String externalSubject);
}
