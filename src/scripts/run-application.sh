#!/bin/bash

# Script para executar a aplicação Micronaut com Maven

set -e

echo "🚀 Iniciando aplicação Micronaut Learning Project..."

# Verificar se Maven wrapper existe
if [ ! -f "./mvnw" ]; then
    echo "❌ Maven wrapper não encontrado. Execute este script a partir do diretório raiz do projeto."
    exit 1
fi

# Verificar se a infraestrutura está rodando
echo "🔍 Verificando se a infraestrutura está rodando..."

# Verificar MongoDB
if ! nc -z localhost 27017; then
    echo "⚠️  MongoDB não está acessível na porta 27017"
    echo "   Execute primeiro: ./scripts/start-infrastructure.sh"
    read -p "Deseja continuar mesmo assim? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
fi

# Verificar Kafka
if ! nc -z localhost 9092; then
    echo "⚠️  Kafka não está acessível na porta 9092"
    echo "   Execute primeiro: ./scripts/start-infrastructure.sh"
    read -p "Deseja continuar mesmo assim? (y/N): " -n 1 -r
    echo
    if [[ ! $REPLY =~ ^[Yy]$ ]]; then
        exit 1
    fi
fi

echo "✅ Infraestrutura verificada"

# Compilar o projeto
echo "🔨 Compilando o projeto..."
./mvnw compile

# Gerar classes Protobuf se necessário
echo "📋 Gerando classes Protobuf..."
./mvnw protobuf:compile protobuf:compile-custom

echo ""
echo "🎯 Iniciando aplicação..."
echo "   Acesse: http://localhost:8080"
echo "   Health Check: http://localhost:8080/health"
echo "   Métricas: http://localhost:8080/prometheus"
echo ""
echo "   Para parar a aplicação: Ctrl+C"
echo ""

# Executar a aplicação
./mvnw exec:java -Dexec.mainClass="com.learning.micronaut.Application"

