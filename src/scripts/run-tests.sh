#!/bin/bash

# Script para executar testes da aplicação Micronaut com Maven

set -e

echo "🧪 Executando testes do Micronaut Learning Project..."

# Verificar se Maven wrapper existe
if [ ! -f "./mvnw" ]; then
    echo "❌ Maven wrapper não encontrado. Execute este script a partir do diretório raiz do projeto."
    exit 1
fi

# Limpar e compilar
echo "🔨 Limpando e compilando o projeto..."
./mvnw clean compile test-compile

# Gerar classes Protobuf
echo "📋 Gerando classes Protobuf..."
./mvnw protobuf:compile protobuf:compile-custom

# Executar testes
echo "🧪 Executando testes unitários..."
./mvnw test

echo ""
echo "✅ Testes executados com sucesso!"
echo ""
echo "📊 Para ver relatório de cobertura:"
echo "   ./mvnw jacoco:report"
echo "   Abra: target/site/jacoco/index.html"
echo ""
echo "🔍 Para executar apenas testes específicos:"
echo "   ./mvnw test -Dtest=UserServiceTest"
echo ""
echo "🚀 Para executar testes de integração:"
echo "   ./mvnw verify"

