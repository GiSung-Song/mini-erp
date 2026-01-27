package com.erp.mini.common.config;

import com.erp.mini.common.response.ErrorResponse;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        Components components = new Components()
                .addSecuritySchemes("sessionAuth",
                        new SecurityScheme()
                                .type(SecurityScheme.Type.APIKEY)
                                .in(SecurityScheme.In.COOKIE)
                                .name("JSESSIONID"));

        return new OpenAPI()
                .components(components)
                .info(new Info()
                        .title("Mini ERP API")
                        .version("v1.0")
                        .description("Mini ERP API 문서"));
    }

    @Bean
    public OpenApiCustomizer globalErrorResponseCustomizer() {
        return openApi -> {
            Map<String, Schema> errorSchemas =
                    ModelConverters.getInstance().read(ErrorResponse.class);

            errorSchemas.forEach((name, schema) -> {
                openApi.getComponents().addSchemas(name, schema);
            });

            if (openApi.getPaths() == null) return;

            openApi.getPaths().values().forEach(path -> {
                path.readOperations().forEach(operation -> {
                    ApiResponses responses = operation.getResponses();
                    if (responses == null) return;

                    addIfAbsent(responses, "400", "요청 값 검증 실패");
                    addIfAbsent(responses, "500", "서버 내부 오류");

                    if (operation.getSecurity() != null && !operation.getSecurity().isEmpty()) {
                        addIfAbsent(responses, "401", "인증 필요");
                        addIfAbsent(responses, "403", "권한 없음");
                    }
                });
            });
        };
    }

    private void addIfAbsent(ApiResponses responses, String statusCode, String description) {
        if (responses.containsKey(statusCode)) return;

        ApiResponse apiResponse = new ApiResponse()
                .description(description)
                .content(new Content().addMediaType(
                        "application/json",
                        new MediaType().schema(new Schema<>().$ref("#/components/schemas/ErrorResponse"))
                ));

        responses.addApiResponse(statusCode, apiResponse);
    }
}
