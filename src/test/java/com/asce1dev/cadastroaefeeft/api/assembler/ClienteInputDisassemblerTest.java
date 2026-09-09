package com.asce1dev.cadastroaefeeft.api.assembler;

import com.asce1dev.cadastroaefeeft.api.model.input.ClienteInput;
import com.asce1dev.cadastroaefeeft.domain.model.Cliente;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ClienteInputDisassemblerTest {

    private final ClienteInputDisassembler disassembler = new ClienteInputDisassembler(new ModelMapper());

    @Test
    void deve_preservar_senha_gov_existente_ao_copiar_input_sem_nova_senha() {
        Cliente cliente = clienteExistente();
        ClienteInput input = new ClienteInput();
        input.setNome("Nome atualizado");

        disassembler.copyToDomainObject(input, cliente);

        assertEquals("Nome atualizado", cliente.getNome());
        assertEquals("ciphertext-existente", cliente.getSenhaGov());
    }

    @Test
    void deve_preservar_senha_gov_existente_ao_copiar_input_com_nova_senha() {
        Cliente cliente = clienteExistente();
        ClienteInput input = new ClienteInput();
        input.setNome("Nome atualizado");
        input.setSenhaGov("nova senha em plaintext");

        disassembler.copyToDomainObject(input, cliente);

        assertEquals("Nome atualizado", cliente.getNome());
        assertEquals("ciphertext-existente", cliente.getSenhaGov());
    }

    private Cliente clienteExistente() {
        Cliente cliente = new Cliente();
        cliente.setNome("Nome atual");
        cliente.setSenhaGov("ciphertext-existente");
        return cliente;
    }
}
