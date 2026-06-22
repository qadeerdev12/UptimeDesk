package com.qadeer.uptimedesk.incident;

import com.qadeer.uptimedesk.check.CheckStatus;
import com.qadeer.uptimedesk.monitor.MonitorStatus;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class IncidentRuleEngineTest {

    private final IncidentRuleEngine ruleEngine = new IncidentRuleEngine();

    @Test
    void opensIncidentWhenMonitorFirstMovesDown() {
        IncidentDecision decision = ruleEngine.evaluate(
                MonitorStatus.UP,
                MonitorStatus.DOWN,
                CheckStatus.FAILURE,
                2,
                2
        );

        assertThat(decision.transition()).isEqualTo(IncidentTransition.OPEN_INCIDENT);
        assertThat(decision.reason()).contains("threshold is 2");
    }

    @Test
    void doesNotReopenIncidentWhenMonitorIsAlreadyDown() {
        IncidentDecision decision = ruleEngine.evaluate(
                MonitorStatus.DOWN,
                MonitorStatus.DOWN,
                CheckStatus.FAILURE,
                3,
                2
        );

        assertThat(decision.transition()).isEqualTo(IncidentTransition.NONE);
        assertThat(decision.reason()).isNull();
    }

    @Test
    void resolvesIncidentWhenDownMonitorRecovers() {
        IncidentDecision decision = ruleEngine.evaluate(
                MonitorStatus.DOWN,
                MonitorStatus.UP,
                CheckStatus.SUCCESS,
                0,
                2
        );

        assertThat(decision.transition()).isEqualTo(IncidentTransition.RESOLVE_INCIDENT);
        assertThat(decision.reason()).contains("recovered");
    }
}
