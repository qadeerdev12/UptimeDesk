package com.qadeer.uptimedesk.incident;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;

@Service
public class IncidentService {

    private final Clock clock;
    private final IncidentRepository incidentRepository;

    public IncidentService(Clock clock, IncidentRepository incidentRepository) {
        this.clock = clock;
        this.incidentRepository = incidentRepository;
    }

    @Transactional
    public IncidentResponse acknowledgeIncident(Long id) {
        Incident incident = incidentRepository.findById(id)
                .orElseThrow(() -> new IncidentNotFoundException(id));

        if (incident.getStatus() == IncidentStatus.RESOLVED) {
            throw new IncidentActionException("Resolved incidents cannot be acknowledged.");
        }

        incident.setStatus(IncidentStatus.ACKNOWLEDGED);
        incident.setAcknowledgedAt(clock.instant());

        return IncidentResponse.from(incidentRepository.save(incident));
    }
}
