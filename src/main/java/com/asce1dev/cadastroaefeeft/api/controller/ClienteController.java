package com.asce1dev.cadastroaefeeft.api.controller;

import com.asce1dev.cadastroaefeeft.api.assembler.ClienteInputDisassembler;
import com.asce1dev.cadastroaefeeft.api.assembler.ClienteModelAssembler;
import com.asce1dev.cadastroaefeeft.api.assembler.ClienteResumoModelAssembler;
import com.asce1dev.cadastroaefeeft.api.model.ClienteModel;
import com.asce1dev.cadastroaefeeft.api.model.ClienteResumoModel;
import com.asce1dev.cadastroaefeeft.api.model.input.ClienteInput;
import com.asce1dev.cadastroaefeeft.api.openapi.ClienteControllerOpenApi;
import com.asce1dev.cadastroaefeeft.domain.model.Cliente;
import com.asce1dev.cadastroaefeeft.domain.service.ClienteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/clientes")
public class ClienteController implements ClienteControllerOpenApi {

	private final ClienteService clienteService;
	private final ClienteModelAssembler clienteModelAssembler;
	private final ClienteInputDisassembler clienteInputDisassembler;
	private final ClienteResumoModelAssembler clienteResumoModelAssembler;

	@GetMapping
	public Page<ClienteResumoModel> listarClientes(
			@RequestParam(required = false) String nome,
			@RequestParam(required = false) String cpf,
			@PageableDefault(sort = "nome", direction = Sort.Direction.ASC) Pageable pageable) {

		Page<Cliente> page = clienteService.listarClientes(nome, cpf, pageable);
		return clienteResumoModelAssembler.toPageModel(page);
	}

	@GetMapping("/{clienteId}")
	public ClienteModel obterClientePorId(@PathVariable Long clienteId) {
		Cliente cliente = clienteService.buscarOuFalhar(clienteId);
		
		return clienteModelAssembler.toModel(cliente);
	}

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ClienteModel salvarCliente(@RequestBody @Valid ClienteInput clienteInput){
			Cliente cliente = clienteInputDisassembler.toDomainObject(clienteInput);

			return clienteModelAssembler.toModel(clienteService.salvarCliente(cliente));
	}
	
	@PutMapping("/{clienteId}")
	public ClienteModel atualizar(@PathVariable Long clienteId,
			@RequestBody @Valid ClienteInput clienteInput) {
			Cliente clienteAtual = clienteService.buscarOuFalhar(clienteId);
			
			clienteInputDisassembler.copyToDomainObject(clienteInput, clienteAtual);	
			
			return clienteModelAssembler.toModel(clienteService.salvarCliente(clienteAtual));
	}

	@DeleteMapping("/{clienteId}")
	@ResponseStatus(HttpStatus.NO_CONTENT)
	public void deletarCliente(@PathVariable Long clienteId) {
		clienteService.deletarCliente(clienteId);
	}

}
