package com.asce1dev.cadastroaefeeft.core.web;

import com.asce1dev.cadastroaefeeft.api.exceptionhandler.Problem;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.*;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

@Configuration
public class OpenApiConfig {

    public static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    public OpenAPI openAPI() {

        var problemSchemaRef = new Schema<>().$ref("#/components/schemas/Problem");

        var problemContent = new Content().addMediaType(
                "application/json",
                new MediaType().schema(problemSchemaRef)
        );

        var responses = new ApiResponses()
                .addApiResponse("400", new ApiResponse().description("Requisição inválida").content(problemContent))
                .addApiResponse("401", new ApiResponse().description("Usuário não autenticado").content(problemContent))
                .addApiResponse("403", new ApiResponse().description("Acesso negado").content(problemContent))
                .addApiResponse("404", new ApiResponse().description("Recurso não encontrado").content(problemContent))
                .addApiResponse("409", new ApiResponse().description("Conflito").content(problemContent));

        return new OpenAPI()
                .info(new Info()
                        .title("Cadastro AEFEEFT API")
                        .description("API para gerenciamento de associados da AEFEEFT")
                        .version("v1.0.0"))
                .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
                .components(new Components()
                        .addSecuritySchemes(
                                SECURITY_SCHEME_NAME,
                                new SecurityScheme()
                                        .name(SECURITY_SCHEME_NAME)
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        )
                        .addResponses("BadRequest", responses.get("400"))
                        .addResponses("Unauthorized", responses.get("401"))
                        .addResponses("Forbidden", responses.get("403"))
                        .addResponses("NotFound", responses.get("404"))
                        .addResponses("Conflict", responses.get("409"))
                );
    }
}