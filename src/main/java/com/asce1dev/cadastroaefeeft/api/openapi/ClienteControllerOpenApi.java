package com.asce1dev.cadastroaefeeft.api.openapi;

import com.asce1dev.cadastroaefeeft.api.model.ClienteModel;
import com.asce1dev.cadastroaefeeft.api.model.ClienteResumoModel;
import com.asce1dev.cadastroaefeeft.api.model.input.ClienteInput;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Tag(name = "Clientes", description = "Gerenciamento de clientes")
public interface ClienteControllerOpenApi {

    @Operation(summary = "Lista clientes com filtro por nome ou CPF")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Página de clientes retornada com sucesso"),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest"),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/Forbidden")
    })
    Page<ClienteResumoModel> listarClientes(

            @Parameter(description = "Filtro por nome do cliente")
            String nome,

            @Parameter(description = "Filtro por CPF do cliente")
            String cpf,

            @Parameter(description = "Parâmetros de paginação")
            Pageable pageable
    );

    @Operation(summary = "Busca um cliente por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente retornado com sucesso"),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/Forbidden"),
            @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound")
    })
    ClienteModel obterClientePorId(
            @Parameter(description = "ID do cliente", example = "1")
            Long clientId
    );

    @Operation(summary = "Adiciona um novo cliente")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cliente criado"),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest"),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/Forbidden"),
            @ApiResponse(responseCode = "409", ref = "#/components/responses/Conflict")
    })
    ClienteModel salvarCliente(ClienteInput clienteInput);

    @Operation(summary = "Atualiza um cliente por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Cliente atualizado"),
            @ApiResponse(responseCode = "400", ref = "#/components/responses/BadRequest"),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/Forbidden"),
            @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound"),
            @ApiResponse(responseCode = "409", ref = "#/components/responses/Conflict")
    })
    ClienteModel atualizar(
            @Parameter(description = "ID do cliente", example = "1")
            Long clienteId,
            ClienteInput clienteInput
    );

    @Operation(summary = "Remove um cliente por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Cliente removido"),
            @ApiResponse(responseCode = "401", ref = "#/components/responses/Unauthorized"),
            @ApiResponse(responseCode = "403", ref = "#/components/responses/Forbidden"),
            @ApiResponse(responseCode = "404", ref = "#/components/responses/NotFound"),
            @ApiResponse(responseCode = "409", ref = "#/components/responses/Conflict")
    })
    void deletarCliente(
            @Parameter(description = "ID do cliente", example = "1")
            Long clienteId
    );

}
