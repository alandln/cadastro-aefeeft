package com.asce1dev.cadastroaefeeft.api.model.input;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EnderecoInput {

	@Size(max = 25)
	private String rua;
	@Size(max = 25)
	private String numero;
	@Size(max = 25)
	private String bairro;
	@Size(max = 10)
	private String cep;
	@Size(max = 25)
	private String cidade;
	@Size(max = 25)
	private String estado;
	
}
