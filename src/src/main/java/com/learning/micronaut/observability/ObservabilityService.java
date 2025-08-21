package com.learning.micronaut.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micronaut.tracing.annotation.NewSpan;
import io.micronaut.tracing.annotation.SpanTag;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Scope;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Serviço de observabilidade que demonstra uso de OpenTelemetry e Micrometer.
 * 
 * Demonstra conceitos importantes de observabilidade:
 * - Distributed Tracing: Rastreamento de requisições entre serviços
 * - Custom Metrics: Métricas específicas da aplicação
 * - Spans: Unidades de trabalho rastreáveis
 * - Tags/Labels: Metadados para filtragem e agregação
 * - Correlation IDs: Rastreamento de requisições
 * 
 * Comparação com .NET:
 * - Similar ao System.Diagnostics.Activity no .NET
 * - Micrometer é similar ao System.Diagnostics.Metrics
 * - OpenTelemetry é padrão cross-platform (mesmo no .NET)
 * - Spans são similares às Activities
 * - Tags são similares aos Activity Tags
 * 
 * Pilares da Observabilidade:
 * 1. Logs: Eventos discretos (já implementado com Logback)
 * 2. Metrics: Dados numéricos agregados (Micrometer)
 * 3. Traces: Rastreamento de requisições (OpenTelemetry)
 * 
 * @author Learning Project
 */
@Singleton
@RequiredArgsConstructor
@Slf4j
public class ObservabilityService {
    
    private final MeterRegistry meterRegistry;
    private final OpenTelemetry openTelemetry;
    
    // Tracer para criar spans manuais
    private final Tracer tracer;
    
    // Métricas customizadas
    private final Counter userCreatedCounter;
    private final Counter userUpdatedCounter;
    private final Counter userDeletedCounter;
    private final Counter apiRequestCounter;
    private final Counter kafkaMessageCounter;
    private final Timer databaseOperationTimer;
    private final Timer externalServiceTimer;
    
    // Cache para rastreamento de operações ativas
    private final Map<String, Long> activeOperations = new ConcurrentHashMap<>();
    
    public ObservabilityService(MeterRegistry meterRegistry, OpenTelemetry openTelemetry) {
        this.meterRegistry = meterRegistry;
        this.openTelemetry = openTelemetry;
        this.tracer = openTelemetry.getTracer("micronaut-learning-project");
        
        // Inicializar contadores
        this.userCreatedCounter = Counter.builder("users.created")
                .description("Número total de usuários criados")
                .register(meterRegistry);
                
        this.userUpdatedCounter = Counter.builder("users.updated")
                .description("Número total de usuários atualizados")
                .register(meterRegistry);
                
        this.userDeletedCounter = Counter.builder("users.deleted")
                .description("Número total de usuários removidos")
                .register(meterRegistry);
                
        this.apiRequestCounter = Counter.builder("api.requests")
                .description("Número total de requisições à API")
                .register(meterRegistry);
                
        this.kafkaMessageCounter = Counter.builder("kafka.messages")
                .description("Número total de mensagens Kafka processadas")
                .register(meterRegistry);
        
        // Inicializar timers
        this.databaseOperationTimer = Timer.builder("database.operations")
                .description("Tempo de operações de banco de dados")
                .register(meterRegistry);
                
        this.externalServiceTimer = Timer.builder("external.service.calls")
                .description("Tempo de chamadas para serviços externos")
                .register(meterRegistry);
    }
    
    /**
     * Registra criação de usuário com métricas e tracing.
     * 
     * @param userId ID do usuário criado
     * @param userEmail Email do usuário
     * @param source Origem da criação (API, import, etc.)
     */
    @NewSpan("observability.user-created")
    public void recordUserCreated(@SpanTag("userId") String userId, 
                                 @SpanTag("userEmail") String userEmail,
                                 @SpanTag("source") String source) {
        
        log.info("Registrando criação de usuário: {} via {}", userId, source);
        
        // Incrementar contador com tags
        userCreatedCounter.increment(
            "source", source,
            "domain", extractDomain(userEmail)
        );
        
        // Adicionar informações ao span atual
        Span currentSpan = Span.current();
        currentSpan.setAttribute("user.id", userId);
        currentSpan.setAttribute("user.email", userEmail);
        currentSpan.setAttribute("operation.source", source);
        currentSpan.addEvent("User created successfully");
        
        log.debug("Métricas de criação de usuário registradas");
    }
    
