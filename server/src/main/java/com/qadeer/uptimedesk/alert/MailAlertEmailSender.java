package com.qadeer.uptimedesk.alert;

import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class MailAlertEmailSender implements AlertEmailSender {
    private final MailSender mailSender;
    private final EmailAlertProperties properties;

    public MailAlertEmailSender(MailSender mailSender, EmailAlertProperties properties) {
        this.mailSender = mailSender;
        this.properties = properties;
    }

    @Override
    public boolean send(AlertEmailMessage message) {
        if (!properties.isEnabled()) {
            return false;
        }

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(message.to());
        mailMessage.setSubject(message.subject());
        mailMessage.setText(message.body());
        mailMessage.setFrom(formatFromAddress());

        mailSender.send(mailMessage);
        return true;
    }

    private String formatFromAddress() {
        if (StringUtils.hasText(properties.getFromName())) {
            return properties.getFromName() + " <" + properties.getFromAddress() + ">";
        }

        return properties.getFromAddress();
    }
}
