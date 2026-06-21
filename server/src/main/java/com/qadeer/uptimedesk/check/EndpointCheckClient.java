package com.qadeer.uptimedesk.check;

import com.qadeer.uptimedesk.monitor.Monitor;

public interface EndpointCheckClient {
    EndpointCheck check(Monitor monitor);
}
