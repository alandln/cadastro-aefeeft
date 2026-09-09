package com.asce1dev.cadastroaefeeft.domain.service;

import com.asce1dev.cadastroaefeeft.core.security.SenhaGovCryptoService;
import com.asce1dev.cadastroaefeeft.domain.exception.ClienteNaoEncontradoException;
import com.asce1dev.cadastroaefeeft.domain.exception.CpfDuplicadoException;
import com.asce1dev.cadastroaefeeft.domain.exception.EntidadeEmUsoException;
import com.asce1dev.cadastroaefeeft.domain.exception.NegocioException;
import com.asce1dev.cadastroaefeeft.domain.model.Cliente;
import com.asce1dev.cadastroaefeeft.domain.repository.ClienteRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private SenhaGovCryptoService senhaGovCryptoService;

    @InjectMocks
    private ClienteService clienteService;

    @Test
    void deve_listar_todos_quando_nome_e_cpf_vazios() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Cliente> pageMock = new PageImpl<>(List.of(), pageable, 0);

        when(clienteRepository.findAll(pageable)).thenReturn(pageMock);

        Page<Cliente> result = clienteService.listarClientes(null, null, pageable);

        assertNotNull(result);
        verify(clienteRepository).findAll(pageable);
        verifyNoMoreInteractions(clienteRepository);
    }

    @Test
    void deve_buscar_clientes_por_nome_quando_informado() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Cliente> pageMock = new PageImpl<>(List.of(), pageable, 0);

        when(clienteRepository.findClienteByNomeStartingWithIgnoreCase("Al", pageable)).thenReturn(pageMock);

        Page<Cliente> result = clienteService.listarClientes("  Al  ", null, pageable);

        assertNotNull(result);
        verify(clienteRepository, times(1))
                .findClienteByNomeStartingWithIgnoreCase("Al", pageable);
        verify(clienteRepository, never()).findAll(any(Pageable.class));
        verifyNoMoreInteractions(clienteRepository);
    }

    @Test
    void deve_buscar_clientes_por_cpf_removendo_mascara() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Cliente> pageMock = new PageImpl<>(List.of(), pageable, 0);

        when(clienteRepository.findClienteByCpfStartingWith("234109", pageable)).thenReturn(pageMock);

        Page<Cliente> result = clienteService.listarClientes(null, " 234.109 ", pageable);

        assertNotNull(result);
        verify(clienteRepository, times(1))
                .findClienteByCpfStartingWith("234109", pageable);
        verify(clienteRepository, never()).findAll(any(Pageable.class));
        verifyNoMoreInteractions(clienteRepository);
    }

    @Test
    void deve_acusar_erro_com_nome_e_cpf() {
        Pageable pageable = PageRequest.of(0, 10);

        assertThrows(NegocioException.class, () ->
                clienteService.listarClientes(" Al ", " 234.109", pageable));

        verifyNoInteractions(clienteRepository);
    }

    @Test
    void deve_salvar_cliente_com_sucesso() {
        Cliente cliente = new Cliente();
        cliente.setCpf("52998224725");

        when(clienteRepository.findByCpf("52998224725")).thenReturn(Optional.empty());
        when(clienteRepository.saveAndFlush(cliente)).thenReturn(cliente);

        Cliente result = clienteService.salvarCliente(cliente);

        assertNotNull(result);
        assertEquals("52998224725", result.getCpf());
        verify(clienteRepository).findByCpf("52998224725");
        verify(clienteRepository).saveAndFlush(cliente);
        verifyNoMoreInteractions(clienteRepository);
    }

    @Test
    void deve_criptografar_senha_gov_informada_no_cadastro() {
        Cliente cliente = new Cliente();
        cliente.setCpf("52998224725");
        cliente.setSenhaGov(" senha original ");

        when(senhaGovCryptoService.criptografar(" senha original ")).thenReturn("ciphertext");
        when(clienteRepository.findByCpf("52998224725")).thenReturn(Optional.empty());
        doAnswer(invocation -> {
            Cliente clienteRecebido = invocation.getArgument(0);
            assertEquals("ciphertext", clienteRecebido.getSenhaGov());
            return clienteRecebido;
        }).when(clienteRepository).saveAndFlush(cliente);

        Cliente result = clienteService.salvarCliente(cliente);

        assertEquals("ciphertext", result.getSenhaGov());
        verify(senhaGovCryptoService).criptografar(" senha original ");
        verify(clienteRepository).saveAndFlush(cliente);
    }

    @Test
    void deve_persistir_null_quando_senha_gov_nao_for_informada_no_cadastro() {
        Cliente cliente = new Cliente();
        cliente.setCpf("52998224725");

        when(clienteRepository.findByCpf("52998224725")).thenReturn(Optional.empty());
        when(clienteRepository.saveAndFlush(cliente)).thenReturn(cliente);

        Cliente result = clienteService.salvarCliente(cliente);

        assertEquals(null, result.getSenhaGov());
        verifyNoInteractions(senhaGovCryptoService);
    }

    @Test
    void deve_persistir_null_quando_senha_gov_for_branca_no_cadastro() {
        Cliente cliente = new Cliente();
        cliente.setCpf("52998224725");
        cliente.setSenhaGov("   ");

        when(clienteRepository.findByCpf("52998224725")).thenReturn(Optional.empty());
        when(clienteRepository.saveAndFlush(cliente)).thenReturn(cliente);

        Cliente result = clienteService.salvarCliente(cliente);

        assertEquals(null, result.getSenhaGov());
        verifyNoInteractions(senhaGovCryptoService);
    }

    @Test
    void deve_criptografar_no_cadastro_senha_que_comeca_com_prefixo_do_envelope() {
        Cliente cliente = new Cliente();
        cliente.setCpf("52998224725");
        cliente.setSenhaGov("v1:senha-real");

        when(senhaGovCryptoService.criptografar("v1:senha-real")).thenReturn("ciphertext");
        when(clienteRepository.findByCpf("52998224725")).thenReturn(Optional.empty());
        when(clienteRepository.saveAndFlush(cliente)).thenReturn(cliente);

        Cliente result = clienteService.salvarCliente(cliente);

        assertEquals("ciphertext", result.getSenhaGov());
        verify(senhaGovCryptoService).criptografar("v1:senha-real");
    }

    @Test
    void deve_normalizar_cpf_mascarado_antes_de_salvar() {
        Cliente cliente = new Cliente();
        cliente.setCpf("529.982.247-25");

        when(clienteRepository.findByCpf("52998224725")).thenReturn(Optional.empty());
        when(clienteRepository.saveAndFlush(cliente)).thenReturn(cliente);

        Cliente result = clienteService.salvarCliente(cliente);

        assertEquals("52998224725", result.getCpf());
        verify(clienteRepository).findByCpf("52998224725");
        verify(clienteRepository).saveAndFlush(cliente);
    }

    @Test
    void deve_manter_cpf_sem_mascara_antes_de_salvar() {
        Cliente cliente = new Cliente();
        cliente.setCpf("52998224725");

        when(clienteRepository.findByCpf("52998224725")).thenReturn(Optional.empty());
        when(clienteRepository.saveAndFlush(cliente)).thenReturn(cliente);

        Cliente result = clienteService.salvarCliente(cliente);

        assertEquals("52998224725", result.getCpf());
        verify(clienteRepository).findByCpf("52998224725");
        verify(clienteRepository).saveAndFlush(cliente);
    }

    @Test
    void deve_acusar_erro_de_cpf_duplicado_no_cadastro() {
        Cliente cliente = new Cliente();
        cliente.setCpf("529.982.247-25");

        Cliente clienteExistente = new Cliente();
        clienteExistente.setId(1L);
        clienteExistente.setCpf("52998224725");

        when(clienteRepository.findByCpf("52998224725"))
                .thenReturn(Optional.of(clienteExistente));

        assertThrows(CpfDuplicadoException.class, () ->
                clienteService.salvarCliente(cliente));

        assertEquals("52998224725", cliente.getCpf());
        verify(clienteRepository).findByCpf("52998224725");
        verify(clienteRepository, never()).saveAndFlush(any(Cliente.class));
    }

    @Test
    void deve_preservar_ciphertext_quando_nova_senha_gov_for_null_na_atualizacao() {
        Cliente cliente = clienteExistenteComSenhaGov();

        when(clienteRepository.findByCpf("52998224725")).thenReturn(Optional.of(cliente));
        when(clienteRepository.saveAndFlush(cliente)).thenReturn(cliente);

        Cliente result = clienteService.atualizarCliente(cliente, null);

        assertEquals("ciphertext-existente", result.getSenhaGov());
        verifyNoInteractions(senhaGovCryptoService);
    }

    @Test
    void deve_preservar_ciphertext_quando_nova_senha_gov_for_branca_na_atualizacao() {
        Cliente cliente = clienteExistenteComSenhaGov();

        when(clienteRepository.findByCpf("52998224725")).thenReturn(Optional.of(cliente));
        when(clienteRepository.saveAndFlush(cliente)).thenReturn(cliente);

        Cliente result = clienteService.atualizarCliente(cliente, "   ");

        assertEquals("ciphertext-existente", result.getSenhaGov());
        verifyNoInteractions(senhaGovCryptoService);
    }

    @Test
    void deve_substituir_ciphertext_quando_nova_senha_gov_for_informada() {
        Cliente cliente = clienteExistenteComSenhaGov();

        when(senhaGovCryptoService.criptografar(" nova senha ")).thenReturn("novo-ciphertext");
        when(clienteRepository.findByCpf("52998224725")).thenReturn(Optional.of(cliente));
        doAnswer(invocation -> {
            Cliente clienteRecebido = invocation.getArgument(0);
            assertEquals("novo-ciphertext", clienteRecebido.getSenhaGov());
            return clienteRecebido;
        }).when(clienteRepository).saveAndFlush(cliente);

        Cliente result = clienteService.atualizarCliente(cliente, " nova senha ");

        assertEquals("novo-ciphertext", result.getSenhaGov());
        verify(senhaGovCryptoService).criptografar(" nova senha ");
        verify(clienteRepository).saveAndFlush(cliente);
    }

    @Test
    void deve_criptografar_na_atualizacao_senha_que_comeca_com_prefixo_do_envelope() {
        Cliente cliente = clienteExistenteComSenhaGov();

        when(senhaGovCryptoService.criptografar("v1:senha-real")).thenReturn("novo-ciphertext");
        when(clienteRepository.findByCpf("52998224725")).thenReturn(Optional.of(cliente));
        when(clienteRepository.saveAndFlush(cliente)).thenReturn(cliente);

        Cliente result = clienteService.atualizarCliente(cliente, "v1:senha-real");

        assertEquals("novo-ciphertext", result.getSenhaGov());
        verify(senhaGovCryptoService).criptografar("v1:senha-real");
    }

    @Test
    void deve_permitir_atualizacao_mantendo_o_proprio_cpf() {
        Cliente cliente = new Cliente();
        cliente.setId(1L);
        cliente.setCpf("529.982.247-25");

        Cliente clienteExistente = new Cliente();
        clienteExistente.setId(1L);
        clienteExistente.setCpf("52998224725");

        when(clienteRepository.findByCpf("52998224725"))
                .thenReturn(Optional.of(clienteExistente));
        when(clienteRepository.saveAndFlush(cliente)).thenReturn(cliente);

        Cliente result = clienteService.atualizarCliente(cliente, null);

        assertEquals("52998224725", result.getCpf());
        verify(clienteRepository).findByCpf("52998224725");
        verify(clienteRepository).saveAndFlush(cliente);
    }

    @Test
    void deve_acusar_erro_ao_atualizar_com_cpf_de_outro_cliente() {
        Cliente cliente = new Cliente();
        cliente.setId(1L);
        cliente.setCpf("529.982.247-25");

        Cliente clienteExistente = new Cliente();
        clienteExistente.setId(2L);
        clienteExistente.setCpf("52998224725");

        when(clienteRepository.findByCpf("52998224725"))
                .thenReturn(Optional.of(clienteExistente));

        assertThrows(CpfDuplicadoException.class, () ->
                clienteService.atualizarCliente(cliente, null));

        assertEquals("52998224725", cliente.getCpf());
        verify(clienteRepository).findByCpf("52998224725");
        verify(clienteRepository, never()).saveAndFlush(any(Cliente.class));
    }

    @Test
    void deve_deletar_cliente_com_sucesso() {
        Long id = 1L;
        Cliente cliente = new Cliente();

        when(clienteRepository.findById(id)).thenReturn(Optional.of(cliente));

        clienteService.deletarCliente(id);

        verify(clienteRepository).findById(id);
        verify(clienteRepository).delete(cliente);
        verify(clienteRepository).flush();
        verifyNoMoreInteractions(clienteRepository);
    }

    @Test
    void deve_acusar_erro_de_cliente_nao_encontrado() {
        Long id = 1L;

        when(clienteRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ClienteNaoEncontradoException.class, () ->
                clienteService.deletarCliente(id));

        verify(clienteRepository).findById(id);
        verify(clienteRepository, never()).delete(any(Cliente.class));
        verify(clienteRepository, never()).flush();
    }

    @Test
    void deve_acusar_erro_de_cliente_em_uso() {
        Long id = 1L;
        Cliente cliente = new Cliente();

        when(clienteRepository.findById(id)).thenReturn(Optional.of(cliente));
        doThrow(new DataIntegrityViolationException(""))
                .when(clienteRepository)
                .flush();

        assertThrows(EntidadeEmUsoException.class, () ->
                clienteService.deletarCliente(id));

        verify(clienteRepository).findById(id);
        verify(clienteRepository).delete(cliente);
        verify(clienteRepository).flush();
    }

    @Test
    void deve_buscar_cliente_quando_id_existir() {
        Cliente cliente = new Cliente();
        Long id = 1L;

        when(clienteRepository.findById(id)).thenReturn(Optional.of(cliente));

        Cliente result = clienteService.buscarOuFalhar(id);

        assertNotNull(result);
        verify(clienteRepository).findById(id);
        verifyNoMoreInteractions(clienteRepository);
    }

    @Test
    void deve_acusar_erro_de_cliente_nao_encontrado_ao_tentar_buscar() {
        Long id = 1L;

        when(clienteRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ClienteNaoEncontradoException.class, () ->
                clienteService.buscarOuFalhar(id));

        verify(clienteRepository).findById(id);
    }

    private Cliente clienteExistenteComSenhaGov() {
        Cliente cliente = new Cliente();
        cliente.setId(1L);
        cliente.setCpf("52998224725");
        cliente.setSenhaGov("ciphertext-existente");
        return cliente;
    }
}
