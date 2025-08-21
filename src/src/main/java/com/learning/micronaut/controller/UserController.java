package com.learning.micronaut.controller;

import com.learning.micronaut.dto.ApiResponse;
import com.learning.micronaut.dto.UserDto;
import com.learning.micronaut.service.UserService;
import io.micronaut.data.model.Pageable;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.*;
import io.micronaut.validation.Validated;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import java.util.List;

/**
 * Controller REST para operações de usuário.
 * 
 * Demonstra conceitos importantes do Micronaut HTTP:
 * - @Controller: Marca a classe como controller REST
 * - @Get, @Post, @Put, @Delete: Mapeamento de métodos HTTP
 * - @Body: Binding do corpo da requisição
 * - @PathVariable: Parâmetros da URL
 * - @QueryValue: Parâmetros de query string
 * - @Valid: Validação automática de DTOs
 * - HttpResponse: Controle de status codes e headers
 * 
 * Comparação com .NET:
 * - Similar aos Controllers no ASP.NET Core
 * - @Controller é similar ao [ApiController]
 * - @Get/@Post/etc são similares ao [HttpGet]/[HttpPost]/etc
 * - @Body é similar ao [FromBody]
 * - @PathVariable é similar ao [FromRoute]
 * - @QueryValue é similar ao [FromQuery]
 * - Reactive types (Mono/Flux) são similares ao Task<T>/IAsyncEnumerable<T>
 * 
 * @author Learning Project
 */
@Controller("/api/users") // Base path para todas as rotas
@RequiredArgsConstructor
@Slf4j
@Validated // Habilita validação automática
public class UserController {
    
    private final UserService userService;
    
    /**
     * Cria um novo usuário.
     * 
     * POST /api/users
     * 
     * Equivalente no .NET:
     * [HttpPost]
     * public async Task<ActionResult<ApiResponse<UserDto>>> CreateUser([FromBody] UserDto userDto)
     */
    @Post
    public Mono<HttpResponse<ApiResponse<UserDto>>> createUser(@Body @Valid UserDto userDto) {
        log.info("Criando usuário: {}", userDto.getEmail());
        
        return userService.createUser(userDto)
                .map(createdUser -> {
                    ApiResponse<UserDto> response = ApiResponse.success(
                        createdUser, 
                        "Usuário criado com sucesso"
                    );
                    return HttpResponse.status(HttpStatus.CREATED).body(response);
                })
                .onErrorReturn(error -> {
                    log.error("Erro ao criar usuário: {}", error.getMessage());
                    ApiResponse<UserDto> errorResponse = ApiResponse.error(error.getMessage());
                    return HttpResponse.status(HttpStatus.BAD_REQUEST).body(errorResponse);
                });
    }
    
    /**
     * Busca usuário por ID.
     * 
     * GET /api/users/{id}
     * 
     * Equivalente no .NET:
     * [HttpGet("{id}")]
     * public async Task<ActionResult<ApiResponse<UserDto>>> GetUser(string id)
     */
    @Get("/{id}")
    public Mono<HttpResponse<ApiResponse<UserDto>>> getUser(@PathVariable String id) {
        log.info("Buscando usuário por ID: {}", id);
        
        return userService.findById(id)
                .map(user -> {
                    ApiResponse<UserDto> response = ApiResponse.success(user);
                    return HttpResponse.ok(response);
                })
                .switchIfEmpty(Mono.fromCallable(() -> {
                    ApiResponse<UserDto> response = ApiResponse.error("Usuário não encontrado");
                    return HttpResponse.status(HttpStatus.NOT_FOUND).body(response);
                }));
    }
    
    /**
     * Lista usuários com paginação.
     * 
     * GET /api/users?page=0&size=10&status=ACTIVE
     * 
     * Equivalente no .NET:
     * [HttpGet]
     * public async Task<ActionResult<ApiResponse<List<UserDto>>>> GetUsers(
     *     [FromQuery] int page = 0, 
     *     [FromQuery] int size = 10,
     *     [FromQuery] string status = null)
     */
    @Get
    public Mono<HttpResponse<ApiResponse<List<UserDto>>>> getUsers(
            @QueryValue(defaultValue = "0") int page,
            @QueryValue(defaultValue = "10") int size,
            @QueryValue(required = false) UserDto.UserStatus status) {
        
        log.info("Listando usuários - página: {}, tamanho: {}, status: {}", page, size, status);
        
        Pageable pageable = Pageable.from(page, size);
        
        Flux<UserDto> usersFlux = status != null 
            ? userService.findByStatus(status)
            : userService.findAll(pageable);
        
        return usersFlux
                .collectList() // Converte Flux<UserDto> para Mono<List<UserDto>>
                .map(users -> {
                    ApiResponse<List<UserDto>> response = ApiResponse.success(users);
                    return HttpResponse.ok(response);
                })
                .onErrorReturn(error -> {
                    log.error("Erro ao listar usuários: {}", error.getMessage());
                    ApiResponse<List<UserDto>> errorResponse = ApiResponse.error(error.getMessage());
                    return HttpResponse.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
                });
    }
    
