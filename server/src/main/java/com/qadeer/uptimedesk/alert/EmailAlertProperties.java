package com.qadeer.uptimedesk.alert;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "uptimedesk.alerts.email")
@Getter
@Setter
public class EmailAlertProperties {
    private boolean enabled;
    private String fromAddress = "noreply@uptimedesk.local";
    private String fromName = "UptimeDesk";
}
