package com.qadeer.uptimedesk.check;

public record EndpointCheck(
        Integer statusCode,
        boolean success,
        String errorMessage
) {
    public static EndpointCheck success(int statusCode) {
        return new EndpointCheck(statusCode, true, null);
    }

    public static EndpointCheck failure(Integer statusCode, String errorMessage) {
        return new EndpointCheck(statusCode, false, errorMessage);
    }
}
