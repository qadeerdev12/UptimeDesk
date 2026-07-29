package com.qadeer.uptimedesk.alert;

import com.qadeer.uptimedesk.auth.UserIdentity;
import com.qadeer.uptimedesk.auth.UserIdentityService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.time.Clock;
import java.util.List;

@RestController
@RequestMapping("/api/alert-channels")
public class AlertChannelController {
    private final AlertChannelRepository alertChannelRepository;
    private final Clock clock;
    private final UserIdentityService userIdentityService;

    public AlertChannelController(
            AlertChannelRepository alertChannelRepository,
            Clock clock,
            UserIdentityService userIdentityService
    ) {
        this.alertChannelRepository = alertChannelRepository;
        this.clock = clock;
        this.userIdentityService = userIdentityService;
    }

    @GetMapping
    List<AlertChannelResponse> listAlertChannels(Authentication authentication) {
        UserIdentity currentUser = currentUser(authentication);

        return alertChannelRepository.findByOwnerIdOrderByCreatedAtDesc(currentUser.getId())
                .stream()
                .map(AlertChannelResponse::from)
                .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    AlertChannelResponse createAlertChannel(
            @Valid @RequestBody CreateAlertChannelRequest request,
            Authentication authentication
    ) {
        AlertChannel channel = new AlertChannel();
        channel.setOwner(currentUser(authentication));
        channel.setType(AlertChannelType.EMAIL);
        channel.setDestination(request.destination().trim());
        channel.setCooldownMinutes(request.cooldownMinutes() == null ? 30 : request.cooldownMinutes());
        channel.setEnabled(true);
        channel.setCreatedAt(clock.instant());
        channel.setUpdatedAt(clock.instant());

        return AlertChannelResponse.from(alertChannelRepository.save(channel));
    }

    @PutMapping("/{id}")
    AlertChannelResponse updateAlertChannel(
            @PathVariable Long id,
            @Valid @RequestBody UpdateAlertChannelRequest request,
            Authentication authentication
    ) {
        AlertChannel channel = findAlertChannel(id, authentication);
        channel.setDestination(request.destination().trim());
        channel.setEnabled(request.enabled());
        channel.setCooldownMinutes(request.cooldownMinutes() == null ? 30 : request.cooldownMinutes());
        channel.setUpdatedAt(clock.instant());

        return AlertChannelResponse.from(alertChannelRepository.save(channel));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void deleteAlertChannel(@PathVariable Long id, Authentication authentication) {
        AlertChannel channel = findAlertChannel(id, authentication);
        alertChannelRepository.delete(channel);
    }

    private AlertChannel findAlertChannel(Long id, Authentication authentication) {
        if (authentication == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required.");
        }

        return alertChannelRepository.findByIdAndOwnerExternalSubject(id, authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Alert channel not found."));
    }

    private UserIdentity currentUser(Authentication authentication) {
        if (authentication == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Authentication is required.");
        }

        return userIdentityService.findOrCreate(authentication.getName(), emailFrom(authentication));
    }

    private String emailFrom(Authentication authentication) {
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getClaimAsString("email");
        }

        return null;
    }
}
