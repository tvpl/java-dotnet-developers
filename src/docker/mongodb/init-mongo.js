// Script de inicialização do MongoDB para o projeto Micronaut Learning
// Este script é executado automaticamente quando o container MongoDB é criado

print('Iniciando configuração do MongoDB para Micronaut Learning Project...');

// Conectar ao banco de dados learning_db
db = db.getSiblingDB('learning_db');

// Criar usuário da aplicação
db.createUser({
  user: 'micronaut_user',
  pwd: 'micronaut_password',
  roles: [
    {
      role: 'readWrite',
      db: 'learning_db'
    }
  ]
});

print('Usuário micronaut_user criado com sucesso');

// Criar coleções com validação de schema
db.createCollection('users', {
  validator: {
    $jsonSchema: {
      bsonType: 'object',
      required: ['name', 'email', 'age', 'status', 'createdAt'],
      properties: {
        name: {
          bsonType: 'string',
          description: 'Nome do usuário é obrigatório e deve ser string'
        },
        email: {
          bsonType: 'string',
          pattern: '^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$',
          description: 'Email deve ter formato válido'
        },
        age: {
          bsonType: 'int',
          minimum: 0,
          maximum: 150,
          description: 'Idade deve ser um número entre 0 e 150'
        },
        status: {
          enum: ['ACTIVE', 'INACTIVE', 'SUSPENDED', 'PENDING'],
          description: 'Status deve ser um dos valores válidos'
        },
        createdAt: {
          bsonType: 'date',
          description: 'Data de criação é obrigatória'
        },
        updatedAt: {
          bsonType: 'date',
          description: 'Data de atualização deve ser uma data válida'
        },
        tags: {
          bsonType: 'array',
          items: {
            bsonType: 'string'
          },
          description: 'Tags devem ser um array de strings'
        }
      }
    }
  }
});

print('Coleção users criada com validação de schema');

// Criar índices para performance
db.users.createIndex({ email: 1 }, { unique: true, name: 'idx_users_email_unique' });
db.users.createIndex({ status: 1 }, { name: 'idx_users_status' });
db.users.createIndex({ createdAt: -1 }, { name: 'idx_users_created_at_desc' });
db.users.createIndex({ tags: 1 }, { name: 'idx_users_tags' });
db.users.createIndex({ 'profile.address.city': 1 }, { name: 'idx_users_city' });

print('Índices criados para a coleção users');

// Inserir dados de exemplo para demonstração
db.users.insertMany([
  {
    name: 'João Silva',
    email: 'joao.silva@example.com',
    age: 30,
    status: 'ACTIVE',
    createdAt: new Date(),
    updatedAt: new Date(),
    tags: ['java', 'micronaut', 'mongodb'],
    profile: {
      bio: 'Desenvolvedor Java sênior especializado em microserviços',
      avatarUrl: 'https://example.com/avatars/joao.jpg',
      address: {
        street: 'Rua das Flores, 123',
        city: 'São Paulo',
        state: 'SP',
        zipCode: '01234-567',
        country: 'Brasil'
      },
      socialLinks: [
        {
          platform: 'LinkedIn',
          url: 'https://linkedin.com/in/joaosilva'
        },
        {
          platform: 'GitHub',
          url: 'https://github.com/joaosilva'
        }
      ]
    }
  },
  {
    name: 'Maria Santos',
    email: 'maria.santos@example.com',
    age: 28,
    status: 'ACTIVE',
    createdAt: new Date(),
    updatedAt: new Date(),
    tags: ['kotlin', 'spring', 'kubernetes'],
    profile: {
      bio: 'Arquiteta de software focada em cloud native',
      avatarUrl: 'https://example.com/avatars/maria.jpg',
      address: {
        street: 'Av. Paulista, 1000',
        city: 'São Paulo',
        state: 'SP',
        zipCode: '01310-100',
        country: 'Brasil'
      },
      socialLinks: [
        {
          platform: 'Twitter',
          url: 'https://twitter.com/mariasantos'
        }
      ]
    }
  },
  {
    name: 'Pedro Oliveira',
    email: 'pedro.oliveira@example.com',
    age: 35,
    status: 'INACTIVE',
    createdAt: new Date(Date.now() - 30 * 24 * 60 * 60 * 1000), // 30 dias atrás
    updatedAt: new Date(),
    tags: ['python', 'data-science', 'machine-learning'],
    profile: {
      bio: 'Cientista de dados com foco em ML',
      avatarUrl: 'https://example.com/avatars/pedro.jpg',
      address: {
        street: 'Rua Oscar Freire, 500',
        city: 'São Paulo',
        state: 'SP',
        zipCode: '01426-001',
        country: 'Brasil'
      }
    }
  }
]);

