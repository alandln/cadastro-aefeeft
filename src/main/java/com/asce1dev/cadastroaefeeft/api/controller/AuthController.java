package com.asce1dev.cadastroaefeeft.api.controller;

import com.asce1dev.cadastroaefeeft.core.security.JwtService;
import com.asce1dev.cadastroaefeeft.domain.exception.NaoAutenticadoException;
import com.asce1dev.cadastroaefeeft.domain.exception.NegocioException;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @PostMapping("/login")
    public TokenResponse login(@RequestBody LoginRequest request) {
        try {
            var authToken = new UsernamePasswordAuthenticationToken(request.username(), request.password());
            var authentication = authenticationManager.authenticate(authToken);

            var user = (UserDetails) authentication.getPrincipal();
            var token = jwtService.generateToken(user);

            return new TokenResponse(token, "Bearer");
        } catch (AuthenticationException e) {
            throw new NaoAutenticadoException("Usuário ou senha inválidos");
        }
    }

    public record LoginRequest(@NotBlank String username, @NotBlank String password) {}
    public record TokenResponse(String accessToken, String tokenType) {}
}