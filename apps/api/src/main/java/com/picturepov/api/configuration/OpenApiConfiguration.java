package com.picturepov.api.configuration;

import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
class OpenApiConfiguration {

    /** Any endpoint can fail unexpectedly: documents the 500 on every operation (ApiProblem is registered by the controllers). */
    @Bean
    OpenApiCustomizer documentInternalErrorResponse() {
        return openApi -> {
            var content = new Content().addMediaType("application/problem+json",
                    new MediaType().schema(new Schema<>().$ref("#/components/schemas/ApiProblem")));
            openApi.getPaths().values().forEach(path -> path.readOperations().forEach(operation ->
                    operation.getResponses().addApiResponse("500",
                            new ApiResponse().description("Unexpected error").content(content))));
        };
    }
}
