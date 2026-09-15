package com.asce1dev.cadastroaefeeft.domain.service;

import com.asce1dev.cadastroaefeeft.domain.exception.NaoAutenticadoException;
import com.asce1dev.cadastroaefeeft.domain.exception.NegocioException;
import com.asce1dev.cadastroaefeeft.domain.model.Role;
import com.asce1dev.cadastroaefeeft.domain.model.Usuario;
import com.asce1dev.cadastroaefeeft.domain.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    void deve_reautenticar_usuario_ativo_com_senha_correta() {
        Usuario usuario = usuarioAtivo();
        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("senha atual", "hash-atual")).thenReturn(true);

        usuarioService.reautenticar("admin", "senha atual");

        verify(passwordEncoder).matches("senha atual", "hash-atual");
    }

    @Test
    void deve_rejeitar_senha_incorreta() {
        Usuario usuario = usuarioAtivo();
        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("senha incorreta", "hash-atual")).thenReturn(false);

        assertThrows(NaoAutenticadoException.class,
                () -> usuarioService.reautenticar("admin", "senha incorreta"));
    }

    @Test
    void deve_rejeitar_usuario_inexistente() {
        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.empty());

        assertThrows(NaoAutenticadoException.class,
                () -> usuarioService.reautenticar("admin", "senha atual"));

        verify(passwordEncoder, never()).matches("senha atual", "hash-atual");
    }

    @Test
    void deve_rejeitar_usuario_inativo_sem_comparar_senha() {
        Usuario usuario = usuarioAtivo();
        usuario.setActive(false);
        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(usuario));

        assertThrows(NaoAutenticadoException.class,
                () -> usuarioService.reautenticar("admin", "senha atual"));

        verify(passwordEncoder, never()).matches("senha atual", "hash-atual");
    }

    @Test
    void deve_codificar_senha_e_forcar_role_user_e_usuario_ativo_ao_salvar() {
        Usuario usuario = new Usuario();
        usuario.setUsername("usuario");
        usuario.setPassword("senha aberta");
        usuario.setRole(Role.ADMIN);
        usuario.setActive(false);
        when(passwordEncoder.encode("senha aberta")).thenReturn("hash-gerado");

        usuarioService.salvarUsuario(usuario);

        verify(passwordEncoder).encode("senha aberta");
        verify(usuarioRepository).saveAndFlush(argThat(usuarioSalvo ->
                "hash-gerado".equals(usuarioSalvo.getPassword())
                        && usuarioSalvo.getRole() == Role.USER
                        && usuarioSalvo.isActive()));
    }

    @Test
    void deve_codificar_nova_senha_ao_alterar() {
        Usuario usuario = usuarioAtivo();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.encode("nova senha")).thenReturn("novo-hash");

        usuarioService.alterarSenha(1L, "nova senha");

        verify(passwordEncoder).encode("nova senha");
        verify(usuarioRepository).saveAndFlush(argThat(usuarioSalvo ->
                "novo-hash".equals(usuarioSalvo.getPassword())));
    }

    @Test
    void nao_deve_permitir_excluir_administrador() {
        Usuario administrador = usuarioAtivo();
        administrador.setRole(Role.ADMIN);
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(administrador));

        NegocioException exception = assertThrows(NegocioException.class,
                () -> usuarioService.deletarUsuario(1L));

        assertEquals("Não é permitido remover administradores do sistema", exception.getMessage());
        verify(usuarioRepository, never()).deleteById(1L);
        verify(usuarioRepository, never()).flush();
    }

    private Usuario usuarioAtivo() {
        Usuario usuario = new Usuario();
        usuario.setUsername("admin");
        usuario.setPassword("hash-atual");
        usuario.setActive(true);
        return usuario;
    }
}
