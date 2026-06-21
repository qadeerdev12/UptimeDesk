package com.qadeer.uptimedesk.check;

import com.qadeer.uptimedesk.monitor.Monitor;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Component
public class JavaHttpEndpointCheckClient implements EndpointCheckClient {

    @Override
    public EndpointCheck check(Monitor monitor) {
        try {
            HttpClient client = HttpClient.newBuilder()
                    .connectTimeout(Duration.ofSeconds(monitor.getTimeoutSeconds()))
                    .build();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(monitor.getUrl()))
                    .timeout(Duration.ofSeconds(monitor.getTimeoutSeconds()))
                    .method(monitor.getMethod().name(), HttpRequest.BodyPublishers.noBody())
                    .build();

            HttpResponse<Void> response = client.send(request, HttpResponse.BodyHandlers.discarding());
            boolean success = response.statusCode() == monitor.getExpectedStatusCode();

            if (success) {
                return EndpointCheck.success(response.statusCode());
            }

            return EndpointCheck.failure(
                    response.statusCode(),
                    "Expected HTTP " + monitor.getExpectedStatusCode() + " but received HTTP " + response.statusCode()
            );
        } catch (Exception ex) {
            return EndpointCheck.failure(null, ex.getMessage());
        }
    }
}
