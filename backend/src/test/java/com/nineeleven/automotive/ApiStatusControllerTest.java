package com.nineeleven.automotive;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nineeleven.automotive.common.web.ApiStatusController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import com.nineeleven.automotive.config.SecurityConfiguration;

@WebMvcTest(ApiStatusController.class)
@Import(SecurityConfiguration.class)
@TestPropertySource(properties = {
        "app.security.jwt-secret=test-only-secret-that-is-at-least-thirty-two-bytes",
        "app.frontend-origin=http://localhost:4200"
})
class ApiStatusControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void returnsFoundationStatus() throws Exception {
        mockMvc.perform(get("/api/v1/system/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.application").value("9-Eleven Automotive Hub API"))
                .andExpect(jsonPath("$.phase").value("phase-1-foundation"))
                .andExpect(jsonPath("$.status").value("UP"));
    }
}
