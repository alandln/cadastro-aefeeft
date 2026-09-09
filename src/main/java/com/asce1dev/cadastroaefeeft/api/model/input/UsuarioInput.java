package com.asce1dev.cadastroaefeeft.api.model.input;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioInput {

    @NotBlank
    @Size(max = 50)
    private String username;

    @NotBlank
    private String password;
}
