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
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Filtro que loga as requisições HTTP que entram e as respostas que saem
 * com informações estruturadas tipo método, URI, status, duração e correlation ID.
 * Dados sensíveis nos headers são mascarados automaticamente.
 */
@Component
@Order(2) // Executa depois do CorrelationIdFilter
public class RequestResponseLoggingFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(RequestResponseLoggingFilter.class);

    // Lista com os nomes de headers que tem dados sensíveis
    private static final List<String> SENSITIVE_HEADERS = Arrays.asList(
            "authorization", "password", "secret", "token", "api-key", "x-api-key"
    );

    // Lista com os nomes de parâmetros que tem dados sensíveis
    private static final List<String> SENSITIVE_PARAM_NAMES = Arrays.asList(
            "password", "senha", "token", "secret"
    );

    // Configurações que vem do application.yml
    @Value("${logging.request-response.enabled:true}")
    private boolean loggingEnabled;

    @Value("${logging.request-response.include-headers:false}")
    private boolean includeHeaders;

    @Value("${logging.request-response.include-query-params:true}")
    private boolean includeQueryParams;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        // Se o log tá desabilitado, só continua sem logar nada
        if (!loggingEnabled) {
            chain.doFilter(request, response);
            return;
        }

        // Faz o cast das requisições
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        // Marca o horário de início pra calcular a duração
        long startTime = System.currentTimeMillis();

        try {
            // Loga a requisição que tá chegando
            logRequest(httpRequest);

            // Continua executando os outros filtros
            chain.doFilter(request, response);

        } finally {
            // Calcula quanto tempo levou e loga a resposta
            long duration = System.currentTimeMillis() - startTime;
            logResponse(httpRequest, httpResponse, duration);
        }
    }

    /**
     * Loga as informações da requisição que tá entrando
     */
    private void logRequest(HttpServletRequest request) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        String correlationId = CorrelationIdUtil.getCorrelationId();
        String clientIp = getClientIp(request);

        // Guarda o path no MDC pra usar nos logs de erro
        CorrelationIdUtil.setMdcContext("requestPath", uri);

        // Monta a mensagem do log
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("Incoming request: ").append(method).append(" ").append(uri);

        // Adiciona query params se tiver e se tiver habilitado
        if (includeQueryParams && queryString != null && !queryString.isBlank()) {
            logMessage.append(" query=").append(maskSensitiveParams(queryString));
        }

        // Adiciona IP e correlation ID
        logMessage.append(" clientIp=").append(maskIpAddress(clientIp));
        logMessage.append(" correlationId=").append(correlationId);

        // Adiciona headers se tiver habilitado
        if (includeHeaders) {
            logMessage.append(" headers=").append(getHeadersAsString(request));
        }

        logger.info(logMessage.toString());
    }

    /**
     * Loga as informações da resposta que tá saindo
     */
    private void logResponse(HttpServletRequest request, HttpServletResponse response, long durationMs) {
        String method = request.getMethod();
        String uri = request.getRequestURI();
        int status = response.getStatus();
        String correlationId = CorrelationIdUtil.getCorrelationId();

        // Monta a mensagem do log
        StringBuilder logMessage = new StringBuilder();
        logMessage.append("Outgoing response: ").append(method).append(" ").append(uri);
        logMessage.append(" status=").append(status);
        logMessage.append(" duration=").append(durationMs).append("ms");
        logMessage.append(" correlationId=").append(correlationId);

        // Escolhe o nível do log baseado no status code
        if (status >= 500) {
            logger.error(logMessage.toString());
        } else if (status >= 400) {
            logger.warn(logMessage.toString());
        } else {
            logger.info(logMessage.toString());
        }
    }

    /**
     * Pega todos os headers e transforma em String, mascarando os sensíveis
     */
    private String getHeadersAsString(HttpServletRequest request) {
        return Collections.list(request.getHeaderNames()).stream()
                .collect(Collectors.toMap(
                        headerName -> headerName,
                        headerName -> isSensitiveHeader(headerName) ? "***MASKED***" : request.getHeader(headerName)
                ))
                .toString();
    }

    /**
     * Verifica se o header tem dados sensíveis
     */
    private boolean isSensitiveHeader(String headerName) {
        String lowerHeader = headerName.toLowerCase();
        return SENSITIVE_HEADERS.stream().anyMatch(lowerHeader::contains);
    }

    /**
     * Mascara os parâmetros sensíveis na query string
     */
    private String maskSensitiveParams(String queryString) {
        if (queryString == null || queryString.isBlank()) {
            return queryString;
        }

        // Separa os parâmetros
        String[] params = queryString.split("&");
        StringBuilder masked = new StringBuilder();

        // Percorre cada parâmetro
        for (String param : params) {
            String[] keyValue = param.split("=", 2);
            if (masked.length() > 0) {
                masked.append("&");
            }
            masked.append(keyValue[0]);
            if (keyValue.length == 2) {
                masked.append("=");
                // Se for sensível, mascara
                if (isSensitiveParam(keyValue[0])) {
                    masked.append("***MASKED***");
                } else {
                    masked.append(keyValue[1]);
                }
            }
        }

        return masked.toString();
    }

    /**
     * Verifica se o parâmetro tem dados sensíveis
     */
    private boolean isSensitiveParam(String paramName) {
        String lowerParam = paramName.toLowerCase();
        return SENSITIVE_PARAM_NAMES.stream().anyMatch(lowerParam::contains);
    }

    /**
     * Pega o IP real do cliente, considerando proxies
     */
    private String getClientIp(HttpServletRequest request) {
        // Tenta pegar do X-Forwarded-For primeiro (quando tem proxy)
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isBlank()) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isBlank()) {
            ip = request.getRemoteAddr();
        }
        // Se tiver vários IPs separados por vírgula, pega o primeiro
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * Mascara o último octeto do IP por privacidade
     * Exemplo: 192.168.1.100 vira 192.168.1.xxx
     */
    private String maskIpAddress(String ip) {
        if (ip == null || ip.isBlank()) {
            return "unknown";
        }
        // IPv4 - mascara o último octeto
        String[] parts = ip.split("\\.");
        if (parts.length == 4) {
            return parts[0] + "." + parts[1] + "." + parts[2] + ".xxx";
        }
        // IPv6 ou outro formato - mascara o último segmento
        int lastColon = ip.lastIndexOf(':');
        if (lastColon > 0) {
            return ip.substring(0, lastColon + 1) + "xxx";
        }
        return ip;
    }
}
