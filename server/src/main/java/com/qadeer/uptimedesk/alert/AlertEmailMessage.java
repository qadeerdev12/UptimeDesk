package com.qadeer.uptimedesk.alert;

import org.springframework.util.StringUtils;

public record AlertEmailMessage(String to, String subject, String body) {
    public AlertEmailMessage {
        if (!StringUtils.hasText(to)) {
            throw new IllegalArgumentException("Alert email recipient is required.");
        }
        if (!StringUtils.hasText(subject)) {
            throw new IllegalArgumentException("Alert email subject is required.");
        }
        if (!StringUtils.hasText(body)) {
            throw new IllegalArgumentException("Alert email body is required.");
        }
    }
}
