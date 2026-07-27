package com.qadeer.uptimedesk.auth;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class UserIdentityServiceTest {

    private final UserIdentityRepository userIdentityRepository = mock(UserIdentityRepository.class);
    private final UserIdentityService userIdentityService = new UserIdentityService(userIdentityRepository);

    @Test
    void createsIdentityWhenSubjectDoesNotExist() {
        when(userIdentityRepository.findByExternalSubject("supabase-user-123")).thenReturn(Optional.empty());
        when(userIdentityRepository.save(org.mockito.ArgumentMatchers.any(UserIdentity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserIdentity identity = userIdentityService.findOrCreate("supabase-user-123", "qadeer@example.com");

        assertThat(identity.getExternalSubject()).isEqualTo("supabase-user-123");
        assertThat(identity.getEmail()).isEqualTo("qadeer@example.com");
        verify(userIdentityRepository).save(identity);
    }

    @Test
    void reusesExistingIdentityAndUpdatesEmailWhenPresent() {
        UserIdentity existingIdentity = new UserIdentity();
        existingIdentity.setExternalSubject("supabase-user-123");

        when(userIdentityRepository.findByExternalSubject("supabase-user-123"))
                .thenReturn(Optional.of(existingIdentity));

        UserIdentity identity = userIdentityService.findOrCreate("supabase-user-123", "updated@example.com");

        assertThat(identity).isSameAs(existingIdentity);
        assertThat(identity.getEmail()).isEqualTo("updated@example.com");
        verify(userIdentityRepository, never()).save(org.mockito.ArgumentMatchers.any(UserIdentity.class));
    }
}
