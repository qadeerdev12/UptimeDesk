package com.qadeer.uptimedesk.alert;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AlertChannelRepository extends JpaRepository<AlertChannel, Long> {
    List<AlertChannel> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);

    List<AlertChannel> findByOwnerIdAndEnabledTrueOrderByCreatedAtDesc(Long ownerId);
}
