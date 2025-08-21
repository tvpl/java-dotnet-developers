package com.learning.micronaut.dto;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.Valid;
import java.util.List;

/**
 * DTO para perfil do usuário.
 * 
 * Demonstra composição de DTOs e validação aninhada com @Valid.
 * 
 * @author Learning Project
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Introspected
@Serdeable
public class UserProfileDto {
    
    private String bio;
    
    private String avatarUrl;
    
    @Valid // Valida o objeto aninhado
    private AddressDto address;
    
    @Valid
    private List<SocialLinkDto> socialLinks;
}

