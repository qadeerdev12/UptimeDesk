package com.qadeer.uptimedesk.incident;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.util.List;

@Service
public class IncidentService {

    private final Clock clock;
    private final IncidentRepository incidentRepository;

    public IncidentService(Clock clock, IncidentRepository incidentRepository) {
        this.clock = clock;
        this.incidentRepository = incidentRepository;
    }

    @Transactional(readOnly = true)
    public List<IncidentResponse> listActiveIncidents() {
        return incidentRepository.findByStatusInOrderByOpenedAtDesc(
                        List.of(IncidentStatus.OPEN, IncidentStatus.ACKNOWLEDGED)
                )
                .stream()
                .map(IncidentResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<IncidentResponse> listMonitorIncidents(Long monitorId) {
        return incidentRepository.findTop20ByMonitorIdOrderByOpenedAtDesc(monitorId)
                .stream()
                .map(IncidentResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public IncidentResponse getIncident(Long id) {
        return incidentRepository.findById(id)
                .map(IncidentResponse::from)
                .orElseThrow(() -> new IncidentNotFoundException(id));
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
