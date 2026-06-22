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

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            boolean statusMatches = response.statusCode() == monitor.getExpectedStatusCode();

            if (!statusMatches) {
                return EndpointCheck.failure(
                        response.statusCode(),
                        "Expected HTTP " + monitor.getExpectedStatusCode() + " but received HTTP " + response.statusCode()
                );
            }

            String expectedKeyword = monitor.getExpectedKeyword();
            boolean keywordRequired = expectedKeyword != null && !expectedKeyword.isBlank();

            if (keywordRequired && !response.body().contains(expectedKeyword)) {
                return EndpointCheck.failure(
                        response.statusCode(),
                        "Response did not contain expected keyword: " + expectedKeyword
                );
            }

            return EndpointCheck.success(response.statusCode());
        } catch (Exception ex) {
            return EndpointCheck.failure(null, ex.getMessage());
        }
    }
}
