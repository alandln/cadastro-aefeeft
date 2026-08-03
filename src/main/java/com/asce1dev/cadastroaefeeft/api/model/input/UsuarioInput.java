package com.asce1dev.cadastroaefeeft.api.model.input;

import com.asce1dev.cadastroaefeeft.domain.model.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioInput {

    @NotBlank
    private String username;

    @NotBlank
    private String password;
}
