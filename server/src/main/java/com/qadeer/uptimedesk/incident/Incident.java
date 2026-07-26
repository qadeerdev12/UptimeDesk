package com.qadeer.uptimedesk.incident;

import com.qadeer.uptimedesk.check.CheckResult;
import com.qadeer.uptimedesk.monitor.Monitor;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Incident {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Monitor monitor;

    @ManyToOne(fetch = FetchType.LAZY)
    private CheckResult openedByCheckResult;

    @ManyToOne(fetch = FetchType.LAZY)
    private CheckResult resolvedByCheckResult;

    @Enumerated(EnumType.STRING)
    private IncidentStatus status = IncidentStatus.OPEN;

    private Instant openedAt = Instant.now();
    private Instant acknowledgedAt;
    private Instant resolvedAt;
    private String openingReason;
    private String resolutionReason;
}
