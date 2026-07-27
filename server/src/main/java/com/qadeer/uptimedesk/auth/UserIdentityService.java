package com.qadeer.uptimedesk.auth;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserIdentityService {

    private final UserIdentityRepository userIdentityRepository;

    public UserIdentityService(UserIdentityRepository userIdentityRepository) {
        this.userIdentityRepository = userIdentityRepository;
    }

    @Transactional
    public UserIdentity findOrCreate(String externalSubject, String email) {
        return userIdentityRepository.findByExternalSubject(externalSubject)
                .map(existingIdentity -> updateEmailIfPresent(existingIdentity, email))
                .orElseGet(() -> createIdentity(externalSubject, email));
    }

    private UserIdentity updateEmailIfPresent(UserIdentity identity, String email) {
        if (email != null && !email.isBlank() && !email.equals(identity.getEmail())) {
            identity.setEmail(email);
        }

        return identity;
    }

    private UserIdentity createIdentity(String externalSubject, String email) {
        UserIdentity identity = new UserIdentity();
        identity.setExternalSubject(externalSubject);
        identity.setEmail(email);

        return userIdentityRepository.save(identity);
    }
}
