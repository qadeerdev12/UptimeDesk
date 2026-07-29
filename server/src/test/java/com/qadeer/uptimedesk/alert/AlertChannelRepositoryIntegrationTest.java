package com.qadeer.uptimedesk.alert;

import com.qadeer.uptimedesk.auth.UserIdentity;
import com.qadeer.uptimedesk.auth.UserIdentityRepository;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class AlertChannelRepositoryIntegrationTest {

    @Autowired
    private AlertChannelRepository alertChannelRepository;

    @Autowired
    private UserIdentityRepository userIdentityRepository;

    @Test
    void persistsAlertChannelForOwner() {
        UserIdentity owner = userIdentityRepository.save(userIdentity("supabase-user-1", "qadeer@example.com"));

        AlertChannel channel = emailChannel(owner, "qadeer@example.com", true);
        channel.setCooldownMinutes(15);
        channel.setLastIncidentAlertSentAt(Instant.parse("2026-07-29T09:00:00Z"));
        channel.setLastRecoveryAlertSentAt(Instant.parse("2026-07-29T09:30:00Z"));
        AlertChannel savedChannel = alertChannelRepository.save(channel);

        assertThat(alertChannelRepository.findByOwnerIdOrderByCreatedAtDesc(owner.getId()))
                .singleElement()
                .satisfies(foundChannel -> {
                    assertThat(foundChannel.getId()).isEqualTo(savedChannel.getId());
                    assertThat(foundChannel.getOwner().getId()).isEqualTo(owner.getId());
                    assertThat(foundChannel.getType()).isEqualTo(AlertChannelType.EMAIL);
                    assertThat(foundChannel.getDestination()).isEqualTo("qadeer@example.com");
                    assertThat(foundChannel.isEnabled()).isTrue();
                    assertThat(foundChannel.getCooldownMinutes()).isEqualTo(15);
                    assertThat(foundChannel.getLastIncidentAlertSentAt()).isEqualTo(Instant.parse("2026-07-29T09:00:00Z"));
                    assertThat(foundChannel.getLastRecoveryAlertSentAt()).isEqualTo(Instant.parse("2026-07-29T09:30:00Z"));
                    assertThat(foundChannel.getCreatedAt()).isNotNull();
                    assertThat(foundChannel.getUpdatedAt()).isNotNull();
                });
    }

    @Test
    void findsOnlyEnabledChannelsForOwner() {
        UserIdentity owner = userIdentityRepository.save(userIdentity("supabase-user-1", "qadeer@example.com"));
        UserIdentity otherOwner = userIdentityRepository.save(userIdentity("supabase-user-2", "other@example.com"));

        alertChannelRepository.save(emailChannel(owner, "enabled@example.com", true));
        alertChannelRepository.save(emailChannel(owner, "disabled@example.com", false));
        alertChannelRepository.save(emailChannel(otherOwner, "other@example.com", true));

        assertThat(alertChannelRepository.findByOwnerIdAndEnabledTrueOrderByCreatedAtDesc(owner.getId()))
                .extracting(AlertChannel::getDestination)
                .containsExactly("enabled@example.com");
    }

    private AlertChannel emailChannel(UserIdentity owner, String destination, boolean enabled) {
        AlertChannel channel = new AlertChannel();
        channel.setOwner(owner);
        channel.setType(AlertChannelType.EMAIL);
        channel.setDestination(destination);
        channel.setEnabled(enabled);

        return channel;
    }

    private UserIdentity userIdentity(String externalSubject, String email) {
        UserIdentity identity = new UserIdentity();
        identity.setExternalSubject(externalSubject);
        identity.setEmail(email);

        return identity;
    }
}
