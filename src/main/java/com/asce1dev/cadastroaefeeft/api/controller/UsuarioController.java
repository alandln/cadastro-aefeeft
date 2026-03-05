package com.asce1dev.cadastroaefeeft.api.controller;

import com.asce1dev.cadastroaefeeft.api.assembler.UsuarioInputDisassembler;
import com.asce1dev.cadastroaefeeft.api.assembler.UsuarioModelAssembler;
import com.asce1dev.cadastroaefeeft.api.model.UsuarioModel;
import com.asce1dev.cadastroaefeeft.api.model.input.SenhaInput;
import com.asce1dev.cadastroaefeeft.api.model.input.UsuarioInput;
import com.asce1dev.cadastroaefeeft.api.openapi.UsuarioControllerOpenApi;
import com.asce1dev.cadastroaefeeft.domain.model.Usuario;
import com.asce1dev.cadastroaefeeft.domain.service.UsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
public class UsuarioController implements UsuarioControllerOpenApi {

    private final UsuarioService usuarioService;
    private final UsuarioModelAssembler usuarioModelAssembler;
    private final UsuarioInputDisassembler usuarioInputDisassembler;

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<UsuarioModel> listarUsuarios() {
        List<Usuario> usuarios = usuarioService.listarUsuarios();
        return usuarioModelAssembler.toCollectionModel(usuarios);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioModel criar(@Valid @RequestBody UsuarioInput usuarioInput) {
        var usuario = usuarioInputDisassembler.toDomainObject(usuarioInput);
        usuario = usuarioService.salvarUsuario(usuario);
        return usuarioModelAssembler.toModel(usuario);
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PatchMapping("/{usuarioId}/senha")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void alterarSenha(@PathVariable Long usuarioId, @Valid @RequestBody SenhaInput senhaInput) {
        usuarioService.alterarSenha(usuarioId, senhaInput.getNewPassword());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{usuarioId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletarUsuario(@PathVariable Long usuarioId) {
        usuarioService.deletarUsuario(usuarioId);
    }
}
