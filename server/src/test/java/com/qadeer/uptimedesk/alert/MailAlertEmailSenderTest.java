package com.qadeer.uptimedesk.alert;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class MailAlertEmailSenderTest {

    private final MailSender mailSender = mock(MailSender.class);
    private final EmailAlertProperties properties = new EmailAlertProperties();
    private final MailAlertEmailSender alertEmailSender = new MailAlertEmailSender(mailSender, properties);

    @Test
    void skipsSendingWhenEmailAlertsAreDisabled() {
        properties.setEnabled(false);

        boolean sent = alertEmailSender.send(new AlertEmailMessage(
                "qadeer@example.com",
                "Monitor is down",
                "Portfolio API is currently failing."
        ));

        assertThat(sent).isFalse();
        verify(mailSender, never()).send(org.mockito.ArgumentMatchers.any(SimpleMailMessage.class));
    }

    @Test
    void sendsEmailWhenEmailAlertsAreEnabled() {
        properties.setEnabled(true);
        properties.setFromAddress("alerts@uptimedesk.dev");
        properties.setFromName("UptimeDesk Alerts");

        boolean sent = alertEmailSender.send(new AlertEmailMessage(
                "qadeer@example.com",
                "Monitor is down",
                "Portfolio API is currently failing."
        ));

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(messageCaptor.capture());

        assertThat(sent).isTrue();
        assertThat(messageCaptor.getValue().getTo()).containsExactly("qadeer@example.com");
        assertThat(messageCaptor.getValue().getSubject()).isEqualTo("Monitor is down");
        assertThat(messageCaptor.getValue().getText()).isEqualTo("Portfolio API is currently failing.");
        assertThat(messageCaptor.getValue().getFrom()).isEqualTo("UptimeDesk Alerts <alerts@uptimedesk.dev>");
    }
}
