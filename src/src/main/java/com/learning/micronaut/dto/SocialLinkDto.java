package com.learning.micronaut.dto;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

/**
 * DTO para links de redes sociais.
 * 
 * @author Learning Project
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Introspected
@Serdeable
public class SocialLinkDto {
    
    @NotBlank(message = "Plataforma é obrigatória")
    private String platform;
    
    @NotBlank(message = "URL é obrigatória")
    @Pattern(regexp = "^https?://.*", message = "URL deve começar com http:// ou https://")
    private String url;
}

