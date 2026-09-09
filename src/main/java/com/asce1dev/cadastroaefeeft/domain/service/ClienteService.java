package com.asce1dev.cadastroaefeeft.domain.service;

import com.asce1dev.cadastroaefeeft.core.security.SenhaGovCryptoService;
import com.asce1dev.cadastroaefeeft.domain.exception.ClienteNaoEncontradoException;
import com.asce1dev.cadastroaefeeft.domain.exception.CpfDuplicadoException;
import com.asce1dev.cadastroaefeeft.domain.exception.EntidadeEmUsoException;
import com.asce1dev.cadastroaefeeft.domain.exception.NegocioException;
import com.asce1dev.cadastroaefeeft.domain.model.Cliente;
import com.asce1dev.cadastroaefeeft.domain.repository.ClienteRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Objects;

@RequiredArgsConstructor
@Service
public class ClienteService {

	private static final String MSG_ENTIDADE_EM_USO = "Cliente de código %d não pode ser removido, pois está em uso";

	private final ClienteRepository clienteRepository;
	private final SenhaGovCryptoService senhaGovCryptoService;
	
	public Page<Cliente> listarClientes(String nome, String cpf, Pageable pageable){

		boolean temNome = nome != null && !nome.trim().isEmpty();
		boolean temCpf = cpf != null && !cpf.trim().isEmpty();

		if(temNome && temCpf) {
			throw new NegocioException("Informe apenas 'nome' ou 'cpf', não ambos.");
		}

		if (!temNome && !temCpf) {
			return clienteRepository.findAll(pageable);
		}

		if(temNome) {
			return clienteRepository.findClienteByNomeStartingWithIgnoreCase(nome.trim(), pageable);
		}

		String cpfLimpo = cpf.trim().replaceAll("\\D", "");
		if (cpfLimpo.isEmpty()) {
			throw new NegocioException("CPF inválido.");
		}
		return clienteRepository.findClienteByCpfStartingWith(cpfLimpo, pageable);

	}

	@Transactional
	public Cliente salvarCliente(Cliente cliente) {
		if (cliente.getSenhaGov() == null || cliente.getSenhaGov().isBlank()) {
			cliente.setSenhaGov(null);
		} else {
			cliente.setSenhaGov(senhaGovCryptoService.criptografar(cliente.getSenhaGov()));
		}

		return persistirCliente(cliente);
	}

	@Transactional
	public Cliente atualizarCliente(Cliente cliente, String novaSenhaGov) {
		if (novaSenhaGov != null && !novaSenhaGov.isBlank()) {
			cliente.setSenhaGov(senhaGovCryptoService.criptografar(novaSenhaGov));
		}

		return persistirCliente(cliente);
	}

	private Cliente persistirCliente(Cliente cliente) {
		String cpfLimpo = cliente.getCpf().replaceAll("\\D", "");
		cliente.setCpf(cpfLimpo);

		clienteRepository.findByCpf(cpfLimpo)
				.filter(clienteExistente -> cliente.getId() == null
						|| !Objects.equals(clienteExistente.getId(), cliente.getId()))
				.ifPresent(clienteExistente -> {
					throw new CpfDuplicadoException();
				});

		return clienteRepository.saveAndFlush(cliente);
	}

	@Transactional
	public void deletarCliente(Long id) {
		Cliente cliente = buscarOuFalhar(id);

		try {
			clienteRepository.delete(cliente);
			clienteRepository.flush();
		} catch (DataIntegrityViolationException e) {
			throw new EntidadeEmUsoException(
					String.format(MSG_ENTIDADE_EM_USO,id));
		}
	}

	public Cliente buscarOuFalhar(Long clienteId) {
		return clienteRepository.findById(clienteId)
				.orElseThrow(() -> new ClienteNaoEncontradoException(clienteId));
	}

}
