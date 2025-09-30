package com.paymentapi.config;

import jakarta.annotation.PreDestroy;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.Duration;
import java.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * Componente que escuta os eventos do ciclo de vida da aplicação Spring Boot
 * e loga informações estruturadas sobre startup, ready e shutdown.
 */
@Component
public class ApplicationEventLogger {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationEventLogger.class);

    private final Environment environment;
    private final Instant applicationStartTime;

    // Configurações injetadas
    @Value("${spring.application.name:payment-api}")
    private String serviceName;

    @Value("${application.version:0.0.1-SNAPSHOT}")
    private String version;

    public ApplicationEventLogger(Environment environment) {
        this.environment = environment;
        // Guarda o horário que a aplicação começou
        this.applicationStartTime = Instant.now();
    }

    /**
     * Loga quando a aplicação tá iniciando (Spring context inicializado)
     */
    @EventListener(ApplicationStartedEvent.class)
    public void onApplicationStarted(ApplicationStartedEvent event) {
        // Pega os profiles ativos
        String[] activeProfiles = environment.getActiveProfiles();
        String profiles = activeProfiles.length > 0 ? String.join(",", activeProfiles) : "default";
        String hostname = getHostname();
        String javaVersion = System.getProperty("java.version");
        String pid = getProcessId();

        // Loga as informações de startup
        logger.info(
                "Application starting: service={} version={} environment={} hostname={} pid={} javaVersion={}",
                serviceName,
                version,
                profiles,
                hostname,
                pid,
                javaVersion
        );
    }

    /**
     * Loga quando a aplicação tá pronta pra receber requisições
     */
    @EventListener(ApplicationReadyEvent.class)
    public void onApplicationReady(ApplicationReadyEvent event) {
        // Calcula quanto tempo levou pra subir
        Duration startupDuration = Duration.between(applicationStartTime, Instant.now());
        String[] activeProfiles = environment.getActiveProfiles();
        String profiles = activeProfiles.length > 0 ? String.join(",", activeProfiles) : "default";
        String hostname = getHostname();

        // Pega info de memória
        RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
        long memoryUsedMb = (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024);
        long memoryTotalMb = Runtime.getRuntime().totalMemory() / (1024 * 1024);

        logger.info(
                "Application ready: service={} version={} environment={} hostname={} startupDuration={}ms "
                        + "memoryUsed={}MB memoryTotal={}MB activeProfiles={}",
                serviceName,
                version,
                profiles,
                hostname,
                startupDuration.toMillis(),
                memoryUsedMb,
                memoryTotalMb,
                profiles
        );
    }

    /**
     * Loga quando a aplicação tá sendo desligada
     */
    @EventListener(ContextClosedEvent.class)
    public void onContextClosed(ContextClosedEvent event) {
        // Calcula quanto tempo a aplicação ficou rodando
        Duration uptime = Duration.between(applicationStartTime, Instant.now());
        String[] activeProfiles = environment.getActiveProfiles();
        String profiles = activeProfiles.length > 0 ? String.join(",", activeProfiles) : "default";
        String hostname = getHostname();

        logger.info(
                "Application shutting down: service={} version={} environment={} hostname={} uptime={}s shutdownStatus={}",
                serviceName,
                version,
                profiles,
                hostname,
                uptime.toSeconds(),
                "graceful"
        );
    }

    /**
     * Hook do PreDestroy pra garantir que loga o shutdown mesmo se o evento não disparar
     */
    @PreDestroy
    public void onPreDestroy() {
        logger.info("Application shutdown hook executed: service={} version={}", serviceName, version);
    }

    /**
     * Pega o hostname da máquina
     */
    private String getHostname() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            logger.warn("Unable to determine hostname", e);
            return "unknown";
        }
    }

    /**
     * Pega o ID do processo (PID)
     */
    private String getProcessId() {
        try {
            RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
            String processName = runtimeMxBean.getName();
            // O nome vem como "pid@hostname", então pega só o pid
            return processName.split("@")[0];
        } catch (Exception e) {
            logger.warn("Unable to determine process ID", e);
            return "unknown";
        }
    }
}
