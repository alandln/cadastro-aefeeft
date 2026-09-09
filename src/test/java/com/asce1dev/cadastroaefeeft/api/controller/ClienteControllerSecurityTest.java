package com.asce1dev.cadastroaefeeft.api.controller;

import com.asce1dev.cadastroaefeeft.api.assembler.ClienteInputDisassembler;
import com.asce1dev.cadastroaefeeft.api.assembler.ClienteModelAssembler;
import com.asce1dev.cadastroaefeeft.api.assembler.ClienteResumoModelAssembler;
import com.asce1dev.cadastroaefeeft.core.security.JwtAuthenticationFilter;
import com.asce1dev.cadastroaefeeft.core.security.JwtService;
import com.asce1dev.cadastroaefeeft.core.security.SecurityConfig;
import com.asce1dev.cadastroaefeeft.domain.service.ClienteService;
import com.asce1dev.cadastroaefeeft.domain.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ClienteController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class ClienteControllerSecurityTest {

    private static final String ENDPOINT = "/clientes/1/senha-gov/revelar";
    private static final String PAYLOAD = """
            {
              "password": "senha atual"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ClienteService clienteService;

    @MockBean
    private UsuarioService usuarioService;

    @MockBean
    private ClienteModelAssembler clienteModelAssembler;

    @MockBean
    private ClienteInputDisassembler clienteInputDisassembler;

    @MockBean
    private ClienteResumoModelAssembler clienteResumoModelAssembler;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void deve_permitir_revelacao_para_admin() throws Exception {
        when(clienteService.revelarSenhaGov(1L)).thenReturn("senha gov");

        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PAYLOAD))
                .andExpect(status().isOk());

        verify(usuarioService).reautenticar("admin", "senha atual");
    }

    @Test
    @WithMockUser(username = "usuario", roles = "USER")
    void deve_negar_revelacao_para_user() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PAYLOAD))
                .andExpect(status().isForbidden());

        verify(usuarioService, never()).reautenticar("usuario", "senha atual");
        verify(clienteService, never()).revelarSenhaGov(1L);
    }

    @Test
    void deve_exigir_autenticacao_para_revelacao() throws Exception {
        mockMvc.perform(post(ENDPOINT)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PAYLOAD))
                .andExpect(status().isUnauthorized());

        verify(usuarioService, never()).reautenticar("admin", "senha atual");
        verify(clienteService, never()).revelarSenhaGov(1L);
    }
}
