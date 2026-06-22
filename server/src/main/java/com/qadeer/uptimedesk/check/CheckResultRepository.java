package com.qadeer.uptimedesk.check;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface CheckResultRepository extends JpaRepository<CheckResult, Long> {
    List<CheckResult> findTop20ByMonitorIdOrderByCheckedAtDesc(Long monitorId);

    List<CheckResult> findByCheckedAtAfter(Instant checkedAt);

    List<CheckResult> findTop5ByStatusOrderByCheckedAtDesc(CheckStatus status);
}
