package com.nineeleven.automotive.common.web;

import java.time.Instant;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/system")
public class ApiStatusController {
    @GetMapping("/status")
    public Map<String, Object> status() {
        return Map.of(
                "application", "9-Eleven Automotive Hub API",
                "phase", "phase-1-foundation",
                "status", "UP",
                "timestamp", Instant.now().toString());
    }
}
