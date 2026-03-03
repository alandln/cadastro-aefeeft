package com.asce1dev.cadastroaefeeft.core.security;

import com.asce1dev.cadastroaefeeft.api.exceptionhandler.Problem;
import com.asce1dev.cadastroaefeeft.api.exceptionhandler.ProblemType;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.time.OffsetDateTime;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;
    private final ObjectMapper mapper;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) ->
                        {
                            writeProblem(response,
                                    HttpServletResponse.SC_UNAUTHORIZED,
                                    ProblemType.NAO_AUTENTICADO,
                                    "Você precisa estar autenticado para acessar este recurso"
                            );
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            writeProblem(response,
                                    HttpServletResponse.SC_FORBIDDEN,
                                    ProblemType.ACESSO_NEGADO,
                                    "Você não tem permissão para executar esta operação."
                            );
                        })
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/login").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    private void writeProblem(HttpServletResponse response, int status, ProblemType type, String detail) throws IOException {
        var problem = Problem.builder()
                .timestamp(OffsetDateTime.now())
                .status(status)
                .type(type.getUri())
                .title(type.getTitle())
                .detail(detail)
                .userMessage(detail)
                .build();

        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        mapper.writeValue(response.getOutputStream(), problem);

    }

}