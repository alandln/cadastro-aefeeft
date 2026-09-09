package com.asce1dev.cadastroaefeeft.domain.model;

import jakarta.persistence.*;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
public class Cliente {
	
	@EqualsAndHashCode.Include
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String nome;


	@Column(unique = true, nullable = false)
	private String cpf;

	@Column(length = 512)
	private String senhaGov;

	private String email;

	private String telefone;
	private String tituloEleitor;
	private LocalDate dataNascimento;
	private String matriculaSiape;
	private String contaCorrente;
	private String nomePai;
	private String nomeMae;
	private String classe;
	private String padrao;
	private String identificacaoUnica;
	private String rg;
	private LocalDate dataEmissaoRg;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private Categoria categoria;

	@Enumerated(EnumType.STRING)
	private EstadoCivil estadoCivil;

	@CreationTimestamp
	@Column(columnDefinition = "dateTime")
	private OffsetDateTime dataCadastro;
	
	@UpdateTimestamp
	@Column(columnDefinition = "dateTime")
	private OffsetDateTime dataAtualizacao;
	
	@Embedded
	private Endereco endereco;
	
}
