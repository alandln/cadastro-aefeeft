package com.asce1dev.cadastroaefeeft.domain.service;

import com.asce1dev.cadastroaefeeft.domain.exception.EntidadeEmUsoException;
import com.asce1dev.cadastroaefeeft.domain.exception.EntidadeNaoEncontradaException;
import com.asce1dev.cadastroaefeeft.domain.exception.NaoAutenticadoException;
import com.asce1dev.cadastroaefeeft.domain.exception.NegocioException;
import com.asce1dev.cadastroaefeeft.domain.exception.UsuarioNaoEncontradoException;
import com.asce1dev.cadastroaefeeft.domain.model.Role;
import com.asce1dev.cadastroaefeeft.domain.model.Usuario;
import com.asce1dev.cadastroaefeeft.domain.repository.UsuarioRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private static final String MSG_ENTIDADE_EM_USO = "Usuário de código %d não pode ser removido," +
            "pois está em uso";
    private static final String MSG_REAUTENTICACAO_INVALIDA =
            "Não foi possível confirmar as credenciais do usuário";

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public void reautenticar(String username, String password) {
        var usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new NaoAutenticadoException(MSG_REAUTENTICACAO_INVALIDA));

        if (!usuario.isActive() || !passwordEncoder.matches(password, usuario.getPassword())) {
            throw new NaoAutenticadoException(MSG_REAUTENTICACAO_INVALIDA);
        }
    }

    @Transactional
    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }

    @Transactional
    public Usuario salvarUsuario(Usuario usuario) {
        try {
            usuario.setRole(Role.USER);
            usuario.setActive(true);
            usuario.setPassword(passwordEncoder.encode(usuario.getPassword()));

            return usuarioRepository.saveAndFlush(usuario);
        } catch (DataIntegrityViolationException e) {
            throw new NegocioException("Username já está em uso");
        }
    }

    @Transactional
    public void alterarSenha(Long usuarioId, String novaSenha) {
        var usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new UsuarioNaoEncontradoException(usuarioId));

        usuario.setPassword(passwordEncoder.encode(novaSenha));
        usuarioRepository.saveAndFlush(usuario);
    }

    @Transactional
    public void deletarUsuario(Long id) {
        var usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new UsuarioNaoEncontradoException(id));

        if (usuario.getRole() == Role.ADMIN) {
            throw new NegocioException("Não é permitido remover administradores do sistema");
        }

        try {
            usuarioRepository.deleteById(id);
            usuarioRepository.flush();
        } catch (DataIntegrityViolationException e) {
            throw new EntidadeEmUsoException(
                    String.format(MSG_ENTIDADE_EM_USO, id));
        }
    }

}
