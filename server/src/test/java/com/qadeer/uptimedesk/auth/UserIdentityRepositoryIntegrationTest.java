package com.qadeer.uptimedesk.auth;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserIdentityRepositoryIntegrationTest {

    @Autowired
    private UserIdentityRepository userIdentityRepository;

    @Test
    void persistsAndFindsIdentityByExternalSubject() {
        UserIdentity identity = new UserIdentity();
        identity.setExternalSubject("supabase-user-123");
        identity.setEmail("qadeer@example.com");

        UserIdentity savedIdentity = userIdentityRepository.save(identity);

        assertThat(userIdentityRepository.findByExternalSubject("supabase-user-123"))
                .hasValueSatisfying(foundIdentity -> {
                    assertThat(foundIdentity.getId()).isEqualTo(savedIdentity.getId());
                    assertThat(foundIdentity.getEmail()).isEqualTo("qadeer@example.com");
                    assertThat(foundIdentity.getCreatedAt()).isNotNull();
                });
    }

    @Test
    void checksIdentityExistenceByExternalSubject() {
        UserIdentity identity = new UserIdentity();
        identity.setExternalSubject("supabase-user-456");
        userIdentityRepository.save(identity);

        assertThat(userIdentityRepository.existsByExternalSubject("supabase-user-456")).isTrue();
        assertThat(userIdentityRepository.existsByExternalSubject("missing-user")).isFalse();
    }
}
