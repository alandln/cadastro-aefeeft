package com.asce1dev.cadastroaefeeft.api.controller;

import com.asce1dev.cadastroaefeeft.api.assembler.ClienteInputDisassembler;
import com.asce1dev.cadastroaefeeft.api.assembler.ClienteModelAssembler;
import com.asce1dev.cadastroaefeeft.api.assembler.ClienteResumoModelAssembler;
import com.asce1dev.cadastroaefeeft.api.model.SenhaGovModel;
import com.asce1dev.cadastroaefeeft.api.model.input.SenhaInput;
import com.asce1dev.cadastroaefeeft.domain.exception.NaoAutenticadoException;
import com.asce1dev.cadastroaefeeft.domain.service.ClienteService;
import com.asce1dev.cadastroaefeeft.domain.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClienteControllerTest {

    @Mock
    private ClienteService clienteService;

    @Mock
    private ClienteModelAssembler clienteModelAssembler;

    @Mock
    private ClienteInputDisassembler clienteInputDisassembler;

    @Mock
    private ClienteResumoModelAssembler clienteResumoModelAssembler;

    @Mock
    private UsuarioService usuarioService;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private ClienteController controller;

    @Test
    void deve_reautenticar_antes_de_revelar_senha_gov_sem_permitir_cache() {
        SenhaInput input = new SenhaInput();
        input.setPassword("senha atual");
        when(authentication.getName()).thenReturn("admin");
        when(clienteService.revelarSenhaGov(1L)).thenReturn("senha gov");

        ResponseEntity<SenhaGovModel> response = controller.revelarSenhaGov(1L, input, authentication);

        InOrder ordem = inOrder(usuarioService, clienteService);
        ordem.verify(usuarioService).reautenticar("admin", "senha atual");
        ordem.verify(clienteService).revelarSenhaGov(1L);
        assertEquals("senha gov", response.getBody().senhaGov());
        assertEquals("no-store", response.getHeaders().getCacheControl());
    }

    @Test
    void nao_deve_buscar_cliente_quando_reautenticacao_falhar() {
        SenhaInput input = new SenhaInput();
        input.setPassword("senha incorreta");
        when(authentication.getName()).thenReturn("admin");
        NaoAutenticadoException falha = new NaoAutenticadoException("Falha genérica");
        org.mockito.Mockito.doThrow(falha)
                .when(usuarioService).reautenticar("admin", "senha incorreta");

        assertThrows(NaoAutenticadoException.class,
                () -> controller.revelarSenhaGov(1L, input, authentication));

        verify(clienteService, never()).revelarSenhaGov(1L);
    }

    @Test
    void endpoint_de_revelacao_deve_exigir_role_admin() throws NoSuchMethodException {
        Method method = ClienteController.class.getMethod(
                "revelarSenhaGov", Long.class, SenhaInput.class, Authentication.class);

        PreAuthorize preAuthorize = method.getAnnotation(PreAuthorize.class);

        assertEquals("hasRole('ADMIN')", preAuthorize.value());
    }
}
