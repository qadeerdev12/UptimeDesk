package com.qadeer.uptimedesk.incident;

public record IncidentDecision(
        IncidentTransition transition,
        String reason
) {
    public static IncidentDecision none() {
        return new IncidentDecision(IncidentTransition.NONE, null);
    }

    public static IncidentDecision open(String reason) {
        return new IncidentDecision(IncidentTransition.OPEN_INCIDENT, reason);
    }

    public static IncidentDecision resolve(String reason) {
        return new IncidentDecision(IncidentTransition.RESOLVE_INCIDENT, reason);
    }
}
