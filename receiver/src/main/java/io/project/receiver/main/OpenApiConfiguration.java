package io.project.receiver.main;

import org.springdoc.core.models.GroupedOpenApi;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    @Bean
    public GroupedOpenApi publicApi() {
        return GroupedOpenApi.builder()
                .group("receiver")
                .packagesToScan("io.project")
                .pathsToMatch("/api/**")
                .build();
    }

}
