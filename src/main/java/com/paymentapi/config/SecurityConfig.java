package com.paymentapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuração de segurança da aplicação.
 * Define quais endpoints são públicos e quais precisam de autenticação.
 * BCryptPasswordEncoder para hashing de senhas.
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
                // Endpoints de métricas são públicos (Prometheus)
                .requestMatchers("/v1/health/**", "/v1/metrics").permitAll()
                // Endpoints do Swagger são públicos
                .requestMatchers("/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                // Qualquer outra requisição precisa estar autenticada
                .anyRequest().authenticated()
            )
            // Desabilita CSRF
            .csrf(AbstractHttpConfigurer::disable)
            // Autenticação HTTP Basic
            .httpBasic(httpBasic -> {})
            .build();
    }

    /**
     * Configura BCryptPasswordEncoder para hashing seguro de senhas.
     *
     * @return BCryptPasswordEncoder configurado com strength = 10
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(10);
    }
}