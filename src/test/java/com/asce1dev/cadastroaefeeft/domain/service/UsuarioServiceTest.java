package com.asce1dev.cadastroaefeeft.domain.service;

import com.asce1dev.cadastroaefeeft.domain.exception.NaoAutenticadoException;
import com.asce1dev.cadastroaefeeft.domain.model.Usuario;
import com.asce1dev.cadastroaefeeft.domain.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
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

    private Usuario usuarioAtivo() {
        Usuario usuario = new Usuario();
        usuario.setUsername("admin");
        usuario.setPassword("hash-atual");
        usuario.setActive(true);
        return usuario;
    }
}
