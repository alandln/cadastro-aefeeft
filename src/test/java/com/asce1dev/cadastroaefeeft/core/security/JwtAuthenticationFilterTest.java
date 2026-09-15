package com.asce1dev.cadastroaefeeft.core.security;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtService jwtService;

    @Mock
    private UserDetailsService userDetailsService;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @InjectMocks
    private JwtAuthenticationFilter filter;

    @AfterEach
    void limparContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void token_valido_deve_autenticar() throws Exception {
        UserDetails user = new User("usuario", "hash",
                List.of(new SimpleGrantedAuthority("ROLE_USER")));
        prepararToken("token-valido");
        when(jwtService.extractUsername("token-valido")).thenReturn("usuario");
        when(userDetailsService.loadUserByUsername("usuario")).thenReturn(user);
        when(jwtService.isTokenValid("token-valido", user)).thenReturn(true);

        filter.doFilterInternal(request, response, filterChain);

        var authentication = SecurityContextHolder.getContext().getAuthentication();
        assertSame(user, authentication.getPrincipal());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void token_expirado_ou_invalido_nao_deve_autenticar() throws Exception {
        prepararToken("token-expirado");
        when(jwtService.extractUsername("token-expirado"))
                .thenThrow(new JwtException("Token inválido"));

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void token_malformado_nao_deve_autenticar() throws Exception {
        prepararToken("token-malformado");
        when(jwtService.extractUsername("token-malformado"))
                .thenThrow(new IllegalArgumentException("Token malformado"));

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void usuario_inexistente_nao_deve_autenticar() throws Exception {
        prepararToken("token-removido");
        when(jwtService.extractUsername("token-removido")).thenReturn("removido");
        when(userDetailsService.loadUserByUsername("removido"))
                .thenThrow(new UsernameNotFoundException("Usuário não encontrado"));

        filter.doFilterInternal(request, response, filterChain);

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    @Test
    void usuario_inativo_nao_deve_autenticar_nem_propagar_erro() throws Exception {
        prepararToken("token-inativo");
        when(jwtService.extractUsername("token-inativo")).thenReturn("inativo");
        when(userDetailsService.loadUserByUsername("inativo"))
                .thenThrow(new DisabledException("Usuário inativo"));

        assertDoesNotThrow(() -> filter.doFilterInternal(request, response, filterChain));

        assertNull(SecurityContextHolder.getContext().getAuthentication());
        verify(filterChain).doFilter(request, response);
    }

    private void prepararToken(String token) {
        when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
    }
}
