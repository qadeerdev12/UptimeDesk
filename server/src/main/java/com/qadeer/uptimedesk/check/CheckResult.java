package com.qadeer.uptimedesk.check;

import com.qadeer.uptimedesk.incident.IncidentTransition;
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
public class CheckResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private Monitor monitor;

    private Instant checkedAt = Instant.now();
    private Integer statusCode;
    private long responseTimeMs;

    @Enumerated(EnumType.STRING)
    private CheckStatus status;

    @Enumerated(EnumType.STRING)
    private IncidentTransition incidentTransition = IncidentTransition.NONE;

    private String incidentReason;

    private String errorMessage;
}