print('Dados de exemplo inseridos na coleção users');

// Criar coleção para auditoria
db.createCollection('audit_events', {
  validator: {
    $jsonSchema: {
      bsonType: 'object',
      required: ['eventType', 'userId', 'timestamp', 'source'],
      properties: {
        eventType: {
          bsonType: 'string',
          description: 'Tipo do evento de auditoria'
        },
        userId: {
          bsonType: 'string',
          description: 'ID do usuário relacionado ao evento'
        },
        timestamp: {
          bsonType: 'date',
          description: 'Timestamp do evento'
        },
        source: {
          bsonType: 'string',
          description: 'Origem do evento'
        },
        details: {
          bsonType: 'object',
          description: 'Detalhes adicionais do evento'
        }
      }
    }
  }
});

// Índices para auditoria
db.audit_events.createIndex({ userId: 1, timestamp: -1 }, { name: 'idx_audit_user_timestamp' });
db.audit_events.createIndex({ eventType: 1 }, { name: 'idx_audit_event_type' });
db.audit_events.createIndex({ timestamp: -1 }, { name: 'idx_audit_timestamp_desc' });

print('Coleção audit_events criada com índices');

// Criar coleção para métricas
db.createCollection('metrics', {
  validator: {
    $jsonSchema: {
      bsonType: 'object',
      required: ['metricName', 'value', 'timestamp'],
      properties: {
        metricName: {
          bsonType: 'string',
          description: 'Nome da métrica'
        },
        value: {
          bsonType: 'double',
          description: 'Valor da métrica'
        },
        timestamp: {
          bsonType: 'date',
          description: 'Timestamp da métrica'
        },
        tags: {
          bsonType: 'object',
          description: 'Tags da métrica'
        }
      }
    }
  }
});

// Índices para métricas
db.metrics.createIndex({ metricName: 1, timestamp: -1 }, { name: 'idx_metrics_name_timestamp' });
db.metrics.createIndex({ timestamp: -1 }, { name: 'idx_metrics_timestamp_desc' });

print('Coleção metrics criada com índices');

// Configurar TTL para limpeza automática de dados antigos (opcional)
// Métricas são mantidas por 30 dias
db.metrics.createIndex({ timestamp: 1 }, { 
  expireAfterSeconds: 30 * 24 * 60 * 60, // 30 dias
  name: 'idx_metrics_ttl' 
});

// Eventos de auditoria são mantidos por 90 dias
db.audit_events.createIndex({ timestamp: 1 }, { 
  expireAfterSeconds: 90 * 24 * 60 * 60, // 90 dias
  name: 'idx_audit_ttl' 
});

print('Configuração de TTL aplicada para limpeza automática');

// Mostrar estatísticas finais
print('=== Estatísticas do banco de dados ===');
print('Coleções criadas:');
db.getCollectionNames().forEach(function(collection) {
  print('- ' + collection + ': ' + db.getCollection(collection).countDocuments() + ' documentos');
});

print('=== Índices criados ===');
db.getCollectionNames().forEach(function(collection) {
  print('Coleção ' + collection + ':');
  db.getCollection(collection).getIndexes().forEach(function(index) {
    print('  - ' + index.name);
  });
});

print('Configuração do MongoDB concluída com sucesso!');
print('Banco: learning_db');
print('Usuário da aplicação: micronaut_user');
print('Dados de exemplo inseridos: ' + db.users.countDocuments() + ' usuários');

