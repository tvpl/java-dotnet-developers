# Micronaut Learning Project - Guia Completo para Desenvolvedores .NET

![Java](https://img.shields.io/badge/Java-17-blue) ![Micronaut](https://img.shields.io/badge/Micronaut-4.2.1-blueviolet) ![Kafka](https://img.shields.io/badge/Kafka-7.5.1-black) ![MongoDB](https://img.shields.io/badge/MongoDB-7.0-green) ![Protobuf](https://img.shields.io/badge/Protobuf-3.25.1-orange) ![Jaeger](https://img.shields.io/badge/Jaeger-1.51-red) ![Docker](https://img.shields.io/badge/Docker-Compose-blue) ![Maven](https://img.shields.io/badge/Maven-3.9.5-red)

## Sumário

1. [Introdução](#1-introdução)
2. [Primeiros Passos](#2-primeiros-passos)
3. [Estrutura do Projeto](#3-estrutura-do-projeto)
4. [Conceitos Fundamentais do Micronaut](#4-conceitos-fundamentais-do-micronaut)
5. [Injeção de Dependência e Beans](#5-injeção-de-dependência-e-beans)
6. [Entidades e Persistência com MongoDB](#6-entidades-e-persistência-com-mongodb)
7. [APIs REST e Controllers](#7-apis-rest-e-controllers)
8. [APIs gRPC e Protocol Buffers](#8-apis-grpc-e-protocol-buffers)
9. [Mensageria com Kafka](#9-mensageria-com-kafka)
10. [Circuit Breaker e Resiliência](#10-circuit-breaker-e-resiliência)
11. [Observabilidade e Monitoramento](#11-observabilidade-e-monitoramento)
12. [Configuração e Profiles](#12-configuração-e-profiles)
13. [Build e Deploy](#13-build-e-deploy)
14. [Scripts Utilitários](#14-scripts-utilitários)
15. [Comparações Detalhadas com .NET](#15-comparações-detalhadas-com-net)
16. [Conclusão e Próximos Passos](#16-conclusão-e-próximos-passos)

---

## 1. Introdução

Este projeto foi meticulosamente desenvolvido como um guia de aprendizado completo para desenvolvedores .NET sênior que desejam dominar o ecossistema Java moderno, especificamente o framework Micronaut. Diferentemente de tutoriais básicos, este projeto implementa um sistema real de gerenciamento de usuários que demonstra padrões de arquitetura de microserviços de nível empresarial.

### 1.1. Por que Micronaut?

Micronaut representa uma evolução significativa no desenvolvimento Java, especialmente relevante para desenvolvedores vindos do .NET Core/5+. Assim como o .NET Core revolucionou o desenvolvimento .NET com sua abordagem modular e performática, o Micronaut faz o mesmo para Java, oferecendo:

**Compilação Ahead-of-Time (AOT):** Diferentemente do Spring Framework tradicional, que resolve dependências em tempo de execução usando reflexão, o Micronaut resolve tudo em tempo de compilação. Isso resulta em aplicações que iniciam em milissegundos (similar ao tempo de startup do .NET), consomem menos memória e são ideais para containers e serverless.

**Arquitetura Reativa Nativa:** Micronaut foi projetado desde o início para programação reativa, usando Project Reactor (Mono/Flux), que é conceitualmente similar ao async/await do .NET, mas com capacidades de streaming mais avançadas.

**Compatibilidade com GraalVM:** Permite compilar aplicações Java para binários nativos, eliminando a necessidade da JVM em produção, resultando em imagens Docker extremamente pequenas e tempos de startup instantâneos.

### 1.2. Arquitetura do Sistema

O projeto implementa um sistema de gerenciamento de usuários com as seguintes características arquiteturais:

```mermaid
graph TB
    subgraph "Cliente"
        WEB[Web Client]
        GRPC[gRPC Client]
        MOBILE[Mobile App]
    end

    subgraph "API Gateway Layer"
        REST[REST Controller]
        GRPC_SVC[gRPC Service]
    end

    subgraph "Business Logic Layer"
        USER_SVC[UserService]
        EXT_SVC[ExternalService]
        OBS_SVC[ObservabilityService]
    end

    subgraph "Data Layer"
        REPO[UserRepository]
        MAPPER[UserMapper]
    end

    subgraph "Infrastructure"
        MONGO[(MongoDB)]
        KAFKA[Kafka Cluster]
        REDIS[(Redis Cache)]
    end

    subgraph "Observability"
        JAEGER[Jaeger Tracing]
        PROMETHEUS[Prometheus Metrics]
        GRAFANA[Grafana Dashboards]
    end

    subgraph "External Services"
        EMAIL_API[Email Validation API]
        GEO_API[Geolocation API]
        NOTIF_API[Notification API]
    end

    WEB --> REST
    GRPC --> GRPC_SVC
    MOBILE --> REST

    REST --> USER_SVC
    GRPC_SVC --> USER_SVC

    USER_SVC --> REPO
    USER_SVC --> EXT_SVC
    USER_SVC --> OBS_SVC

    REPO --> MAPPER
    MAPPER --> MONGO

    USER_SVC --> KAFKA
    KAFKA --> USER_SVC

    EXT_SVC --> EMAIL_API
    EXT_SVC --> GEO_API
    EXT_SVC --> NOTIF_API

    OBS_SVC --> JAEGER
    OBS_SVC --> PROMETHEUS
    PROMETHEUS --> GRAFANA

    USER_SVC --> REDIS
```

### 1.3. Tecnologias e Suas Equivalências .NET

| Tecnologia Java | Equivalente .NET | Propósito |
|----------------|------------------|-----------|
| **Micronaut 4.2.1** | ASP.NET Core 8.0 | Framework web e DI container |
| **Maven** | MSBuild/NuGet | Sistema de build e gerenciamento de dependências |
| **MongoDB** | MongoDB.Driver ou CosmosDB | Banco de dados NoSQL |
| **Apache Kafka** | Azure Service Bus / RabbitMQ | Message broker para eventos |
| **Protocol Buffers** | System.Text.Json ou MessagePack | Serialização binária eficiente |
| **Resilience4j** | Polly | Padrões de resiliência (Circuit Breaker, Retry) |
| **OpenTelemetry** | System.Diagnostics.Activity | Tracing distribuído |
| **Micrometer** | System.Diagnostics.Metrics | Métricas da aplicação |
| **Lombok** | Source Generators | Geração automática de código |
| **JUnit 5** | xUnit/NUnit | Framework de testes |
| **Testcontainers** | Docker.DotNet | Testes de integração com containers |

---

## 2. Primeiros Passos

### 2.1. Pré-requisitos Detalhados

**Java Development Kit (JDK) 17 ou superior:**
```bash
# Verificar versão instalada
java -version
javac -version

# No macOS com Homebrew
brew install openjdk@17

# No Ubuntu/Debian
sudo apt update
sudo apt install openjdk-17-jdk

# No Windows
# Baixar do site oficial da Oracle ou usar OpenJDK
```

**Docker Desktop:**
O projeto utiliza Docker Compose para orquestrar toda a infraestrutura de desenvolvimento. Certifique-se de que o Docker Desktop está configurado com pelo menos 4GB de RAM disponível para containers.

**IDE Recomendada:**
- **IntelliJ IDEA Community/Ultimate:** Melhor suporte para Java, Maven e Micronaut
- **VS Code:** Com extensões Java Extension Pack e Micronaut Tools

### 2.2. Configuração do Ambiente

**Clone e Preparação:**
```bash
# Extrair o projeto
tar -xzf micronaut-learning-project-maven.tar.gz
cd micronaut-learning-project

# Verificar estrutura
ls -la

# Tornar scripts executáveis (se necessário)
chmod +x scripts/*.sh
```

**Inicialização da Infraestrutura:**
```bash
# Iniciar todos os serviços de infraestrutura
./scripts/start-infrastructure.sh

# Verificar status dos serviços
./scripts/check-services.sh

# Configurar tópicos Kafka
./scripts/setup-kafka-topics.sh
```

Este processo iniciará:
- **MongoDB** na porta 27017 com dados de exemplo
- **Kafka** na porta 9092 com tópicos pré-configurados
- **Schema Registry** na porta 8081 para gerenciar schemas Protobuf
- **Jaeger** na porta 16686 para visualização de traces
- **Prometheus** na porta 9090 para coleta de métricas
- **Grafana** na porta 3000 com dashboards pré-configurados
- **Kafka UI** na porta 8080 para monitoramento do Kafka
- **Redis** na porta 6379 para cache

### 2.3. Executando a Aplicação

**Compilação e Execução:**
```bash
# Opção 1: Usando o script utilitário (recomendado)
./scripts/run-application.sh

# Opção 2: Usando Maven diretamente
./mvnw compile exec:java

# Opção 3: Compilar e executar JAR
./mvnw package
java -jar target/micronaut-learning-project-0.1.0.jar
```

**Verificação da Aplicação:**
```bash
# Health check
curl http://localhost:8080/health

# Métricas Prometheus
curl http://localhost:8080/prometheus

# Endpoint de usuários
curl http://localhost:8080/users
```

### 2.4. Interfaces de Monitoramento

Após inicializar tudo, você terá acesso a:

- **Aplicação Micronaut:** http://localhost:8080
- **Grafana (admin/admin123):** http://localhost:3000
- **Jaeger Tracing:** http://localhost:16686
- **Prometheus:** http://localhost:9090
- **Kafka UI:** http://localhost:8080

---

## 3. Estrutura do Projeto

### 3.1. Organização de Diretórios

```
micronaut-learning-project/
├── .mvn/wrapper/                    # Maven wrapper configuration
├── docker/                         # Docker Compose configurations
│   ├── grafana/                    # Grafana dashboards and datasources
│   ├── mongodb/                    # MongoDB initialization scripts
│   └── prometheus/                 # Prometheus configuration
├── scripts/                        # Utility scripts
├── src/
│   ├── main/
│   │   ├── java/com/learning/micronaut/
│   │   │   ├── Application.java           # Main application class
│   │   │   ├── controller/                # REST Controllers
│   │   │   │   └── UserController.java
│   │   │   ├── dto/                       # Data Transfer Objects
│   │   │   │   ├── AddressDto.java
│   │   │   │   ├── ApiResponse.java
│   │   │   │   ├── SocialLinkDto.java
│   │   │   │   ├── UserDto.java
│   │   │   │   └── UserProfileDto.java
│   │   │   ├── entity/                    # MongoDB Entities
│   │   │   │   └── User.java
│   │   │   ├── grpc/                      # gRPC Services
│   │   │   │   └── UserGrpcService.java
│   │   │   ├── kafka/                     # Kafka Producers/Consumers
│   │   │   │   ├── NotificationConsumer.java
│   │   │   │   ├── UserEventConsumer.java
│   │   │   │   └── UserEventProducer.java
│   │   │   ├── observability/             # Observability Services
│   │   │   │   ├── ObservabilityInterceptor.java
│   │   │   │   └── ObservabilityService.java
│   │   │   ├── repository/                # Data Repositories
│   │   │   │   └── UserRepository.java
│   │   │   └── service/                   # Business Services
│   │   │       ├── ExternalService.java
│   │   │       ├── UserMapper.java
│   │   │       └── UserService.java
│   │   ├── proto/                         # Protocol Buffer definitions
│   │   │   ├── events.proto
│   │   │   └── user.proto
│   │   └── resources/
│   │       ├── application.yml            # Main configuration
│   │       └── logback.xml               # Logging configuration
│   └── test/                             # Test classes
├── docker-compose.yml                    # Infrastructure orchestration
├── mvnw / mvnw.cmd                      # Maven wrapper scripts
├── pom.xml                              # Maven project configuration
└── README.md                            # This documentation
```

### 3.2. Comparação com Estrutura .NET

| Java/Micronaut | .NET Core/ASP.NET | Descrição |
|----------------|-------------------|-----------|
| `src/main/java` | Raiz do projeto | Código fonte principal |
| `src/main/resources` | `appsettings.json`, `wwwroot` | Recursos e configurações |
| `src/test/java` | Projetos de teste | Testes unitários e integração |
| `pom.xml` | `.csproj` | Configuração do projeto e dependências |
| `Application.java` | `Program.cs` | Ponto de entrada da aplicação |
| `controller/` | `Controllers/` | Controladores REST |
| `service/` | `Services/` | Lógica de negócio |
| `repository/` | `Repositories/` ou `DbContext` | Acesso a dados |
| `dto/` | `Models/` ou `DTOs/` | Objetos de transferência de dados |
| `entity/` | `Entities/` | Entidades de domínio |

---


## 4. Conceitos Fundamentais do Micronaut

### 4.1. Application.java - O Ponto de Entrada

O arquivo `Application.java` é o coração da aplicação Micronaut, equivalente ao `Program.cs` no ASP.NET Core:

```java
package com.learning.micronaut;

import io.micronaut.runtime.Micronaut;

public class Application {
    public static void main(String[] args) {
        Micronaut.run(Application.class, args);
    }
}
```

**Análise Detalhada:**

**`Micronaut.run()`:** Este método inicializa o contexto da aplicação, similar ao `WebApplication.CreateBuilder(args).Build().Run()` no .NET. Internamente, ele:
- Escaneia o classpath em busca de beans anotados
- Resolve todas as dependências em tempo de compilação (não runtime como Spring)
- Configura o servidor HTTP (Netty por padrão)
- Inicializa todos os serviços singleton

**Diferenças Conceituais com .NET:**
- No .NET, você configura serviços explicitamente no `Program.cs` usando `builder.Services.AddScoped<>()`, `AddSingleton<>()`, etc.
- No Micronaut, a configuração é feita através de anotações nas próprias classes, e o framework descobre automaticamente os beans durante a compilação.

### 4.2. Configuração com application.yml

O arquivo `src/main/resources/application.yml` centraliza todas as configurações da aplicação:

```yaml
micronaut:
  application:
    name: micronaut-learning-project
  server:
    port: 8080
  executors:
    io:
      type: fixed
      nThreads: 75

# MongoDB Configuration
mongodb:
  uri: mongodb://micronaut_user:micronaut_password@localhost:27017/learning_db

# Kafka Configuration
kafka:
  bootstrap:
    servers: localhost:9092
  producers:
    default:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: io.confluent.kafka.serializers.protobuf.KafkaProtobufSerializer
      schema.registry.url: http://localhost:8081
  consumers:
    default:
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializer
      schema.registry.url: http://localhost:8081
      group-id: micronaut-learning-group
      auto-offset-reset: earliest

# Circuit Breaker Configuration
resilience4j:
  circuitbreaker:
    instances:
      external-service:
        sliding-window-size: 10
        minimum-number-of-calls: 5
        failure-rate-threshold: 50
        wait-duration-in-open-state: 5s
        permitted-number-of-calls-in-half-open-state: 3
  retry:
    instances:
      external-service:
        max-attempts: 3
        wait-duration: 1s
        exponential-backoff-multiplier: 2

# Tracing Configuration
tracing:
  jaeger:
    enabled: true
    sampler:
      probability: 1.0
    sender:
      endpoint: http://localhost:14268/api/traces

# Metrics Configuration
micrometer:
  metrics:
    enabled: true
    export:
      prometheus:
        enabled: true
        step: PT1M
        descriptions: true

# Management Endpoints
endpoints:
  all:
    enabled: true
    sensitive: false
  health:
    enabled: true
    details-visible: ANONYMOUS
  prometheus:
    enabled: true
```

**Comparação com appsettings.json (.NET):**

| Micronaut (YAML) | .NET (JSON) | Propósito |
|------------------|-------------|-----------|
| `micronaut.application.name` | `ApplicationName` | Nome da aplicação |
| `micronaut.server.port` | `Kestrel.Endpoints.Http.Url` | Porta do servidor |
| `mongodb.uri` | `ConnectionStrings.MongoDB` | String de conexão MongoDB |
| `kafka.bootstrap.servers` | `Kafka.BootstrapServers` | Servidores Kafka |
| `endpoints.health.enabled` | `HealthChecks` | Health checks |

**Vantagens do YAML sobre JSON:**
- Mais legível para configurações complexas
- Suporte nativo a comentários
- Hierarquia mais clara
- Menos verboso que JSON

### 4.3. Profiles e Ambientes

Micronaut suporta profiles similares aos do .NET, permitindo configurações específicas por ambiente:

```yaml
# application.yml (configuração base)
micronaut:
  application:
    name: micronaut-learning-project

---
# Profile de desenvolvimento
micronaut:
  environments: dev
  
logging:
  level:
    com.learning.micronaut: DEBUG
    
mongodb:
  uri: mongodb://localhost:27017/learning_db_dev

---
# Profile de produção
micronaut:
  environments: prod
  
logging:
  level:
    root: WARN
    com.learning.micronaut: INFO
    
mongodb:
  uri: ${MONGODB_URI:mongodb://localhost:27017/learning_db}
```

**Ativação de Profiles:**
```bash
# Via variável de ambiente
export MICRONAUT_ENVIRONMENTS=prod
./mvnw exec:java

# Via argumento JVM
./mvnw exec:java -Dmicronaut.environments=prod

# Via application.yml
micronaut:
  environments: dev,local
```

**Comparação com .NET:**
- No .NET: `appsettings.Development.json`, `appsettings.Production.json`
- No Micronaut: Seções separadas por `---` no mesmo arquivo ou arquivos separados (`application-dev.yml`)

---

## 5. Injeção de Dependência e Beans

### 5.1. Conceitos Fundamentais

O sistema de injeção de dependência do Micronaut é baseado no padrão JSR-330 e resolve todas as dependências em tempo de compilação, resultando em melhor performance e detecção precoce de erros.

### 5.2. Anotações de Escopo

**@Singleton - Instância Única**

Exemplo do projeto (`UserService.java`):
```java
@Singleton
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserEventProducer eventProducer;
    private final ObservabilityService observabilityService;
    
    // Métodos do serviço...
}
```

**Análise Detalhada:**
- **`@Singleton`:** Cria uma única instância da classe para toda a aplicação (equivale a `services.AddSingleton<UserService>()` no .NET)
- **`@RequiredArgsConstructor`:** Anotação do Lombok que gera automaticamente um construtor com todos os campos `final` (similar ao constructor injection no .NET)
- **`@Slf4j`:** Gera automaticamente um logger estático `log` (equivale a injetar `ILogger<UserService>` no .NET)

**@Prototype - Nova Instância**

```java
@Prototype
public class UserMapper {
    public UserDto toDto(User user) {
        // Conversão de entidade para DTO
    }
    
    public User toEntity(UserDto dto) {
        // Conversão de DTO para entidade
    }
}
```

**Comparação de Escopos:**

| Micronaut | .NET Core | Comportamento |
|-----------|-----------|---------------|
| `@Singleton` | `AddSingleton<>()` | Uma instância para toda aplicação |
| `@Prototype` | `AddTransient<>()` | Nova instância a cada injeção |
| `@RequestScope` | `AddScoped<>()` | Uma instância por requisição HTTP |

### 5.3. Injeção de Dependência na Prática

**Constructor Injection (Recomendado):**
```java
@Singleton
public class UserService {
    private final UserRepository userRepository;
    private final UserEventProducer eventProducer;
    
    // Lombok gera este construtor automaticamente
    public UserService(UserRepository userRepository, 
                      UserEventProducer eventProducer) {
        this.userRepository = userRepository;
        this.eventProducer = eventProducer;
    }
}
```

**Field Injection (Não Recomendado):**
```java
@Singleton
public class UserService {
    @Inject
    private UserRepository userRepository;
    
    @Inject
    private UserEventProducer eventProducer;
}
```

**Method Injection:**
```java
@Singleton
public class UserService {
    private UserRepository userRepository;
    
    @Inject
    public void setUserRepository(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
}
```

### 5.4. Qualificadores e Beans Nomeados

Quando você tem múltiplas implementações da mesma interface:

```java
// Interface
public interface NotificationService {
    void sendNotification(String message);
}

// Implementação por email
@Singleton
@Named("email")
public class EmailNotificationService implements NotificationService {
    @Override
    public void sendNotification(String message) {
        // Enviar por email
    }
}

// Implementação por SMS
@Singleton
@Named("sms")
public class SmsNotificationService implements NotificationService {
    @Override
    public void sendNotification(String message) {
        // Enviar por SMS
    }
}

// Uso com qualificador
@Singleton
public class UserService {
    private final NotificationService emailService;
    private final NotificationService smsService;
    
    public UserService(@Named("email") NotificationService emailService,
                      @Named("sms") NotificationService smsService) {
        this.emailService = emailService;
        this.smsService = smsService;
    }
}
```

**Comparação com .NET:**
```csharp
// .NET - Registro no Program.cs
builder.Services.AddSingleton<INotificationService, EmailNotificationService>();
builder.Services.AddSingleton<INotificationService, SmsNotificationService>();

// .NET - Uso com IServiceProvider
public class UserService
{
    private readonly IServiceProvider _serviceProvider;
    
    public UserService(IServiceProvider serviceProvider)
    {
        _serviceProvider = serviceProvider;
    }
    
    public void SendEmail()
    {
        var emailService = _serviceProvider.GetServices<INotificationService>()
            .OfType<EmailNotificationService>().First();
    }
}
```

### 5.5. Factory Beans

Para criação de beans mais complexos:

```java
@Factory
public class DatabaseConfiguration {
    
    @Singleton
    @Named("primary")
    public MongoClient primaryMongoClient(@Value("${mongodb.primary.uri}") String uri) {
        return MongoClients.create(uri);
    }
    
    @Singleton
    @Named("secondary")
    public MongoClient secondaryMongoClient(@Value("${mongodb.secondary.uri}") String uri) {
        return MongoClients.create(uri);
    }
}
```

**Equivalente no .NET:**
```csharp
// Program.cs
builder.Services.AddSingleton<IMongoClient>(provider =>
{
    var connectionString = builder.Configuration.GetConnectionString("MongoDB");
    return new MongoClient(connectionString);
});
```

### 5.6. Conditional Beans

Beans que são criados apenas sob certas condições:

```java
@Singleton
@Requires(property = "redis.enabled", value = "true")
public class RedisService {
    // Só é criado se redis.enabled=true na configuração
}

@Singleton
@Requires(env = "production")
public class ProductionOnlyService {
    // Só é criado no ambiente de produção
}

@Singleton
@Requires(beans = MongoClient.class)
public class MongoHealthIndicator {
    // Só é criado se MongoClient estiver disponível
}
```

---

## 6. Entidades e Persistência com MongoDB

### 6.1. Definição de Entidades

O arquivo `src/main/java/com/learning/micronaut/entity/User.java` demonstra como definir entidades MongoDB no Micronaut:

```java
@MappedEntity("users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue
    private String id;
    
    @NonNull
    private String name;
    
    @NonNull
    @Indexed(unique = true)
    private String email;
    
    @NonNull
    private Integer age;
    
    @NonNull
    private UserDto.UserStatus status;
    
    @CreatedDate
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    private LocalDateTime updatedAt;
    
    private List<String> tags;
    
    @Embedded
    private UserProfile profile;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserProfile {
        private String bio;
        private String avatarUrl;
        
        @Embedded
        private Address address;
        
        private List<SocialLink> socialLinks;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Address {
        private String street;
        private String city;
        private String state;
        private String zipCode;
        private String country;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SocialLink {
        private String platform;
        private String url;
    }
}
```

**Análise das Anotações:**

**Anotações do Micronaut Data:**
- **`@MappedEntity("users")`:** Define que esta classe mapeia para a coleção "users" no MongoDB (equivale ao `[Table("users")]` no Entity Framework)
- **`@Id`:** Marca o campo como chave primária (equivale ao `[Key]` no EF)
- **`@GeneratedValue`:** O valor é gerado automaticamente pelo banco (equivale ao `[DatabaseGenerated(DatabaseGeneratedOption.Identity)]`)
- **`@Indexed(unique = true)`:** Cria um índice único no campo (equivale ao `[Index(IsUnique = true)]`)
- **`@CreatedDate` / `@LastModifiedDate`:** Preenchidos automaticamente pelo Micronaut Data (similar ao `[DatabaseGenerated]` com triggers)
- **`@Embedded`:** Indica que o objeto é incorporado no documento principal (similar ao `[Owned]` no EF Core)

**Anotações do Lombok:**
- **`@Data`:** Gera getters, setters, toString, equals, hashCode automaticamente
- **`@Builder`:** Implementa o padrão Builder para criação de objetos
- **`@NoArgsConstructor` / `@AllArgsConstructor`:** Gera construtores sem argumentos e com todos os argumentos

**Comparação com Entity Framework Core:**
```csharp
[Table("users")]
public class User
{
    [Key]
    [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
    public string Id { get; set; }
    
    [Required]
    [MaxLength(100)]
    public string Name { get; set; }
    
    [Required]
    [EmailAddress]
    [Index(IsUnique = true)]
    public string Email { get; set; }
    
    [Range(0, 150)]
    public int Age { get; set; }
    
    public UserStatus Status { get; set; }
    
    [DatabaseGenerated(DatabaseGeneratedOption.Identity)]
    public DateTime CreatedAt { get; set; }
    
    public DateTime UpdatedAt { get; set; }
    
    public List<string> Tags { get; set; }
    
    [Owned]
    public UserProfile Profile { get; set; }
}
```

### 6.2. Repositórios com Micronaut Data

O arquivo `src/main/java/com/learning/micronaut/repository/UserRepository.java` demonstra o poder do Micronaut Data:

```java
@MongoRepository
public interface UserRepository extends ReactiveMongoRepository<User, String> {
    
    // Query methods - Micronaut gera a implementação automaticamente
    Publisher<User> findByEmail(String email);
    
    Publisher<User> findByStatus(UserDto.UserStatus status);
    
    Publisher<User> findByAgeGreaterThan(Integer age);
    
    Publisher<User> findByTagsContains(String tag);
    
    Publisher<User> findByProfileAddressCity(String city);
    
    // Query personalizada usando MongoDB Query Language
    @Query("{ 'profile.address.city': :city, 'status': :status }")
    Publisher<User> findByCityAndStatus(String city, UserDto.UserStatus status);
    
    // Aggregation pipeline
    @Aggregation(pipeline = [
        "{ $match: { status: :status } }",
        "{ $group: { _id: '$profile.address.city', count: { $sum: 1 } } }",
        "{ $sort: { count: -1 } }"
    ])
    Publisher<Map<String, Object>> countUsersByCity(UserDto.UserStatus status);
    
    // Update customizado
    @Query(value = "{ email: :email }", update = "{ $set: { status: :status, updatedAt: :updatedAt } }")
    Publisher<Long> updateStatusByEmail(String email, UserDto.UserStatus status, LocalDateTime updatedAt);
    
    // Delete customizado
    @Query("{ status: :status, createdAt: { $lt: :date } }")
    Publisher<Long> deleteInactiveUsersBefore(UserDto.UserStatus status, LocalDateTime date);
}
```

**Análise Detalhada:**

**Query Methods:** O Micronaut Data analisa o nome do método e gera automaticamente a query MongoDB correspondente:
- `findByEmail` → `{ "email": "valor" }`
- `findByAgeGreaterThan` → `{ "age": { "$gt": valor } }`
- `findByTagsContains` → `{ "tags": { "$in": ["valor"] } }`
- `findByProfileAddressCity` → `{ "profile.address.city": "valor" }`

**Programação Reativa:** Todos os métodos retornam `Publisher<T>` (do Project Reactor), que é similar ao `IAsyncEnumerable<T>` do .NET, mas com capacidades de streaming mais avançadas.

**Comparação com Entity Framework Core:**
```csharp
public interface IUserRepository
{
    Task<User> FindByEmailAsync(string email);
    Task<IEnumerable<User>> FindByStatusAsync(UserStatus status);
    Task<IEnumerable<User>> FindByAgeGreaterThanAsync(int age);
    
    // EF Core - LINQ Query
    IQueryable<User> GetUsersByCity(string city, UserStatus status)
    {
        return _context.Users
            .Where(u => u.Profile.Address.City == city && u.Status == status);
    }
    
    // EF Core - Raw SQL
    [FromSql("SELECT * FROM users WHERE profile_address_city = {0} AND status = {1}")]
    Task<List<User>> FindByCityAndStatusAsync(string city, UserStatus status);
}
```

### 6.3. Mapeamento entre Entidades e DTOs

O arquivo `src/main/java/com/learning/micronaut/service/UserMapper.java` demonstra conversões entre entidades e DTOs:

```java
@Singleton
public class UserMapper {
    
    public UserDto toDto(User user) {
        if (user == null) return null;
        
        return UserDto.builder()
            .id(user.getId())
            .name(user.getName())
            .email(user.getEmail())
            .age(user.getAge())
            .status(user.getStatus())
            .createdAt(user.getCreatedAt())
            .updatedAt(user.getUpdatedAt())
            .tags(user.getTags() != null ? new ArrayList<>(user.getTags()) : null)
            .profile(toProfileDto(user.getProfile()))
            .build();
    }
    
    public User toEntity(UserDto dto) {
        if (dto == null) return null;
        
        return User.builder()
            .id(dto.getId())
            .name(dto.getName())
            .email(dto.getEmail())
            .age(dto.getAge())
            .status(dto.getStatus())
            .createdAt(dto.getCreatedAt())
            .updatedAt(dto.getUpdatedAt())
            .tags(dto.getTags() != null ? new ArrayList<>(dto.getTags()) : null)
            .profile(toProfileEntity(dto.getProfile()))
            .build();
    }
    
    private UserProfileDto toProfileDto(User.UserProfile profile) {
        if (profile == null) return null;
        
        return UserProfileDto.builder()
            .bio(profile.getBio())
            .avatarUrl(profile.getAvatarUrl())
            .address(toAddressDto(profile.getAddress()))
            .socialLinks(profile.getSocialLinks() != null ? 
                profile.getSocialLinks().stream()
                    .map(this::toSocialLinkDto)
                    .collect(Collectors.toList()) : null)
            .build();
    }
    
    // Métodos auxiliares para conversão de objetos aninhados...
}
```

**Vantagens do Padrão Mapper:**
- **Separação de Responsabilidades:** Entidades representam o modelo de dados, DTOs representam contratos de API
- **Evolução Independente:** Mudanças no banco não afetam a API e vice-versa
- **Segurança:** DTOs podem omitir campos sensíveis
- **Performance:** DTOs podem ser otimizados para casos específicos

**Alternativas Automáticas:**
- **MapStruct:** Gera mappers automaticamente em tempo de compilação
- **ModelMapper:** Mapeamento por reflexão em runtime

**Comparação com .NET:**
```csharp
// AutoMapper no .NET
public class UserProfile : Profile
{
    public UserProfile()
    {
        CreateMap<User, UserDto>();
        CreateMap<UserDto, User>();
    }
}

// Uso
var userDto = _mapper.Map<UserDto>(user);
var user = _mapper.Map<User>(userDto);
```

---


## 7. APIs REST e Controllers

### 7.1. Estrutura do Controller

O arquivo `src/main/java/com/learning/micronaut/controller/UserController.java` demonstra como criar APIs REST no Micronaut:

```java
@Controller("/users")
@RequiredArgsConstructor
@Slf4j
@Validated
public class UserController {
    
    private final UserService userService;
    private final ObservabilityService observabilityService;
    
    @Get
    @Produces(MediaType.APPLICATION_JSON)
    public Publisher<ApiResponse<List<UserDto>>> getAllUsers() {
        log.info("Buscando todos os usuários");
        
        return userService.findAll()
            .collectList()
            .map(users -> ApiResponse.<List<UserDto>>builder()
                .success(true)
                .data(users)
                .message("Usuários encontrados com sucesso")
                .timestamp(LocalDateTime.now())
                .build())
            .doOnSuccess(response -> 
                observabilityService.recordApiRequest("GET", "/users", 200, 
                    System.currentTimeMillis() - startTime))
            .doOnError(error -> {
                log.error("Erro ao buscar usuários", error);
                observabilityService.recordApiRequest("GET", "/users", 500, 
                    System.currentTimeMillis() - startTime);
            });
    }
    
    @Get("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Publisher<ApiResponse<UserDto>> getUserById(@PathVariable String id) {
        log.info("Buscando usuário por ID: {}", id);
        
        return userService.findById(id)
            .map(user -> ApiResponse.<UserDto>builder()
                .success(true)
                .data(user)
                .message("Usuário encontrado")
                .timestamp(LocalDateTime.now())
                .build())
            .switchIfEmpty(Mono.fromCallable(() -> 
                ApiResponse.<UserDto>builder()
                    .success(false)
                    .message("Usuário não encontrado")
                    .timestamp(LocalDateTime.now())
                    .build()));
    }
    
    @Post
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Publisher<ApiResponse<UserDto>> createUser(@Body @Valid UserDto userDto) {
        log.info("Criando novo usuário: {}", userDto.getEmail());
        
        return userService.create(userDto)
            .map(createdUser -> ApiResponse.<UserDto>builder()
                .success(true)
                .data(createdUser)
                .message("Usuário criado com sucesso")
                .timestamp(LocalDateTime.now())
                .build())
            .doOnSuccess(response -> 
                log.info("Usuário criado: {}", response.getData().getId()));
    }
    
    @Put("/{id}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Publisher<ApiResponse<UserDto>> updateUser(
            @PathVariable String id, 
            @Body @Valid UserDto userDto) {
        
        log.info("Atualizando usuário: {}", id);
        
        return userService.update(id, userDto)
            .map(updatedUser -> ApiResponse.<UserDto>builder()
                .success(true)
                .data(updatedUser)
                .message("Usuário atualizado com sucesso")
                .timestamp(LocalDateTime.now())
                .build());
    }
    
    @Delete("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Publisher<ApiResponse<Void>> deleteUser(@PathVariable String id) {
        log.info("Removendo usuário: {}", id);
        
        return userService.delete(id)
            .then(Mono.fromCallable(() -> ApiResponse.<Void>builder()
                .success(true)
                .message("Usuário removido com sucesso")
                .timestamp(LocalDateTime.now())
                .build()));
    }
    
    @Get("/search")
    @Produces(MediaType.APPLICATION_JSON)
    public Publisher<ApiResponse<List<UserDto>>> searchUsers(
            @QueryValue @Nullable String email,
            @QueryValue @Nullable UserDto.UserStatus status,
            @QueryValue @Nullable String city,
            @QueryValue @Nullable String tag) {
        
        log.info("Buscando usuários com filtros - email: {}, status: {}, city: {}, tag: {}", 
                email, status, city, tag);
        
        return userService.search(email, status, city, tag)
            .collectList()
            .map(users -> ApiResponse.<List<UserDto>>builder()
                .success(true)
                .data(users)
                .message(String.format("Encontrados %d usuários", users.size()))
                .timestamp(LocalDateTime.now())
                .build());
    }
}
```

**Análise das Anotações REST:**

**Anotações de Roteamento:**
- **`@Controller("/users")`:** Define o controlador e o prefixo da rota (equivale ao `[ApiController]` + `[Route("users")]` no .NET)
- **`@Get`, `@Post`, `@Put`, `@Delete`:** Definem os métodos HTTP (equivalem ao `[HttpGet]`, `[HttpPost]`, etc.)
- **`@Get("/{id}")`:** Define parâmetros de rota (equivale ao `[HttpGet("{id}")]`)

**Anotações de Parâmetros:**
- **`@PathVariable`:** Extrai valor da URL (equivale ao `[FromRoute]` no .NET)
- **`@QueryValue`:** Extrai parâmetro de query string (equivale ao `[FromQuery]`)
- **`@Body`:** Extrai dados do corpo da requisição (equivale ao `[FromBody]`)
- **`@Valid`:** Valida o objeto usando Bean Validation (equivale ao `[Valid]` + ModelState no .NET)

**Anotações de Conteúdo:**
- **`@Produces(MediaType.APPLICATION_JSON)`:** Define o tipo de conteúdo da resposta (equivale ao `[Produces("application/json")]`)
- **`@Consumes(MediaType.APPLICATION_JSON)`:** Define o tipo de conteúdo aceito (equivale ao `[Consumes("application/json")]`)

### 7.2. Programação Reativa em Controllers

**Publisher vs Task:**
```java
// Micronaut - Reativo
public Publisher<ApiResponse<UserDto>> getUserById(String id) {
    return userService.findById(id)
        .map(user -> ApiResponse.success(user))
        .switchIfEmpty(Mono.fromCallable(() -> ApiResponse.notFound()));
}

// .NET - Async/Await
public async Task<ActionResult<ApiResponse<UserDto>>> GetUserById(string id)
{
    var user = await _userService.FindByIdAsync(id);
    if (user == null)
        return NotFound(ApiResponse.NotFound());
    
    return Ok(ApiResponse.Success(user));
}
```

**Vantagens da Programação Reativa:**
- **Backpressure:** Controle automático de fluxo quando o consumidor é mais lento que o produtor
- **Composição:** Operadores funcionais para transformar streams de dados
- **Eficiência:** Melhor uso de threads e recursos do sistema
- **Streaming:** Processamento de dados em tempo real

### 7.3. Validação de Dados

O projeto usa Bean Validation (JSR-303) para validar dados de entrada:

```java
// UserDto.java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDto {
    
    private String id;
    
    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 2, max = 100, message = "Nome deve ter entre 2 e 100 caracteres")
    private String name;
    
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve ter formato válido")
    private String email;
    
    @NotNull(message = "Idade é obrigatória")
    @Min(value = 0, message = "Idade deve ser maior ou igual a 0")
    @Max(value = 150, message = "Idade deve ser menor ou igual a 150")
    private Integer age;
    
    @NotNull(message = "Status é obrigatório")
    private UserStatus status;
    
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    @Size(max = 10, message = "Máximo 10 tags permitidas")
    private List<String> tags;
    
    @Valid
    private UserProfileDto profile;
    
    public enum UserStatus {
        ACTIVE, INACTIVE, SUSPENDED, PENDING
    }
}
```

**Comparação com .NET:**
```csharp
public class UserDto
{
    public string Id { get; set; }
    
    [Required(ErrorMessage = "Nome é obrigatório")]
    [StringLength(100, MinimumLength = 2, ErrorMessage = "Nome deve ter entre 2 e 100 caracteres")]
    public string Name { get; set; }
    
    [Required(ErrorMessage = "Email é obrigatório")]
    [EmailAddress(ErrorMessage = "Email deve ter formato válido")]
    public string Email { get; set; }
    
    [Required(ErrorMessage = "Idade é obrigatória")]
    [Range(0, 150, ErrorMessage = "Idade deve estar entre 0 e 150")]
    public int Age { get; set; }
    
    [Required(ErrorMessage = "Status é obrigatório")]
    public UserStatus Status { get; set; }
    
    public DateTime CreatedAt { get; set; }
    public DateTime UpdatedAt { get; set; }
    
    [MaxLength(10, ErrorMessage = "Máximo 10 tags permitidas")]
    public List<string> Tags { get; set; }
    
    [Valid]
    public UserProfileDto Profile { get; set; }
}
```

### 7.4. Tratamento de Erros

Micronaut oferece várias formas de tratar erros:

```java
@Error(global = true)
@Singleton
public class GlobalExceptionHandler {
    
    @Error
    public HttpResponse<ApiResponse<Void>> handleValidationException(
            HttpRequest request, 
            ConstraintViolationException ex) {
        
        List<String> errors = ex.getConstraintViolations()
            .stream()
            .map(violation -> violation.getPropertyPath() + ": " + violation.getMessage())
            .collect(Collectors.toList());
        
        ApiResponse<Void> response = ApiResponse.<Void>builder()
            .success(false)
            .message("Dados inválidos")
            .errors(errors)
            .timestamp(LocalDateTime.now())
            .build();
        
        return HttpResponse.badRequest(response);
    }
    
    @Error
    public HttpResponse<ApiResponse<Void>> handleNotFoundException(
            HttpRequest request, 
            EntityNotFoundException ex) {
        
        ApiResponse<Void> response = ApiResponse.<Void>builder()
            .success(false)
            .message(ex.getMessage())
            .timestamp(LocalDateTime.now())
            .build();
        
        return HttpResponse.notFound(response);
    }
    
    @Error
    public HttpResponse<ApiResponse<Void>> handleGenericException(
            HttpRequest request, 
            Exception ex) {
        
        log.error("Erro não tratado", ex);
        
        ApiResponse<Void> response = ApiResponse.<Void>builder()
            .success(false)
            .message("Erro interno do servidor")
            .timestamp(LocalDateTime.now())
            .build();
        
        return HttpResponse.serverError(response);
    }
}
```

**Comparação com .NET:**
```csharp
public class GlobalExceptionMiddleware
{
    public async Task InvokeAsync(HttpContext context, RequestDelegate next)
    {
        try
        {
            await next(context);
        }
        catch (ValidationException ex)
        {
            context.Response.StatusCode = 400;
            await context.Response.WriteAsync(JsonSerializer.Serialize(new
            {
                Success = false,
                Message = "Dados inválidos",
                Errors = ex.Errors
            }));
        }
        catch (NotFoundException ex)
        {
            context.Response.StatusCode = 404;
            await context.Response.WriteAsync(JsonSerializer.Serialize(new
            {
                Success = false,
                Message = ex.Message
            }));
        }
    }
}
```

---

## 8. APIs gRPC e Protocol Buffers

### 8.1. Definição de Contratos Protobuf

O arquivo `src/main/proto/user.proto` define os contratos gRPC:

```protobuf
syntax = "proto3";

package com.learning.micronaut.grpc;

option java_package = "com.learning.micronaut.grpc";
option java_outer_classname = "UserProto";

// Serviço gRPC para operações de usuário
service UserGrpcService {
    rpc GetUser(GetUserRequest) returns (UserResponse);
    rpc CreateUser(CreateUserRequest) returns (UserResponse);
    rpc UpdateUser(UpdateUserRequest) returns (UserResponse);
    rpc DeleteUser(DeleteUserRequest) returns (DeleteUserResponse);
    rpc ListUsers(ListUsersRequest) returns (stream UserResponse);
    rpc SearchUsers(SearchUsersRequest) returns (stream UserResponse);
}

// Mensagens de requisição
message GetUserRequest {
    string id = 1;
}

message CreateUserRequest {
    string name = 1;
    string email = 2;
    int32 age = 3;
    UserStatus status = 4;
    repeated string tags = 5;
    UserProfile profile = 6;
}

message UpdateUserRequest {
    string id = 1;
    string name = 2;
    string email = 3;
    int32 age = 4;
    UserStatus status = 5;
    repeated string tags = 6;
    UserProfile profile = 7;
}

message DeleteUserRequest {
    string id = 1;
}

message ListUsersRequest {
    int32 page = 1;
    int32 size = 2;
}

message SearchUsersRequest {
    string email = 1;
    UserStatus status = 2;
    string city = 3;
    string tag = 4;
}

// Mensagens de resposta
message UserResponse {
    string id = 1;
    string name = 2;
    string email = 3;
    int32 age = 4;
    UserStatus status = 5;
    repeated string tags = 6;
    UserProfile profile = 7;
    int64 created_at = 8;
    int64 updated_at = 9;
}

message DeleteUserResponse {
    bool success = 1;
    string message = 2;
}

// Tipos complexos
message UserProfile {
    string bio = 1;
    string avatar_url = 2;
    Address address = 3;
    repeated SocialLink social_links = 4;
}

message Address {
    string street = 1;
    string city = 2;
    string state = 3;
    string zip_code = 4;
    string country = 5;
}

message SocialLink {
    string platform = 1;
    string url = 2;
}

// Enums
enum UserStatus {
    ACTIVE = 0;
    INACTIVE = 1;
    SUSPENDED = 2;
    PENDING = 3;
}
```

**Vantagens do Protocol Buffers:**
- **Eficiência:** Serialização binária muito mais rápida que JSON
- **Tipagem Forte:** Contratos bem definidos e versionados
- **Compatibilidade:** Evolução de schema sem quebrar clientes existentes
- **Multiplataforma:** Suporte para Java, C#, Python, Go, etc.
- **Streaming:** Suporte nativo para streaming bidirecional

### 8.2. Implementação do Serviço gRPC

O arquivo `src/main/java/com/learning/micronaut/grpc/UserGrpcService.java`:

```java
@Singleton
@RequiredArgsConstructor
@Slf4j
public class UserGrpcService extends UserGrpcServiceGrpc.UserGrpcServiceImplBase {
    
    private final UserService userService;
    private final ObservabilityService observabilityService;
    
    @Override
    public void getUser(GetUserRequest request, StreamObserver<UserResponse> responseObserver) {
        log.info("gRPC: Buscando usuário por ID: {}", request.getId());
        
        userService.findById(request.getId())
            .map(this::toGrpcResponse)
            .subscribe(
                response -> {
                    responseObserver.onNext(response);
                    responseObserver.onCompleted();
                    observabilityService.recordApiRequest("gRPC", "GetUser", 200, 
                        System.currentTimeMillis() - startTime);
                },
                error -> {
                    log.error("Erro ao buscar usuário via gRPC", error);
                    responseObserver.onError(Status.NOT_FOUND
                        .withDescription("Usuário não encontrado")
                        .asRuntimeException());
                    observabilityService.recordApiRequest("gRPC", "GetUser", 404, 
                        System.currentTimeMillis() - startTime);
                }
            );
    }
    
    @Override
    public void createUser(CreateUserRequest request, StreamObserver<UserResponse> responseObserver) {
        log.info("gRPC: Criando usuário: {}", request.getEmail());
        
        UserDto userDto = fromGrpcRequest(request);
        
        userService.create(userDto)
            .map(this::toGrpcResponse)
            .subscribe(
                response -> {
                    responseObserver.onNext(response);
                    responseObserver.onCompleted();
                },
                error -> {
                    log.error("Erro ao criar usuário via gRPC", error);
                    responseObserver.onError(Status.INVALID_ARGUMENT
                        .withDescription("Dados inválidos: " + error.getMessage())
                        .asRuntimeException());
                }
            );
    }
    
    @Override
    public void listUsers(ListUsersRequest request, StreamObserver<UserResponse> responseObserver) {
        log.info("gRPC: Listando usuários - página: {}, tamanho: {}", 
                request.getPage(), request.getSize());
        
        userService.findAll()
            .map(this::toGrpcResponse)
            .subscribe(
                response -> responseObserver.onNext(response),
                error -> {
                    log.error("Erro ao listar usuários via gRPC", error);
                    responseObserver.onError(Status.INTERNAL
                        .withDescription("Erro interno")
                        .asRuntimeException());
                },
                () -> {
                    responseObserver.onCompleted();
                    log.info("gRPC: Listagem de usuários concluída");
                }
            );
    }
    
    @Override
    public void searchUsers(SearchUsersRequest request, StreamObserver<UserResponse> responseObserver) {
        log.info("gRPC: Buscando usuários com filtros");
        
        userService.search(
                request.getEmail().isEmpty() ? null : request.getEmail(),
                request.getStatus() == UserProto.UserStatus.ACTIVE ? UserDto.UserStatus.ACTIVE : null,
                request.getCity().isEmpty() ? null : request.getCity(),
                request.getTag().isEmpty() ? null : request.getTag()
            )
            .map(this::toGrpcResponse)
            .subscribe(
                response -> responseObserver.onNext(response),
                error -> responseObserver.onError(Status.INTERNAL
                    .withDescription("Erro na busca")
                    .asRuntimeException()),
                () -> responseObserver.onCompleted()
            );
    }
    
    // Métodos de conversão entre DTOs e mensagens Protobuf
    private UserResponse toGrpcResponse(UserDto userDto) {
        UserResponse.Builder builder = UserResponse.newBuilder()
            .setId(userDto.getId())
            .setName(userDto.getName())
            .setEmail(userDto.getEmail())
            .setAge(userDto.getAge())
            .setStatus(toGrpcStatus(userDto.getStatus()))
            .setCreatedAt(userDto.getCreatedAt().toEpochSecond(ZoneOffset.UTC))
            .setUpdatedAt(userDto.getUpdatedAt().toEpochSecond(ZoneOffset.UTC));
        
        if (userDto.getTags() != null) {
            builder.addAllTags(userDto.getTags());
        }
        
        if (userDto.getProfile() != null) {
            builder.setProfile(toGrpcProfile(userDto.getProfile()));
        }
        
        return builder.build();
    }
    
    private UserDto fromGrpcRequest(CreateUserRequest request) {
        return UserDto.builder()
            .name(request.getName())
            .email(request.getEmail())
            .age(request.getAge())
            .status(fromGrpcStatus(request.getStatus()))
            .tags(new ArrayList<>(request.getTagsList()))
            .profile(fromGrpcProfile(request.getProfile()))
            .build();
    }
    
    // Métodos auxiliares para conversão de enums e objetos complexos...
}
```

**Comparação com gRPC no .NET:**
```csharp
public class UserGrpcService : UserGrpcService.UserGrpcServiceBase
{
    private readonly IUserService _userService;
    
    public override async Task<UserResponse> GetUser(GetUserRequest request, ServerCallContext context)
    {
        var user = await _userService.FindByIdAsync(request.Id);
        if (user == null)
        {
            throw new RpcException(new Status(StatusCode.NotFound, "Usuário não encontrado"));
        }
        
        return ToGrpcResponse(user);
    }
    
    public override async Task ListUsers(ListUsersRequest request, 
        IServerStreamWriter<UserResponse> responseStream, ServerCallContext context)
    {
        var users = _userService.FindAllAsync();
        
        await foreach (var user in users)
        {
            await responseStream.WriteAsync(ToGrpcResponse(user));
        }
    }
}
```

### 8.3. Configuração gRPC

No `application.yml`:

```yaml
grpc:
  server:
    port: 9090
    enable-reflection: true
  client:
    plaintext: true
    max-retry-attempts: 3
```

**Testando APIs gRPC:**

```bash
# Usando grpcurl (instalar primeiro)
# Listar serviços
grpcurl -plaintext localhost:9090 list

# Chamar método
grpcurl -plaintext -d '{"id": "user123"}' \
  localhost:9090 com.learning.micronaut.grpc.UserGrpcService/GetUser

# Streaming
grpcurl -plaintext -d '{"page": 1, "size": 10}' \
  localhost:9090 com.learning.micronaut.grpc.UserGrpcService/ListUsers
```

---

## 9. Mensageria com Kafka

### 9.1. Definição de Eventos Protobuf

O arquivo `src/main/proto/events.proto` define os eventos Kafka:

```protobuf
syntax = "proto3";

package com.learning.micronaut.events;

option java_package = "com.learning.micronaut.events";
option java_outer_classname = "EventsProto";

// Evento de usuário criado
message UserCreatedEvent {
    string user_id = 1;
    string name = 2;
    string email = 3;
    int32 age = 4;
    UserStatus status = 5;
    int64 created_at = 6;
    string source = 7;
    map<string, string> metadata = 8;
}

// Evento de usuário atualizado
message UserUpdatedEvent {
    string user_id = 1;
    string name = 2;
    string email = 3;
    int32 age = 4;
    UserStatus status = 5;
    int64 updated_at = 6;
    repeated string changed_fields = 7;
    string source = 8;
    map<string, string> metadata = 9;
}

// Evento de usuário removido
message UserDeletedEvent {
    string user_id = 1;
    string email = 2;
    int64 deleted_at = 3;
    string reason = 4;
    string source = 5;
    map<string, string> metadata = 6;
}

// Evento de notificação
message NotificationEvent {
    string id = 1;
    string user_id = 2;
    NotificationType type = 3;
    string title = 4;
    string message = 5;
    map<string, string> data = 6;
    int64 created_at = 7;
    bool urgent = 8;
}

// Enums
enum UserStatus {
    ACTIVE = 0;
    INACTIVE = 1;
    SUSPENDED = 2;
    PENDING = 3;
}

enum NotificationType {
    EMAIL = 0;
    SMS = 1;
    PUSH = 2;
    IN_APP = 3;
}
```

### 9.2. Produtor Kafka

O arquivo `src/main/java/com/learning/micronaut/kafka/UserEventProducer.java`:

```java
@KafkaClient
@RequiredArgsConstructor
@Slf4j
public interface UserEventProducer {
    
    @Topic("user-events")
    @SendTo
    Publisher<RecordMetadata> sendUserCreatedEvent(
        @KafkaKey String userId,
        UserCreatedEvent event
    );
    
    @Topic("user-events")
    @SendTo
    Publisher<RecordMetadata> sendUserUpdatedEvent(
        @KafkaKey String userId,
        UserUpdatedEvent event
    );
    
    @Topic("user-events")
    @SendTo
    Publisher<RecordMetadata> sendUserDeletedEvent(
        @KafkaKey String userId,
        UserDeletedEvent event
    );
    
    @Topic("notifications")
    @SendTo
    Publisher<RecordMetadata> sendNotificationEvent(
        @KafkaKey String notificationId,
        NotificationEvent event
    );
}

// Implementação do serviço que usa o produtor
@Singleton
@RequiredArgsConstructor
@Slf4j
public class UserEventService {
    
    private final UserEventProducer eventProducer;
    private final ObservabilityService observabilityService;
    
    public Mono<Void> publishUserCreated(UserDto user) {
        UserCreatedEvent event = UserCreatedEvent.newBuilder()
            .setUserId(user.getId())
            .setName(user.getName())
            .setEmail(user.getEmail())
            .setAge(user.getAge())
            .setStatus(toEventStatus(user.getStatus()))
            .setCreatedAt(user.getCreatedAt().toEpochSecond(ZoneOffset.UTC))
            .setSource("user-service")
            .putMetadata("version", "1.0")
            .putMetadata("environment", "development")
            .build();
        
        return Mono.fromPublisher(eventProducer.sendUserCreatedEvent(user.getId(), event))
            .doOnSuccess(metadata -> {
                log.info("Evento UserCreated publicado - tópico: {}, partição: {}, offset: {}",
                    metadata.topic(), metadata.partition(), metadata.offset());
                observabilityService.recordKafkaMessage("user-events", "UserCreated", true, 
                    System.currentTimeMillis() - startTime);
            })
            .doOnError(error -> {
                log.error("Erro ao publicar evento UserCreated", error);
                observabilityService.recordKafkaMessage("user-events", "UserCreated", false, 
                    System.currentTimeMillis() - startTime);
            })
            .then();
    }
    
    public Mono<Void> publishUserUpdated(UserDto user, List<String> changedFields) {
        UserUpdatedEvent event = UserUpdatedEvent.newBuilder()
            .setUserId(user.getId())
            .setName(user.getName())
            .setEmail(user.getEmail())
            .setAge(user.getAge())
            .setStatus(toEventStatus(user.getStatus()))
            .setUpdatedAt(user.getUpdatedAt().toEpochSecond(ZoneOffset.UTC))
            .addAllChangedFields(changedFields)
            .setSource("user-service")
            .putMetadata("version", "1.0")
            .build();
        
        return Mono.fromPublisher(eventProducer.sendUserUpdatedEvent(user.getId(), event))
            .doOnSuccess(metadata -> 
                log.info("Evento UserUpdated publicado para usuário: {}", user.getId()))
            .then();
    }
    
    public Mono<Void> publishUserDeleted(String userId, String email, String reason) {
        UserDeletedEvent event = UserDeletedEvent.newBuilder()
            .setUserId(userId)
            .setEmail(email)
            .setDeletedAt(Instant.now().getEpochSecond())
            .setReason(reason)
            .setSource("user-service")
            .putMetadata("version", "1.0")
            .build();
        
        return Mono.fromPublisher(eventProducer.sendUserDeletedEvent(userId, event))
            .doOnSuccess(metadata -> 
                log.info("Evento UserDeleted publicado para usuário: {}", userId))
            .then();
    }
}
```

**Comparação com .NET (Confluent.Kafka):**
```csharp
public class UserEventProducer
{
    private readonly IProducer<string, UserCreatedEvent> _producer;
    
    public async Task PublishUserCreatedAsync(UserDto user)
    {
        var @event = new UserCreatedEvent
        {
            UserId = user.Id,
            Name = user.Name,
            Email = user.Email,
            Age = user.Age,
            Status = user.Status,
            CreatedAt = user.CreatedAt.ToUnixTimeSeconds(),
            Source = "user-service"
        };
        
        var message = new Message<string, UserCreatedEvent>
        {
            Key = user.Id,
            Value = @event
        };
        
        var result = await _producer.ProduceAsync("user-events", message);
        _logger.LogInformation("Evento publicado - tópico: {Topic}, partição: {Partition}, offset: {Offset}",
            result.Topic, result.Partition, result.Offset);
    }
}
```

### 9.3. Consumidor Kafka

O arquivo `src/main/java/com/learning/micronaut/kafka/UserEventConsumer.java`:

```java
@KafkaListener(groupId = "user-event-processor")
@RequiredArgsConstructor
@Slf4j
public class UserEventConsumer {
    
    private final ObservabilityService observabilityService;
    private final ExternalService externalService;
    
    @Topic("user-events")
    public void handleUserCreatedEvent(
            @KafkaKey String userId,
            UserCreatedEvent event,
            @Header Map<String, Object> headers) {
        
        long startTime = System.currentTimeMillis();
        
        try {
            log.info("Processando evento UserCreated - usuário: {}, email: {}", 
                    event.getUserId(), event.getEmail());
            
            // Processar evento de usuário criado
            processUserCreatedEvent(event);
            
            // Registrar métricas de sucesso
            observabilityService.recordKafkaMessage("user-events", "UserCreated", true, 
                System.currentTimeMillis() - startTime);
            
            log.info("Evento UserCreated processado com sucesso - usuário: {}", event.getUserId());
            
        } catch (Exception e) {
            log.error("Erro ao processar evento UserCreated - usuário: {}", event.getUserId(), e);
            
            // Registrar métricas de erro
            observabilityService.recordKafkaMessage("user-events", "UserCreated", false, 
                System.currentTimeMillis() - startTime);
            
            // Em produção, você poderia enviar para uma Dead Letter Queue
            handleEventProcessingError(userId, event, e);
        }
    }
    
    @Topic("user-events")
    public void handleUserUpdatedEvent(
            @KafkaKey String userId,
            UserUpdatedEvent event) {
        
        try {
            log.info("Processando evento UserUpdated - usuário: {}, campos alterados: {}", 
                    event.getUserId(), event.getChangedFieldsList());
            
            // Processar evento de usuário atualizado
            processUserUpdatedEvent(event);
            
            // Se email foi alterado, validar com serviço externo
            if (event.getChangedFieldsList().contains("email")) {
                validateEmailAsync(event.getEmail());
            }
            
            log.info("Evento UserUpdated processado com sucesso - usuário: {}", event.getUserId());
            
        } catch (Exception e) {
            log.error("Erro ao processar evento UserUpdated - usuário: {}", event.getUserId(), e);
            handleEventProcessingError(userId, event, e);
        }
    }
    
    @Topic("user-events")
    public void handleUserDeletedEvent(
            @KafkaKey String userId,
            UserDeletedEvent event) {
        
        try {
            log.info("Processando evento UserDeleted - usuário: {}, motivo: {}", 
                    event.getUserId(), event.getReason());
            
            // Processar evento de usuário removido
            processUserDeletedEvent(event);
            
            // Limpar dados relacionados
            cleanupUserData(event.getUserId());
            
            log.info("Evento UserDeleted processado com sucesso - usuário: {}", event.getUserId());
            
        } catch (Exception e) {
            log.error("Erro ao processar evento UserDeleted - usuário: {}", event.getUserId(), e);
            handleEventProcessingError(userId, event, e);
        }
    }
    
    private void processUserCreatedEvent(UserCreatedEvent event) {
        // Lógica específica para usuário criado
        // Exemplo: enviar email de boas-vindas, criar perfil em outros sistemas, etc.
        
        // Simular processamento
        try {
            Thread.sleep(100); // Simular trabalho
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        
        // Enviar notificação de boas-vindas
        sendWelcomeNotification(event);
    }
    
    private void processUserUpdatedEvent(UserUpdatedEvent event) {
        // Lógica específica para usuário atualizado
        // Exemplo: sincronizar com outros sistemas, invalidar cache, etc.
        
        // Se status mudou para INACTIVE, processar desativação
        if (event.getChangedFieldsList().contains("status") && 
            event.getStatus() == EventsProto.UserStatus.INACTIVE) {
            processUserDeactivation(event);
        }
    }
    
    private void processUserDeletedEvent(UserDeletedEvent event) {
        // Lógica específica para usuário removido
        // Exemplo: limpar dados, notificar outros sistemas, etc.
        
        log.info("Limpando dados do usuário removido: {}", event.getUserId());
    }
    
    private void validateEmailAsync(String email) {
        // Validar email usando serviço externo de forma assíncrona
        externalService.validateEmail(email)
            .subscribe(
                isValid -> log.info("Validação de email: {} - válido: {}", email, isValid),
                error -> log.warn("Erro na validação de email: {}", email, error)
            );
    }
    
    private void sendWelcomeNotification(UserCreatedEvent event) {
        // Enviar notificação de boas-vindas
        log.info("Enviando notificação de boas-vindas para: {}", event.getEmail());
    }
    
    private void processUserDeactivation(UserUpdatedEvent event) {
        // Processar desativação do usuário
        log.info("Processando desativação do usuário: {}", event.getUserId());
    }
    
    private void cleanupUserData(String userId) {
        // Limpar dados relacionados ao usuário
        log.info("Limpando dados relacionados ao usuário: {}", userId);
    }
    
    private void handleEventProcessingError(String userId, Object event, Exception error) {
        // Em produção, você poderia:
        // 1. Enviar para Dead Letter Queue
        // 2. Registrar em sistema de monitoramento
        // 3. Tentar reprocessar após um delay
        // 4. Notificar equipe de suporte
        
        log.error("Falha no processamento do evento - usuário: {}, evento: {}", 
                userId, event.getClass().getSimpleName(), error);
    }
}
```

**Comparação com .NET:**
```csharp
[KafkaConsumer("user-event-processor")]
public class UserEventConsumer
{
    [Topic("user-events")]
    public async Task HandleUserCreatedEvent(string userId, UserCreatedEvent @event)
    {
        try
        {
            _logger.LogInformation("Processando evento UserCreated - usuário: {UserId}", @event.UserId);
            
            await ProcessUserCreatedEventAsync(@event);
            
            _logger.LogInformation("Evento processado com sucesso - usuário: {UserId}", @event.UserId);
        }
        catch (Exception ex)
        {
            _logger.LogError(ex, "Erro ao processar evento UserCreated - usuário: {UserId}", @event.UserId);
            throw; // Para reprocessamento ou DLQ
        }
    }
}
```

### 9.4. Configuração Kafka Avançada

```yaml
kafka:
  bootstrap:
    servers: localhost:9092
  
  # Configurações do produtor
  producers:
    default:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: io.confluent.kafka.serializers.protobuf.KafkaProtobufSerializer
      schema.registry.url: http://localhost:8081
      # Configurações de performance
      acks: all                    # Aguardar confirmação de todas as réplicas
      retries: 3                   # Número de tentativas em caso de falha
      batch.size: 16384           # Tamanho do batch em bytes
      linger.ms: 5                # Tempo de espera para formar batch
      buffer.memory: 33554432     # Buffer de memória do produtor
      compression.type: snappy    # Compressão das mensagens
      
  # Configurações do consumidor
  consumers:
    default:
      key-deserializer: org.apache.kafka.common.serialization.StringDeserializer
      value-deserializer: io.confluent.kafka.serializers.protobuf.KafkaProtobufDeserializer
      schema.registry.url: http://localhost:8081
      group-id: micronaut-learning-group
      auto-offset-reset: earliest
      enable-auto-commit: false    # Commit manual para maior controle
      max-poll-records: 100       # Máximo de registros por poll
      session-timeout-ms: 30000   # Timeout da sessão
      heartbeat-interval-ms: 3000 # Intervalo de heartbeat
      
    # Consumidor específico para processamento em lote
    batch-processor:
      group-id: batch-processor-group
      max-poll-records: 1000
      fetch-min-bytes: 1024
      fetch-max-wait-ms: 500
```

---


## 10. Circuit Breaker e Resiliência

### 10.1. Implementação com Resilience4j

O arquivo `src/main/java/com/learning/micronaut/service/ExternalService.java` demonstra padrões de resiliência:

```java
@Singleton
@RequiredArgsConstructor
@Slf4j
public class ExternalService {
    
    @Client("${app.external.service.url}")
    private final HttpClient httpClient;
    
    @CircuitBreaker(name = "external-service", fallbackMethod = "validateEmailFallback")
    @Retry(name = "external-service")
    @NewSpan("external-service.validate-email")
    public Mono<Boolean> validateEmail(@SpanTag("email") String email) {
        log.debug("Validando email via serviço externo: {}", email);
        
        return Mono.fromCallable(() -> {
            // Simular diferentes cenários para demonstrar Circuit Breaker
            simulateExternalServiceBehavior();
            return callExternalEmailValidationService(email);
        })
        .timeout(Duration.ofSeconds(3))
        .doOnSuccess(result -> 
            log.info("Email validado com sucesso: {} -> {}", email, result))
        .doOnError(error -> 
            log.warn("Erro na validação de email: {} -> {}", email, error.getMessage()));
    }
    
    public Mono<Boolean> validateEmailFallback(String email, Exception exception) {
        log.warn("Usando fallback para validação de email: {} - motivo: {}", 
                email, exception.getMessage());
        
        // Estratégia de fallback: validação básica local
        boolean isValid = email != null && 
                         email.contains("@") && 
                         email.contains(".") &&
                         email.length() > 5;
        
        log.info("Validação de email via fallback: {} -> {}", email, isValid);
        return Mono.just(isValid);
    }
}
```

**Configuração do Circuit Breaker:**

```yaml
resilience4j:
  circuitbreaker:
    instances:
      external-service:
        # Configurações da janela deslizante
        sliding-window-size: 10                    # Janela de 10 chamadas
        minimum-number-of-calls: 5                 # Mínimo 5 chamadas para avaliar
        failure-rate-threshold: 50                 # Abre se 50% falharem
        slow-call-rate-threshold: 50               # Considera chamadas lentas
        slow-call-duration-threshold: 2s           # Chamadas > 2s são lentas
        
        # Configurações de estado
        wait-duration-in-open-state: 5s            # Aguarda 5s antes de tentar half-open
        permitted-number-of-calls-in-half-open-state: 3  # 3 chamadas no estado half-open
        
        # Configurações de transição automática
        automatic-transition-from-open-to-half-open-enabled: true
        
        # Configurações de registro
        register-health-indicator: true
        record-exceptions:
          - java.io.IOException
          - java.util.concurrent.TimeoutException
        ignore-exceptions:
          - java.lang.IllegalArgumentException
          
  retry:
    instances:
      external-service:
        max-attempts: 3                           # Máximo 3 tentativas
        wait-duration: 1s                         # Aguarda 1s entre tentativas
        exponential-backoff-multiplier: 2         # Backoff exponencial
        retry-exceptions:
          - java.io.IOException
          - java.util.concurrent.TimeoutException
```

**Estados do Circuit Breaker:**

1. **CLOSED (Fechado):** Estado normal, todas as chamadas passam
2. **OPEN (Aberto):** Circuito aberto, chamadas falham imediatamente e vão para fallback
3. **HALF_OPEN (Meio-Aberto):** Permite algumas chamadas para testar se o serviço se recuperou

**Comparação com Polly (.NET):**

```csharp
// .NET com Polly
var circuitBreakerPolicy = Policy
    .Handle<HttpRequestException>()
    .Or<TimeoutException>()
    .CircuitBreakerAsync(
        handledEventsAllowedBeforeBreaking: 5,
        durationOfBreak: TimeSpan.FromSeconds(30),
        onBreak: (exception, duration) => 
        {
            _logger.LogWarning("Circuit breaker opened for {Duration}s", duration.TotalSeconds);
        },
        onReset: () => 
        {
            _logger.LogInformation("Circuit breaker closed");
        });

var retryPolicy = Policy
    .Handle<HttpRequestException>()
    .WaitAndRetryAsync(
        retryCount: 3,
        sleepDurationProvider: retryAttempt => TimeSpan.FromSeconds(Math.Pow(2, retryAttempt)),
        onRetry: (outcome, delay, retryCount, context) =>
        {
            _logger.LogWarning("Retry {RetryCount} after {Delay}s", retryCount, delay.TotalSeconds);
        });

var combinedPolicy = Policy.WrapAsync(circuitBreakerPolicy, retryPolicy);

// Uso
var result = await combinedPolicy.ExecuteAsync(async () =>
{
    return await _httpClient.GetStringAsync("https://external-api.com/validate");
});
```

### 10.2. Padrões de Resiliência Implementados

**Timeout:** Evita esperas indefinidas
```java
.timeout(Duration.ofSeconds(3))
```

**Bulkhead:** Isolamento de recursos (implícito via thread pools)
```yaml
micronaut:
  executors:
    io:
      type: fixed
      nThreads: 75
    external-calls:
      type: fixed
      nThreads: 10  # Pool dedicado para chamadas externas
```

**Rate Limiting:** Controle de taxa de requisições
```java
@RateLimiter(name = "external-service")
public Mono<Boolean> validateEmail(String email) {
    // Implementação
}
```

---

## 11. Observabilidade e Monitoramento

### 11.1. Tracing Distribuído com OpenTelemetry

O arquivo `src/main/java/com/learning/micronaut/observability/ObservabilityService.java` demonstra instrumentação manual:

```java
@Singleton
@RequiredArgsConstructor
@Slf4j
public class ObservabilityService {
    
    private final MeterRegistry meterRegistry;
    private final OpenTelemetry openTelemetry;
    private final Tracer tracer;
    
    @NewSpan("observability.user-created")
    public void recordUserCreated(@SpanTag("userId") String userId, 
                                 @SpanTag("userEmail") String userEmail,
                                 @SpanTag("source") String source) {
        
        log.info("Registrando criação de usuário: {} via {}", userId, source);
        
        // Incrementar contador com tags
        userCreatedCounter.increment(
            "source", source,
            "domain", extractDomain(userEmail)
        );
        
        // Adicionar informações ao span atual
        Span currentSpan = Span.current();
        currentSpan.setAttribute("user.id", userId);
        currentSpan.setAttribute("user.email", userEmail);
        currentSpan.setAttribute("operation.source", source);
        currentSpan.addEvent("User created successfully");
        
        log.debug("Métricas de criação de usuário registradas");
    }
    
    public Span createCustomSpan(String operationName, Map<String, String> attributes) {
        Span span = tracer.spanBuilder(operationName).startSpan();
        
        // Adicionar atributos customizados
        if (attributes != null) {
            attributes.forEach(span::setAttribute);
        }
        
        log.debug("Span customizado criado: {} com {} atributos", operationName, 
                 attributes != null ? attributes.size() : 0);
        
        return span;
    }
    
    public Timer.Sample startDatabaseOperation(String operation, String collection) {
        log.debug("Iniciando operação de banco: {} em {}", operation, collection);
        
        // Criar span manual para operação de banco
        Span span = tracer.spanBuilder("database." + operation)
                .setSpanKind(SpanKind.CLIENT)
                .setAttribute("db.system", "mongodb")
                .setAttribute("db.collection.name", collection)
                .setAttribute("db.operation", operation)
                .startSpan();
        
        return Timer.start(meterRegistry);
    }
}
```

**Instrumentação Automática:**

O Micronaut instrumenta automaticamente:
- **HTTP Requests/Responses:** Cada requisição gera um span
- **Database Operations:** Queries MongoDB são rastreadas
- **Kafka Messages:** Produção e consumo de mensagens
- **External HTTP Calls:** Chamadas para APIs externas

**Visualização no Jaeger:**

Acesse `http://localhost:16686` para ver:
- **Traces:** Fluxo completo de uma requisição
- **Spans:** Operações individuais dentro de um trace
- **Dependencies:** Mapa de dependências entre serviços
- **Performance:** Latência e gargalos

### 11.2. Métricas com Micrometer

**Tipos de Métricas:**

```java
// Contadores - valores que só aumentam
private final Counter userCreatedCounter = Counter.builder("users.created")
    .description("Número total de usuários criados")
    .register(meterRegistry);

// Gauges - valores que podem subir e descer
Gauge.builder("active.connections")
    .description("Número de conexões ativas")
    .register(meterRegistry, this, obj -> getActiveConnections());

// Timers - medição de duração
private final Timer databaseOperationTimer = Timer.builder("database.operations")
    .description("Tempo de operações de banco de dados")
    .register(meterRegistry);

// Histogramas - distribuição de valores
DistributionSummary.builder("request.size")
    .description("Tamanho das requisições")
    .register(meterRegistry);
```

**Métricas Automáticas do Micronaut:**

- `http_server_requests_total` - Total de requisições HTTP
- `http_server_requests_seconds` - Duração das requisições
- `jvm_memory_used_bytes` - Uso de memória JVM
- `jvm_gc_pause_seconds` - Pausas do Garbage Collector
- `process_cpu_usage` - Uso de CPU do processo
- `kafka_producer_record_send_total` - Mensagens Kafka enviadas

### 11.3. Interceptor de Observabilidade

O arquivo `src/main/java/com/learning/micronaut/observability/ObservabilityInterceptor.java` demonstra AOP:

```java
@Singleton
@InterceptorBean(Observed.class)
@RequiredArgsConstructor
@Slf4j
public class ObservabilityInterceptor implements MethodInterceptor<Object, Object> {
    
    private final ObservabilityService observabilityService;
    
    @Override
    @Nullable
    public Object intercept(MethodInvocationContext<Object, Object> context) {
        String className = context.getDeclaringType().getSimpleName();
        String methodName = context.getMethodName();
        String operationName = className + "." + methodName;
        
        // Obter configuração da anotação
        Observed annotation = context.getAnnotation(Observed.class);
        String customName = annotation != null && !annotation.value().isEmpty() 
            ? annotation.value() 
            : operationName;
        
        long startTime = System.currentTimeMillis();
        Span span = null;
        boolean success = false;
        
        try {
            // Criar span se rastreamento estiver habilitado
            if (annotation == null || annotation.recordTrace()) {
                span = observabilityService.createCustomSpan(
                    "method." + customName,
                    Map.of(
                        "class.name", className,
                        "method.name", methodName
                    )
                );
            }
            
            // Executar o método original
            Object result = context.proceed();
            success = true;
            
            if (span != null && result != null) {
                span.setAttribute("method.result_type", result.getClass().getSimpleName());
                span.addEvent("Method executed successfully");
            }
            
            return result;
            
        } catch (Throwable t) {
            success = false;
            
            if (span != null) {
                span.setAttribute("method.error", true);
                span.setAttribute("error.type", t.getClass().getSimpleName());
                span.setAttribute("error.message", t.getMessage());
                span.addEvent("Method execution failed");
            }
            
            throw t;
            
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            
            if (span != null) {
                span.setAttribute("method.duration_ms", duration);
                span.setAttribute("method.success", success);
                span.end();
            }
            
            log.debug("Método {} executado em {}ms - sucesso: {}", 
                     operationName, duration, success);
        }
    }
}

// Anotação customizada para observabilidade
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Observed {
    String value() default "";
    boolean recordMetrics() default true;
    boolean recordTrace() default true;
}
```

**Uso da Anotação:**

```java
@Singleton
public class UserService {
    
    @Observed("user-creation")
    public Mono<UserDto> create(UserDto userDto) {
        // Implementação
    }
    
    @Observed(recordMetrics = false)
    public Mono<UserDto> findById(String id) {
        // Implementação
    }
}
```

**Comparação com .NET:**

```csharp
// .NET com System.Diagnostics
public class UserService
{
    private static readonly ActivitySource ActivitySource = new("UserService");
    
    public async Task<UserDto> CreateAsync(UserDto userDto)
    {
        using var activity = ActivitySource.StartActivity("user-creation");
        activity?.SetTag("user.email", userDto.Email);
        
        try
        {
            var result = await CreateUserInternalAsync(userDto);
            activity?.SetTag("user.id", result.Id);
            return result;
        }
        catch (Exception ex)
        {
            activity?.SetStatus(ActivityStatusCode.Error, ex.Message);
            throw;
        }
    }
}
```

### 11.4. Dashboards Grafana

O projeto inclui dashboards pré-configurados em `docker/grafana/dashboards/`:

**Métricas Principais:**
- Taxa de requisições HTTP por endpoint
- Latência P50, P95, P99 das requisições
- Taxa de erro por status code
- Uso de CPU e memória JVM
- Operações de banco de dados
- Mensagens Kafka produzidas/consumidas
- Estado dos Circuit Breakers

**Alertas Configurados:**
- Taxa de erro > 5%
- Latência P95 > 1 segundo
- Uso de memória > 80%
- Circuit Breaker aberto
- Falhas na conexão com MongoDB/Kafka

---

## 12. Configuração e Profiles

### 12.1. Configuração Hierárquica

```yaml
# application.yml - Configuração base
micronaut:
  application:
    name: micronaut-learning-project
  server:
    port: 8080

# Configurações comuns
logging:
  level:
    root: INFO

---
# Profile de desenvolvimento
micronaut:
  environments: dev

logging:
  level:
    com.learning.micronaut: DEBUG
    org.mongodb.driver: DEBUG

mongodb:
  uri: mongodb://localhost:27017/learning_db_dev

kafka:
  bootstrap:
    servers: localhost:9092

tracing:
  jaeger:
    enabled: true
    sampler:
      probability: 1.0  # 100% das requisições

---
# Profile de teste
micronaut:
  environments: test

mongodb:
  uri: mongodb://localhost:27017/learning_db_test

kafka:
  bootstrap:
    servers: localhost:9092

tracing:
  jaeger:
    enabled: false  # Desabilitar tracing em testes

---
# Profile de produção
micronaut:
  environments: prod

logging:
  level:
    root: WARN
    com.learning.micronaut: INFO

mongodb:
  uri: ${MONGODB_URI}

kafka:
  bootstrap:
    servers: ${KAFKA_BOOTSTRAP_SERVERS}

tracing:
  jaeger:
    enabled: true
    sampler:
      probability: 0.1  # 10% das requisições

# Configurações de segurança para produção
micronaut:
  server:
    cors:
      enabled: false
  security:
    enabled: true
```

### 12.2. Configuração Tipada

```java
@ConfigurationProperties("app.external")
@Data
public class ExternalServiceConfiguration {
    
    private String serviceUrl = "https://api.external.com";
    private Duration timeout = Duration.ofSeconds(5);
    private int maxRetries = 3;
    private boolean enabled = true;
    
    @ConfigurationProperties("circuit-breaker")
    @Data
    public static class CircuitBreakerConfig {
        private int slidingWindowSize = 10;
        private int minimumNumberOfCalls = 5;
        private float failureRateThreshold = 50.0f;
        private Duration waitDurationInOpenState = Duration.ofSeconds(5);
    }
}

// Uso
@Singleton
@RequiredArgsConstructor
public class ExternalService {
    private final ExternalServiceConfiguration config;
    
    public void callExternalService() {
        if (!config.isEnabled()) {
            return;
        }
        
        // Usar config.getServiceUrl(), config.getTimeout(), etc.
    }
}
```

**Comparação com .NET:**

```csharp
// appsettings.json
{
  "ExternalService": {
    "ServiceUrl": "https://api.external.com",
    "Timeout": "00:00:05",
    "MaxRetries": 3,
    "Enabled": true,
    "CircuitBreaker": {
      "SlidingWindowSize": 10,
      "MinimumNumberOfCalls": 5,
      "FailureRateThreshold": 50.0
    }
  }
}

// C# Configuration
public class ExternalServiceConfiguration
{
    public string ServiceUrl { get; set; } = "https://api.external.com";
    public TimeSpan Timeout { get; set; } = TimeSpan.FromSeconds(5);
    public int MaxRetries { get; set; } = 3;
    public bool Enabled { get; set; } = true;
    public CircuitBreakerConfig CircuitBreaker { get; set; } = new();
}

// Program.cs
builder.Services.Configure<ExternalServiceConfiguration>(
    builder.Configuration.GetSection("ExternalService"));
```

---

## 13. Build e Deploy

### 13.1. Build com Maven

**Estrutura do pom.xml:**

```xml
<properties>
    <maven.compiler.source>17</maven.compiler.source>
    <maven.compiler.target>17</maven.compiler.target>
    <micronaut.version>4.2.1</micronaut.version>
    <protobuf.version>3.25.1</protobuf.version>
</properties>

<dependencies>
    <!-- Micronaut Core -->
    <dependency>
        <groupId>io.micronaut</groupId>
        <artifactId>micronaut-http-server-netty</artifactId>
    </dependency>
    
    <!-- MongoDB -->
    <dependency>
        <groupId>io.micronaut.mongodb</groupId>
        <artifactId>micronaut-mongo-reactive</artifactId>
    </dependency>
    
    <!-- Kafka -->
    <dependency>
        <groupId>io.micronaut.kafka</groupId>
        <artifactId>micronaut-kafka</artifactId>
    </dependency>
    
    <!-- Observabilidade -->
    <dependency>
        <groupId>io.micronaut.tracing</groupId>
        <artifactId>micronaut-tracing-opentelemetry-jaeger</artifactId>
    </dependency>
</dependencies>

<build>
    <plugins>
        <!-- Compiler Plugin com Annotation Processing -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-compiler-plugin</artifactId>
            <configuration>
                <annotationProcessorPaths>
                    <path>
                        <groupId>io.micronaut</groupId>
                        <artifactId>micronaut-inject-java</artifactId>
                        <version>${micronaut.version}</version>
                    </path>
                    <path>
                        <groupId>org.projectlombok</groupId>
                        <artifactId>lombok</artifactId>
                        <version>${lombok.version}</version>
                    </path>
                </annotationProcessorPaths>
            </configuration>
        </plugin>
        
        <!-- Protobuf Plugin -->
        <plugin>
            <groupId>org.xolstice.maven.plugins</groupId>
            <artifactId>protobuf-maven-plugin</artifactId>
            <executions>
                <execution>
                    <goals>
                        <goal>compile</goal>
                        <goal>compile-custom</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
        
        <!-- Shade Plugin para JAR executável -->
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-shade-plugin</artifactId>
            <executions>
                <execution>
                    <phase>package</phase>
                    <goals>
                        <goal>shade</goal>
                    </goals>
                    <configuration>
                        <transformers>
                            <transformer implementation="org.apache.maven.plugins.shade.resource.ManifestResourceTransformer">
                                <mainClass>com.learning.micronaut.Application</mainClass>
                            </transformer>
                        </transformers>
                    </configuration>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

### 13.2. Scripts de Build

**Build Completo:**
```bash
./scripts/build-application.sh

# Ou manualmente:
./mvnw clean compile
./mvnw protobuf:compile protobuf:compile-custom
./mvnw test
./mvnw package
```

**Build Nativo com GraalVM:**
```bash
./scripts/build-application.sh --native

# Ou manualmente:
./mvnw package -Pnative
```

### 13.3. Containerização

**Dockerfile:**
```dockerfile
FROM openjdk:17-jre-slim

WORKDIR /app

# Copiar JAR da aplicação
COPY target/micronaut-learning-project-0.1.0.jar app.jar

# Configurar usuário não-root
RUN addgroup --system micronaut && adduser --system --group micronaut
USER micronaut

# Expor porta
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
  CMD curl -f http://localhost:8080/health || exit 1

# Comando de execução
ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Docker Compose para Aplicação:**
```yaml
version: '3.8'
services:
  micronaut-app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - MICRONAUT_ENVIRONMENTS=docker
      - MONGODB_URI=mongodb://mongodb:27017/learning_db
      - KAFKA_BOOTSTRAP_SERVERS=kafka:29092
      - JAEGER_ENDPOINT=http://jaeger:14268/api/traces
    depends_on:
      - mongodb
      - kafka
      - jaeger
    networks:
      - micronaut-network
```

---

## 14. Scripts Utilitários

### 14.1. Scripts de Infraestrutura

**start-infrastructure.sh:**
- Inicia todos os serviços Docker em ordem
- Aguarda cada serviço ficar pronto antes de iniciar o próximo
- Verifica health checks
- Exibe URLs de acesso

**stop-infrastructure.sh:**
- Para todos os containers
- Opção `--clean` remove volumes e dados persistentes
- Remove redes Docker criadas

**check-services.sh:**
- Verifica status de todos os serviços
- Testa conectividade HTTP e TCP
- Mostra uso de recursos dos containers
- Lista volumes e redes

### 14.2. Scripts de Aplicação

**run-application.sh:**
- Verifica se infraestrutura está rodando
- Compila o projeto
- Gera classes Protobuf
- Executa a aplicação com logs detalhados

**run-tests.sh:**
- Executa testes unitários e de integração
- Gera relatórios de cobertura
- Suporte para testes específicos

**build-application.sh:**
- Build completo com testes
- Cria JAR executável
- Opção para build nativo com GraalVM
- Mostra estatísticas do build

### 14.3. Scripts de Kafka

**setup-kafka-topics.sh:**
- Cria tópicos necessários para a aplicação
- Configura partições e replicação
- Lista tópicos criados
- Fornece comandos para monitoramento

---

## 15. Comparações Detalhadas com .NET

### 15.1. Conceitos Fundamentais

| Conceito | Java/Micronaut | .NET/ASP.NET Core | Observações |
|----------|----------------|-------------------|-------------|
| **Ponto de Entrada** | `Application.java` com `Micronaut.run()` | `Program.cs` com `WebApplication.CreateBuilder()` | Ambos configuram e iniciam a aplicação |
| **Injeção de Dependência** | Anotações (`@Singleton`, `@Inject`) | `IServiceCollection` no `Program.cs` | Micronaut resolve em compile-time, .NET em runtime |
| **Configuração** | `application.yml` | `appsettings.json` | YAML vs JSON, ambos suportam profiles/ambientes |
| **Controllers** | `@Controller`, `@Get`, `@Post` | `[ApiController]`, `[HttpGet]`, `[HttpPost]` | Sintaxe similar, conceitos idênticos |
| **Validação** | Bean Validation (`@Valid`, `@NotNull`) | Data Annotations (`[Required]`, `[Valid]`) | Padrões muito similares |
| **Middleware** | `HttpServerFilter` | `Middleware` | Interceptação de requisições HTTP |

### 15.2. Persistência de Dados

| Aspecto | Micronaut Data | Entity Framework Core | Observações |
|---------|----------------|----------------------|-------------|
| **Query Methods** | `findByEmail(String email)` | `FirstOrDefaultAsync(u => u.Email == email)` | Micronaut gera queries por nome, EF usa LINQ |
| **Mapeamento** | `@MappedEntity`, `@Id` | `[Table]`, `[Key]` | Anotações vs Atributos |
| **Relacionamentos** | `@Embedded`, `@Relation` | `[Owned]`, navegação automática | Conceitos similares |
| **Migrations** | Não suportado nativamente | `Add-Migration`, `Update-Database` | EF tem melhor suporte para migrations |
| **Programação Reativa** | `Publisher<T>`, `Mono<T>` | `IAsyncEnumerable<T>`, `Task<T>` | Micronaut mais focado em streams |

### 15.3. Mensageria

| Aspecto | Micronaut Kafka | .NET com Confluent.Kafka | Observações |
|---------|-----------------|--------------------------|-------------|
| **Produtor** | `@KafkaClient`, `@Topic` | `IProducer<TKey, TValue>` | Micronaut mais declarativo |
| **Consumidor** | `@KafkaListener`, `@Topic` | `IConsumer<TKey, TValue>` | Micronaut com anotações, .NET mais imperativo |
| **Serialização** | Protobuf automático | Configuração manual | Micronaut integra melhor com Schema Registry |
| **Error Handling** | Interceptors, DLQ automático | Try/catch manual | Micronaut oferece mais abstrações |

### 15.4. Observabilidade

| Aspecto | Micronaut | .NET | Observações |
|---------|-----------|------|-------------|
| **Tracing** | OpenTelemetry + `@NewSpan` | `System.Diagnostics.Activity` | Padrões similares, OpenTelemetry é comum |
| **Métricas** | Micrometer | `System.Diagnostics.Metrics` | Conceitos idênticos |
| **Logging** | SLF4J + Logback | `ILogger<T>` + Serilog | Estruturas similares |
| **Health Checks** | `@Endpoint` | `IHealthCheck` | Implementações similares |

### 15.5. Resiliência

| Padrão | Micronaut (Resilience4j) | .NET (Polly) | Observações |
|--------|--------------------------|--------------|-------------|
| **Circuit Breaker** | `@CircuitBreaker` | `CircuitBreakerPolicy` | Anotações vs Policies |
| **Retry** | `@Retry` | `RetryPolicy` | Configuração similar |
| **Timeout** | `@TimeLimiter` | `TimeoutPolicy` | Conceitos idênticos |
| **Bulkhead** | `@Bulkhead` | `BulkheadPolicy` | Isolamento de recursos |

---

## 16. Conclusão e Próximos Passos

### 16.1. O que Você Aprendeu

Ao explorar este projeto, você teve contato com os principais conceitos do desenvolvimento Java moderno:

**Arquitetura de Microserviços:** Implementação de um serviço completo com APIs REST e gRPC, persistência, mensageria e observabilidade.

**Micronaut Framework:** Compreensão das vantagens da compilação AOT, injeção de dependência em compile-time e programação reativa nativa.

**Padrões de Resiliência:** Implementação de Circuit Breaker, Retry, Timeout e Fallback para criar sistemas robustos.

**Observabilidade Moderna:** Instrumentação com OpenTelemetry, métricas com Micrometer e visualização com Jaeger e Grafana.

**Mensageria Assíncrona:** Uso do Kafka com serialização Protobuf para comunicação entre serviços.

**Persistência NoSQL:** Integração com MongoDB usando Micronaut Data e programação reativa.

### 16.2. Vantagens do Micronaut para Desenvolvedores .NET

**Familiaridade:** Muitos conceitos são similares ao ASP.NET Core, facilitando a transição.

**Performance:** Startup rápido e baixo consumo de memória, ideal para containers e cloud.

**Produtividade:** Menos configuração manual, mais convenções inteligentes.

**Ecossistema:** Integração nativa com tecnologias modernas (Kafka, MongoDB, OpenTelemetry).

**GraalVM:** Possibilidade de compilar para binários nativos, eliminando a JVM.

### 16.3. Próximos Passos Recomendados

**1. Explore Funcionalidades Avançadas:**
- Implementar autenticação e autorização com JWT
- Adicionar cache distribuído com Redis
- Implementar rate limiting e throttling
- Criar testes de carga com JMeter ou Gatling

**2. Deploy em Produção:**
- Configurar CI/CD com GitHub Actions ou Jenkins
- Deploy em Kubernetes com Helm charts
- Configurar monitoramento com Prometheus e Alertmanager
- Implementar logging centralizado com ELK Stack

**3. Otimizações de Performance:**
- Compilar para imagem nativa com GraalVM
- Implementar connection pooling otimizado
- Configurar cache de queries MongoDB
- Otimizar serialização Protobuf

**4. Segurança:**
- Implementar OAuth2/OpenID Connect
- Configurar HTTPS e certificados
- Implementar audit logging
- Adicionar validação de entrada robusta

**5. Testes Avançados:**
- Testes de contrato com Pact
- Testes de performance automatizados
- Testes de caos com Chaos Monkey
- Testes de segurança com OWASP ZAP

### 16.4. Recursos Adicionais

**Documentação Oficial:**
- [Micronaut Documentation](https://docs.micronaut.io/)
- [Micronaut Guides](https://guides.micronaut.io/)
- [Micronaut Data Documentation](https://micronaut-projects.github.io/micronaut-data/latest/guide/)

**Comunidade:**
- [Micronaut Gitter](https://gitter.im/micronautfw/questions)
- [Stack Overflow - Micronaut](https://stackoverflow.com/questions/tagged/micronaut)
- [GitHub - Micronaut Examples](https://github.com/micronaut-projects/micronaut-examples)

**Cursos e Tutoriais:**
- [Baeldung Micronaut Tutorials](https://www.baeldung.com/micronaut)
- [Micronaut University](https://micronaut.io/training/)
- [YouTube - Micronaut Deep Dive](https://www.youtube.com/c/MicronautFramework)

### 16.5. Considerações Finais

Este projeto demonstra que a transição de .NET para Java/Micronaut não é apenas possível, mas também produtiva. Os conceitos fundamentais de desenvolvimento de software são universais, e as diferenças são principalmente sintáticas.

O Micronaut oferece uma experiência de desenvolvimento moderna que rivaliza com o ASP.NET Core em termos de produtividade e supera em alguns aspectos como performance de startup e consumo de memória.

A escolha entre .NET e Java/Micronaut deve ser baseada em fatores como:
- **Ecossistema existente** da sua organização
- **Expertise da equipe** de desenvolvimento
- **Requisitos específicos** do projeto (performance, integrações, etc.)
- **Estratégia de cloud** e containerização

Independentemente da escolha, os padrões e práticas demonstrados neste projeto são aplicáveis em ambos os ecossistemas, tornando você um desenvolvedor mais versátil e preparado para os desafios da arquitetura de microserviços moderna.

---

**Desenvolvido com ❤️ para a comunidade de desenvolvedores**

*Este projeto é open source e está disponível para contribuições. Sinta-se à vontade para abrir issues, sugerir melhorias ou contribuir com código.*

