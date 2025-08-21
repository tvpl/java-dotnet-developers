package com.learning.micronaut.observability;

import io.micronaut.aop.InterceptorBean;
import io.micronaut.aop.MethodInterceptor;
import io.micronaut.aop.MethodInvocationContext;
import io.micronaut.core.annotation.Nullable;
import io.opentelemetry.api.trace.Span;
import jakarta.inject.Singleton;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;

/**
 * Interceptor que adiciona observabilidade automática a métodos anotados.
 * 
 * Demonstra conceitos importantes de AOP (Aspect-Oriented Programming):
 * - @InterceptorBean: Marca como interceptor do Micronaut
 * - MethodInterceptor: Interface para interceptação de métodos
 * - Cross-cutting concerns: Funcionalidades transversais (logging, metrics, tracing)
 * - Annotations: Metadados para controlar comportamento
 * 
 * Comparação com .NET:
 * - Similar aos Interceptors no Castle DynamicProxy
 * - Similar aos ActionFilters no ASP.NET Core
 * - Similar aos Decorators no .NET
 * - AOP é menos comum no .NET, mas existe via PostSharp, Castle, etc.
 * 
 * Vantagens do AOP:
 * - Separação de responsabilidades
 * - Reutilização de código transversal
 * - Menor acoplamento
 * - Facilita manutenção
 * 
 * @author Learning Project
 */
@Singleton
@InterceptorBean(Observed.class) // Intercepta métodos anotados com @Observed
@RequiredArgsConstructor
@Slf4j
public class ObservabilityInterceptor implements MethodInterceptor<Object, Object> {
    
    private final ObservabilityService observabilityService;
    
    /**
     * Intercepta chamadas de métodos para adicionar observabilidade automática.
     * 
     * Este método é chamado automaticamente pelo Micronaut sempre que
     * um método anotado com @Observed é invocado.
     * 
     * @param context Contexto da invocação do método
     * @return Resultado da execução do método
     */
    @Override
    @Nullable
    public Object intercept(MethodInvocationContext<Object, Object> context) {
        // Obter informações do método
        String className = context.getDeclaringType().getSimpleName();
        String methodName = context.getMethodName();
        String operationName = className + "." + methodName;
        
        // Obter configuração da anotação
        Observed annotation = context.getAnnotation(Observed.class);
        String customName = annotation != null && !annotation.value().isEmpty() 
            ? annotation.value() 
            : operationName;
        
        boolean recordMetrics = annotation == null || annotation.recordMetrics();
        boolean recordTrace = annotation == null || annotation.recordTrace();
        
        log.debug("Interceptando método: {} (metrics: {}, trace: {})", 
                 operationName, recordMetrics, recordTrace);
        
        // Variáveis para rastreamento
        long startTime = System.currentTimeMillis();
        Span span = null;
        boolean success = false;
        Throwable exception = null;
        Object result = null;
        
        try {
            // Criar span se rastreamento estiver habilitado
            if (recordTrace) {
                span = observabilityService.createCustomSpan(
                    "method." + customName,
                    java.util.Map.of(
                        "class.name", className,
                        "method.name", methodName,
                        "method.parameters", getParameterInfo(context)
                    )
                );
                
                // Adicionar informações dos parâmetros ao span
                addParametersToSpan(span, context);
            }
            
            // Executar o método original
            result = context.proceed();
            success = true;
            
            // Adicionar informações do resultado ao span
            if (span != null && result != null) {
                span.setAttribute("method.result_type", result.getClass().getSimpleName());
                span.addEvent("Method executed successfully");
            }
            
            return result;
            
        } catch (Throwable t) {
            exception = t;
            success = false;
            
            // Registrar erro no span
            if (span != null) {
                span.setAttribute("method.error", true);
                span.setAttribute("error.type", t.getClass().getSimpleName());
                span.setAttribute("error.message", t.getMessage());
                span.addEvent("Method execution failed");
            }
            
            log.warn("Erro na execução do método {}: {}", operationName, t.getMessage());
            throw t;
            
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            
            // Registrar métricas se habilitado
            if (recordMetrics) {
                recordMethodMetrics(className, methodName, success, duration, exception);
            }
            
            // Finalizar span
            if (span != null) {
                span.setAttribute("method.duration_ms", duration);
                span.setAttribute("method.success", success);
                span.end();
            }
            
            log.debug("Método {} executado em {}ms - sucesso: {}", 
                     operationName, duration, success);
        }
    }
    
