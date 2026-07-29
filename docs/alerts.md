# Alerts Setup

Sprint 8 adds notification support for monitor outages and recoveries.

## Email Configuration

Email alerts are disabled by default. Enable them only after SMTP credentials are available in the backend environment.

Required environment variables:

```text
ALERT_EMAIL_ENABLED=true
ALERT_EMAIL_FROM=alerts@your-domain.com
ALERT_EMAIL_FROM_NAME=UptimeDesk Alerts
SMTP_HOST=smtp.your-provider.com
SMTP_PORT=587
SMTP_USERNAME=your-smtp-username
SMTP_PASSWORD=your-smtp-password
SMTP_AUTH=true
SMTP_STARTTLS_ENABLE=true
```

Local development can keep `ALERT_EMAIL_ENABLED=false` so alert code can be tested without sending real email.

## Current Alert Flow

- Alert destinations are stored as `AlertChannel` records owned by the authenticated user.
- Email sending is routed through `AlertEmailSender`.
- `MailAlertEmailSender` uses Spring's `MailSender` and returns `false` when email alerts are disabled.

## Next Steps

- Call the email sender when an incident opens.
- Send a recovery email when an incident resolves.
- Add cooldown rules so repeated failures do not spam users.
- Add a frontend alert settings page for managing alert channels.
