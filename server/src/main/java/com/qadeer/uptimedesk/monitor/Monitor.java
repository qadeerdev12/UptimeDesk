package com.qadeer.uptimedesk.monitor;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

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

    private String expectedKeyword;

    @ElementCollection
    @CollectionTable(name = "monitor_request_headers", joinColumns = @JoinColumn(name = "monitor_id"))
    @MapKeyColumn(name = "header_name")
    @Column(name = "header_value")
    private Map<String, String> requestHeaders = new HashMap<>();

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
