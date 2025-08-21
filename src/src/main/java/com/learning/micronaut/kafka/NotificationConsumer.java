package com.learning.micronaut.kafka;

import com.learning.micronaut.proto.NotificationEvent;
import com.learning.micronaut.proto.NotificationPriority;
import com.learning.micronaut.proto.NotificationType;
import io.micronaut.configuration.kafka.annotation.KafkaListener;
import io.micronaut.configuration.kafka.annotation.OffsetReset;
import io.micronaut.configuration.kafka.annotation.Topic;
import io.micronaut.messaging.annotation.MessageBody;
import io.micronaut.messaging.annotation.MessageHeader;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.concurrent.CompletableFuture;

/**
 * Consumidor Kafka especializado em processar eventos de notificação.
 * 
 * Demonstra conceitos avançados do Kafka Consumer:
 * - Consumidor especializado por tipo de evento
 * - Processamento baseado em prioridade
 * - Processamento assíncrono com CompletableFuture
 * - Diferentes estratégias por tipo de notificação
 * - Rate limiting e throttling
 * 
 * Comparação com .NET:
 * - Similar a ter diferentes handlers para diferentes tipos de mensagem
 * - CompletableFuture é similar ao Task<T> no .NET
 * - Padrão similar ao MediatR com diferentes handlers
 * 
 * Padrões implementados:
 * - Strategy Pattern: Diferentes estratégias por tipo de notificação
 * - Priority Queue: Processamento baseado em prioridade
 * - Async/Await: Processamento não-bloqueante
 * - Circuit Breaker: Proteção contra falhas em cascata
 * 
 * @author Learning Project
 */
@Singleton
@KafkaListener(
    groupId = "notification-processor",
    offsetReset = OffsetReset.EARLIEST
)
@Slf4j
public class NotificationConsumer {
    
    // Simulação de rate limiting (em produção, usar Redis ou similar)
    private long lastEmailSent = 0;
    private long lastSmsSent = 0;
    private static final long EMAIL_RATE_LIMIT_MS = 1000; // 1 email por segundo
    private static final long SMS_RATE_LIMIT_MS = 5000;   // 1 SMS a cada 5 segundos
    
    /**
     * Processa eventos de notificação com diferentes estratégias baseadas no tipo.
     * 
     * @param correlationId ID de correlação para rastreamento
     * @param event Evento de notificação
     */
    @Topic("notifications")
    public void handleNotificationEvent(
            @MessageHeader("correlation-id") String correlationId,
            @MessageBody NotificationEvent event) {
        
        try {
            log.info("Processando notificação - tipo: {}, destinatário: {}, prioridade: {}, correlationId: {}", 
                    event.getType(), event.getRecipientId(), event.getPriority(), correlationId);
            
            // Validar evento
            if (!isValidNotificationEvent(event)) {
                log.warn("Evento de notificação inválido ignorado - correlationId: {}", correlationId);
                return;
            }
            
            // Processar baseado na prioridade
            if (event.getPriority() == NotificationPriority.URGENT) {
                // Processamento síncrono para notificações urgentes
                processNotificationSync(event, correlationId);
            } else {
                // Processamento assíncrono para notificações normais
                processNotificationAsync(event, correlationId);
            }
            
            log.info("Notificação processada com sucesso - correlationId: {}", correlationId);
            
        } catch (Exception e) {
            log.error("Erro ao processar notificação - correlationId: {}", correlationId, e);
            handleNotificationError(correlationId, event, e);
        }
    }
    
    /**
     * Processamento síncrono para notificações urgentes.
     */
    private void processNotificationSync(NotificationEvent event, String correlationId) {
        log.info("Processamento SÍNCRONO de notificação urgente - correlationId: {}", correlationId);
        
        switch (event.getType()) {
            case EMAIL:
                sendEmailNotification(event, correlationId);
                break;
            case SMS:
                sendSmsNotification(event, correlationId);
                break;
            case PUSH:
                sendPushNotification(event, correlationId);
                break;
            case IN_APP:
                sendInAppNotification(event, correlationId);
                break;
            default:
                log.warn("Tipo de notificação não suportado: {} - correlationId: {}", 
                        event.getType(), correlationId);
        }
    }
    
