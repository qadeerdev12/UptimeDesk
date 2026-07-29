package com.qadeer.uptimedesk.alert;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.qadeer.uptimedesk.auth.UserIdentity;
import com.qadeer.uptimedesk.auth.UserIdentityRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AlertChannelControllerIntegrationTest {

    @Autowired
    private AlertChannelRepository alertChannelRepository;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserIdentityRepository userIdentityRepository;

    @Test
    void rejectsUnauthenticatedAlertChannelRequests() throws Exception {
        mockMvc.perform(get("/api/alert-channels"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createsListsUpdatesAndDeletesEmailAlertChannel() throws Exception {
        String createPayload = """
                {
                  "destination": "alerts@example.com",
                  "cooldownMinutes": 20
                }
                """;

        String createResponse = mockMvc.perform(post("/api/alert-channels")
                        .with(jwtFor("supabase-user-1", "qadeer@example.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(createPayload))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.type").value("EMAIL"))
                .andExpect(jsonPath("$.destination").value("alerts@example.com"))
                .andExpect(jsonPath("$.enabled").value(true))
                .andExpect(jsonPath("$.cooldownMinutes").value(20))
                .andReturn()
                .getResponse()
                .getContentAsString();
        int channelId = objectMapper.readTree(createResponse).get("id").asInt();

        mockMvc.perform(get("/api/alert-channels")
                        .with(jwtFor("supabase-user-1", "qadeer@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(channelId));

        String updatePayload = """
                {
                  "destination": "ops@example.com",
                  "enabled": false,
                  "cooldownMinutes": 45
                }
                """;

        mockMvc.perform(put("/api/alert-channels/{id}", channelId)
                        .with(jwtFor("supabase-user-1", "qadeer@example.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.destination").value("ops@example.com"))
                .andExpect(jsonPath("$.enabled").value(false))
                .andExpect(jsonPath("$.cooldownMinutes").value(45));

        mockMvc.perform(delete("/api/alert-channels/{id}", channelId)
                        .with(jwtFor("supabase-user-1", "qadeer@example.com")))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/alert-channels")
                        .with(jwtFor("supabase-user-1", "qadeer@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));
    }

    @Test
    void hidesAlertChannelsOwnedByOtherUsers() throws Exception {
        UserIdentity owner = userIdentityRepository.save(userIdentity("owner-user", "owner@example.com"));
        AlertChannel channel = new AlertChannel();
        channel.setOwner(owner);
        channel.setDestination("owner@example.com");
        AlertChannel savedChannel = alertChannelRepository.save(channel);

        mockMvc.perform(get("/api/alert-channels")
                        .with(jwtFor("other-user", "other@example.com")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        String updatePayload = """
                {
                  "destination": "other@example.com",
                  "enabled": true,
                  "cooldownMinutes": 15
                }
                """;

        mockMvc.perform(put("/api/alert-channels/{id}", savedChannel.getId())
                        .with(jwtFor("other-user", "other@example.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updatePayload))
                .andExpect(status().isNotFound());
    }

    @Test
    void rejectsInvalidEmailAlertDestination() throws Exception {
        String invalidPayload = """
                {
                  "destination": "not-an-email",
                  "cooldownMinutes": 20
                }
                """;

        mockMvc.perform(post("/api/alert-channels")
                        .with(jwtFor("supabase-user-1", "qadeer@example.com"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidPayload))
                .andExpect(status().isBadRequest());
    }

    private UserIdentity userIdentity(String externalSubject, String email) {
        UserIdentity identity = new UserIdentity();
        identity.setExternalSubject(externalSubject);
        identity.setEmail(email);
        return identity;
    }

    private static org.springframework.test.web.servlet.request.RequestPostProcessor jwtFor(String subject, String email) {
        return jwt().jwt(jwt -> jwt.subject(subject).claim("email", email));
    }
}
