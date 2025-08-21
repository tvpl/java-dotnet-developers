package com.learning.micronaut.service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.tracing.annotation.NewSpan;
import io.micronaut.tracing.annotation.SpanTag;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;

/**
 * Serviço que demonstra integração com APIs externas usando Circuit Breaker.
 * 
 * Demonstra conceitos importantes de resiliência:
 * - @CircuitBreaker: Proteção contra falhas em cascata
 * - @Retry: Tentativas automáticas em caso de falha
 * - @NewSpan: Rastreamento distribuído com OpenTelemetry
 * - Fallback methods: Respostas alternativas quando serviço está indisponível
 * - Timeout handling: Proteção contra chamadas lentas
 * 
 * Comparação com .NET:
 * - Similar ao Polly no .NET para resiliência
 * - @CircuitBreaker é similar ao CircuitBreakerPolicy
 * - @Retry é similar ao RetryPolicy
 * - HttpClient é similar ao HttpClient no .NET
 * - Spans são similares ao Activity no .NET
 * 
 * Padrões de resiliência implementados:
 * - Circuit Breaker: Evita chamadas para serviços com falha
 * - Retry: Tenta novamente em caso de falha temporária
 * - Timeout: Evita esperas indefinidas
 * - Fallback: Resposta alternativa quando serviço indisponível
 * - Bulkhead: Isolamento de recursos (implícito via thread pools)
 * 
 * @author Learning Project
 */
@Singleton
@RequiredArgsConstructor
@Slf4j
public class ExternalService {
    
    @Client("${app.external.service.url}")
    private final HttpClient httpClient;
    
    private final Random random = new Random();
    
    /**
     * Chama serviço externo para validar email com Circuit Breaker e Retry.
     * 
     * Configurações do Circuit Breaker (definidas em application.yml):
     * - sliding-window-size: 10 (janela de 10 chamadas)
     * - minimum-number-of-calls: 5 (mínimo 5 chamadas para avaliar)
     * - failure-rate-threshold: 50% (abre se 50% das chamadas falharem)
     * - wait-duration-in-open-state: 5s (aguarda 5s antes de tentar half-open)
     * 
     * @param email Email para validar
     * @return Mono<Boolean> indicando se email é válido
     */
    @CircuitBreaker(name = "external-service", fallbackMethod = "validateEmailFallback")
    @Retry(name = "external-service")
    @NewSpan("external-service.validate-email") // Cria span para rastreamento
    public Mono<Boolean> validateEmail(@SpanTag("email") String email) {
        log.debug("Validando email via serviço externo: {}", email);
        
        return Mono.fromCallable(() -> {
            // Simular diferentes cenários para demonstrar Circuit Breaker
            simulateExternalServiceBehavior();
            
            // Simular chamada HTTP real
            return callExternalEmailValidationService(email);
        })
        .timeout(Duration.ofSeconds(3)) // Timeout de 3 segundos
        .doOnSuccess(result -> 
            log.info("Email validado com sucesso: {} -> {}", email, result))
        .doOnError(error -> 
            log.warn("Erro na validação de email: {} -> {}", email, error.getMessage()));
    }
    
    /**
     * Método de fallback para validação de email.
     * 
     * Este método é chamado quando:
     * - Circuit Breaker está aberto
     * - Todas as tentativas de retry falharam
     * - Timeout foi excedido
     * 
     * @param email Email que estava sendo validado
     * @param exception Exceção que causou o fallback
     * @return Mono<Boolean> com resposta padrão
     */
    public Mono<Boolean> validateEmailFallback(String email, Exception exception) {
        log.warn("Usando fallback para validação de email: {} - motivo: {}", 
                email, exception.getMessage());
        
        // Estratégia de fallback: validação básica local
        boolean isValid = email != null && 
                         email.contains("@") && 
                         email.contains(".") &&
                         email.length() > 5;
        
        log.info("Validação de email via fallback: {} -> {}", email, isValid);
        return Mono.just(isValid);
    }
    
    /**
     * Busca informações de geolocalização por IP com Circuit Breaker.
     * 
     * @param ipAddress Endereço IP
     * @return Mono com informações de localização
     */
    @CircuitBreaker(name = "external-service", fallbackMethod = "getLocationFallback")
    @Retry(name = "external-service")
    @NewSpan("external-service.get-location")
    public Mono<Map<String, Object>> getLocationByIp(@SpanTag("ip") String ipAddress) {
        log.debug("Buscando localização para IP: {}", ipAddress);
        
        return Mono.fromCallable(() -> {
            simulateExternalServiceBehavior();
            return callExternalLocationService(ipAddress);
        })
        .timeout(Duration.ofSeconds(5))
        .doOnSuccess(result -> 
            log.info("Localização obtida com sucesso para IP: {}", ipAddress))
        .doOnError(error -> 
            log.warn("Erro ao buscar localização para IP: {} -> {}", ipAddress, error.getMessage()));
    }
    
