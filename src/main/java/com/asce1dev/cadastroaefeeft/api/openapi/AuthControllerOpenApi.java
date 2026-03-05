package com.asce1dev.cadastroaefeeft.api.openapi;

import com.asce1dev.cadastroaefeeft.api.controller.AuthController.LoginRequest;
import com.asce1dev.cadastroaefeeft.api.controller.AuthController.TokenResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "Autenticação", description = "Endpoints de autenticação e token JWT")
public interface AuthControllerOpenApi {

    @Operation(summary = "Realiza login e gera um token JWT")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login realizado com sucesso"),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized"),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest")
    })
    TokenResponse login(LoginRequest request);
}
