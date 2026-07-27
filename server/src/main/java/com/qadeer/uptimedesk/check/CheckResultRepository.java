package com.qadeer.uptimedesk.check;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface CheckResultRepository extends JpaRepository<CheckResult, Long> {
    List<CheckResult> findTop20ByMonitorIdOrderByCheckedAtDesc(Long monitorId);

    List<CheckResult> findByCheckedAtAfter(Instant checkedAt);

    List<CheckResult> findByMonitorOwnerExternalSubjectAndCheckedAtAfter(String externalSubject, Instant checkedAt);

    List<CheckResult> findTop5ByStatusOrderByCheckedAtDesc(CheckStatus status);

    List<CheckResult> findTop5ByMonitorOwnerExternalSubjectAndStatusOrderByCheckedAtDesc(
            String externalSubject,
            CheckStatus status
    );

    Optional<CheckResult> findByIdAndMonitorOwnerExternalSubject(Long id, String externalSubject);
}
