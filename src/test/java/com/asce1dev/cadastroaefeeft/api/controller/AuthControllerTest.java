package com.asce1dev.cadastroaefeeft.api.controller;

import com.asce1dev.cadastroaefeeft.core.security.JwtAuthenticationFilter;
import com.asce1dev.cadastroaefeeft.core.security.JwtService;
import com.asce1dev.cadastroaefeeft.core.security.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, JwtAuthenticationFilter.class})
class AuthControllerTest {

    private static final String LOGIN = "/auth/login";
    private static final String SENHA = "senha atual";

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthenticationManager authenticationManager;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private UserDetailsService userDetailsService;

    @Test
    void login_valido_deve_retornar_token_sem_expor_senha() throws Exception {
        var user = new User("usuario", "hash-bcrypt", List.of());
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                user, null, user.getAuthorities());
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(jwtService.generateToken(user)).thenReturn("jwt-gerado");

        mockMvc.perform(post(LOGIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload("usuario", SENHA)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("jwt-gerado"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(content().string(not(containsString(SENHA))))
                .andExpect(content().string(not(containsString("hash-bcrypt"))));
    }

    @Test
    void credenciais_invalidas_devem_retornar_401_sem_expor_senha() throws Exception {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Detalhe interno"));

        mockMvc.perform(post(LOGIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload("usuario", SENHA)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(not(containsString(SENHA))))
                .andExpect(content().string(not(containsString("Detalhe interno"))));
    }

    @Test
    void usuario_inativo_deve_retornar_401_sem_expor_senha() throws Exception {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new DisabledException("Usuário inativo interno"));

        mockMvc.perform(post(LOGIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload("usuario", SENHA)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(not(containsString(SENHA))))
                .andExpect(content().string(not(containsString("Usuário inativo interno"))));
    }

    @Test
    void payload_invalido_deve_retornar_400_sem_expor_senha() throws Exception {
        mockMvc.perform(post(LOGIN)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload("", "")))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(not(containsString("hash"))));
    }

    private String payload(String username, String password) {
        return """
                {
                  "username": "%s",
                  "password": "%s"
                }
                """.formatted(username, password);
    }
}
