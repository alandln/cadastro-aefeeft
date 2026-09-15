package com.asce1dev.cadastroaefeeft.api.controller;

import com.asce1dev.cadastroaefeeft.api.assembler.UsuarioInputDisassembler;
import com.asce1dev.cadastroaefeeft.api.assembler.UsuarioModelAssembler;
import com.asce1dev.cadastroaefeeft.api.model.UsuarioModel;
import com.asce1dev.cadastroaefeeft.core.security.JwtAuthenticationFilter;
import com.asce1dev.cadastroaefeeft.core.security.JwtService;
import com.asce1dev.cadastroaefeeft.core.security.SecurityConfig;
import com.asce1dev.cadastroaefeeft.domain.model.Usuario;
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
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.List;
import java.util.function.Supplier;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UsuarioController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class UsuarioControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UsuarioService usuarioService;

    @MockBean
    private UsuarioModelAssembler usuarioModelAssembler;

    @MockBean
    private UsuarioInputDisassembler usuarioInputDisassembler;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void admin_deve_acessar_todos_os_endpoints_de_usuario() throws Exception {
        Usuario usuario = new Usuario();
        UsuarioModel model = new UsuarioModel();
        when(usuarioService.listarUsuarios()).thenReturn(List.of());
        when(usuarioInputDisassembler.toDomainObject(any())).thenReturn(usuario);
        when(usuarioService.salvarUsuario(usuario)).thenReturn(usuario);
        when(usuarioModelAssembler.toModel(usuario)).thenReturn(model);

        mockMvc.perform(get("/usuarios")).andExpect(status().isOk());
        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(usuarioPayload()))
                .andExpect(status().isCreated());
        mockMvc.perform(patch("/usuarios/1/senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(senhaPayload()))
                .andExpect(status().isNoContent());
        mockMvc.perform(delete("/usuarios/1")).andExpect(status().isNoContent());

        verify(usuarioService).listarUsuarios();
        verify(usuarioService).salvarUsuario(usuario);
        verify(usuarioService).alterarSenha(1L, "nova senha");
        verify(usuarioService).deletarUsuario(1L);
    }

    @Test
    @WithMockUser(username = "usuario", roles = "USER")
    void user_deve_receber_403_em_todos_os_endpoints_de_usuario() throws Exception {
        for (Supplier<MockHttpServletRequestBuilder> request : requests()) {
            mockMvc.perform(request.get()).andExpect(status().isForbidden());
        }

        verificarServicesNaoChamados();
    }

    @Test
    void anonimo_deve_receber_401_em_todos_os_endpoints_de_usuario() throws Exception {
        for (Supplier<MockHttpServletRequestBuilder> request : requests()) {
            mockMvc.perform(request.get()).andExpect(status().isUnauthorized());
        }

        verificarServicesNaoChamados();
    }

    private List<Supplier<MockHttpServletRequestBuilder>> requests() {
        return List.of(
                () -> get("/usuarios"),
                () -> post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(usuarioPayload()),
                () -> patch("/usuarios/1/senha")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(senhaPayload()),
                () -> delete("/usuarios/1")
        );
    }

    private void verificarServicesNaoChamados() {
        verify(usuarioService, never()).listarUsuarios();
        verify(usuarioService, never()).salvarUsuario(any());
        verify(usuarioService, never()).alterarSenha(any(), any());
        verify(usuarioService, never()).deletarUsuario(any());
    }

    private String usuarioPayload() {
        return """
                {
                  "username": "novo.usuario",
                  "password": "senha inicial"
                }
                """;
    }

    private String senhaPayload() {
        return """
                {
                  "password": "nova senha"
                }
                """;
    }
}
