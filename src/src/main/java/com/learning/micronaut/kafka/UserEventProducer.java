package com.learning.micronaut.kafka;

import com.learning.micronaut.dto.UserDto;
import com.learning.micronaut.proto.*;
import io.micronaut.configuration.kafka.annotation.KafkaClient;
import io.micronaut.configuration.kafka.annotation.Topic;
import io.micronaut.messaging.annotation.MessageBody;
import io.micronaut.messaging.annotation.MessageHeader;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

/**
 * Produtor Kafka para eventos de usuário usando serialização Protobuf.
 * 
 * Demonstra conceitos importantes do Kafka no Micronaut:
 * - @KafkaClient: Marca a interface como cliente Kafka
 * - @Topic: Especifica o tópico de destino
 * - @MessageBody: Corpo da mensagem (será serializado em Protobuf)
 * - @MessageHeader: Headers da mensagem
 * - Serialização automática usando KafkaProtobufSerializer
 * 
 * Comparação com .NET:
 * - Similar ao IProducer<TKey, TValue> do Confluent.Kafka
 * - @KafkaClient é similar ao ProducerBuilder no .NET
 * - Protobuf serialization é mais eficiente que JSON
 * - Headers são similares aos Message Headers no .NET
 * 
 * Vantagens do Protobuf sobre JSON no Kafka:
 * - Menor tamanho das mensagens (economia de banda e storage)
 * - Serialização/deserialização mais rápida
 * - Schema evolution (compatibilidade entre versões)
 * - Validação de tipos em tempo de compilação
 * 
 * @author Learning Project
 */
@KafkaClient // Marca como cliente Kafka - será implementado automaticamente pelo Micronaut
public interface UserEventProducer {
    
    /**
     * Envia evento de criação de usuário.
     * 
     * @param correlationId ID de correlação para rastreamento
     * @param event Evento de criação do usuário
     */
    @Topic("user-events") // Tópico de destino
    void sendUserCreatedEvent(
            @MessageHeader("correlation-id") String correlationId,
            @MessageBody UserCreatedEvent event
    );
    
    /**
     * Envia evento de atualização de usuário.
     * 
     * @param correlationId ID de correlação para rastreamento
     * @param event Evento de atualização do usuário
     */
    @Topic("user-events")
    void sendUserUpdatedEvent(
            @MessageHeader("correlation-id") String correlationId,
            @MessageBody UserUpdatedEvent event
    );
    
    /**
     * Envia evento de exclusão de usuário.
     * 
     * @param correlationId ID de correlação para rastreamento
     * @param event Evento de exclusão do usuário
     */
    @Topic("user-events")
    void sendUserDeletedEvent(
            @MessageHeader("correlation-id") String correlationId,
            @MessageBody UserDeletedEvent event
    );
    
    /**
     * Envia evento de notificação.
     * 
     * @param correlationId ID de correlação para rastreamento
     * @param event Evento de notificação
     */
    @Topic("notifications")
    void sendNotificationEvent(
            @MessageHeader("correlation-id") String correlationId,
            @MessageBody NotificationEvent event
    );
}

/**
 * Serviço que utiliza o produtor para enviar eventos.
 * 
 * Esta classe demonstra como usar o produtor Kafka em um serviço,
 * incluindo a criação dos eventos Protobuf e o tratamento de erros.
 */
@Singleton
@RequiredArgsConstructor
@Slf4j
class UserEventService {
    
    private final UserEventProducer eventProducer;
    
    /**
     * Publica evento de criação de usuário.
     * 
     * @param user Usuário criado
     * @param createdBy Quem criou o usuário
     */
    public void publishUserCreated(UserDto user, String createdBy) {
        try {
            String correlationId = UUID.randomUUID().toString();
            
            // Cria o evento base
            BaseEvent baseEvent = createBaseEvent("USER_CREATED", correlationId);
            
            // Converte UserDto para User Protobuf
            User protoUser = convertToProtoUser(user);
            
            // Cria o evento específico
            UserCreatedEvent event = UserCreatedEvent.newBuilder()
                    .setBase(baseEvent)
                    .setUser(protoUser)
                    .setCreatedBy(createdBy != null ? createdBy : "system")
                    .build();
            
            // Envia o evento
            eventProducer.sendUserCreatedEvent(correlationId, event);
            
            log.info("Evento USER_CREATED enviado para usuário: {} (correlationId: {})", 
                    user.getId(), correlationId);
            
        } catch (Exception e) {
            log.error("Erro ao enviar evento USER_CREATED para usuário: {}", user.getId(), e);
            // Em produção, considere implementar retry ou dead letter queue
        }
    }
    
