package com.qadeer.uptimedesk.monitor;

public class MonitorNotFoundException extends RuntimeException {

    public MonitorNotFoundException(Long id) {
        super("Monitor not found: " + id);
    }
}
