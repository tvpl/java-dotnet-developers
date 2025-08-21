package com.learning.micronaut.dto;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;

/**
 * DTO para endereço do usuário.
 * 
 * @author Learning Project
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Introspected
@Serdeable
public class AddressDto {
    
    @NotBlank(message = "Rua é obrigatória")
    private String street;
    
    @NotBlank(message = "Cidade é obrigatória")
    private String city;
    
    @NotBlank(message = "Estado é obrigatório")
    private String state;
    
    @NotBlank(message = "CEP é obrigatório")
    private String zipCode;
    
    @NotBlank(message = "País é obrigatório")
    private String country;
}