    /**
     * Fallback para busca de localização.
     */
    public Mono<Map<String, Object>> getLocationFallback(String ipAddress, Exception exception) {
        log.warn("Usando fallback para localização do IP: {} - motivo: {}", 
                ipAddress, exception.getMessage());
        
        // Resposta padrão quando serviço externo está indisponível
        Map<String, Object> defaultLocation = Map.of(
            "country", "Unknown",
            "city", "Unknown",
            "latitude", 0.0,
            "longitude", 0.0,
            "source", "fallback"
        );
        
        return Mono.just(defaultLocation);
    }
    
    /**
     * Envia notificação para serviço externo de terceiros.
     * 
     * @param userId ID do usuário
     * @param message Mensagem da notificação
     * @return Mono<String> com ID da notificação enviada
     */
    @CircuitBreaker(name = "external-service", fallbackMethod = "sendNotificationFallback")
    @Retry(name = "external-service")
    @NewSpan("external-service.send-notification")
    public Mono<String> sendExternalNotification(
            @SpanTag("userId") String userId, 
            @SpanTag("message") String message) {
        
        log.debug("Enviando notificação externa para usuário: {}", userId);
        
        return Mono.fromCallable(() -> {
            simulateExternalServiceBehavior();
            return callExternalNotificationService(userId, message);
        })
        .timeout(Duration.ofSeconds(10))
        .doOnSuccess(notificationId -> 
            log.info("Notificação externa enviada: {} -> {}", userId, notificationId))
        .doOnError(error -> 
            log.warn("Erro ao enviar notificação externa: {} -> {}", userId, error.getMessage()));
    }
    
    /**
     * Fallback para envio de notificação externa.
     */
    public Mono<String> sendNotificationFallback(String userId, String message, Exception exception) {
        log.warn("Usando fallback para notificação externa: {} - motivo: {}", 
                userId, exception.getMessage());
        
        // Em produção, poderia:
        // 1. Salvar na fila para retry posterior
        // 2. Usar canal alternativo (email ao invés de push)
        // 3. Registrar para auditoria
        
        String fallbackId = "fallback-" + System.currentTimeMillis();
        log.info("Notificação externa via fallback: {} -> {}", userId, fallbackId);
        
        return Mono.just(fallbackId);
    }
    
    /**
     * Simula comportamentos diferentes do serviço externo para demonstrar Circuit Breaker.
     * 
     * Esta simulação cria cenários de:
     * - Sucesso (70% das vezes)
     * - Timeout (15% das vezes)
     * - Erro de servidor (10% das vezes)
     * - Erro de rede (5% das vezes)
     */
    private void simulateExternalServiceBehavior() throws Exception {
        int scenario = random.nextInt(100);
        
        if (scenario < 70) {
            // 70% sucesso - simula latência normal
            Thread.sleep(random.nextInt(500) + 100); // 100-600ms
            return;
        } else if (scenario < 85) {
            // 15% timeout - simula serviço lento
            Thread.sleep(4000); // 4 segundos (maior que timeout de 3s)
            return;
        } else if (scenario < 95) {
            // 10% erro de servidor
            throw new RuntimeException("Erro interno do serviço externo (HTTP 500)");
        } else {
            // 5% erro de rede
            throw new RuntimeException("Erro de conectividade de rede");
        }
    }
    
    /**
     * Simula chamada para serviço de validação de email.
     */
    private Boolean callExternalEmailValidationService(String email) {
        // Simulação de lógica de validação externa
        // Em produção, faria chamada HTTP real para API de validação
        
        log.debug("Chamando API externa de validação de email...");
        
        // Simular diferentes resultados baseados no email
        if (email.contains("invalid")) {
            return false;
        } else if (email.contains("@test.com")) {
            return false; // Domínio de teste
        } else {
            return email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
        }
    }
    
    /**
     * Simula chamada para serviço de geolocalização.
     */
    private Map<String, Object> callExternalLocationService(String ipAddress) {
        log.debug("Chamando API externa de geolocalização...");
        
        // Simulação de resposta de API de geolocalização
        return Map.of(
            "country", "Brasil",
            "city", "São Paulo",
            "latitude", -23.5505,
            "longitude", -46.6333,
            "source", "external-api",
            "ip", ipAddress
        );
    }
    
    /**
     * Simula chamada para serviço de notificação externa.
     */
    private String callExternalNotificationService(String userId, String message) {
        log.debug("Chamando API externa de notificação...");
        
        // Simulação de envio de notificação
        String notificationId = "ext-notif-" + System.currentTimeMillis() + "-" + userId;
        
        log.debug("Notificação externa simulada enviada: {}", notificationId);
        return notificationId;
    }
    
    /**
     * Método utilitário para verificar saúde do serviço externo.
     * 
     * @return Mono<Boolean> indicando se serviço está saudável
     */
    @NewSpan("external-service.health-check")
    public Mono<Boolean> healthCheck() {
        return Mono.fromCallable(() -> {
            try {
                // Simular health check simples
                Thread.sleep(100);
                
                // 90% de chance de estar saudável
                return random.nextInt(100) < 90;
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return false;
            }
        })
        .timeout(Duration.ofSeconds(2))
        .onErrorReturn(false)
        .doOnSuccess(healthy -> 
            log.debug("Health check do serviço externo: {}", healthy ? "OK" : "FAIL"));
    }
}