    /**
     * Registra atualização de usuário.
     */
    @NewSpan("observability.user-updated")
    public void recordUserUpdated(@SpanTag("userId") String userId,
                                 @SpanTag("changedFields") String changedFields) {
        
        log.info("Registrando atualização de usuário: {} - campos: {}", userId, changedFields);
        
        userUpdatedCounter.increment(
            "fields_changed", String.valueOf(changedFields.split(",").length)
        );
        
        Span.current().setAttribute("user.id", userId);
        Span.current().setAttribute("changed.fields", changedFields);
        Span.current().addEvent("User updated successfully");
    }
    
    /**
     * Registra remoção de usuário.
     */
    @NewSpan("observability.user-deleted")
    public void recordUserDeleted(@SpanTag("userId") String userId,
                                 @SpanTag("reason") String reason) {
        
        log.info("Registrando remoção de usuário: {} - motivo: {}", userId, reason);
        
        userDeletedCounter.increment("reason", reason);
        
        Span.current().setAttribute("user.id", userId);
        Span.current().setAttribute("deletion.reason", reason);
        Span.current().addEvent("User deleted successfully");
    }
    
    /**
     * Registra requisição à API com detalhes.
     */
    public void recordApiRequest(String method, String endpoint, int statusCode, long durationMs) {
        log.debug("Registrando requisição API: {} {} -> {} ({}ms)", 
                 method, endpoint, statusCode, durationMs);
        
        apiRequestCounter.increment(
            "method", method,
            "endpoint", endpoint,
            "status_code", String.valueOf(statusCode),
            "status_class", getStatusClass(statusCode)
        );
        
        // Registrar também como timer
        Timer.Sample sample = Timer.start(meterRegistry);
        sample.stop(Timer.builder("api.request.duration")
                .tag("method", method)
                .tag("endpoint", endpoint)
                .tag("status_code", String.valueOf(statusCode))
                .register(meterRegistry));
    }
    
    /**
     * Registra processamento de mensagem Kafka.
     */
    @NewSpan("observability.kafka-message")
    public void recordKafkaMessage(@SpanTag("topic") String topic,
                                  @SpanTag("messageType") String messageType,
                                  @SpanTag("success") boolean success,
                                  long processingTimeMs) {
        
        log.debug("Registrando mensagem Kafka: {} - tipo: {} - sucesso: {} ({}ms)", 
                 topic, messageType, success, processingTimeMs);
        
        kafkaMessageCounter.increment(
            "topic", topic,
            "message_type", messageType,
            "status", success ? "success" : "error"
        );
        
        Span.current().setAttribute("kafka.topic", topic);
        Span.current().setAttribute("kafka.message_type", messageType);
        Span.current().setAttribute("processing.success", success);
        Span.current().setAttribute("processing.duration_ms", processingTimeMs);
        
        if (success) {
            Span.current().addEvent("Kafka message processed successfully");
        } else {
            Span.current().addEvent("Kafka message processing failed");
        }
    }
    
    /**
     * Inicia rastreamento de operação de banco de dados.
     */
    public Timer.Sample startDatabaseOperation(String operation, String collection) {
        log.debug("Iniciando operação de banco: {} em {}", operation, collection);
        
        // Criar span manual para operação de banco
        Span span = tracer.spanBuilder("database." + operation)
                .setSpanKind(SpanKind.CLIENT)
                .setAttribute("db.system", "mongodb")
                .setAttribute("db.collection.name", collection)
                .setAttribute("db.operation", operation)
                .startSpan();
        
        // Armazenar o span no contexto
        String operationId = operation + "-" + System.currentTimeMillis();
        activeOperations.put(operationId, System.currentTimeMillis());
        
        return Timer.start(meterRegistry);
    }
    
