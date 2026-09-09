package com.asce1dev.cadastroaefeeft.api.model.input;

import com.asce1dev.cadastroaefeeft.domain.model.Categoria;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class InputValidationTest {

    private static jakarta.validation.ValidatorFactory validatorFactory;
    private static Validator validator;

    @BeforeAll
    static void configurarValidator() {
        validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @AfterAll
    static void fecharValidatorFactory() {
        validatorFactory.close();
    }

    @Test
    void deve_rejeitar_campo_de_cliente_acima_do_limite() {
        ClienteInput input = clienteValido();
        input.setNome("a".repeat(81));

        Set<ConstraintViolation<ClienteInput>> violacoes = validator.validate(input);

        assertTrue(possuiViolacao(violacoes, "nome"));
    }

    @Test
    void deve_validar_limite_do_endereco_por_validacao_aninhada() {
        ClienteInput input = clienteValido();
        input.getEndereco().setRua("a".repeat(26));

        Set<ConstraintViolation<ClienteInput>> violacoes = validator.validate(input);

        assertTrue(possuiViolacao(violacoes, "endereco.rua"));
    }

    @Test
    void deve_aceitar_username_com_50_caracteres_e_rejeitar_com_51() {
        UsuarioInput input = new UsuarioInput();
        input.setPassword("senha");
        input.setUsername("a".repeat(50));

        assertTrue(validator.validate(input).isEmpty());

        input.setUsername("a".repeat(51));

        assertTrue(possuiViolacao(validator.validate(input), "username"));
    }

    @Test
    void deve_aceitar_cpf_valido_com_e_sem_mascara() {
        ClienteInput comMascara = clienteValido();
        comMascara.setCpf("529.982.247-25");

        ClienteInput semMascara = clienteValido();
        semMascara.setCpf("52998224725");

        assertFalse(possuiViolacao(validator.validate(comMascara), "cpf"));
        assertFalse(possuiViolacao(validator.validate(semMascara), "cpf"));
    }

    @Test
    void deve_aceitar_campos_opcionais_nulos() {
        ClienteInput input = clienteValido();

        assertTrue(validator.validate(input).isEmpty());
    }

    private ClienteInput clienteValido() {
        ClienteInput input = new ClienteInput();
        input.setNome("Cliente válido");
        input.setCpf("52998224725");
        input.setCategoria(Categoria.APOSENTADO);
        input.setEndereco(new EnderecoInput());
        return input;
    }

    private boolean possuiViolacao(Set<? extends ConstraintViolation<?>> violacoes, String campo) {
        return violacoes.stream()
                .anyMatch(violacao -> violacao.getPropertyPath().toString().equals(campo));
    }
}
