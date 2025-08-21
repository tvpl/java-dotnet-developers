#!/bin/bash

# Script para iniciar a infraestrutura do Micronaut Learning Project
# Otimizado para macOS Silicon

set -e

echo "🚀 Iniciando infraestrutura do Micronaut Learning Project..."

# Verificar se Docker está rodando
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker não está rodando. Por favor, inicie o Docker Desktop."
    exit 1
fi

# Verificar se Docker Compose está disponível
if ! command -v docker-compose &> /dev/null; then
    echo "❌ Docker Compose não encontrado. Por favor, instale o Docker Compose."
    exit 1
fi

# Criar rede se não existir
echo "📡 Criando rede Docker..."
docker network create micronaut-learning-network 2>/dev/null || echo "ℹ️  Rede já existe"

# Iniciar serviços de infraestrutura em ordem
echo "🗄️  Iniciando MongoDB..."
docker-compose up -d mongodb

echo "⏳ Aguardando MongoDB ficar pronto..."
sleep 10

echo "🐘 Iniciando Zookeeper..."
docker-compose up -d zookeeper

echo "⏳ Aguardando Zookeeper ficar pronto..."
sleep 15

echo "📨 Iniciando Kafka..."
docker-compose up -d kafka

echo "⏳ Aguardando Kafka ficar pronto..."
sleep 20

echo "📋 Iniciando Schema Registry..."
docker-compose up -d schema-registry

echo "⏳ Aguardando Schema Registry ficar pronto..."
sleep 15

echo "🔍 Iniciando Jaeger..."
docker-compose up -d jaeger

echo "📊 Iniciando Prometheus..."
docker-compose up -d prometheus

echo "📈 Iniciando Grafana..."
docker-compose up -d grafana

echo "🌐 Iniciando Kafka UI..."
docker-compose up -d kafka-ui

echo "💾 Iniciando Redis..."
docker-compose up -d redis

echo "⏳ Aguardando todos os serviços ficarem prontos..."
sleep 30

# Verificar status dos serviços
echo "🔍 Verificando status dos serviços..."
docker-compose ps

echo ""
echo "✅ Infraestrutura iniciada com sucesso!"
echo ""
echo "🌐 URLs dos serviços:"
echo "  📊 Grafana:        http://localhost:3000 (admin/admin123)"
echo "  📈 Prometheus:     http://localhost:9090"
echo "  🔍 Jaeger:         http://localhost:16686"
echo "  🌐 Kafka UI:       http://localhost:8080"
echo "  🗄️  MongoDB:        mongodb://localhost:27017"
echo "  📨 Kafka:          localhost:9092"
echo "  📋 Schema Registry: http://localhost:8081"
echo "  💾 Redis:          localhost:6379"
echo ""
echo "📝 Para parar todos os serviços: ./scripts/stop-infrastructure.sh"
echo "📝 Para ver logs: docker-compose logs -f [service-name]"
echo ""
echo "🎯 Agora você pode iniciar a aplicação Micronaut!"
echo "   ./mvnw exec:java"
echo "   ou"
echo "   ./mvnw compile exec:java -Dexec.mainClass=com.learning.micronaut.Application"

