package com.asce1dev.cadastroaefeeft.api.openapi;

import com.asce1dev.cadastroaefeeft.api.model.UsuarioModel;
import com.asce1dev.cadastroaefeeft.api.model.input.SenhaInput;
import com.asce1dev.cadastroaefeeft.api.model.input.UsuarioInput;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.util.List;

@Tag(name = "Usuários", description = "Gerenciamento de usuários do sistema (ADMIN)")
public interface UsuarioControllerOpenApi {

    @Operation(summary = "Lista usuários (ADMIN)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista retornada"),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/Forbidden")
    })
    List<UsuarioModel> listarUsuarios();

    @Operation(summary = "Cria usuário (ADMIN)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuário criado"),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest"),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/Forbidden"),
            @ApiResponse(responseCode = "409", ref = "#/components/responses/Conflict")
    })
    UsuarioModel criar(UsuarioInput usuarioInput);

    @Operation(summary = "Altera senha de um usuário (ADMIN)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Senha alterada"),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest"),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/Forbidden"),
            @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound")
    })
    void alterarSenha(
            @Parameter(description = "ID do usuário", example = "1")
            Long usuarioId,
            SenhaInput senhaInput
    );

    @Operation(summary = "Remove usuário (ADMIN)")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Usuário removido"),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest"),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/Forbidden"),
            @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound")
    })
    void deletarUsuario(
            @Parameter(description = "ID do usuário", example = "1")
            Long usuarioId
    );
}