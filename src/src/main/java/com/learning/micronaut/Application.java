package com.learning.micronaut;

import io.micronaut.runtime.Micronaut;
import lombok.extern.slf4j.Slf4j;

/**
 * Classe principal da aplicação Micronaut.
 * 
 * Esta é a classe de entrada da aplicação, similar ao Program.cs no .NET.
 * O Micronaut usa injeção de dependência baseada em anotações, similar ao .NET Core DI.
 * 
 * Principais diferenças do .NET:
 * - No .NET: Program.cs com WebApplication.CreateBuilder()
 * - No Java/Micronaut: Micronaut.build().args(args).start()
 * 
 * @author Learning Project
 */
@Slf4j
public class Application {
    
    public static void main(String[] args) {
        log.info("Iniciando aplicação Micronaut Learning Project...");
        
        // Inicializa o contexto do Micronaut
        // Equivalente ao WebApplication.CreateBuilder() no .NET
        Micronaut.build(args)
                .classes(Application.class)
                .banner(true) // Mostra o banner do Micronaut na inicialização
                .start();
                
        log.info("Aplicação iniciada com sucesso!");
    }
}

