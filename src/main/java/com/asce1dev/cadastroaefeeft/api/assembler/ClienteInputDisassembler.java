package com.asce1dev.cadastroaefeeft.api.assembler;

import com.asce1dev.cadastroaefeeft.api.model.input.ClienteInput;
import com.asce1dev.cadastroaefeeft.domain.model.Cliente;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ClienteInputDisassembler {

	private final ModelMapper modelMapper;
	
	public Cliente toDomainObject(ClienteInput clienteInput) {
		return modelMapper.map(clienteInput, Cliente.class);
	}

	public void copyToDomainObject(ClienteInput clienteInput, Cliente cliente) {
		String senhaGovAtual = cliente.getSenhaGov();
		modelMapper.map(clienteInput, cliente);
		cliente.setSenhaGov(senhaGovAtual);
	}
}
