package com.nineeleven.automotive.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {
    @Bean
    OpenAPI automotiveHubOpenApi() {
        return new OpenAPI().info(new Info()
                .title("9-Eleven Automotive Hub API")
                .version("v1")
                .description("REST API for the 9-Eleven Automotive Hub workshop platform."));
    }
}
