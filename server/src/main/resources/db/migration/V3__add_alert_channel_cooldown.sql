ALTER TABLE alert_channel
    ADD COLUMN cooldown_minutes INTEGER NOT NULL DEFAULT 30;

ALTER TABLE alert_channel
    ADD COLUMN last_incident_alert_sent_at TIMESTAMP WITH TIME ZONE;

ALTER TABLE alert_channel
    ADD COLUMN last_recovery_alert_sent_at TIMESTAMP WITH TIME ZONE;

ALTER TABLE alert_channel
    ADD CONSTRAINT chk_alert_channel_cooldown_minutes CHECK (cooldown_minutes >= 1);
