package com.learning.micronaut.service;

import com.learning.micronaut.dto.AddressDto;
import com.learning.micronaut.dto.SocialLinkDto;
import com.learning.micronaut.dto.UserDto;
import com.learning.micronaut.dto.UserProfileDto;
import com.learning.micronaut.entity.User;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper para conversão entre DTOs e Entidades.
 * 
 * Demonstra padrões de mapeamento em Java:
 * - Conversão manual entre objetos (similar ao AutoMapper no .NET)
 * - Tratamento de objetos nulos
 * - Mapeamento de listas e objetos aninhados
 * 
 * Comparação com .NET:
 * - Similar ao AutoMapper ou mapeamento manual no .NET
 * - Em Java, geralmente usa-se MapStruct ou mapeamento manual
 * - Stream API para mapeamento de listas (similar ao LINQ Select)
 * 
 * Alternativas em Java:
 * - MapStruct: Geração automática de mappers em tempo de compilação
 * - ModelMapper: Mapeamento automático em tempo de execução
 * - Mapeamento manual: Mais controle, mas mais código
 * 
 * @author Learning Project
 */
@Singleton
@Slf4j
public class UserMapper {
    
    /**
     * Converte DTO para Entidade.
     * 
     * @param dto DTO do usuário
     * @return Entidade do usuário
     */
    public User toEntity(UserDto dto) {
        if (dto == null) {
            return null;
        }
        
        log.debug("Convertendo UserDto para User entity: {}", dto.getEmail());
        
        return User.builder()
                .id(dto.getId())
                .name(dto.getName())
                .email(dto.getEmail())
                .age(dto.getAge())
                .status(dto.getStatus())
                .createdAt(dto.getCreatedAt())
                .updatedAt(dto.getUpdatedAt())
                .tags(dto.getTags())
                .profile(toEntityProfile(dto.getProfile()))
                .build();
    }
    
    /**
     * Converte Entidade para DTO.
     * 
     * @param entity Entidade do usuário
     * @return DTO do usuário
     */
    public UserDto toDto(User entity) {
        if (entity == null) {
            return null;
        }
        
        log.debug("Convertendo User entity para UserDto: {}", entity.getEmail());
        
        return UserDto.builder()
                .id(entity.getId())
                .name(entity.getName())
                .email(entity.getEmail())
                .age(entity.getAge())
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .tags(entity.getTags())
                .profile(toDtoProfile(entity.getProfile()))
                .build();
    }
    
    /**
     * Converte DTO Profile para Entity Profile.
     * 
     * @param profileDto DTO do perfil
     * @return Entidade do perfil
     */
    public User.UserProfile toEntityProfile(UserProfileDto profileDto) {
        if (profileDto == null) {
            return null;
        }
        
        return User.UserProfile.builder()
                .bio(profileDto.getBio())
                .avatarUrl(profileDto.getAvatarUrl())
                .address(toEntityAddress(profileDto.getAddress()))
                .socialLinks(toEntitySocialLinks(profileDto.getSocialLinks()))
                .build();
    }
    
    /**
     * Converte Entity Profile para DTO Profile.
     * 
     * @param profile Entidade do perfil
     * @return DTO do perfil
     */
    public UserProfileDto toDtoProfile(User.UserProfile profile) {
        if (profile == null) {
            return null;
        }
        
        return UserProfileDto.builder()
                .bio(profile.getBio())
                .avatarUrl(profile.getAvatarUrl())
                .address(toDtoAddress(profile.getAddress()))
                .socialLinks(toDtoSocialLinks(profile.getSocialLinks()))
                .build();
    }
    
    /**
     * Converte DTO Address para Entity Address.
     */
    private User.Address toEntityAddress(AddressDto addressDto) {
        if (addressDto == null) {
            return null;
        }
        
        return User.Address.builder()
                .street(addressDto.getStreet())
                .city(addressDto.getCity())
                .state(addressDto.getState())
                .zipCode(addressDto.getZipCode())
                .country(addressDto.getCountry())
                .build();
    }
    
    /**
     * Converte Entity Address para DTO Address.
     */
    private AddressDto toDtoAddress(User.Address address) {
        if (address == null) {
            return null;
        }
        
        return AddressDto.builder()
                .street(address.getStreet())
                .city(address.getCity())
                .state(address.getState())
                .zipCode(address.getZipCode())
                .country(address.getCountry())
                .build();
    }
    
    /**
     * Converte lista de DTO SocialLinks para Entity SocialLinks.
     * 
     * Demonstra o uso da Stream API para mapeamento de listas.
     * Similar ao LINQ Select no .NET: list.Select(item => mapper(item)).ToList()
     */
    private List<User.SocialLink> toEntitySocialLinks(List<SocialLinkDto> socialLinkDtos) {
        if (socialLinkDtos == null) {
            return null;
        }
        
        return socialLinkDtos.stream()
                .map(this::toEntitySocialLink)
                .collect(Collectors.toList());
    }
    
    /**
     * Converte lista de Entity SocialLinks para DTO SocialLinks.
     */
    private List<SocialLinkDto> toDtoSocialLinks(List<User.SocialLink> socialLinks) {
        if (socialLinks == null) {
            return null;
        }
        
        return socialLinks.stream()
                .map(this::toDtoSocialLink)
                .collect(Collectors.toList());
    }
    
    /**
     * Converte DTO SocialLink para Entity SocialLink.
     */
    private User.SocialLink toEntitySocialLink(SocialLinkDto socialLinkDto) {
        if (socialLinkDto == null) {
            return null;
        }
        
        return User.SocialLink.builder()
                .platform(socialLinkDto.getPlatform())
                .url(socialLinkDto.getUrl())
                .build();
    }
    
    /**
     * Converte Entity SocialLink para DTO SocialLink.
     */
    private SocialLinkDto toDtoSocialLink(User.SocialLink socialLink) {
        if (socialLink == null) {
            return null;
        }
        
        return SocialLinkDto.builder()
                .platform(socialLink.getPlatform())
                .url(socialLink.getUrl())
                .build();
    }
    
    /**
     * Converte lista de DTOs para lista de Entidades.
     * 
     * Método utilitário para conversão em lote.
     */
    public List<User> toEntities(List<UserDto> dtos) {
        if (dtos == null) {
            return null;
        }
        
        return dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());
    }
    
    /**
     * Converte lista de Entidades para lista de DTOs.
     * 
     * Método utilitário para conversão em lote.
     */
    public List<UserDto> toDtos(List<User> entities) {
        if (entities == null) {
            return null;
        }
        
        return entities.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}