    /**
     * Atualiza um usuário existente.
     * 
     * PUT /api/users/{id}
     * 
     * Equivalente no .NET:
     * [HttpPut("{id}")]
     * public async Task<ActionResult<ApiResponse<UserDto>>> UpdateUser(string id, [FromBody] UserDto userDto)
     */
    @Put("/{id}")
    public Mono<HttpResponse<ApiResponse<UserDto>>> updateUser(
            @PathVariable String id, 
            @Body @Valid UserDto userDto) {
        
        log.info("Atualizando usuário: {}", id);
        
        return userService.updateUser(id, userDto)
                .map(updatedUser -> {
                    ApiResponse<UserDto> response = ApiResponse.success(
                        updatedUser, 
                        "Usuário atualizado com sucesso"
                    );
                    return HttpResponse.ok(response);
                })
                .onErrorReturn(error -> {
                    log.error("Erro ao atualizar usuário: {}", error.getMessage());
                    HttpStatus status = error instanceof IllegalArgumentException 
                        ? HttpStatus.NOT_FOUND 
                        : HttpStatus.BAD_REQUEST;
                    ApiResponse<UserDto> errorResponse = ApiResponse.error(error.getMessage());
                    return HttpResponse.status(status).body(errorResponse);
                });
    }
    
    /**
     * Remove um usuário.
     * 
     * DELETE /api/users/{id}
     * 
     * Equivalente no .NET:
     * [HttpDelete("{id}")]
     * public async Task<ActionResult<ApiResponse<object>>> DeleteUser(string id)
     */
    @Delete("/{id}")
    public Mono<HttpResponse<ApiResponse<Object>>> deleteUser(@PathVariable String id) {
        log.info("Removendo usuário: {}", id);
        
        return userService.deleteUser(id)
                .then(Mono.fromCallable(() -> {
                    ApiResponse<Object> response = ApiResponse.success(
                        null, 
                        "Usuário removido com sucesso"
                    );
                    return HttpResponse.ok(response);
                }))
                .onErrorReturn(error -> {
                    log.error("Erro ao remover usuário: {}", error.getMessage());
                    HttpStatus status = error instanceof IllegalArgumentException 
                        ? HttpStatus.NOT_FOUND 
                        : HttpStatus.INTERNAL_SERVER_ERROR;
                    ApiResponse<Object> errorResponse = ApiResponse.error(error.getMessage());
                    return HttpResponse.status(status).body(errorResponse);
                });
    }
    
    /**
     * Busca usuários por nome.
     * 
     * GET /api/users/search?name=João
     * 
     * Demonstra endpoint de busca com parâmetros de query.
     */
    @Get("/search")
    public Mono<HttpResponse<ApiResponse<List<UserDto>>>> searchUsers(
            @QueryValue @NotBlank String name) {
        
        log.info("Buscando usuários por nome: {}", name);
        
        return userService.searchByName(name)
                .collectList()
                .map(users -> {
                    ApiResponse<List<UserDto>> response = ApiResponse.success(users);
                    return HttpResponse.ok(response);
                });
    }
    
    /**
     * Busca usuário por email.
     * 
     * GET /api/users/by-email?email=user@example.com
     * 
     * Demonstra endpoint específico com validação de email.
     */
    @Get("/by-email")
    public Mono<HttpResponse<ApiResponse<UserDto>>> getUserByEmail(
            @QueryValue @NotBlank String email) {
        
        log.info("Buscando usuário por email: {}", email);
        
        return userService.findByEmail(email)
                .map(user -> {
                    ApiResponse<UserDto> response = ApiResponse.success(user);
                    return HttpResponse.ok(response);
                })
                .switchIfEmpty(Mono.fromCallable(() -> {
                    ApiResponse<UserDto> response = ApiResponse.error("Usuário não encontrado");
                    return HttpResponse.status(HttpStatus.NOT_FOUND).body(response);
                }));
    }
    
    /**
     * Busca usuários por tags.
     * 
     * GET /api/users/by-tags?tags=java,spring,microservices
     * 
     * Demonstra endpoint com parâmetros de array.
     */
    @Get("/by-tags")
    public Mono<HttpResponse<ApiResponse<List<UserDto>>>> getUsersByTags(
            @QueryValue List<String> tags) {
        
        log.info("Buscando usuários por tags: {}", tags);
        
        return userService.findByTags(tags)
                .collectList()
                .map(users -> {
                    ApiResponse<List<UserDto>> response = ApiResponse.success(users);
                    return HttpResponse.ok(response);
                });
    }
    
    /**
     * Endpoint de health check específico para usuários.
     * 
     * GET /api/users/health
     * 
     * Demonstra endpoint simples para monitoramento.
     */
    @Get("/health")
    public Mono<HttpResponse<ApiResponse<Object>>> health() {
        return userService.countByStatus(UserDto.UserStatus.ACTIVE)
                .map(activeUsers -> {
                    ApiResponse<Object> response = ApiResponse.success(
                        null, 
                        String.format("Serviço funcionando. %d usuários ativos", activeUsers)
                    );
                    return HttpResponse.ok(response);
                });
    }
}