    /**
     * Registra métricas do método interceptado.
     */
    private void recordMethodMetrics(String className, String methodName, 
                                   boolean success, long duration, Throwable exception) {
        
        // Registrar contador de execuções
        // Em produção, usar o MeterRegistry diretamente
        log.debug("METRICS: method.executions - class: {}, method: {}, success: {}, duration: {}ms", 
                 className, methodName, success, duration);
        
        // Registrar métricas de erro se houver
        if (!success && exception != null) {
            log.debug("METRICS: method.errors - class: {}, method: {}, error_type: {}", 
                     className, methodName, exception.getClass().getSimpleName());
        }
        
        // Registrar métricas de performance
        if (duration > 1000) { // Métodos que demoram mais de 1 segundo
            log.warn("METRICS: method.slow_execution - class: {}, method: {}, duration: {}ms", 
                    className, methodName, duration);
        }
    }
    
    /**
     * Adiciona informações dos parâmetros ao span.
     */
    private void addParametersToSpan(Span span, MethodInvocationContext<Object, Object> context) {
        Object[] parameters = context.getParameterValues();
        String[] parameterNames = Arrays.stream(context.getArguments())
                .map(arg -> arg.getName())
                .toArray(String[]::new);
        
        for (int i = 0; i < parameters.length && i < parameterNames.length; i++) {
            Object param = parameters[i];
            String paramName = parameterNames[i];
            
            if (param != null) {
                // Adicionar tipo do parâmetro
                span.setAttribute("param." + paramName + ".type", param.getClass().getSimpleName());
                
                // Adicionar valor se for tipo simples (evitar objetos complexos)
                if (isSimpleType(param)) {
                    span.setAttribute("param." + paramName + ".value", param.toString());
                }
            } else {
                span.setAttribute("param." + paramName + ".value", "null");
            }
        }
    }
    
    /**
     * Obtém informações resumidas dos parâmetros.
     */
    private String getParameterInfo(MethodInvocationContext<Object, Object> context) {
        Object[] parameters = context.getParameterValues();
        
        if (parameters.length == 0) {
            return "no-parameters";
        }
        
        StringBuilder info = new StringBuilder();
        for (int i = 0; i < parameters.length; i++) {
            if (i > 0) info.append(", ");
            
            Object param = parameters[i];
            if (param != null) {
                info.append(param.getClass().getSimpleName());
            } else {
                info.append("null");
            }
        }
        
        return info.toString();
    }
    
    /**
     * Verifica se o tipo é simples (seguro para logging).
     */
    private boolean isSimpleType(Object obj) {
        return obj instanceof String ||
               obj instanceof Number ||
               obj instanceof Boolean ||
               obj instanceof Character ||
               obj.getClass().isPrimitive();
    }
}

/**
 * Anotação para marcar métodos que devem ser observados automaticamente.
 * 
 * Uso:
 * @Observed // Usa configurações padrão
 * @Observed("custom-operation-name") // Nome customizado
 * @Observed(recordMetrics = false) // Desabilita métricas
 * @Observed(recordTrace = false) // Desabilita tracing
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Observed {
    
    /**
     * Nome customizado para a operação (opcional).
     * Se não fornecido, usa ClassName.methodName
     */
    String value() default "";
    
    /**
     * Se deve registrar métricas para este método.
     */
    boolean recordMetrics() default true;
    
    /**
     * Se deve criar spans de rastreamento para este método.
     */
    boolean recordTrace() default true;
}

