package com.asce1dev.cadastroaefeeft.api.assembler;

import com.asce1dev.cadastroaefeeft.api.model.input.ClienteInput;
import com.asce1dev.cadastroaefeeft.api.model.input.UsuarioInput;
import com.asce1dev.cadastroaefeeft.domain.model.Cliente;
import com.asce1dev.cadastroaefeeft.domain.model.Usuario;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UsuarioInputDisassembler {

    private final ModelMapper modelMapper;

    public Usuario toDomainObject(UsuarioInput usuarioInput) {
        return modelMapper.map(usuarioInput, Usuario.class);
    }
}

