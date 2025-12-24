# 🚀 Getting Started - Guia de Início Rápido

Este guia irá ajudá-lo a configurar e executar o Buddy API em seu ambiente local em poucos minutos.

## 📋 Pré-requisitos

Antes de começar, certifique-se de ter instalado:

### Obrigatórios

- **Java 21** ou superior ([Download JDK](https://adoptium.net/))
- **Docker** e **Docker Compose** ([Download Docker](https://www.docker.com/get-started))
- **Git** ([Download Git](https://git-scm.com/downloads))

### Recomendados

- **IntelliJ IDEA** ou outra IDE Java ([Download IntelliJ](https://www.jetbrains.com/idea/download/))
- **Postman** ou **Insomnia** para testar a API ([Download Postman](https://www.postman.com/downloads/))
- **PostgreSQL Client** (opcional) - DBeaver, pgAdmin, etc.

## 🔧 Instalação

### Passo 1: Clonar o Repositório

```bash
# Clone o repositório
git clone https://github.com/hywenklis/buddy-api.git

# Entre no diretório do projeto
cd buddy-api
```

### Passo 2: Iniciar a Infraestrutura

O projeto usa Docker Compose para gerenciar PostgreSQL, Redis e WireMock:

```bash
# Inicie os containers
docker-compose up -d

# Verifique se os containers estão rodando
docker-compose ps
```

Você deverá ver três containers em execução:
- `buddy-postgres` (PostgreSQL na porta 5432)
- `buddy-redis` (Redis na porta 6379)
- `buddy-wiremock` (WireMock na porta 8089)

### Passo 3: Configurar Variáveis de Ambiente (Opcional)

O projeto já vem com configurações padrão para desenvolvimento local em `application-local.yml`. Se necessário, você pode criar um arquivo `.env` ou modificar as configurações:

```yaml
# src/main/resources/application-local.yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/buddydb
    username: buddyuser
    password: buddypassword
  
  redis:
    host: localhost
    port: 6379
```

### Passo 4: Executar a Aplicação

#### Opção A: Via Gradle (Terminal)

```bash
# Compile e execute
./gradlew bootRun

# Ou no Windows
gradlew.bat bootRun
```

#### Opção B: Via IDE (IntelliJ)

1. Abra o projeto no IntelliJ IDEA
2. Aguarde a indexação e download de dependências
3. Localize a classe `BuddyApplication.java`
4. Clique com o botão direito e selecione "Run 'BuddyApplication'"

#### Opção C: Executar o JAR

```bash
# Compile o projeto
./gradlew clean build

# Execute o JAR
java -jar build/libs/app.jar
```

### Passo 5: Verificar a Instalação

Após iniciar a aplicação, verifique se está funcionando:

```bash
# Teste o health check
curl http://localhost:8080/api/actuator/health

# Resposta esperada:
# {"status":"UP"}
```

## 🌐 Acessar a Documentação Swagger

A documentação interativa da API está disponível em:

```
http://localhost:8080/api/swagger-ui/index.html
```

Através do Swagger UI, você pode:
- Visualizar todos os endpoints disponíveis
- Testar as requisições diretamente no navegador
- Ver os modelos de requisição e resposta
- Entender a estrutura da API

## 🎯 Quick Start - Primeiro Teste

Vamos fazer uma requisição simples para criar uma conta:

### 1. Criar uma Conta de Usuário

```bash
curl -X POST http://localhost:8080/api/accounts \
  -H "Content-Type: application/json" \
  -d '{
    "email": "usuario@example.com",
    "phone": "11987654321",
    "password": "SenhaSegura123!",
    "consent": true
  }'
```

**Resposta esperada (201 Created):**
```json
{
  "id": "uuid-gerado",
  "message": "Account created successfully. Please verify your email."
}
```

### 2. Listar Pets Disponíveis

```bash
curl -X GET http://localhost:8080/api/pets \
  -H "Content-Type: application/json"
```

## 🧪 Executar Testes

Para garantir que tudo está funcionando corretamente:

```bash
# Execute todos os testes
./gradlew test

# Execute com relatório de cobertura
./gradlew test jacocoTestReport

# Ver relatório de cobertura
open build/reports/jacoco/test/html/index.html
```

## 🛠️ Ferramentas de Desenvolvimento

### Gradle Tasks Úteis

```bash
# Compilar o projeto
./gradlew build

# Limpar e compilar
./gradlew clean build

# Executar checkstyle (verificação de estilo)
./gradlew checkstyleMain

# Executar spotbugs (análise de bugs)
./gradlew spotbugsMain

# Executar PMD (análise de código)
./gradlew pmdMain

# Ver todas as tasks disponíveis
./gradlew tasks
```

### Logs e Debug

Os logs da aplicação são exibidos no console. Para configurar o nível de log, edite:

```yaml
# application-local.yml
logging:
  level:
    com.buddy.api: DEBUG
    org.springframework: INFO
```

### Hot Reload com Spring Boot DevTools

Para desenvolvimento mais ágil, o Spring Boot DevTools está incluído no projeto e permite hot reload automático quando você faz alterações no código.

## 🐳 Comandos Docker Úteis

```bash
# Ver logs dos containers
docker-compose logs -f

# Ver logs de um container específico
docker-compose logs -f postgres

# Parar os containers
docker-compose stop

# Parar e remover os containers
docker-compose down

# Remover volumes (cuidado: apaga dados do banco)
docker-compose down -v

# Reiniciar um container específico
docker-compose restart postgres
```

## 🔍 Verificação de Saúde do Sistema

### Endpoints do Actuator

```bash
# Health check
curl http://localhost:8080/api/actuator/health

# Informações da aplicação
curl http://localhost:8080/api/actuator/info

# Métricas
curl http://localhost:8080/api/actuator/metrics
```

## 🗃️ Banco de Dados

### Conectar ao PostgreSQL

```bash
# Via Docker
docker exec -it buddy-postgres psql -U buddyuser -d buddydb

# Via cliente local
psql -h localhost -p 5432 -U buddyuser -d buddydb
```

### Verificar Migrações Flyway

```sql
-- Verificar histórico de migrações
SELECT * FROM flyway_schema_history ORDER BY installed_rank;
```

## 🔴 Troubleshooting Comum

### Erro: "Port 8080 already in use"

```bash
# Encontre o processo usando a porta
lsof -i :8080

# Ou no Windows
netstat -ano | findstr :8080

# Mate o processo ou mude a porta no application.yml
```

### Erro: "Connection refused" ao conectar no banco

```bash
# Verifique se o PostgreSQL está rodando
docker-compose ps

# Reinicie o container
docker-compose restart postgres

# Verifique os logs
docker-compose logs postgres
```

### Erro: "Redis connection refused"

```bash
# Verifique o Redis
docker-compose ps redis

# Teste a conexão
docker exec -it buddy-redis redis-cli ping
# Deve retornar: PONG
```

### Limpar e Reconstruir Tudo

```bash
# Pare e remova todos os containers
docker-compose down -v

# Limpe o build do Gradle
./gradlew clean

# Reconstrua e inicie novamente
docker-compose up -d
./gradlew bootRun
```

## 📚 Próximos Passos

Agora que você tem o projeto rodando, explore:

1. **[API Documentation](./API-Documentation.md)** - Conheça todos os endpoints disponíveis
2. **[Architecture Overview](./Architecture-Overview.md)** - Entenda a arquitetura do projeto
3. **[Development Guide](./Development-Guide.md)** - Guia completo para desenvolvedores
4. **[Contributing](./Contributing.md)** - Como contribuir para o projeto

## 🆘 Precisa de Ajuda?

- 📫 Abra uma [Issue](https://github.com/hywenklis/buddy-api/issues)
- 💬 Veja as [Discussões](https://github.com/hywenklis/buddy-api/discussions)
- 📧 Contato: hywenklis@hotmail.com

---

**Boa sorte e divirta-se desenvolvendo!** 🚀🐾
