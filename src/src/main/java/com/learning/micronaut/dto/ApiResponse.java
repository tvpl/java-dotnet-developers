package com.learning.micronaut.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO genérico para responses padronizadas da API.
 * 
 * Demonstra o uso de generics em Java (similar ao .NET) e
 * padrões de response consistentes para APIs REST.
 * 
 * Comparação com .NET:
 * - Similar ao ActionResult<T> ou ApiResponse<T> no ASP.NET Core
 * - @JsonInclude.Include.NON_NULL evita serializar campos nulos (similar ao JsonIgnore condicional)
 * 
 * @param <T> Tipo dos dados retornados
 * @author Learning Project
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Introspected
@Serdeable
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    
    @Builder.Default
    private boolean success = true;
    
    private String message;
    
    private T data;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @Builder.Default
    private LocalDateTime timestamp = LocalDateTime.now();
    
    private List<ValidationError> errors;
    
    private PaginationInfo pagination;
    
    /**
     * Cria uma response de sucesso com dados.
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .build();
    }
    
    /**
     * Cria uma response de sucesso com dados e mensagem.
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .message(message)
                .build();
    }
    
    /**
     * Cria uma response de erro com mensagem.
     */
    public static <T> ApiResponse<T> error(String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .message(message)
                .build();
    }
    
    /**
     * Cria uma response de erro com lista de erros de validação.
     */
    public static <T> ApiResponse<T> validationError(List<ValidationError> errors) {
        return ApiResponse.<T>builder()
                .success(false)
                .message("Erro de validação")
                .errors(errors)
                .build();
    }
    
    /**
     * DTO para erros de validação.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Introspected
    @Serdeable
    public static class ValidationError {
        private String field;
        private String message;
        private Object rejectedValue;
    }
    
    /**
     * DTO para informações de paginação.
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Introspected
    @Serdeable
    public static class PaginationInfo {
        private int page;
        private int size;
        private long total;
        private int totalPages;
        private boolean hasNext;
        private boolean hasPrevious;
    }
}

