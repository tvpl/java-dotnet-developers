#!/bin/bash

# Script para parar a infraestrutura do Micronaut Learning Project

set -e

echo "🛑 Parando infraestrutura do Micronaut Learning Project..."

# Parar todos os serviços
echo "📦 Parando todos os containers..."
docker-compose down

# Opção para limpar volumes (dados persistentes)
if [[ "$1" == "--clean" ]]; then
    echo "🧹 Removendo volumes de dados..."
    docker-compose down -v
    
    echo "🗑️  Removendo imagens não utilizadas..."
    docker image prune -f
    
    echo "🔄 Removendo rede..."
    docker network rm micronaut-learning-network 2>/dev/null || echo "ℹ️  Rede já foi removida"
fi

echo ""
echo "✅ Infraestrutura parada com sucesso!"

if [[ "$1" == "--clean" ]]; then
    echo "🧹 Dados persistentes foram removidos."
    echo "   Na próxima inicialização, o banco será recriado do zero."
else
    echo "💾 Dados persistentes foram mantidos."
    echo "   Use --clean para remover todos os dados: ./scripts/stop-infrastructure.sh --clean"
fi

echo ""
echo "🚀 Para reiniciar: ./scripts/start-infrastructure.sh"

