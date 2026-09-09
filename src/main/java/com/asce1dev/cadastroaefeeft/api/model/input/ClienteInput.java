package com.asce1dev.cadastroaefeeft.api.model.input;

import com.asce1dev.cadastroaefeeft.domain.model.Categoria;
import com.asce1dev.cadastroaefeeft.domain.model.EstadoCivil;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.validator.constraints.br.CPF;

import java.time.LocalDate;

@Getter
@Setter
public class ClienteInput {

	@NotBlank
	@Size(max = 80)
	private String nome;
	
	@NotBlank
	@CPF
	private String cpf;

	@Email
	@Size(max = 60)
	private String email;

	@Size(max = 60)
	private String senhaGov;

	@Size(max = 25)
	private String matriculaSiape;
	@Size(max = 30)
	private String telefone;
	@Size(max = 25)
	private String contaCorrente;
	private LocalDate dataNascimento;
	@Size(max = 60)
	private String nomePai;
	@Size(max = 60)
	private String nomeMae;
	@Size(max = 25)
	private String tituloEleitor;
	@Size(max = 25)
	private String classe;
	@Size(max = 60)
	private String padrao;
	@Size(max = 25)
	private String identificacaoUnica;
	@Size(max = 7)
	private String rg;
	private LocalDate dataEmissaoRg;

	private EstadoCivil estadoCivil;

	@NotNull
	private Categoria categoria;

	@Valid
	@NotNull
	private EnderecoInput endereco;
	
}
