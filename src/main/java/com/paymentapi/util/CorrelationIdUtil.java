package com.paymentapi.util;

import java.util.UUID;
import org.slf4j.MDC;

/**
 * Classe utilitária pra gerenciar os correlation IDs durante o ciclo de vida da requisição.
 * Os IDs ficam armazenados no MDC (Mapped Diagnostic Context) pra aparecer automaticamente nos logs.
 */
public final class CorrelationIdUtil {

    private static final String CORRELATION_ID_KEY = "correlationId";
    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";

    private CorrelationIdUtil() {
    }

    /**
     * Gera um novo correlation ID usando UUID
     *
     * @return String com o correlation ID gerado
     */
    public static String generateCorrelationId() {
        String id = UUID.randomUUID().toString();
        return id;
    }

    /**
     * Coloca o correlation ID no MDC da thread atual
     *
     * @param correlationId O ID que vai ser guardado
     */
    public static void setCorrelationId(String correlationId) {
        if (correlationId != null && !correlationId.isBlank()) {
            MDC.put(CORRELATION_ID_KEY, correlationId);
        }
    }

    /**
     * Pega o correlation ID atual do MDC
     *
     * @return O correlation ID ou null se não tiver nenhum
     */
    public static String getCorrelationId() {
        String id = MDC.get(CORRELATION_ID_KEY);
        return id;
    }

    /**
     * Remove o correlation ID do MDC
     * Precisa chamar isso no final da requisição pra não dar memory leak
     */
    public static void clearCorrelationId() {
        MDC.remove(CORRELATION_ID_KEY);
    }

    /**
     * Limpa todo o contexto do MDC
     * Importante chamar no finally pra não vazar memória
     */
    public static void clearAllMdc() {
        MDC.clear();
    }

    /**
     * Retorna o nome do header HTTP padrão pro correlation ID
     *
     * @return Nome do header
     */
    public static String getCorrelationIdHeader() {
        return CORRELATION_ID_HEADER;
    }

    /**
     * Adiciona uma informação extra no MDC pra aparecer nos logs
     *
     * @param key A chave do contexto
     * @param value O valor do contexto
     */
    public static void setMdcContext(String key, String value) {
        if (key != null && !key.isBlank() && value != null) {
            MDC.put(key, value);
        }
    }

    /**
     * Remove uma chave específica do MDC
     *
     * @param key A chave que vai ser removida
     */
    public static void removeMdcContext(String key) {
        if (key != null) {
            MDC.remove(key);
        }
    }
}
