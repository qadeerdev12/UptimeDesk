package com.qadeer.uptimedesk.alert;

import com.qadeer.uptimedesk.auth.UserIdentity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;

@Entity
@Getter
@Setter
@NoArgsConstructor
public class AlertChannel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private UserIdentity owner;

    @NotNull
    @Enumerated(EnumType.STRING)
    private AlertChannelType type = AlertChannelType.EMAIL;

    @NotBlank
    private String destination;

    private boolean enabled = true;

    @Min(1)
    private int cooldownMinutes = 30;

    private Instant lastIncidentAlertSentAt;

    private Instant lastRecoveryAlertSentAt;

    private Instant createdAt = Instant.now();

    private Instant updatedAt = Instant.now();
}
