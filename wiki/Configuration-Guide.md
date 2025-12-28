# ⚙️ Configuration Guide - Guia de Configuração

Este guia detalha todas as configurações disponíveis no Buddy API, incluindo propriedades da aplicação, variáveis de ambiente e configurações de serviços externos.

## 📁 Arquivos de Configuração

### Estrutura de Configuração

```
src/main/resources/
├── application.yml              # Configuração base
├── application-local.yml        # Configuração para desenvolvimento local
├── application-dev.yml          # Configuração para ambiente de desenvolvimento
├── application-prod.yml         # Configuração para produção
└── bootstrap.yml                # Configuração bootstrap (Spring Cloud Config)
```

## 🔧 application.yml (Base)

### Configuração Mínima

```yaml
spring:
  profiles:
    active: local  # Perfil ativo padrão

  application:
    name: buddy-api
```

## 💻 application-local.yml (Desenvolvimento Local)

### Banco de Dados

```yaml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/buddydb
    username: buddyuser
    password: buddypassword
    driver-class-name: org.postgresql.Driver
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
      idle-timeout: 300000
      connection-timeout: 20000
      max-lifetime: 1200000

  jpa:
    hibernate:
      ddl-auto: validate  # Nunca use 'update' ou 'create-drop' em produção!
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        dialect: org.hibernate.dialect.PostgreSQLDialect
        jdbc:
          time_zone: UTC
```

**Propriedades Importantes**:
- `ddl-auto: validate` - Valida o schema sem alterá-lo
- `show-sql: true` - Mostra SQL no console (apenas para desenvolvimento)
- `format_sql: true` - Formata SQL para melhor legibilidade

### Flyway (Migrações)

```yaml
spring:
  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration
    schemas: public
    table: flyway_schema_history
```

### Redis (Cache)

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
      password:  # Vazio para desenvolvimento local
      timeout: 2000ms
      jedis:
        pool:
          max-active: 8
          max-idle: 8
          min-idle: 0
          max-wait: -1ms
  
  cache:
    type: redis
    redis:
      time-to-live: 600000  # 10 minutos em millisegundos
      cache-null-values: false
```

### Spring Security

```yaml
spring:
  security:
    user:
      name: admin
      password: admin  # MUDAR EM PRODUÇÃO!

buddy:
  security:
    jwt:
      secret: ${JWT_SECRET:sua-chave-secreta-muito-longa-e-segura-aqui-minimo-256-bits}
      expiration: 86400000  # 24 horas em millisegundos
      issuer: buddy-api
```

**⚠️ IMPORTANTE**: 
- A chave JWT deve ter pelo menos 256 bits (32 caracteres)
- NUNCA commite a chave secreta no código
- Use variáveis de ambiente em produção

### Logging

```yaml
logging:
  level:
    root: INFO
    com.buddy.api: DEBUG
    org.springframework: INFO
    org.springframework.security: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

### Actuator (Monitoramento)

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
      base-path: /api/actuator
  
  endpoint:
    health:
      show-details: always
      probes:
        enabled: true
  
  info:
    env:
      enabled: true
    git:
      enabled: true
      mode: full
```

### Server

```yaml
server:
  port: 8080
  servlet:
    context-path: /api
  
  error:
    include-message: always
    include-binding-errors: always
    include-stacktrace: never  # Mudar para 'always' apenas em dev
    include-exception: false

  compression:
    enabled: true
    mime-types: application/json,application/xml,text/html,text/xml,text/plain
```

## 🚀 application-prod.yml (Produção)

### Configurações Críticas para Produção

```yaml
spring:
  datasource:
    url: ${DATABASE_URL}
    username: ${DATABASE_USERNAME}
    password: ${DATABASE_PASSWORD}
    hikari:
      maximum-pool-size: 20
      minimum-idle: 10
  
  jpa:
    show-sql: false  # Desabilitar em produção
    properties:
      hibernate:
        format_sql: false
  
  data:
    redis:
      host: ${REDIS_HOST}
      port: ${REDIS_PORT}
      password: ${REDIS_PASSWORD}

buddy:
  security:
    jwt:
      secret: ${JWT_SECRET}
      expiration: 3600000  # 1 hora em produção

logging:
  level:
    root: WARN
    com.buddy.api: INFO
  file:
    name: /var/log/buddy-api/application.log
    max-size: 10MB
    max-history: 30

server:
  error:
    include-stacktrace: never
    include-exception: false
