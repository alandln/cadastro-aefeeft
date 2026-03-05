package com.asce1dev.cadastroaefeeft.api.assembler;

import com.asce1dev.cadastroaefeeft.api.model.ClienteModel;
import com.asce1dev.cadastroaefeeft.domain.model.Cliente;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ClienteModelAssembler {

	private final ModelMapper modelMapper;
	
	public ClienteModel toModel(Cliente cliente) {
		return modelMapper.map(cliente, ClienteModel.class);
	}
}
