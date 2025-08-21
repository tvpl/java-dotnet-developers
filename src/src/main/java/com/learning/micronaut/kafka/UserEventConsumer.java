package com.learning.micronaut.kafka;

import com.learning.micronaut.proto.*;
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

/**
 * Consumidor Kafka para eventos de usuário usando deserialização Protobuf.
 * 
 * Demonstra conceitos importantes do Kafka Consumer no Micronaut:
 * - @KafkaListener: Marca a classe como consumidor Kafka
 * - @Topic: Especifica o tópico a ser consumido
 * - @OffsetReset: Estratégia de leitura (earliest, latest, none)
 * - @MessageBody: Corpo da mensagem (deserializado automaticamente do Protobuf)
 * - @MessageHeader: Acesso aos headers da mensagem
 * - Processamento assíncrono e tratamento de erros
 * 
 * Comparação com .NET:
 * - Similar ao IConsumer<TKey, TValue> do Confluent.Kafka
 * - @KafkaListener é similar ao ConsumerBuilder no .NET
 * - Métodos anotados são similares aos event handlers no .NET
 * - Deserialização automática elimina boilerplate code
 * 
 * Padrões de consumo:
 * - At-least-once delivery: Mensagem pode ser processada mais de uma vez
 * - Idempotência: Operações devem ser seguras para reprocessamento
 * - Error handling: Dead letter queues, retry policies
 * - Offset management: Controle de posição de leitura
 * 
 * @author Learning Project
 */
@Singleton
@KafkaListener(
    groupId = "user-event-processor", // Grupo de consumidores
    offsetReset = OffsetReset.EARLIEST // Lê desde o início se não houver offset salvo
)
@Slf4j
public class UserEventConsumer {
    
    /**
     * Processa eventos de criação de usuário.
     * 
     * Este método é chamado automaticamente quando uma mensagem
     * UserCreatedEvent chega no tópico 'user-events'.
     * 
     * Equivalente no .NET:
     * consumer.Consume() em um loop, com deserialização manual
     * 
     * @param correlationId ID de correlação do header
     * @param event Evento de criação deserializado automaticamente
     */
    @Topic("user-events")
    public void handleUserCreatedEvent(
            @MessageHeader("correlation-id") String correlationId,
            @MessageBody UserCreatedEvent event) {
        
        try {
            log.info("Processando evento USER_CREATED - usuário: {}, correlationId: {}", 
                    event.getUser().getEmail(), correlationId);
            
            // Validar se o evento é válido
            if (!isValidUserCreatedEvent(event)) {
                log.warn("Evento USER_CREATED inválido ignorado - correlationId: {}", correlationId);
                return;
            }
            
            // Processar o evento
            processUserCreatedEvent(event);
            
            // Log de auditoria
            logAuditEvent("USER_CREATED_PROCESSED", event.getUser().getId(), correlationId);
            
            log.info("Evento USER_CREATED processado com sucesso - usuário: {}, correlationId: {}", 
                    event.getUser().getId(), correlationId);
            
        } catch (Exception e) {
            log.error("Erro ao processar evento USER_CREATED - correlationId: {}", 
                     correlationId, e);
            
            // Em produção, implementar:
            // - Dead letter queue para mensagens com erro
            // - Retry policy com backoff exponencial
            // - Alertas para falhas críticas
            handleEventProcessingError(correlationId, "USER_CREATED", e);
        }
    }
    
    /**
     * Processa eventos de atualização de usuário.
     */
    @Topic("user-events")
    public void handleUserUpdatedEvent(
            @MessageHeader("correlation-id") String correlationId,
            @MessageBody UserUpdatedEvent event) {
        
        try {
            log.info("Processando evento USER_UPDATED - usuário: {}, correlationId: {}", 
                    event.getUser().getId(), correlationId);
            
            if (!isValidUserUpdatedEvent(event)) {
                log.warn("Evento USER_UPDATED inválido ignorado - correlationId: {}", correlationId);
                return;
            }
            
            processUserUpdatedEvent(event);
            logAuditEvent("USER_UPDATED_PROCESSED", event.getUser().getId(), correlationId);
            
            log.info("Evento USER_UPDATED processado com sucesso - usuário: {}, correlationId: {}", 
                    event.getUser().getId(), correlationId);
            
        } catch (Exception e) {
            log.error("Erro ao processar evento USER_UPDATED - correlationId: {}", 
                     correlationId, e);
            handleEventProcessingError(correlationId, "USER_UPDATED", e);
        }
    }
    
