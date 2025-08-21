package com.learning.micronaut.service;

import com.learning.micronaut.dto.UserDto;
import com.learning.micronaut.entity.User;
import com.learning.micronaut.repository.UserRepository;
import io.micronaut.data.model.Page;
import io.micronaut.data.model.Pageable;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Serviço para operações de negócio relacionadas ao User.
 * 
 * Demonstra conceitos importantes do Micronaut e Java:
 * - @Singleton: Marca a classe como singleton (similar ao AddSingleton no .NET)
 * - @RequiredArgsConstructor (Lombok): Gera construtor com campos final (injeção por construtor)
 * - Reactive Programming: Uso do Reactor (Mono/Flux) para operações assíncronas
 * - Mapeamento entre DTOs e Entidades
 * 
 * Comparação com .NET:
 * - Similar aos Services no ASP.NET Core com injeção de dependência
 * - Mono<T> é similar ao Task<T> (operação assíncrona que retorna um valor)
 * - Flux<T> é similar ao IAsyncEnumerable<T> (stream assíncrono de valores)
 * - @Singleton é similar ao services.AddSingleton<T>()
 * 
 * @author Learning Project
 */
@Singleton // Marca como singleton - uma única instância será criada e reutilizada
@RequiredArgsConstructor // Lombok gera construtor com todos os campos final
@Slf4j // Lombok gera logger estático
public class UserService {
    
    // Injeção de dependência por construtor (imutável e testável)
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    
    /**
     * Cria um novo usuário.
     * 
     * @param userDto DTO com dados do usuário
     * @return Mono com o usuário criado
     */
    public Mono<UserDto> createUser(UserDto userDto) {
        log.debug("Criando usuário: {}", userDto.getEmail());
        
        return Mono.fromCallable(() -> {
                    // Verifica se email já existe
                    return userRepository.existsByEmail(userDto.getEmail());
                })
                .flatMap(exists -> {
                    if (Boolean.TRUE.equals(exists)) {
                        return Mono.error(new IllegalArgumentException("Email já está em uso"));
                    }
                    
                    // Mapeia DTO para entidade
                    User user = userMapper.toEntity(userDto);
                    user.setCreatedAt(LocalDateTime.now());
                    
                    // Salva no banco
                    return Mono.from(userRepository.save(user))
                            .map(userMapper::toDto)
                            .doOnSuccess(savedUser -> 
                                log.info("Usuário criado com sucesso: {}", savedUser.getId()));
                });
    }
    
    /**
     * Busca usuário por ID.
     * 
     * @param id ID do usuário
     * @return Mono com o usuário encontrado ou vazio
     */
    public Mono<UserDto> findById(String id) {
        log.debug("Buscando usuário por ID: {}", id);
        
        return Mono.from(userRepository.findById(id))
                .map(userMapper::toDto)
                .doOnSuccess(user -> log.debug("Usuário encontrado: {}", user.getEmail()))
                .doOnError(error -> log.error("Erro ao buscar usuário: {}", error.getMessage()));
    }
    
    /**
     * Busca usuário por email.
     * 
     * @param email Email do usuário
     * @return Mono com o usuário encontrado ou vazio
     */
    public Mono<UserDto> findByEmail(String email) {
        log.debug("Buscando usuário por email: {}", email);
        
        return Mono.from(userRepository.findByEmail(email))
                .map(userMapper::toDto);
    }
    
    /**
     * Lista todos os usuários com paginação.
     * 
     * @param pageable Informações de paginação
     * @return Flux com os usuários
     */
    public Flux<UserDto> findAll(Pageable pageable) {
        log.debug("Listando usuários - página: {}, tamanho: {}", 
                 pageable.getNumber(), pageable.getSize());
        
        return Flux.from(userRepository.findAll())
                .map(userMapper::toDto)
                .skip(pageable.getOffset())
                .take(pageable.getSize());
    }
    
    /**
     * Busca usuários por status.
     * 
     * @param status Status do usuário
     * @return Flux com os usuários encontrados
     */
    public Flux<UserDto> findByStatus(UserDto.UserStatus status) {
        log.debug("Buscando usuários por status: {}", status);
        
        return Flux.from(userRepository.findByStatus(status))
                .map(userMapper::toDto);
    }
    
    /**
     * Atualiza um usuário existente.
     * 
     * @param id ID do usuário
     * @param userDto DTO com novos dados
     * @return Mono com o usuário atualizado
     */
    public Mono<UserDto> updateUser(String id, UserDto userDto) {
        log.debug("Atualizando usuário: {}", id);
        
        return Mono.from(userRepository.findById(id))
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Usuário não encontrado")))
                .map(existingUser -> {
                    // Atualiza campos
                    existingUser.setName(userDto.getName());
                    existingUser.setEmail(userDto.getEmail());
                    existingUser.setAge(userDto.getAge());
                    existingUser.setStatus(userDto.getStatus());
                    existingUser.setTags(userDto.getTags());
                    existingUser.updateTimestamp();
                    
                    // Atualiza perfil se fornecido
                    if (userDto.getProfile() != null) {
                        existingUser.setProfile(userMapper.toEntityProfile(userDto.getProfile()));
                    }
                    
                    return existingUser;
                })
                .flatMap(user -> Mono.from(userRepository.update(user)))
                .map(userMapper::toDto)
                .doOnSuccess(updatedUser -> 
                    log.info("Usuário atualizado com sucesso: {}", updatedUser.getId()));
    }
    
    /**
     * Remove um usuário.
     * 
     * @param id ID do usuário
     * @return Mono<Void> indicando conclusão
     */
    public Mono<Void> deleteUser(String id) {
        log.debug("Removendo usuário: {}", id);
        
        return Mono.from(userRepository.existsById(id))
                .filter(exists -> exists)
                .switchIfEmpty(Mono.error(new IllegalArgumentException("Usuário não encontrado")))
                .then(Mono.from(userRepository.deleteById(id)))
                .then()
                .doOnSuccess(v -> log.info("Usuário removido com sucesso: {}", id));
    }
    
    /**
     * Busca usuários por nome (busca parcial).
     * 
     * @param name Nome ou parte do nome
     * @return Flux com usuários encontrados
     */
    public Flux<UserDto> searchByName(String name) {
        log.debug("Buscando usuários por nome: {}", name);
        
        return Flux.from(userRepository.findByNameContainingIgnoreCase(name))
                .map(userMapper::toDto);
    }
    
    /**
     * Conta usuários por status.
     * 
     * @param status Status do usuário
     * @return Mono com a contagem
     */
    public Mono<Long> countByStatus(UserDto.UserStatus status) {
        return Mono.from(userRepository.countByStatus(status));
    }
    
    /**
     * Busca usuários por tags.
     * 
     * @param tags Lista de tags
     * @return Flux com usuários que possuem pelo menos uma das tags
     */
    public Flux<UserDto> findByTags(List<String> tags) {
        log.debug("Buscando usuários por tags: {}", tags);
        
        return Flux.from(userRepository.findByTagsIn(tags))
                .map(userMapper::toDto);
    }
}

