package com.example.archive.controller;

import com.example.archive.dto.ProjectRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@SuppressWarnings("null")
class ProjectControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @DisplayName("GET /api/projects - 시드 데이터 정상 조회 확인")
    void testGetAllProjects() throws Exception {
        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(8))))
                .andExpect(jsonPath("$[0].title").exists());
    }

    @Test
    @DisplayName("GET /api/projects/{id} - 존재하지 않는 프로젝트 조회 시 404 반환 확인")
    void testGetNonExistentProject() throws Exception {
        mockMvc.perform(get("/api/projects/999999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message", containsString("999999")));
    }

    @Test
    @DisplayName("POST /api/projects - 유효성 검사 실패(필수값 누락) 시 400 반환 확인")
    void testCreateProjectValidationFailure() throws Exception {
        ProjectRequest invalidRequest = new ProjectRequest(
                "", // empty title
                "", // empty category
                "description",
                "problem",
                "solution",
                "tech",
                "https://example.com/img.jpg",
                "https://github.com",
                "https://example.com"
        );

        mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.fields.title").exists())
                .andExpect(jsonPath("$.fields.category").exists());
    }

    @Test
    @DisplayName("POST /api/projects - 정상 생성 및 단건 조회 확인")
    void testCreateAndGetProject() throws Exception {
        ProjectRequest validRequest = new ProjectRequest(
                "테스트 프로젝트",
                "BACKEND",
                "설명입니다.",
                "문제 상황",
                "해결책",
                "Java · Spring Boot",
                "https://example.com/test.jpg",
                "https://github.com/test",
                "https://example.com/demo"
        );

        String response = mockMvc.perform(post("/api/projects")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("테스트 프로젝트"))
                .andExpect(jsonPath("$.category").value("BACKEND"))
                .andReturn().getResponse().getContentAsString();

        long createdId = objectMapper.readTree(response).path("id").asLong();

        mockMvc.perform(get("/api/projects/" + createdId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdId))
                .andExpect(jsonPath("$.title").value("테스트 프로젝트"));
    }
}
