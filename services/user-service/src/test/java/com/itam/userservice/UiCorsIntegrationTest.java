package com.itam.userservice;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = com.itam.userservice.boot.UserServiceApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UiCorsIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void publicApiAllowsBrowserCallsFromAssetServiceOrigin() throws Exception {
        mockMvc.perform(options("/api/v1/users")
                .header("Origin", "http://localhost:8081")
                .header("Access-Control-Request-Method", "GET")
                .header(
                    "Access-Control-Request-Headers",
                    "X-Authenticated-Tenant-Id,X-Authenticated-User-Id,X-Authenticated-Actor,X-Correlation-Id,Content-Type"
                ))
            .andExpect(status().isOk())
            .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:8081"))
            .andExpect(header().string("Access-Control-Allow-Methods", containsString("GET")))
            .andExpect(header().string("Access-Control-Allow-Headers", containsString("X-Authenticated-Tenant-Id")))
            .andExpect(header().string("Access-Control-Allow-Headers", containsString("X-Authenticated-User-Id")));
    }

    @Test
    void internalRoleLookupAllowsBrowserCallsFromAssetServiceOrigin() throws Exception {
        mockMvc.perform(options("/internal/api/v1/users/00000000-0000-0000-0000-000000000001/roles")
                .header("Origin", "http://localhost:8081")
                .header("Access-Control-Request-Method", "GET")
                .header(
                    "Access-Control-Request-Headers",
                    "X-Authenticated-Tenant-Id,X-Authenticated-User-Id,X-Authenticated-Actor,X-Correlation-Id"
                ))
            .andExpect(status().isOk())
            .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:8081"))
            .andExpect(header().string("Access-Control-Allow-Methods", containsString("GET")))
            .andExpect(header().string("Access-Control-Allow-Headers", containsString("X-Authenticated-Tenant-Id")));
    }
}
