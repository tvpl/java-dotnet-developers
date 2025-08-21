#!/bin/bash

# Script para fazer build completo da aplicação Micronaut com Maven

set -e

echo "🔨 Fazendo build completo do Micronaut Learning Project..."

# Verificar se Maven wrapper existe
if [ ! -f "./mvnw" ]; then
    echo "❌ Maven wrapper não encontrado. Execute este script a partir do diretório raiz do projeto."
    exit 1
fi

# Limpar projeto
echo "🧹 Limpando projeto..."
./mvnw clean

# Gerar classes Protobuf
echo "📋 Gerando classes Protobuf..."
./mvnw protobuf:compile protobuf:compile-custom

# Compilar
echo "🔨 Compilando código fonte..."
./mvnw compile

# Executar testes
echo "🧪 Executando testes..."
./mvnw test

# Criar JAR executável
echo "📦 Criando JAR executável..."
./mvnw package

# Verificar se JAR foi criado
if [ -f "target/micronaut-learning-project-0.1.0.jar" ]; then
    echo ""
    echo "✅ Build concluído com sucesso!"
    echo ""
    echo "📦 JAR executável criado:"
    echo "   target/micronaut-learning-project-0.1.0.jar"
    echo ""
    echo "🚀 Para executar o JAR:"
    echo "   java -jar target/micronaut-learning-project-0.1.0.jar"
    echo ""
    echo "🐳 Para criar imagem Docker:"
    echo "   docker build -t micronaut-learning-project ."
    echo ""
    echo "📊 Tamanho do JAR:"
    ls -lh target/micronaut-learning-project-0.1.0.jar
else
    echo "❌ Erro: JAR não foi criado"
    exit 1
fi

# Opção para build nativo (GraalVM)
if [[ "$1" == "--native" ]]; then
    echo ""
    echo "🏗️  Criando imagem nativa com GraalVM..."
    echo "   (Isso pode demorar vários minutos)"
    
    ./mvnw package -Pnative
    
    if [ -f "target/micronaut-learning-project" ]; then
        echo ""
        echo "✅ Imagem nativa criada com sucesso!"
        echo "   target/micronaut-learning-project"
        echo ""
        echo "🚀 Para executar a imagem nativa:"
        echo "   ./target/micronaut-learning-project"
        echo ""
        echo "📊 Tamanho da imagem nativa:"
        ls -lh target/micronaut-learning-project
    else
        echo "❌ Erro: Imagem nativa não foi criada"
        exit 1
    fi
fi

