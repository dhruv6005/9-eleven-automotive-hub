package com.nineeleven.automotive;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.nineeleven.automotive.common.web.ApiStatusController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ApiStatusController.class)
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
