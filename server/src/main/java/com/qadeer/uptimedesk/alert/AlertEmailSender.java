package com.qadeer.uptimedesk.alert;

public interface AlertEmailSender {
    boolean send(AlertEmailMessage message);
}
