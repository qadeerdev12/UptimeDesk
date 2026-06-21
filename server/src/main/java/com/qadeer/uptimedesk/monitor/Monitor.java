package com.qadeer.uptimedesk.monitor;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class Monitor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    private String name;

    @NotBlank
    private String url;

    @NotNull
    @Enumerated(EnumType.STRING)
    private HttpMethod method = HttpMethod.GET;

    @Min(100)
    @Max(599)
    private int expectedStatusCode = 200;

    @Min(1)
    private int intervalMinutes = 5;

    @Min(1)
    private int timeoutSeconds = 5;

    @Min(1)
    @Max(10)
    private int failureThreshold = 2;

    @Min(0)
    private int consecutiveFailures = 0;

    private boolean active = true;

    @Enumerated(EnumType.STRING)
    private MonitorStatus status = MonitorStatus.UNKNOWN;

    private Instant createdAt = Instant.now();
    private Instant lastCheckedAt;
}