```

## 🌍 Variáveis de Ambiente

### Lista Completa de Variáveis

| Variável | Descrição | Obrigatória | Default | Exemplo |
|----------|-----------|-------------|---------|---------|
| `SPRING_PROFILES_ACTIVE` | Perfil do Spring ativo | Sim | local | prod |
| `DATABASE_URL` | URL do PostgreSQL | Sim | - | jdbc:postgresql://localhost:5432/buddydb |
| `DATABASE_USERNAME` | Usuário do banco | Sim | - | buddyuser |
| `DATABASE_PASSWORD` | Senha do banco | Sim | - | sua-senha |
| `REDIS_HOST` | Host do Redis | Sim | localhost | redis.example.com |
| `REDIS_PORT` | Porta do Redis | Não | 6379 | 6379 |
| `REDIS_PASSWORD` | Senha do Redis | Não | - | sua-senha-redis |
| `JWT_SECRET` | Chave secreta JWT | Sim | - | min-256-bits-secret |
| `JWT_EXPIRATION` | Tempo de expiração JWT (ms) | Não | 86400000 | 3600000 |
| `SERVER_PORT` | Porta do servidor | Não | 8080 | 8080 |
| `LOG_LEVEL` | Nível de log | Não | INFO | DEBUG |
| `MANAGER_SERVICE_URL` | URL do Manager Service | Sim | - | https://manager.example.com |

### Configurar Variáveis Localmente

#### Linux/macOS

```bash
# Criar arquivo .env
cat > .env << 'EOF'
export SPRING_PROFILES_ACTIVE=local
export DATABASE_URL=jdbc:postgresql://localhost:5432/buddydb
export DATABASE_USERNAME=buddyuser
export DATABASE_PASSWORD=buddypassword
export REDIS_HOST=localhost
export REDIS_PORT=6379
export JWT_SECRET=sua-chave-secreta-muito-longa-e-segura-aqui-minimo-256-bits
export MANAGER_SERVICE_URL=http://localhost:8089
EOF

# Carregar variáveis
source .env

# Executar a aplicação
./gradlew bootRun
```

#### Windows (PowerShell)

```powershell
# Definir variáveis
$env:SPRING_PROFILES_ACTIVE="local"
$env:DATABASE_URL="jdbc:postgresql://localhost:5432/buddydb"
$env:DATABASE_USERNAME="buddyuser"
$env:DATABASE_PASSWORD="buddypassword"
$env:JWT_SECRET="sua-chave-secreta-muito-longa-e-segura-aqui-minimo-256-bits"

# Executar a aplicação
.\gradlew.bat bootRun
```

#### Docker Compose

```yaml
# compose.yml
services:
  buddy-api:
    image: buddy-api:latest
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - DATABASE_URL=jdbc:postgresql://postgres:5432/buddydb
      - DATABASE_USERNAME=buddyuser
      - DATABASE_PASSWORD=buddypassword
      - REDIS_HOST=redis
      - REDIS_PORT=6379
      - JWT_SECRET=${JWT_SECRET}
```

## 🔌 Configurações de Integrações

### Feign Clients

```yaml
feign:
  client:
    config:
      default:
        connectTimeout: 5000
        readTimeout: 10000
        loggerLevel: full
      
      manager-service:
        url: ${MANAGER_SERVICE_URL:http://localhost:8089}
        connectTimeout: 3000
        readTimeout: 5000

spring:
  cloud:
    openfeign:
      client:
        config:
          manager-service:
            url: ${MANAGER_SERVICE_URL}
```

### Email Configuration (Futuro)

```yaml
spring:
  mail:
    host: smtp.gmail.com
    port: 587
    username: ${EMAIL_USERNAME}
    password: ${EMAIL_PASSWORD}
    properties:
      mail:
        smtp:
          auth: true
          starttls:
            enable: true
```

## 📊 Configurações de Performance

### Connection Pooling (HikariCP)

```yaml
spring:
  datasource:
    hikari:
      # Pool size
      maximum-pool-size: 20        # Máximo de conexões
      minimum-idle: 5              # Mínimo de conexões ociosas
      
      # Timeouts
      connection-timeout: 30000    # 30 segundos
      idle-timeout: 600000         # 10 minutos
      max-lifetime: 1800000        # 30 minutos
      
      # Health check
      connection-test-query: SELECT 1
      validation-timeout: 5000
      
      # Pool name
      pool-name: BuddyHikariPool
```

### JVM Options

```bash
# Desenvolvimento
JAVA_OPTS="-Xms512m -Xmx1024m -XX:+UseG1GC"

# Produção
JAVA_OPTS="-Xms2g -Xmx4g -XX:+UseG1GC -XX:MaxGCPauseMillis=200 -XX:+HeapDumpOnOutOfMemoryError"
```

## 🔒 Configurações de Segurança

### CORS

```java
@Configuration
public class CorsConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                    .allowedOrigins(
                        "http://localhost:3000",
                        "https://buddyclient.vercel.app"
                    )
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH")
                    .allowedHeaders("*")
                    .allowCredentials(true)
                    .maxAge(3600);
            }
        };
    }
}
```

### Rate Limiting

```yaml
buddy:
  rate-limit:
    enabled: true
    requests-per-minute: 100
    redis-key-prefix: "rate-limit:"
