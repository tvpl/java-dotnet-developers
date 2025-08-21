package com.learning.micronaut.entity;

import com.learning.micronaut.dto.UserDto;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.data.annotation.GeneratedValue;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Entidade User para persistência no MongoDB.
 * 
 * Demonstra o uso do Micronaut Data com MongoDB:
 * - @MappedEntity: Marca a classe como entidade persistente (similar ao [Table] no Entity Framework)
 * - @Id: Marca o campo como chave primária
 * - @GeneratedValue: Gera automaticamente o valor do ID
 * - @Version: Controle de versão otimista (similar ao RowVersion no EF)
 * 
 * Comparação com .NET:
 * - Similar às entidades do Entity Framework Core
 * - Lombok elimina a necessidade de propriedades verbosas do C#
 * - MongoDB é NoSQL, então não há relacionamentos como no SQL Server
 * 
 * @author Learning Project
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Introspected
@MappedEntity("users") // Nome da coleção no MongoDB
public class User {
    
    @Id
    @GeneratedValue
    private String id;
    
    private String name;
    
    private String email;
    
    private Integer age;
    
    @Builder.Default
    private UserDto.UserStatus status = UserDto.UserStatus.ACTIVE;
    
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    
    private LocalDateTime updatedAt;
    
    private List<String> tags;
    
    private UserProfile profile;
    
    @Version
    private Long version; // Para controle de concorrência otimista
    
    /**
     * Classe aninhada para perfil do usuário.
     * No MongoDB, objetos aninhados são armazenados como subdocumentos.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Introspected
    public static class UserProfile {
        private String bio;
        private String avatarUrl;
        private Address address;
        private List<SocialLink> socialLinks;
    }
    
    /**
     * Classe aninhada para endereço.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Introspected
    public static class Address {
        private String street;
        private String city;
        private String state;
        private String zipCode;
        private String country;
    }
    
    /**
     * Classe aninhada para links sociais.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Introspected
    public static class SocialLink {
        private String platform;
        private String url;
    }
    
    /**
     * Método para atualizar o timestamp de modificação.
     * Similar aos interceptors do Entity Framework.
     */
    public void updateTimestamp() {
        this.updatedAt = LocalDateTime.now();
    }
}

