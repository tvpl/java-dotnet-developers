#!/bin/bash

# Script para criar tópicos Kafka necessários para a aplicação

set -e

echo "📨 Configurando tópicos Kafka para Micronaut Learning Project..."

# Verificar se Kafka está rodando
if ! nc -z localhost 9092; then
    echo "❌ Kafka não está acessível na porta 9092"
    echo "   Execute primeiro: ./scripts/start-infrastructure.sh"
    exit 1
fi

# Função para criar tópico
create_topic() {
    local topic_name=$1
    local partitions=${2:-3}
    local replication=${3:-1}
    
    echo "  Criando tópico: $topic_name (partições: $partitions, replicação: $replication)"
    
    docker-compose exec kafka kafka-topics \
        --create \
        --topic "$topic_name" \
        --partitions "$partitions" \
        --replication-factor "$replication" \
        --bootstrap-server localhost:9092 \
        --if-not-exists
}

echo ""
echo "🏗️  Criando tópicos..."

# Tópicos principais da aplicação
create_topic "user-events" 3 1
create_topic "notifications" 3 1
create_topic "audit-events" 2 1
create_topic "metrics-events" 2 1

# Tópicos para dead letter queues
create_topic "user-events-dlq" 1 1
create_topic "notifications-dlq" 1 1

echo ""
echo "📋 Listando tópicos criados:"
docker-compose exec kafka kafka-topics \
    --list \
    --bootstrap-server localhost:9092

echo ""
echo "📊 Detalhes dos tópicos:"
docker-compose exec kafka kafka-topics \
    --describe \
    --bootstrap-server localhost:9092

echo ""
echo "✅ Tópicos Kafka configurados com sucesso!"
echo ""
echo "🔍 Para monitorar mensagens em um tópico:"
echo "  docker-compose exec kafka kafka-console-consumer \\"
echo "    --bootstrap-server localhost:9092 \\"
echo "    --topic user-events \\"
echo "    --from-beginning"
echo ""
echo "📝 Para enviar mensagem de teste:"
echo "  docker-compose exec kafka kafka-console-producer \\"
echo "    --bootstrap-server localhost:9092 \\"
echo "    --topic user-events"

