package com.learning.micronaut.repository;

import com.learning.micronaut.dto.UserDto;
import com.learning.micronaut.entity.User;
import io.micronaut.data.mongodb.annotation.MongoRepository;
import io.micronaut.data.repository.reactive.ReactiveStreamsCrudRepository;
import org.reactivestreams.Publisher;

import java.util.List;

/**
 * Repositório para operações de dados do User usando MongoDB.
 * 
 * Demonstra o uso do Micronaut Data com MongoDB:
 * - @MongoRepository: Marca como repositório MongoDB
 * - ReactiveStreamsCrudRepository: Interface reativa para operações CRUD
 * - Query methods: Métodos que são implementados automaticamente baseados no nome
 * 
 * Comparação com .NET:
 * - Similar ao IRepository<T> ou DbContext no Entity Framework
 * - Reactive Streams é similar ao IAsyncEnumerable no .NET
 * - Query methods são similares aos LINQ expressions
 * 
 * Principais diferenças:
 * - Java usa Reactive Streams (Publisher<T>) ao invés de Task<T>
 * - Métodos de query são inferidos pelo nome (findByEmail, findByStatus, etc.)
 * - Não há necessidade de implementar a interface, o Micronaut gera automaticamente
 * 
 * @author Learning Project
 */
@MongoRepository // Anotação que marca esta interface como um repositório MongoDB
public interface UserRepository extends ReactiveStreamsCrudRepository<User, String> {
    
    /**
     * Busca usuário por email.
     * O Micronaut Data gera automaticamente a implementação baseada no nome do método.
     * 
     * Equivalente no .NET: await context.Users.FirstOrDefaultAsync(u => u.Email == email)
     */
    Publisher<User> findByEmail(String email);
    
    /**
     * Busca usuários por status.
     * 
     * Equivalente no .NET: await context.Users.Where(u => u.Status == status).ToListAsync()
     */
    Publisher<User> findByStatus(UserDto.UserStatus status);
    
    /**
     * Busca usuários por idade maior que o valor especificado.
     * 
     * Equivalente no .NET: await context.Users.Where(u => u.Age > age).ToListAsync()
     */
    Publisher<User> findByAgeGreaterThan(Integer age);
    
    /**
     * Busca usuários por nome contendo o texto especificado (case insensitive).
     * 
     * Equivalente no .NET: await context.Users.Where(u => u.Name.Contains(name)).ToListAsync()
     */
    Publisher<User> findByNameContainingIgnoreCase(String name);
    
    /**
     * Busca usuários por lista de tags.
     * No MongoDB, este método busca documentos onde o array 'tags' contém qualquer um dos valores.
     * 
     * Equivalente no .NET com MongoDB: await collection.Find(u => u.Tags.Any(t => tags.Contains(t))).ToListAsync()
     */
    Publisher<User> findByTagsIn(List<String> tags);
    
    /**
     * Conta usuários por status.
     * 
     * Equivalente no .NET: await context.Users.CountAsync(u => u.Status == status)
     */
    Publisher<Long> countByStatus(UserDto.UserStatus status);
    
    /**
     * Verifica se existe usuário com o email especificado.
     * 
     * Equivalente no .NET: await context.Users.AnyAsync(u => u.Email == email)
     */
    Publisher<Boolean> existsByEmail(String email);
    
    /**
     * Busca usuários ordenados por data de criação (mais recentes primeiro).
     * 
     * Equivalente no .NET: await context.Users.OrderByDescending(u => u.CreatedAt).ToListAsync()
     */
    Publisher<User> findAllByOrderByCreatedAtDesc();
    
    /**
     * Remove usuários por status.
     * 
     * Equivalente no .NET: 
     * var users = await context.Users.Where(u => u.Status == status).ToListAsync();
     * context.Users.RemoveRange(users);
     * await context.SaveChangesAsync();
     */
    Publisher<Long> deleteByStatus(UserDto.UserStatus status);
}

