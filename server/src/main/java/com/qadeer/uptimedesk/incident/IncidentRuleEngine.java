package com.qadeer.uptimedesk.incident;

import com.qadeer.uptimedesk.check.CheckStatus;
import com.qadeer.uptimedesk.monitor.MonitorStatus;
import org.springframework.stereotype.Service;

@Service
public class IncidentRuleEngine {

    public IncidentDecision evaluate(
            MonitorStatus previousMonitorStatus,
            MonitorStatus currentMonitorStatus,
            CheckStatus checkStatus,
            int consecutiveFailures,
            int failureThreshold
    ) {
        if (
                checkStatus == CheckStatus.FAILURE
                        && currentMonitorStatus == MonitorStatus.DOWN
                        && previousMonitorStatus != MonitorStatus.DOWN
        ) {
            return IncidentDecision.open(
                    "Monitor reached " + consecutiveFailures + " consecutive failures; threshold is " + failureThreshold + "."
            );
        }

        if (
                checkStatus == CheckStatus.SUCCESS
                        && previousMonitorStatus == MonitorStatus.DOWN
                        && currentMonitorStatus == MonitorStatus.UP
        ) {
            return IncidentDecision.resolve("Monitor recovered after a successful check.");
        }

        return IncidentDecision.none();
    }
}
