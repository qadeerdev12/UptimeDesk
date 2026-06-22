package com.qadeer.uptimedesk.check;

import com.qadeer.uptimedesk.monitor.Monitor;
import com.qadeer.uptimedesk.monitor.HttpMethod;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

class JavaHttpEndpointCheckClientTest {

    private HttpServer server;

    @AfterEach
    void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void succeedsWhenExpectedKeywordIsPresent() throws IOException {
        startServer("service is healthy");

        Monitor monitor = monitorFor("/health");
        monitor.setExpectedKeyword("healthy");

        EndpointCheck result = new JavaHttpEndpointCheckClient().check(monitor);

        assertThat(result.success()).isTrue();
        assertThat(result.statusCode()).isEqualTo(200);
        assertThat(result.errorMessage()).isNull();
    }

    @Test
    void failsWhenExpectedKeywordIsMissing() throws IOException {
        startServer("service is unavailable");

        Monitor monitor = monitorFor("/health");
        monitor.setExpectedKeyword("healthy");

        EndpointCheck result = new JavaHttpEndpointCheckClient().check(monitor);

        assertThat(result.success()).isFalse();
        assertThat(result.statusCode()).isEqualTo(200);
        assertThat(result.errorMessage()).contains("expected keyword");
    }

    private void startServer(String responseBody) throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/health", exchange -> {
            byte[] response = responseBody.getBytes(StandardCharsets.UTF_8);
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        server.start();
    }

    private Monitor monitorFor(String path) {
        Monitor monitor = new Monitor();
        monitor.setName("Test API");
        monitor.setUrl("http://localhost:" + server.getAddress().getPort() + path);
        monitor.setMethod(HttpMethod.GET);
        monitor.setExpectedStatusCode(200);
        monitor.setTimeoutSeconds(5);

        return monitor;
    }
}
