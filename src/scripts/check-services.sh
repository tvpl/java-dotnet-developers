#!/bin/bash

# Script para verificar o status de todos os serviços

set -e

echo "🔍 Verificando status dos serviços do Micronaut Learning Project..."
echo ""

# Função para verificar se um serviço está respondendo
check_service() {
    local service_name=$1
    local url=$2
    local expected_status=${3:-200}
    
    echo -n "  $service_name: "
    
    if curl -s -o /dev/null -w "%{http_code}" "$url" | grep -q "$expected_status"; then
        echo "✅ OK"
    else
        echo "❌ FAIL"
    fi
}

# Função para verificar porta TCP
check_port() {
    local service_name=$1
    local host=$2
    local port=$3
    
    echo -n "  $service_name: "
    
    if nc -z "$host" "$port" 2>/dev/null; then
        echo "✅ OK (porta $port)"
    else
        echo "❌ FAIL (porta $port não acessível)"
    fi
}

echo "🌐 Serviços Web:"
check_service "Grafana" "http://localhost:3000/api/health"
check_service "Prometheus" "http://localhost:9090/-/healthy"
check_service "Jaeger UI" "http://localhost:16686/"
check_service "Kafka UI" "http://localhost:8080/actuator/health"
check_service "Schema Registry" "http://localhost:8081/subjects"

echo ""
echo "🔌 Serviços TCP:"
check_port "MongoDB" "localhost" "27017"
check_port "Kafka" "localhost" "9092"
check_port "Zookeeper" "localhost" "2181"
check_port "Redis" "localhost" "6379"

echo ""
echo "📦 Status dos containers:"
docker-compose ps

echo ""
echo "💾 Uso de volumes:"
docker volume ls | grep micronaut

echo ""
echo "📊 Uso de recursos:"
echo "  CPU e Memória dos containers:"
docker stats --no-stream --format "table {{.Container}}\t{{.CPUPerc}}\t{{.MemUsage}}" $(docker-compose ps -q) 2>/dev/null || echo "  Nenhum container rodando"

echo ""
echo "🔍 Para logs detalhados de um serviço:"
echo "  docker-compose logs -f [service-name]"
echo ""
echo "🔍 Para logs de todos os serviços:"
echo "  docker-compose logs -f"

