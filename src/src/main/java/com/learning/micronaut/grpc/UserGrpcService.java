package com.learning.micronaut.grpc;

import com.learning.micronaut.dto.UserDto;
import com.learning.micronaut.proto.*;
import com.learning.micronaut.service.UserService;
import io.grpc.stub.StreamObserver;
import io.micronaut.grpc.annotation.GrpcService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementação do serviço gRPC para operações de usuário.
 * 
 * Demonstra conceitos importantes do gRPC no Micronaut:
 * - @GrpcService: Marca a classe como serviço gRPC
 * - Implementação da interface gerada pelo protobuf
 * - StreamObserver: Para respostas assíncronas
 * - Conversão entre DTOs Java e mensagens Protobuf
 * 
 * Comparação com .NET:
 * - Similar aos gRPC Services no ASP.NET Core
 * - StreamObserver é similar ao ServerCallContext
 * - Protobuf messages são similares aos generated types no .NET
 * - Mais eficiente que REST para comunicação entre serviços
 * 
 * Vantagens do gRPC sobre REST:
 * - Serialização binária (mais rápida e menor)
 * - HTTP/2 (multiplexing, server push)
 * - Contratos fortemente tipados
 * - Streaming bidirecional
 * - Geração automática de clientes
 * 
 * @author Learning Project
 */
@GrpcService // Marca como serviço gRPC - será registrado automaticamente
@RequiredArgsConstructor
@Slf4j
public class UserGrpcService extends UserServiceGrpc.UserServiceImplBase {
    
    private final UserService userService;
    
    /**
     * Cria um novo usuário via gRPC.
     * 
     * Equivalente no .NET gRPC:
     * public override async Task<CreateUserResponse> CreateUser(CreateUserRequest request, ServerCallContext context)
     */
    @Override
    public void createUser(CreateUserRequest request, StreamObserver<CreateUserResponse> responseObserver) {
        log.info("Criando usuário via gRPC: {}", request.getEmail());
        
        try {
            // Converte Protobuf para DTO
            UserDto userDto = protoToDto(request);
            
            // Chama o serviço de negócio
            userService.createUser(userDto)
                    .subscribe(
                        createdUser -> {
                            // Converte DTO para Protobuf e envia resposta
                            CreateUserResponse response = CreateUserResponse.newBuilder()
                                    .setUser(dtoToProto(createdUser))
                                    .setSuccess(true)
                                    .setMessage("Usuário criado com sucesso")
                                    .build();
                            
                            responseObserver.onNext(response);
                            responseObserver.onCompleted();
                            
                            log.info("Usuário criado via gRPC: {}", createdUser.getId());
                        },
                        error -> {
                            log.error("Erro ao criar usuário via gRPC: {}", error.getMessage());
                            
                            CreateUserResponse errorResponse = CreateUserResponse.newBuilder()
                                    .setSuccess(false)
                                    .setMessage(error.getMessage())
                                    .build();
                            
                            responseObserver.onNext(errorResponse);
                            responseObserver.onCompleted();
                        }
                    );
        } catch (Exception e) {
            log.error("Erro inesperado ao criar usuário via gRPC: {}", e.getMessage());
            responseObserver.onError(e);
        }
    }
    
    /**
     * Busca usuário por ID via gRPC.
     */
    @Override
    public void getUser(GetUserRequest request, StreamObserver<GetUserResponse> responseObserver) {
        log.info("Buscando usuário via gRPC: {}", request.getId());
        
        try {
            userService.findById(request.getId())
                    .subscribe(
                        user -> {
                            GetUserResponse response = GetUserResponse.newBuilder()
                                    .setUser(dtoToProto(user))
                                    .setFound(true)
                                    .build();
                            
                            responseObserver.onNext(response);
                            responseObserver.onCompleted();
                        },
                        error -> {
                            log.error("Erro ao buscar usuário via gRPC: {}", error.getMessage());
                            responseObserver.onError(error);
                        },
                        () -> {
                            // Usuário não encontrado
                            GetUserResponse response = GetUserResponse.newBuilder()
                                    .setFound(false)
                                    .build();
                            
                            responseObserver.onNext(response);
                            responseObserver.onCompleted();
                        }
                    );
        } catch (Exception e) {
            log.error("Erro inesperado ao buscar usuário via gRPC: {}", e.getMessage());
            responseObserver.onError(e);
        }
    }
    
