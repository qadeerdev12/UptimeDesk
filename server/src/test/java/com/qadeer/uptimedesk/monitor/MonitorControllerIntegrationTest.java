package com.qadeer.uptimedesk.monitor;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MonitorControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createsReadsUpdatesAndDeletesMonitor() throws Exception {
        String createPayload = """
                {
                  "name": "Portfolio API",
                  "url": "https://example.com/health",
                  "method": "GET",
                  "expectedStatusCode": 200,
                  "intervalMinutes": 5,
                  "timeoutSeconds": 5
                }
                """;

        int monitorId = mockMvc.perform(post("/api/monitors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Portfolio API"))
                .andExpect(jsonPath("$.url").value("https://example.com/health"))
                .andExpect(jsonPath("$.status").value("UNKNOWN"))
                .andReturn()
                .getResponse()
                .getContentAsString()
                .replaceAll(".*\"id\":(\\d+).*", "$1")
                .transform(Integer::parseInt);

        mockMvc.perform(get("/api/monitors/{id}", monitorId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(monitorId))
                .andExpect(jsonPath("$.name").value("Portfolio API"));

        String updatePayload = """
                {
                  "name": "Portfolio Website",
                  "url": "https://example.com/status",
                  "method": "GET",
                  "expectedStatusCode": 204,
                  "intervalMinutes": 10,
                  "timeoutSeconds": 3,
                  "expectedKeyword": "ready",
                  "requestHeaders": {
                    "X-Health-Check": "uptimedesk"
                  },
                  "failureThreshold": 2,
                  "active": false
                }
                """;

        mockMvc.perform(put("/api/monitors/{id}", monitorId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Portfolio Website"))
                .andExpect(jsonPath("$.expectedStatusCode").value(204))
                .andExpect(jsonPath("$.expectedKeyword").value("ready"))
                .andExpect(jsonPath("$.requestHeaders.X-Health-Check").value("uptimedesk"))
                .andExpect(jsonPath("$.active").value(false));

        mockMvc.perform(delete("/api/monitors/{id}", monitorId))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/monitors/{id}", monitorId))
                .andExpect(status().isNotFound());
    }

    @Test
    void rejectsInvalidMonitorUrl() throws Exception {
        String invalidPayload = """
                {
                  "name": "Broken Monitor",
                  "url": "not-a-url",
                  "method": "GET",
                  "expectedStatusCode": 200,
                  "intervalMinutes": 5,
                  "timeoutSeconds": 5
                }
                """;

        mockMvc.perform(post("/api/monitors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void listsMonitorsAsResponses() throws Exception {
        String createPayload = """
                {
                  "name": "Kanban API",
                  "url": "https://example.com/kanban/health",
                  "method": "GET",
                  "expectedStatusCode": 200,
                  "intervalMinutes": 1,
                  "timeoutSeconds": 5
                }
                """;

        mockMvc.perform(post("/api/monitors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPayload))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/monitors"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("Kanban API"));
    }
}
