package com.mycompany._thstudy.auth;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthFlowIntegrationTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void loginThenAccessProtectedApi_success() throws Exception {
    String email = "itest_" + System.currentTimeMillis() + "@test.com";
    String password = "password1234";

    String signupBody = objectMapper.writeValueAsString(new SignupPayload(email, password, "itest-user"));
    String loginBody = objectMapper.writeValueAsString(new LoginPayload(email, password));

    mockMvc.perform(post("/api/auth/signup")
            .contentType(MediaType.APPLICATION_JSON)
            .content(signupBody))
        .andExpect(status().isCreated());

    MvcResult loginResult = mockMvc.perform(post("/api/auth/login")
            .contentType(MediaType.APPLICATION_JSON)
            .content(loginBody))
        .andExpect(status().isOk())
        .andReturn();

    JsonNode loginJson = objectMapper.readTree(loginResult.getResponse().getContentAsString());
    String accessToken = loginJson.path("data").path("accessToken").asText();

    mockMvc.perform(get("/api/accounts")
            .header("Authorization", "Bearer " + accessToken))
        .andExpect(status().isOk());
  }

  private record SignupPayload(String email, String password, String nickname) {
  }

  private record LoginPayload(String email, String password) {
  }
}

