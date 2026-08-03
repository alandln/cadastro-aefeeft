package com.asce1dev.cadastroaefeeft.api.model.input;

import com.asce1dev.cadastroaefeeft.domain.model.Categoria;
import com.asce1dev.cadastroaefeeft.domain.model.EstadoCivil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.br.CPF;

import java.time.LocalDate;

@Getter
@Setter
public class ClienteInput {

	@NotBlank
	private String nome;
	
	@NotBlank
	@CPF
	private String cpf;

	@Email
	private String email;

	private String senhaGov;
	
	private String matriculaSiape;
	private String telefone;
	private String contaCorrente;
	private LocalDate dataNascimento;
	private String nomePai;
	private String nomeMae;
	private String tituloEleitor;
	private String classe;
	private String padrao;
	private String identificacaoUnica;
	private String rg;
	private LocalDate dataEmissaoRg;

	private EstadoCivil estadoCivil;

	@NotNull
	private Categoria categoria;

	@Valid
	@NotNull
	private EnderecoInput endereco;
	
}
