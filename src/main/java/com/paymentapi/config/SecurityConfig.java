package com.paymentapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuração de segurança da aplicação.
 * Define quais endpoints são públicos e quais precisam de autenticação.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
            // Configura as autorizações de requisições
            .authorizeHttpRequests(authz -> authz
                // Endpoints do Actuator são públicos
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                // Endpoints de health check são públicos (Kubernetes)
                .requestMatchers("/health/**").permitAll()
                // Endpoints do Swagger são públicos
                .requestMatchers("/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                // Qualquer outra requisição precisa estar autenticada
                .anyRequest().authenticated()
            )
            // Desabilita CSRF (pra APIs REST geralmente tá desabilitado)
            .csrf(AbstractHttpConfigurer::disable)
            // Autenticação HTTP Basic
            .httpBasic(httpBasic -> {})
            .build();
    }
}