    /**
     * Processa eventos de exclusão de usuário.
     */
    @Topic("user-events")
    public void handleUserDeletedEvent(
            @MessageHeader("correlation-id") String correlationId,
            @MessageBody UserDeletedEvent event) {
        
        try {
            log.info("Processando evento USER_DELETED - usuário: {}, correlationId: {}", 
                    event.getUserId(), correlationId);
            
            if (!isValidUserDeletedEvent(event)) {
                log.warn("Evento USER_DELETED inválido ignorado - correlationId: {}", correlationId);
                return;
            }
            
            processUserDeletedEvent(event);
            logAuditEvent("USER_DELETED_PROCESSED", event.getUserId(), correlationId);
            
            log.info("Evento USER_DELETED processado com sucesso - usuário: {}, correlationId: {}", 
                    event.getUserId(), correlationId);
            
        } catch (Exception e) {
            log.error("Erro ao processar evento USER_DELETED - correlationId: {}", 
                     correlationId, e);
            handleEventProcessingError(correlationId, "USER_DELETED", e);
        }
    }
    
    /**
     * Processa o evento de criação de usuário.
     * 
     * Aqui você implementaria a lógica de negócio específica,
     * como enviar email de boas-vindas, criar perfil em outros sistemas, etc.
     */
    private void processUserCreatedEvent(UserCreatedEvent event) {
        User user = event.getUser();
        
        // Exemplo de processamentos que poderiam ser feitos:
        
        // 1. Enviar email de boas-vindas
        log.info("Enviando email de boas-vindas para: {}", user.getEmail());
        // emailService.sendWelcomeEmail(user.getEmail(), user.getName());
        
        // 2. Criar perfil em sistema de analytics
        log.info("Criando perfil de analytics para usuário: {}", user.getId());
        // analyticsService.createUserProfile(user);
        
        // 3. Adicionar a lista de marketing (se consentimento dado)
        log.info("Avaliando adição à lista de marketing para usuário: {}", user.getId());
        // if (hasMarketingConsent(user)) {
        //     marketingService.addToList(user.getEmail());
        // }
        
        // 4. Inicializar configurações padrão
        log.info("Inicializando configurações padrão para usuário: {}", user.getId());
        // userSettingsService.initializeDefaults(user.getId());
        
        // 5. Registrar métricas
        recordUserCreatedMetrics(user);
    }
    
    /**
     * Processa o evento de atualização de usuário.
     */
    private void processUserUpdatedEvent(UserUpdatedEvent event) {
        User user = event.getUser();
        User previousUser = event.getPreviousUser();
        
        log.info("Processando alterações do usuário: {}", user.getId());
        
        // Verificar se email foi alterado
        if (!user.getEmail().equals(previousUser.getEmail())) {
            log.info("Email alterado de {} para {} - usuário: {}", 
                    previousUser.getEmail(), user.getEmail(), user.getId());
            // emailService.sendEmailChangeNotification(previousUser.getEmail(), user.getEmail());
        }
        
        // Verificar se status foi alterado
        if (!user.getStatus().equals(previousUser.getStatus())) {
            log.info("Status alterado de {} para {} - usuário: {}", 
                    previousUser.getStatus(), user.getStatus(), user.getId());
            handleStatusChange(user, previousUser.getStatus());
        }
        
        // Atualizar índices de busca
        log.info("Atualizando índices de busca para usuário: {}", user.getId());
        // searchService.updateUserIndex(user);
        
        recordUserUpdatedMetrics(user, event.getChangedFieldsList());
    }
    
