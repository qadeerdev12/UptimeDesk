package com.qadeer.uptimedesk.alert;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UpdateAlertChannelRequest(
        @Email
        @NotBlank
        String destination,

        @NotNull
        Boolean enabled,

        @Min(1)
        @Max(1440)
        Integer cooldownMinutes
) {
}