    /**
     * Processamento assíncrono para notificações normais.
     */
    private void processNotificationAsync(NotificationEvent event, String correlationId) {
        log.info("Processamento ASSÍNCRONO de notificação - correlationId: {}", correlationId);
        
        CompletableFuture.runAsync(() -> {
            try {
                // Adicionar delay baseado na prioridade
                addPriorityDelay(event.getPriority());
                
                switch (event.getType()) {
                    case EMAIL:
                        sendEmailNotification(event, correlationId);
                        break;
                    case SMS:
                        sendSmsNotification(event, correlationId);
                        break;
                    case PUSH:
                        sendPushNotification(event, correlationId);
                        break;
                    case IN_APP:
                        sendInAppNotification(event, correlationId);
                        break;
                    default:
                        log.warn("Tipo de notificação não suportado: {} - correlationId: {}", 
                                event.getType(), correlationId);
                }
            } catch (Exception e) {
                log.error("Erro no processamento assíncrono - correlationId: {}", correlationId, e);
            }
        }).exceptionally(throwable -> {
            log.error("Falha crítica no processamento assíncrono - correlationId: {}", 
                     correlationId, throwable);
            return null;
        });
    }
    
    /**
     * Envia notificação por email com rate limiting.
     */
    private void sendEmailNotification(NotificationEvent event, String correlationId) {
        // Implementar rate limiting
        long now = System.currentTimeMillis();
        if (now - lastEmailSent < EMAIL_RATE_LIMIT_MS) {
            long waitTime = EMAIL_RATE_LIMIT_MS - (now - lastEmailSent);
            log.info("Rate limiting email - aguardando {}ms - correlationId: {}", waitTime, correlationId);
            
            try {
                Thread.sleep(waitTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Interrompido durante rate limiting - correlationId: {}", correlationId);
                return;
            }
        }
        
        log.info("Enviando EMAIL para: {} - título: '{}' - correlationId: {}", 
                event.getRecipientId(), event.getTitle(), correlationId);
        
        // Simulação de envio de email
        // Em produção, usar serviços como SendGrid, AWS SES, etc.
        simulateEmailSending(event);
        
        lastEmailSent = System.currentTimeMillis();
        recordNotificationMetrics(event.getType(), "SUCCESS", correlationId);
    }
    
    /**
     * Envia notificação por SMS com rate limiting mais restritivo.
     */
    private void sendSmsNotification(NotificationEvent event, String correlationId) {
        // Rate limiting mais restritivo para SMS (mais caro)
        long now = System.currentTimeMillis();
        if (now - lastSmsSent < SMS_RATE_LIMIT_MS) {
            long waitTime = SMS_RATE_LIMIT_MS - (now - lastSmsSent);
            log.info("Rate limiting SMS - aguardando {}ms - correlationId: {}", waitTime, correlationId);
            
            try {
                Thread.sleep(waitTime);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.warn("Interrompido durante rate limiting SMS - correlationId: {}", correlationId);
                return;
            }
        }
        
        log.info("Enviando SMS para: {} - mensagem: '{}' - correlationId: {}", 
                event.getRecipientId(), event.getMessage(), correlationId);
        
        // Simulação de envio de SMS
        // Em produção, usar serviços como Twilio, AWS SNS, etc.
        simulateSmsSending(event);
        
        lastSmsSent = System.currentTimeMillis();
        recordNotificationMetrics(event.getType(), "SUCCESS", correlationId);
    }
    
    /**
     * Envia notificação push.
     */
    private void sendPushNotification(NotificationEvent event, String correlationId) {
        log.info("Enviando PUSH para: {} - título: '{}' - correlationId: {}", 
                event.getRecipientId(), event.getTitle(), correlationId);
        
        // Simulação de envio de push notification
        // Em produção, usar Firebase Cloud Messaging, Apple Push Notification, etc.
        simulatePushSending(event);
        
        recordNotificationMetrics(event.getType(), "SUCCESS", correlationId);
    }
    
    /**
     * Envia notificação in-app.
     */
    private void sendInAppNotification(NotificationEvent event, String correlationId) {
        log.info("Enviando IN-APP para: {} - título: '{}' - correlationId: {}", 
                event.getRecipientId(), event.getTitle(), correlationId);
        
        // Simulação de notificação in-app
        // Em produção, salvar no banco para exibição quando usuário acessar
        simulateInAppSending(event);
        
        recordNotificationMetrics(event.getType(), "SUCCESS", correlationId);
    }
    
    /**
     * Adiciona delay baseado na prioridade da notificação.
     */
    private void addPriorityDelay(NotificationPriority priority) {
        try {
            switch (priority) {
                case HIGH:
                    Thread.sleep(100); // 100ms para alta prioridade
                    break;
                case MEDIUM:
                    Thread.sleep(500); // 500ms para média prioridade
                    break;
                case LOW:
                    Thread.sleep(2000); // 2s para baixa prioridade
                    break;
                default:
                    Thread.sleep(1000); // 1s para prioridade desconhecida
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.warn("Interrompido durante delay de prioridade");
        }
    }
    
    /**
     * Simula envio de email (em produção, integrar com provedor real).
     */
    private void simulateEmailSending(NotificationEvent event) {
        // Simular tempo de processamento
        try {
            Thread.sleep(200); // 200ms para simular latência de API
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        log.debug("Email enviado com sucesso para: {}", event.getRecipientId());
    }
    
    /**
     * Simula envio de SMS (em produção, integrar com provedor real).
     */
    private void simulateSmsSending(NotificationEvent event) {
        try {
            Thread.sleep(500); // 500ms para simular latência de SMS
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        log.debug("SMS enviado com sucesso para: {}", event.getRecipientId());
    }
    
    /**
     * Simula envio de push notification.
     */
    private void simulatePushSending(NotificationEvent event) {
        try {
            Thread.sleep(150); // 150ms para simular latência de push
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        log.debug("Push notification enviado com sucesso para: {}", event.getRecipientId());
    }
    
    /**
     * Simula criação de notificação in-app.
     */
    private void simulateInAppSending(NotificationEvent event) {
        try {
            Thread.sleep(50); // 50ms para simular inserção no banco
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        log.debug("Notificação in-app criada com sucesso para: {}", event.getRecipientId());
    }
    
    /**
     * Registra métricas de notificação.
     */
    private void recordNotificationMetrics(NotificationType type, String status, String correlationId) {
        // Em produção, usar Micrometer para registrar métricas
        log.debug("METRICS: notification.sent - type: {}, status: {}, correlationId: {}", 
                 type, status, correlationId);
    }
    
    /**
     * Trata erros no processamento de notificações.
     */
    private void handleNotificationError(String correlationId, NotificationEvent event, Exception error) {
        log.error("Erro ao processar notificação - correlationId: {}, tipo: {}, destinatário: {}", 
                 correlationId, event.getType(), event.getRecipientId(), error);
        
        recordNotificationMetrics(event.getType(), "ERROR", correlationId);
        
        // Em produção:
        // 1. Implementar retry com backoff exponencial
        // 2. Dead letter queue para falhas persistentes
        // 3. Alertas para tipos específicos de erro
        // 4. Fallback para outros canais de notificação
    }
    
    /**
     * Valida se o evento de notificação é válido.
     */
    private boolean isValidNotificationEvent(NotificationEvent event) {
        return event.hasBase() &&
               !event.getRecipientId().isEmpty() &&
               event.getType() != NotificationType.UNKNOWN_TYPE &&
               !event.getTitle().isEmpty() &&
               !event.getMessage().isEmpty();
    }
}