    /**
     * Atualiza usuário via gRPC.
     */
    @Override
    public void updateUser(UpdateUserRequest request, StreamObserver<UpdateUserResponse> responseObserver) {
        log.info("Atualizando usuário via gRPC: {}", request.getId());
        
        try {
            UserDto userDto = protoToDto(request.getUser());
            
            userService.updateUser(request.getId(), userDto)
                    .subscribe(
                        updatedUser -> {
                            UpdateUserResponse response = UpdateUserResponse.newBuilder()
                                    .setUser(dtoToProto(updatedUser))
                                    .setSuccess(true)
                                    .setMessage("Usuário atualizado com sucesso")
                                    .build();
                            
                            responseObserver.onNext(response);
                            responseObserver.onCompleted();
                        },
                        error -> {
                            log.error("Erro ao atualizar usuário via gRPC: {}", error.getMessage());
                            
                            UpdateUserResponse errorResponse = UpdateUserResponse.newBuilder()
                                    .setSuccess(false)
                                    .setMessage(error.getMessage())
                                    .build();
                            
                            responseObserver.onNext(errorResponse);
                            responseObserver.onCompleted();
                        }
                    );
        } catch (Exception e) {
            log.error("Erro inesperado ao atualizar usuário via gRPC: {}", e.getMessage());
            responseObserver.onError(e);
        }
    }
    
    /**
     * Remove usuário via gRPC.
     */
    @Override
    public void deleteUser(DeleteUserRequest request, StreamObserver<DeleteUserResponse> responseObserver) {
        log.info("Removendo usuário via gRPC: {}", request.getId());
        
        try {
            userService.deleteUser(request.getId())
                    .subscribe(
                        () -> {
                            DeleteUserResponse response = DeleteUserResponse.newBuilder()
                                    .setSuccess(true)
                                    .setMessage("Usuário removido com sucesso")
                                    .build();
                            
                            responseObserver.onNext(response);
                            responseObserver.onCompleted();
                        },
                        error -> {
                            log.error("Erro ao remover usuário via gRPC: {}", error.getMessage());
                            
                            DeleteUserResponse errorResponse = DeleteUserResponse.newBuilder()
                                    .setSuccess(false)
                                    .setMessage(error.getMessage())
                                    .build();
                            
                            responseObserver.onNext(errorResponse);
                            responseObserver.onCompleted();
                        }
                    );
        } catch (Exception e) {
            log.error("Erro inesperado ao remover usuário via gRPC: {}", e.getMessage());
            responseObserver.onError(e);
        }
    }
    
