package com.learning.micronaut.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para User usado nas APIs HTTP REST.
 * 
 * Demonstra o uso do Lombok para reduzir boilerplate code:
 * - @Data: Gera getters, setters, toString, equals e hashCode
 * - @Builder: Implementa o padrão Builder
 * - @NoArgsConstructor/@AllArgsConstructor: Gera construtores
 * 
 * Comparação com .NET:
 * - Similar a records no C# 9+ ou classes com propriedades automáticas
 * - Lombok elimina a necessidade de escrever getters/setters manualmente
 * - @Introspected é necessário para reflexão no Micronaut (similar ao reflection no .NET)
 * 
 * @author Learning Project
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Introspected
@Serdeable // Habilita serialização/deserialização automática no Micronaut
public class UserDto {
    
    private String id;
    
    @NotBlank(message = "Nome é obrigatório")
    private String name;
    
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ter formato válido")
    private String email;
    
    @NotNull(message = "Idade é obrigatória")
    @Min(value = 0, message = "Idade deve ser maior que zero")
    private Integer age;
    
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
    
    private List<String> tags;
    
    private UserProfileDto profile;
    
    /**
     * Enum para status do usuário.
     * Similar aos enums do C#, mas em Java são mais poderosos
     * pois podem ter métodos e construtores.
     */
    public enum UserStatus {
        ACTIVE("Ativo"),
        INACTIVE("Inativo"),
        SUSPENDED("Suspenso"),
        PENDING("Pendente");
        
        private final String description;
        
        UserStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
}