    /**
     * Processa o evento de exclusão de usuário.
     */
    private void processUserDeletedEvent(UserDeletedEvent event) {
        String userId = event.getUserId();
        
        log.info("Processando exclusão do usuário: {}", userId);
        
        // Limpar dados relacionados (GDPR compliance)
        log.info("Limpando dados relacionados do usuário: {}", userId);
        // dataCleanupService.cleanupUserData(userId);
        
        // Remover de listas de marketing
        log.info("Removendo usuário de listas de marketing: {}", userId);
        // marketingService.removeFromAllLists(userId);
        
        // Invalidar caches
        log.info("Invalidando caches do usuário: {}", userId);
        // cacheService.invalidateUserCache(userId);
        
        // Notificar sistemas dependentes
        log.info("Notificando sistemas dependentes sobre exclusão: {}", userId);
        // integrationService.notifyUserDeletion(userId);
        
        recordUserDeletedMetrics(userId, event.getReason());
    }
    
    /**
     * Trata mudanças de status do usuário.
     */
    private void handleStatusChange(User user, UserStatus previousStatus) {
        switch (user.getStatus()) {
            case SUSPENDED:
                log.info("Usuário suspenso: {}", user.getId());
                // suspensionService.handleSuspension(user.getId());
                break;
            case INACTIVE:
                log.info("Usuário inativado: {}", user.getId());
                // inactivationService.handleInactivation(user.getId());
                break;
            case ACTIVE:
                if (previousStatus == UserStatus.SUSPENDED || previousStatus == UserStatus.INACTIVE) {
                    log.info("Usuário reativado: {}", user.getId());
                    // reactivationService.handleReactivation(user.getId());
                }
                break;
            default:
                log.debug("Mudança de status não requer ação especial: {} -> {}", 
                         previousStatus, user.getStatus());
        }
    }
    
    /**
     * Registra métricas de criação de usuário.
     */
    private void recordUserCreatedMetrics(User user) {
        // Em produção, usar Micrometer ou similar
        log.debug("Registrando métricas de criação - usuário: {}, idade: {}", 
                 user.getId(), user.getAge());
    }
    
    /**
     * Registra métricas de atualização de usuário.
     */
    private void recordUserUpdatedMetrics(User user, java.util.List<String> changedFields) {
        log.debug("Registrando métricas de atualização - usuário: {}, campos alterados: {}", 
                 user.getId(), changedFields);
    }
    
    /**
     * Registra métricas de exclusão de usuário.
     */
    private void recordUserDeletedMetrics(String userId, String reason) {
        log.debug("Registrando métricas de exclusão - usuário: {}, motivo: {}", userId, reason);
    }
    
    /**
     * Registra evento de auditoria.
     */
    private void logAuditEvent(String action, String userId, String correlationId) {
        log.info("AUDIT: {} - usuário: {}, correlationId: {}", action, userId, correlationId);
        // Em produção, enviar para sistema de auditoria
    }
    
    /**
     * Trata erros no processamento de eventos.
     */
    private void handleEventProcessingError(String correlationId, String eventType, Exception error) {
        log.error("Erro crítico no processamento - correlationId: {}, tipo: {}, erro: {}", 
                 correlationId, eventType, error.getMessage());
        
        // Em produção:
        // 1. Enviar para dead letter queue
        // 2. Alertar equipe de operações
        // 3. Registrar métricas de erro
        // 4. Considerar circuit breaker se muitos erros
    }
    
    // Métodos de validação
    
    private boolean isValidUserCreatedEvent(UserCreatedEvent event) {
        return event.hasBase() && 
               event.hasUser() && 
               !event.getUser().getEmail().isEmpty() &&
               !event.getUser().getName().isEmpty();
    }
    
    private boolean isValidUserUpdatedEvent(UserUpdatedEvent event) {
        return event.hasBase() && 
               event.hasUser() && 
               event.hasPreviousUser() &&
               !event.getUser().getId().isEmpty();
    }
    
    private boolean isValidUserDeletedEvent(UserDeletedEvent event) {
        return event.hasBase() && 
               !event.getUserId().isEmpty();
    }
}