    /**
     * Lista usuários com paginação via gRPC.
     */
    @Override
    public void listUsers(ListUsersRequest request, StreamObserver<ListUsersResponse> responseObserver) {
        log.info("Listando usuários via gRPC - página: {}, tamanho: {}", request.getPage(), request.getSize());
        
        try {
            // Para simplificar, vamos buscar todos e aplicar paginação manualmente
            // Em produção, seria melhor implementar paginação no repositório
            userService.findAll(null)
                    .collectList()
                    .subscribe(
                        users -> {
                            // Aplicar filtro se fornecido
                            List<UserDto> filteredUsers = users;
                            if (!request.getFilter().isEmpty()) {
                                filteredUsers = users.stream()
                                        .filter(user -> user.getName().toLowerCase()
                                                .contains(request.getFilter().toLowerCase()))
                                        .collect(Collectors.toList());
                            }
                            
                            // Aplicar paginação
                            int page = request.getPage();
                            int size = request.getSize();
                            int start = page * size;
                            int end = Math.min(start + size, filteredUsers.size());
                            
                            List<UserDto> pagedUsers = filteredUsers.subList(start, end);
                            
                            // Converter para Protobuf
                            List<User> protoUsers = pagedUsers.stream()
                                    .map(this::dtoToProto)
                                    .collect(Collectors.toList());
                            
                            ListUsersResponse response = ListUsersResponse.newBuilder()
                                    .addAllUsers(protoUsers)
                                    .setTotal(filteredUsers.size())
                                    .setPage(page)
                                    .setSize(size)
                                    .build();
                            
                            responseObserver.onNext(response);
                            responseObserver.onCompleted();
                        },
                        error -> {
                            log.error("Erro ao listar usuários via gRPC: {}", error.getMessage());
                            responseObserver.onError(error);
                        }
                    );
        } catch (Exception e) {
            log.error("Erro inesperado ao listar usuários via gRPC: {}", e.getMessage());
            responseObserver.onError(e);
        }
    }
    
    /**
     * Converte CreateUserRequest (Protobuf) para UserDto.
     */
    private UserDto protoToDto(CreateUserRequest request) {
        UserDto.UserDtoBuilder builder = UserDto.builder()
                .name(request.getName())
                .email(request.getEmail())
                .age(request.getAge());
        
        if (request.hasProfile()) {
            builder.profile(protoToDto(request.getProfile()));
        }
        
        return builder.build();
    }
    
    /**
     * Converte User (Protobuf) para UserDto.
     */
    private UserDto protoToDto(User protoUser) {
        UserDto.UserDtoBuilder builder = UserDto.builder()
                .id(protoUser.getId())
                .name(protoUser.getName())
                .email(protoUser.getEmail())
                .age(protoUser.getAge())
                .status(UserDto.UserStatus.valueOf(protoUser.getStatus().name()));
        
        if (protoUser.hasCreatedAt()) {
            builder.createdAt(LocalDateTime.ofEpochSecond(
                    protoUser.getCreatedAt().getSeconds(),
                    protoUser.getCreatedAt().getNanos(),
                    ZoneOffset.UTC));
        }
        
        if (protoUser.hasUpdatedAt()) {
            builder.updatedAt(LocalDateTime.ofEpochSecond(
                    protoUser.getUpdatedAt().getSeconds(),
                    protoUser.getUpdatedAt().getNanos(),
                    ZoneOffset.UTC));
        }
        
        if (!protoUser.getTagsList().isEmpty()) {
            builder.tags(protoUser.getTagsList());
        }
        
        if (protoUser.hasProfile()) {
            builder.profile(protoToDto(protoUser.getProfile()));
        }
        
        return builder.build();
    }
    
    /**
     * Converte UserDto para User (Protobuf).
     */
    private User dtoToProto(UserDto userDto) {
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
        
        if (userDto.getProfile() != null) {
            builder.setProfile(dtoToProto(userDto.getProfile()));
        }
        
        return builder.build();
    }
    
    // Métodos auxiliares para conversão de objetos aninhados...
    // (implementação similar aos métodos acima)
    
    private com.learning.micronaut.dto.UserProfileDto protoToDto(UserProfile protoProfile) {
        // Implementação da conversão do perfil
        return com.learning.micronaut.dto.UserProfileDto.builder()
                .bio(protoProfile.getBio())
                .avatarUrl(protoProfile.getAvatarUrl())
                .build();
    }
    
    private UserProfile dtoToProto(com.learning.micronaut.dto.UserProfileDto profileDto) {
        // Implementação da conversão do perfil
        return UserProfile.newBuilder()
                .setBio(profileDto.getBio() != null ? profileDto.getBio() : "")
                .setAvatarUrl(profileDto.getAvatarUrl() != null ? profileDto.getAvatarUrl() : "")
                .build();
    }
}

