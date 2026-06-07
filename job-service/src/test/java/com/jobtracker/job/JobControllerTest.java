package com.jobtracker.job;

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

    @Test
    void contextLoads() {
        // Verifies the whole application context (controllers, JPA, beans) wires up.
    }

    @Test
    void createsAndReturnsJob() throws Exception {
        JobRequest request = new JobRequest(
                "Test Co", JobStatus.APPLIED, LocalDate.now(), "hr@test.co");

        mockMvc.perform(post("/api/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.company").value("Test Co"))
                .andExpect(jsonPath("$.status").value("APPLIED"));
    }

    @Test
    void rejectsInvalidJob() throws Exception {
        // Missing company -> @NotBlank should trigger a 400.
        String badPayload = """
                {"status":"APPLIED","dateApplied":"2026-01-01"}
                """;

        mockMvc.perform(post("/api/jobs")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badPayload))
                .andExpect(status().isBadRequest());
    }

    @Test
    void statsReturnsAllStatuses() throws Exception {
        // Even with no matching data, every status appears (0-filled).
        mockMvc.perform(get("/api/jobs/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(JobStatus.values().length));
    }
}
