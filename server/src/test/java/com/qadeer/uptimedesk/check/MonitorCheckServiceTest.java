package com.qadeer.uptimedesk.check;

import com.qadeer.uptimedesk.monitor.Monitor;
import com.qadeer.uptimedesk.monitor.MonitorRepository;
import com.qadeer.uptimedesk.monitor.MonitorStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MonitorCheckServiceTest {

    @Mock
    private CheckResultRepository checkResultRepository;

    @Mock
    private MonitorRepository monitorRepository;

    @Mock
    private EndpointCheckClient endpointCheckClient;

    @Test
    void keepsMonitorUpUntilFailureThresholdIsReached() {
        Monitor monitor = new Monitor();
        monitor.setFailureThreshold(2);

        when(endpointCheckClient.check(monitor)).thenReturn(EndpointCheck.failure(500, "Server error"));
        when(checkResultRepository.save(any(CheckResult.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MonitorCheckService service = new MonitorCheckService(
                checkResultRepository,
                monitorRepository,
                endpointCheckClient
        );

        service.check(monitor);

        assertThat(monitor.getConsecutiveFailures()).isEqualTo(1);
        assertThat(monitor.getStatus()).isEqualTo(MonitorStatus.UNKNOWN);

        service.check(monitor);

        assertThat(monitor.getConsecutiveFailures()).isEqualTo(2);
        assertThat(monitor.getStatus()).isEqualTo(MonitorStatus.DOWN);
    }

    @Test
    void successfulCheckResetsFailureCountAndMarksMonitorUp() {
        Monitor monitor = new Monitor();
        monitor.setFailureThreshold(2);
        monitor.setConsecutiveFailures(1);
        monitor.setStatus(MonitorStatus.DOWN);

        when(endpointCheckClient.check(monitor)).thenReturn(EndpointCheck.success(200));
        when(checkResultRepository.save(any(CheckResult.class))).thenAnswer(invocation -> invocation.getArgument(0));

        MonitorCheckService service = new MonitorCheckService(
                checkResultRepository,
                monitorRepository,
                endpointCheckClient
        );

        CheckResult result = service.check(monitor);

        assertThat(result.getStatus()).isEqualTo(CheckStatus.SUCCESS);
        assertThat(monitor.getConsecutiveFailures()).isZero();
        assertThat(monitor.getStatus()).isEqualTo(MonitorStatus.UP);
    }
}
