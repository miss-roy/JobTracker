package com.jobtracker.job;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jobtracker.job.dto.JobRequest;
import com.jobtracker.job.model.JobStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class JobControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    /** Registers a fresh user and returns their bearer token. */
    private String register() throws Exception {
        String username = "user-" + UUID.randomUUID();
        String body = objectMapper.writeValueAsString(
                java.util.Map.of("username", username, "password", "secret123"));
        String json = mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        JsonNode node = objectMapper.readTree(json);
        return node.get("token").asText();
    }

    @Test
    void contextLoads() {
    }

    @Test
    void rejectsUnauthenticatedRequests() throws Exception {
        mockMvc.perform(get("/api/jobs")).andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsBadLogin() throws Exception {
        String body = objectMapper.writeValueAsString(
                java.util.Map.of("username", "nobody", "password", "wrong"));
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void createsAndListsOwnJob() throws Exception {
        String token = register();
        JobRequest request = new JobRequest("Test Co", JobStatus.APPLIED, LocalDate.now(), "hr@test.co");

        mockMvc.perform(post("/api/jobs").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.company").value("Test Co"));

        mockMvc.perform(get("/api/jobs").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].company").value("Test Co"));
    }

    @Test
    void usersCannotSeeEachOthersJobs() throws Exception {
        String tokenA = register();
        String tokenB = register();
        JobRequest job = new JobRequest("A-Only Corp", JobStatus.OFFERED, LocalDate.now(), null);

        mockMvc.perform(post("/api/jobs").header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(job)))
                .andExpect(status().isCreated());

        // B sees an empty board — never A's data.
        mockMvc.perform(get("/api/jobs").header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void statsReturnsAllStatuses() throws Exception {
        String token = register();
        mockMvc.perform(get("/api/jobs/stats").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(JobStatus.values().length));
    }

    @Test
    void rejectsInvalidJob() throws Exception {
        String token = register();
        String badPayload = "{\"status\":\"APPLIED\",\"dateApplied\":\"2026-01-01\"}"; // missing company

        mockMvc.perform(post("/api/jobs").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON).content(badPayload))
                .andExpect(status().isBadRequest());
    }
}