    /**
     * Publica evento de atualização de usuário.
     * 
     * @param user Usuário atualizado
     * @param previousUser Usuário antes da atualização
     * @param updatedBy Quem atualizou o usuário
     */
    public void publishUserUpdated(UserDto user, UserDto previousUser, String updatedBy) {
        try {
            String correlationId = UUID.randomUUID().toString();
            
            BaseEvent baseEvent = createBaseEvent("USER_UPDATED", correlationId);
            
            User protoUser = convertToProtoUser(user);
            User protoPreviousUser = convertToProtoUser(previousUser);
            
            UserUpdatedEvent event = UserUpdatedEvent.newBuilder()
                    .setBase(baseEvent)
                    .setUser(protoUser)
                    .setPreviousUser(protoPreviousUser)
                    .setUpdatedBy(updatedBy != null ? updatedBy : "system")
                    .addChangedFields("name") // Em produção, detectar campos alterados
                    .build();
            
            eventProducer.sendUserUpdatedEvent(correlationId, event);
            
            log.info("Evento USER_UPDATED enviado para usuário: {} (correlationId: {})", 
                    user.getId(), correlationId);
            
        } catch (Exception e) {
            log.error("Erro ao enviar evento USER_UPDATED para usuário: {}", user.getId(), e);
        }
    }
    
    /**
     * Publica evento de exclusão de usuário.
     * 
     * @param userId ID do usuário excluído
     * @param deletedBy Quem excluiu o usuário
     * @param reason Motivo da exclusão
     */
    public void publishUserDeleted(String userId, String deletedBy, String reason) {
        try {
            String correlationId = UUID.randomUUID().toString();
            
            BaseEvent baseEvent = createBaseEvent("USER_DELETED", correlationId);
            
            UserDeletedEvent event = UserDeletedEvent.newBuilder()
                    .setBase(baseEvent)
                    .setUserId(userId)
                    .setDeletedBy(deletedBy != null ? deletedBy : "system")
                    .setReason(reason != null ? reason : "User requested deletion")
                    .build();
            
            eventProducer.sendUserDeletedEvent(correlationId, event);
            
            log.info("Evento USER_DELETED enviado para usuário: {} (correlationId: {})", 
                    userId, correlationId);
            
        } catch (Exception e) {
            log.error("Erro ao enviar evento USER_DELETED para usuário: {}", userId, e);
        }
    }
    
    /**
     * Publica evento de notificação.
     * 
     * @param recipientId ID do destinatário
     * @param type Tipo de notificação
     * @param title Título da notificação
     * @param message Mensagem da notificação
     */
    public void publishNotification(String recipientId, NotificationType type, 
                                  String title, String message) {
        try {
            String correlationId = UUID.randomUUID().toString();
            
            BaseEvent baseEvent = createBaseEvent("NOTIFICATION", correlationId);
            
            NotificationEvent event = NotificationEvent.newBuilder()
                    .setBase(baseEvent)
                    .setRecipientId(recipientId)
                    .setType(type)
                    .setTitle(title)
                    .setMessage(message)
                    .setPriority(NotificationPriority.MEDIUM)
                    .build();
            
            eventProducer.sendNotificationEvent(correlationId, event);
            
            log.info("Evento NOTIFICATION enviado para usuário: {} (correlationId: {})", 
                    recipientId, correlationId);
            
        } catch (Exception e) {
            log.error("Erro ao enviar evento NOTIFICATION para usuário: {}", recipientId, e);
        }
    }
    
    /**
     * Cria evento base com informações comuns.
     */
    private BaseEvent createBaseEvent(String eventType, String correlationId) {
        return BaseEvent.newBuilder()
                .setEventId(UUID.randomUUID().toString())
                .setEventType(eventType)
                .setSource("micronaut-learning-project")
                .setTimestamp(com.google.protobuf.Timestamp.newBuilder()
                        .setSeconds(LocalDateTime.now().toEpochSecond(ZoneOffset.UTC))
                        .setNanos(LocalDateTime.now().getNano())
                        .build())
                .setCorrelationId(correlationId)
                .putMetadata("service", "user-service")
                .putMetadata("version", "1.0.0")
                .build();
    }
    
    /**
     * Converte UserDto para User Protobuf.
     * 
     * Em um projeto real, este método estaria em uma classe utilitária
     * ou seria gerado automaticamente por uma biblioteca como MapStruct.
     */
    private User convertToProtoUser(UserDto userDto) {
        User.Builder builder = User.newBuilder()
                .setId(userDto.getId() != null ? userDto.getId() : "")
                .setName(userDto.getName())
                .setEmail(userDto.getEmail())
                .setAge(userDto.getAge())
                .setStatus(UserStatus.valueOf(userDto.getStatus().name()));
        
        if (userDto.getCreatedAt() != null) {
            builder.setCreatedAt(com.google.protobuf.Timestamp.newBuilder()
                    .setSeconds(userDto.getCreatedAt().toEpochSecond(ZoneOffset.UTC))
                    .setNanos(userDto.getCreatedAt().getNano())
                    .build());
        }
        
        if (userDto.getUpdatedAt() != null) {
            builder.setUpdatedAt(com.google.protobuf.Timestamp.newBuilder()
                    .setSeconds(userDto.getUpdatedAt().toEpochSecond(ZoneOffset.UTC))
                    .setNanos(userDto.getUpdatedAt().getNano())
                    .build());
        }
        
        if (userDto.getTags() != null) {
            builder.addAllTags(userDto.getTags());
        }
        
        return builder.build();
    }
}

