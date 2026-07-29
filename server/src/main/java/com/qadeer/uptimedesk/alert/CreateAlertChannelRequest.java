package com.qadeer.uptimedesk.alert;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateAlertChannelRequest(
        @Email
        @NotBlank
        String destination,

        @Min(1)
        @Max(1440)
        Integer cooldownMinutes
) {
}
