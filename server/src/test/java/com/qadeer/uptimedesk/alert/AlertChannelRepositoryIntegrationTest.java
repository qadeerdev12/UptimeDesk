package com.qadeer.uptimedesk.alert;

import com.qadeer.uptimedesk.auth.UserIdentity;
import com.qadeer.uptimedesk.auth.UserIdentityRepository;
import org.junit.jupiter.api.Test;
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

        AlertChannel savedChannel = alertChannelRepository.save(emailChannel(owner, "qadeer@example.com", true));

        assertThat(alertChannelRepository.findByOwnerIdOrderByCreatedAtDesc(owner.getId()))
                .singleElement()
                .satisfies(channel -> {
                    assertThat(channel.getId()).isEqualTo(savedChannel.getId());
                    assertThat(channel.getOwner().getId()).isEqualTo(owner.getId());
                    assertThat(channel.getType()).isEqualTo(AlertChannelType.EMAIL);
                    assertThat(channel.getDestination()).isEqualTo("qadeer@example.com");
                    assertThat(channel.isEnabled()).isTrue();
                    assertThat(channel.getCreatedAt()).isNotNull();
                    assertThat(channel.getUpdatedAt()).isNotNull();
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