    /**
     * Finaliza rastreamento de operação de banco de dados.
     */
    public void endDatabaseOperation(Timer.Sample sample, String operation, 
                                   String collection, boolean success, int recordCount) {
        
        // Parar o timer
        sample.stop(Timer.builder("database.operation.duration")
                .tag("operation", operation)
                .tag("collection", collection)
                .tag("success", String.valueOf(success))
                .register(meterRegistry));
        
        // Finalizar span
        Span currentSpan = Span.current();
        currentSpan.setAttribute("db.record_count", recordCount);
        currentSpan.setAttribute("db.success", success);
        
        if (success) {
            currentSpan.addEvent("Database operation completed successfully");
        } else {
            currentSpan.addEvent("Database operation failed");
        }
        
        currentSpan.end();
        
        log.debug("Operação de banco finalizada: {} em {} - sucesso: {} - registros: {}", 
                 operation, collection, success, recordCount);
    }
    
    /**
     * Registra chamada para serviço externo.
     */
    public Timer.Sample startExternalServiceCall(String serviceName, String operation) {
        log.debug("Iniciando chamada para serviço externo: {} - operação: {}", serviceName, operation);
        
        Span span = tracer.spanBuilder("external." + serviceName + "." + operation)
                .setSpanKind(SpanKind.CLIENT)
                .setAttribute("service.name", serviceName)
                .setAttribute("service.operation", operation)
                .startSpan();
        
        return Timer.start(meterRegistry);
    }
    
    /**
     * Finaliza rastreamento de chamada para serviço externo.
     */
    public void endExternalServiceCall(Timer.Sample sample, String serviceName, 
                                     String operation, boolean success, String errorMessage) {
        
        sample.stop(Timer.builder("external.service.duration")
                .tag("service", serviceName)
                .tag("operation", operation)
                .tag("success", String.valueOf(success))
                .register(meterRegistry));
        
        Span currentSpan = Span.current();
        currentSpan.setAttribute("service.success", success);
        
        if (success) {
            currentSpan.addEvent("External service call completed successfully");
        } else {
            currentSpan.addEvent("External service call failed");
            if (errorMessage != null) {
                currentSpan.setAttribute("error.message", errorMessage);
            }
        }
        
        currentSpan.end();
        
        log.debug("Chamada para serviço externo finalizada: {} - {} - sucesso: {}", 
                 serviceName, operation, success);
    }
    
    /**
     * Cria span customizado para operação específica.
     */
    public Span createCustomSpan(String operationName, Map<String, String> attributes) {
        Span span = tracer.spanBuilder(operationName).startSpan();
        
        // Adicionar atributos customizados
        if (attributes != null) {
            attributes.forEach(span::setAttribute);
        }
        
        log.debug("Span customizado criado: {} com {} atributos", operationName, 
                 attributes != null ? attributes.size() : 0);
        
        return span;
    }
    
    /**
     * Registra evento customizado no span atual.
     */
    public void addEventToCurrentSpan(String eventName, Map<String, String> attributes) {
        Span currentSpan = Span.current();
        
        if (attributes != null && !attributes.isEmpty()) {
            // OpenTelemetry não suporta atributos em eventos diretamente,
            // então adicionamos como atributos do span
            attributes.forEach(currentSpan::setAttribute);
        }
        
        currentSpan.addEvent(eventName);
        log.debug("Evento adicionado ao span atual: {}", eventName);
    }
    
    /**
     * Obtém métricas atuais do sistema.
     */
    public Map<String, Object> getCurrentMetrics() {
        return Map.of(
            "users_created_total", userCreatedCounter.count(),
            "users_updated_total", userUpdatedCounter.count(),
            "users_deleted_total", userDeletedCounter.count(),
            "api_requests_total", apiRequestCounter.count(),
            "kafka_messages_total", kafkaMessageCounter.count(),
            "database_operations_avg_ms", databaseOperationTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS),
            "external_service_calls_avg_ms", externalServiceTimer.mean(java.util.concurrent.TimeUnit.MILLISECONDS),
            "active_operations", activeOperations.size()
        );
    }
    
    // Métodos utilitários
    
    private String extractDomain(String email) {
        if (email == null || !email.contains("@")) {
            return "unknown";
        }
        return email.substring(email.indexOf("@") + 1);
    }
    
    private String getStatusClass(int statusCode) {
        if (statusCode >= 200 && statusCode < 300) return "2xx";
        if (statusCode >= 300 && statusCode < 400) return "3xx";
        if (statusCode >= 400 && statusCode < 500) return "4xx";
        if (statusCode >= 500) return "5xx";
        return "unknown";
    }
}