```

## 🐳 Docker Configuration

### Dockerfile

```dockerfile
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY build/libs/app.jar app.jar
EXPOSE 8080

# Healthcheck
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/api/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
```

### docker-compose.yml Completo

```yaml
version: '3.8'

services:
  postgres:
    image: postgres:15-alpine
    container_name: buddy-postgres
    environment:
      POSTGRES_DB: buddydb
      POSTGRES_USER: buddyuser
      POSTGRES_PASSWORD: buddypassword
    ports:
      - "5432:5432"
    volumes:
      - postgres-data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U buddyuser"]
      interval: 10s
      timeout: 5s
      retries: 5

  redis:
    image: redis:7-alpine
    container_name: buddy-redis
    ports:
      - "6379:6379"
    volumes:
      - redis-data:/data
    healthcheck:
      test: ["CMD", "redis-cli", "ping"]
      interval: 10s
      timeout: 3s
      retries: 5

  buddy-api:
    build: .
    container_name: buddy-api
    depends_on:
      postgres:
        condition: service_healthy
      redis:
        condition: service_healthy
    environment:
      SPRING_PROFILES_ACTIVE: prod
      DATABASE_URL: jdbc:postgresql://postgres:5432/buddydb
      DATABASE_USERNAME: buddyuser
      DATABASE_PASSWORD: buddypassword
      REDIS_HOST: redis
      REDIS_PORT: 6379
      JWT_SECRET: ${JWT_SECRET}
    ports:
      - "8080:8080"
    healthcheck:
      test: ["CMD", "wget", "--no-verbose", "--tries=1", "--spider", "http://localhost:8080/api/actuator/health"]
      interval: 30s
      timeout: 5s
      retries: 3

volumes:
  postgres-data:
  redis-data:
```

## 🧪 Configurações de Teste

### application-test.yml

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    driver-class-name: org.h2.Driver
    username: sa
    password:
  
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true
  
  flyway:
    enabled: false  # Desabilitar Flyway para testes com H2

  data:
    redis:
      host: localhost
      port: 6379  # Embedded Redis para testes

logging:
  level:
    com.buddy.api: DEBUG
```

## 📝 Boas Práticas

### 1. Nunca Commite Secrets

```bash
# .gitignore
.env
*.env
**/application-local.yml
**/application-prod.yml
```

### 2. Use Spring Cloud Config (Opcional)

```yaml
# bootstrap.yml
spring:
  cloud:
    config:
      uri: http://config-server:8888
      fail-fast: true
      retry:
        initial-interval: 1000
        max-attempts: 6
```

### 3. Externalize Configuration

Ordem de precedência (do maior para o menor):
1. Argumentos de linha de comando
2. Variáveis de ambiente
3. Propriedades do sistema Java
4. `application-{profile}.yml`
5. `application.yml`

### 4. Validação de Configuração

```java
@Configuration
@Validated
public class JwtProperties {
    @NotBlank
    @Value("${buddy.security.jwt.secret}")
    private String secret;
    
    @Positive
    @Value("${buddy.security.jwt.expiration}")
    private long expiration;
}
```

## 🔍 Troubleshooting

### Verificar Configurações Ativas

```bash
# Via Actuator
curl http://localhost:8080/api/actuator/env

# Ver propriedade específica
curl http://localhost:8080/api/actuator/env/spring.datasource.url
```

### Logs de Configuração

```yaml
logging:
  level:
    org.springframework.boot.context.config: DEBUG
    org.springframework.cloud.config: DEBUG
```

## 📚 Próximos Passos

- **[Getting Started](./Getting-Started.md)** - Guia de início rápido
- **[Deployment Guide](./Deployment-Guide.md)** - Como fazer deploy
- **[Troubleshooting](./Troubleshooting.md)** - Solução de problemas

---

**Mantido por**: @hywenklis | **Última atualização**: Dezembro 2024
