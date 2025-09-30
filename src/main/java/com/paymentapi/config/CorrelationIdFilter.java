package com.paymentapi.config;

import com.paymentapi.util.CorrelationIdUtil;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Filtro que gera e propaga os correlation IDs pra cada requisição HTTP.
 * O ID fica guardado no MDC pra aparecer em todos os logs automaticamente
 * e também é retornado no header da resposta pro cliente poder rastrear.
 */
@Component
@Order(1) // Executa primeiro na cadeia de filtros
public class CorrelationIdFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // Faz o cast pra poder usar os métodos de HTTP
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        try {
            // Verifica se já tem um correlation ID no header da requisição
            String correlationId = httpRequest.getHeader(CorrelationIdUtil.getCorrelationIdHeader());

            // Se não tiver, gera um novo
            if (correlationId == null || correlationId.isBlank()) {
                correlationId = CorrelationIdUtil.generateCorrelationId();
            }

            // Guarda o correlation ID no MDC pra usar nos logs
            CorrelationIdUtil.setCorrelationId(correlationId);

            // Adiciona o correlation ID no header da resposta
            httpResponse.setHeader(CorrelationIdUtil.getCorrelationIdHeader(), correlationId);

            // Continua a execução dos outros filtros
            chain.doFilter(request, response);

        } finally {
            // Limpa o MDC no final pra não dar memory leak e não misturar as requisições
            CorrelationIdUtil.clearAllMdc();
        }
    }
}
