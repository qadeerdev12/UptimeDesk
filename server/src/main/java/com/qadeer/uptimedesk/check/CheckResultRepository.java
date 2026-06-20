package com.qadeer.uptimedesk.check;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CheckResultRepository extends JpaRepository<CheckResult, Long> {
    List<CheckResult> findTop20ByMonitorIdOrderByCheckedAtDesc(Long monitorId);
}
