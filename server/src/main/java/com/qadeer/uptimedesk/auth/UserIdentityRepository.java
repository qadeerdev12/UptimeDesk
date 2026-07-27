package com.qadeer.uptimedesk.auth;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserIdentityRepository extends JpaRepository<UserIdentity, Long> {
    Optional<UserIdentity> findByExternalSubject(String externalSubject);

    boolean existsByExternalSubject(String externalSubject);
}
