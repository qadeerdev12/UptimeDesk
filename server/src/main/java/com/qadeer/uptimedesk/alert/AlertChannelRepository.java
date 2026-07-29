package com.qadeer.uptimedesk.alert;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AlertChannelRepository extends JpaRepository<AlertChannel, Long> {
    List<AlertChannel> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);

    List<AlertChannel> findByOwnerIdAndEnabledTrueOrderByCreatedAtDesc(Long ownerId);

    Optional<AlertChannel> findByIdAndOwnerExternalSubject(Long id, String externalSubject);
}
