# 🔧 Troubleshooting - Solução de Problemas

Este guia ajuda a diagnosticar e resolver problemas comuns no Buddy API.

## 🔍 Diagnóstico Inicial

Antes de começar, verifique:

```bash
# Versão do Java
java -version  # Deve ser 21 ou superior

# Docker está rodando?
docker --version
docker-compose ps

# Gradle está funcionando?
./gradlew --version
```

---

## 🚀 Problemas de Inicialização

### Erro: "Port 8080 already in use"

**Sintoma**: A aplicação não inicia e exibe erro de porta em uso.

**Causa**: Outro processo está usando a porta 8080.

**Solução 1 - Encontrar e matar o processo**:

```bash
# Linux/macOS
lsof -i :8080
kill -9 <PID>

# Windows (PowerShell)
netstat -ano | findstr :8080
taskkill /PID <PID> /F
```

**Solução 2 - Mudar a porta**:

```yaml
# application-local.yml
server:
  port: 8081
```

---

### Erro: "Failed to configure a DataSource"

**Sintoma**: Erro ao iniciar indicando problema com datasource.

**Causa**: PostgreSQL não está acessível ou credenciais incorretas.

**Solução**:

```bash
# 1. Verificar se PostgreSQL está rodando
docker-compose ps

# 2. Verificar logs do PostgreSQL
docker-compose logs postgres

# 3. Reiniciar PostgreSQL
docker-compose restart postgres

# 4. Verificar credenciais em application-local.yml
spring:
  datasource:
    url: jdbc:postgresql://localhost:5432/buddydb
    username: buddyuser
    password: buddypassword
```

**Teste de conexão**:

```bash
# Conectar via psql
docker exec -it buddy-postgres psql -U buddyuser -d buddydb

# Se funcionar, o problema está na configuração do Spring
```

---

### Erro: "Could not connect to Redis"

**Sintoma**: Aplicação não consegue conectar ao Redis.

**Causa**: Redis não está rodando ou host/porta incorretos.

**Solução**:

```bash
# Verificar Redis
docker-compose ps redis

# Testar conexão
docker exec -it buddy-redis redis-cli ping
# Deve retornar: PONG

# Reiniciar Redis
docker-compose restart redis

# Verificar configuração
spring:
  data:
    redis:
      host: localhost
      port: 6379
```

---

### Erro: "Flyway migration failed"

**Sintoma**: Erro durante migrations do Flyway.

**Causa**: Schema inconsistente ou migration corrompida.

**Solução 1 - Verificar histórico**:

```sql
-- Conectar ao banco
docker exec -it buddy-postgres psql -U buddyuser -d buddydb

-- Ver histórico de migrations
SELECT * FROM flyway_schema_history ORDER BY installed_rank;
```

**Solução 2 - Reparar migration**:

```bash
# Se uma migration falhou, remova-a do histórico
DELETE FROM flyway_schema_history WHERE success = false;
```

**Solução 3 - Resetar banco (⚠️ APAGA TUDO)**:

```bash
# Parar containers
docker-compose down -v

# Iniciar novamente
docker-compose up -d

# Aguardar e iniciar aplicação
./gradlew bootRun
```

---

## 🔐 Problemas de Autenticação

### Erro: "401 Unauthorized"

**Sintoma**: Requisições retornam 401 mesmo com token.

**Diagnóstico**:

```bash
# Verificar se o token está sendo enviado corretamente
curl -H "Authorization: Bearer SEU_TOKEN" http://localhost:8080/api/profiles/me
```

**Causas e Soluções**:

1. **Token expirado**:
   - Faça login novamente para obter novo token
   - Verifique `jwt.expiration` em application.yml

2. **Token malformado**:
   - Certifique-se de incluir "Bearer " antes do token
   - Correto: `Authorization: Bearer eyJhbGc...`
   - Errado: `Authorization: eyJhbGc...`

3. **JWT_SECRET diferente**:
   - Use a mesma chave em todas as instâncias
   - Em produção, use variável de ambiente

**Teste de token**:

```bash
# Decodificar token JWT (sem verificar assinatura)
# Use https://jwt.io ou:
echo "SEU_TOKEN" | cut -d'.' -f2 | base64 -d 2>/dev/null | jq
```

---

### Erro: "403 Forbidden"

**Sintoma**: Usuário autenticado mas sem permissão.

**Causa**: Role inadequada ou recurso protegido.

**Solução**:

```java
// Verificar roles necessárias no endpoint
@PreAuthorize("hasRole('SHELTER')")
public void createPet() { ... }

// Seu token precisa ter a role correta
```

Para verificar roles do usuário:

```bash
# Decodificar token e ver claims
echo "SEU_TOKEN_PAYLOAD" | base64 -d
```

---

## 💾 Problemas com Banco de Dados

### Erro: "Connection pool exhausted"

**Sintoma**: Erro "HikariPool - Connection is not available".

**Causa**: Muitas conexões abertas ou pool configurado incorretamente.

**Solução**:

```yaml
# Ajustar configuração do pool
spring:
  datasource:
    hikari:
      maximum-pool-size: 20      # Aumentar se necessário
      minimum-idle: 5
      connection-timeout: 30000
```

**Diagnóstico**:

```sql
-- Ver conexões ativas
SELECT count(*) FROM pg_stat_activity;

-- Ver detalhes das conexões
SELECT * FROM pg_stat_activity WHERE datname = 'buddydb';
```

---

### Erro: "Deadlock detected"

**Sintoma**: Transações falham com erro de deadlock.

**Causa**: Múltiplas transações tentando acessar os mesmos recursos em ordens diferentes.

**Solução**:

1. **Reduzir escopo de transações**:
   ```java
   @Transactional  // Apenas onde necessário
   public void method() { ... }
   ```

2. **Usar lock explícito se necessário**:
   ```java
   @Lock(LockModeType.PESSIMISTIC_WRITE)
   Optional<Entity> findById(UUID id);
   ```

3. **Verificar queries N+1**:
   ```java
   // Usar JOIN FETCH
   @Query("SELECT p FROM Pet p JOIN FETCH p.images WHERE p.id = :id")
   Optional<Pet> findByIdWithImages(UUID id);
   ```

---

### Dados não estão sendo salvos

**Sintoma**: Chamadas save() não persistem dados.

**Causa**: Falta de `@Transactional` ou flush não executado.

**Solução**:

```java
@Transactional  // Adicionar anotação
public void saveData() {
    repository.save(entity);
    // Não precisa flush, commit automático ao fim do método
}
```

---

## 🧪 Problemas com Testes

### Testes falhando aleatoriamente

**Sintoma**: Testes passam às vezes, falham outras vezes.

**Causa**: Dependência de ordem de execução ou estado compartilhado.

**Solução**:

```java
@BeforeEach
void setUp() {
    // Limpar estado antes de cada teste
    repository.deleteAll();
}

@AfterEach
void tearDown() {
    // Limpar depois também
    repository.deleteAll();
}
```

---

### Erro: "Embedded Redis failed to start"

**Sintoma**: Testes com Redis falham ao iniciar.

**Causa**: Porta já em uso ou problema com embedded Redis.

**Solução**:

```java
// Usar porta aleatória
@TestConfiguration
public class EmbeddedRedisConfig {
    @Bean
    public RedisServer redisServer() {
        return new RedisServer(findAvailablePort());
    }
}
```

---

### Erro: "H2 syntax error"

**Sintoma**: Queries funcionam em PostgreSQL mas falham em H2.

**Causa**: Diferenças de sintaxe SQL entre PostgreSQL e H2.

**Solução**:

```java
// Usar JPQL em vez de SQL nativo quando possível
@Query("SELECT p FROM Pet p WHERE p.name = :name")
List<Pet> findByName(@Param("name") String name);

// Ou use @Query com nativeQuery mas teste em H2
```

---

## 🐳 Problemas com Docker

### Containers não iniciam

**Sintoma**: `docker-compose up` falha.

**Causa**: Portas em conflito ou volumes corrompidos.

**Solução**:

```bash
# Parar tudo e remover volumes
docker-compose down -v

# Limpar containers órfãos
docker system prune -a

# Iniciar novamente
docker-compose up -d
```

---

### Volumes do Docker corrompidos

**Sintoma**: Dados persistentes inconsistentes.

**Solução**:

```bash
# Listar volumes
docker volume ls

# Remover volume específico (⚠️ APAGA DADOS)
docker volume rm buddy-api_postgres-data

# Remover todos os volumes não usados
docker volume prune
```

---

### Performance ruim no Docker (macOS/Windows)

**Sintoma**: Aplicação muito lenta no Docker.

**Causa**: I/O de volumes montados é lento em macOS/Windows.

**Solução**:

```yaml
# docker-compose.yml
services:
  buddy-api:
    volumes:
      # Usar cached ou delegated
      - ./src:/app/src:cached
```

Ou execute diretamente sem Docker:

```bash
./gradlew bootRun
```

---

## 🔧 Problemas de Build

### Erro: "Task checkstyle failed"

**Sintoma**: Build falha na verificação de estilo.

**Causa**: Código não segue padrões de estilo.

**Solução**:

```bash
# Ver erros específicos
./gradlew checkstyleMain --info

# Ignorar temporariamente (não recomendado)
./gradlew build -x checkstyleMain
```

**Corrigir no IntelliJ**:
1. Importar config: `config/checkstyle/checkstyle.xml`
2. Use `Ctrl+Alt+L` para formatar código

---

### Erro: "Task spotbugs failed"

**Sintoma**: SpotBugs encontra bugs potenciais.

**Solução**:

```bash
# Ver relatório detalhado
./gradlew spotbugsMain
open build/reports/spotbugs/main.html
```

**Corrigir bugs comuns**:
- Verificações de null
- Resources não fechados
- Comparações incorretas

---

### Erro de compilação MapStruct

**Sintoma**: Erro "cannot find symbol" em mappers.

**Causa**: Processador de anotações não executado.

**Solução**:

```bash
# Limpar e rebuildar
./gradlew clean build

# No IntelliJ: Enable annotation processing
# Settings → Build → Compiler → Annotation Processors
```

---

## 📊 Problemas de Performance

### API muito lenta

**Diagnóstico**:

```bash
# Ativar logging de queries
logging:
  level:
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

**Causas comuns**:

1. **Problema N+1**:
   ```java
   // Ruim: N+1 queries
   List<Pet> pets = petRepository.findAll();
   pets.forEach(pet -> pet.getImages().size());  // N queries extras
   
   // Bom: 1 query com JOIN FETCH
   @Query("SELECT DISTINCT p FROM Pet p LEFT JOIN FETCH p.images")
   List<Pet> findAllWithImages();
   ```

2. **Falta de índices**:
   ```sql
   -- Verificar queries lentas
   SELECT query, calls, total_time, mean_time 
   FROM pg_stat_statements 
   ORDER BY total_time DESC 
   LIMIT 10;
   
   -- Criar índice se necessário
   CREATE INDEX idx_pet_shelter_id ON pet(shelter_id);
   ```

3. **Cache não configurado**:
   ```java
   @Cacheable(value = "pets", key = "#id")
   public Pet findById(UUID id) { ... }
   ```

---

### Alto consumo de memória

**Diagnóstico**:

```bash
# Ativar métricas JVM
curl http://localhost:8080/api/actuator/metrics/jvm.memory.used

# Heap dump (se disponível)
jmap -dump:live,format=b,file=heap.bin <PID>
```

**Soluções**:

```bash
# Ajustar heap size
export JAVA_OPTS="-Xms512m -Xmx2g"

# Usar G1GC
export JAVA_OPTS="-XX:+UseG1GC -XX:MaxGCPauseMillis=200"
```

---

## 🌐 Problemas de Rede/CORS

### Erro: "CORS policy blocked"

**Sintoma**: Browser bloqueia requisições por CORS.

**Solução**:

```java
@Configuration
public class CorsConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                    .allowedOrigins("http://localhost:3000")  // Adicionar origem
                    .allowedMethods("GET", "POST", "PUT", "DELETE")
                    .allowedHeaders("*")
                    .allowCredentials(true);
            }
        };
    }
}
```

---

## 🆘 Quando Nada Funciona

### Reset Completo

```bash
# 1. Parar tudo
docker-compose down -v

# 2. Limpar Docker
docker system prune -a
docker volume prune

# 3. Limpar build
./gradlew clean

# 4. Remover caches
rm -rf ~/.gradle/caches/
rm -rf .gradle/

# 5. Rebuildar
./gradlew clean build

# 6. Iniciar novamente
docker-compose up -d
./gradlew bootRun
```

---

## 📞 Obter Ajuda

Se o problema persistir:

1. **Verificar logs completos**:
   ```bash
   ./gradlew bootRun > app.log 2>&1
   ```

2. **Coletar informações**:
   - Versão do Java: `java -version`
   - Versão do Docker: `docker --version`
   - Sistema operacional
   - Logs de erro completos

3. **Abrir Issue**:
   - [GitHub Issues](https://github.com/hywenklis/buddy-api/issues)
   - Incluir todas as informações coletadas
   - Descrever passos para reproduzir

4. **Comunidade**:
   - [GitHub Discussions](https://github.com/hywenklis/buddy-api/discussions)
   - Email: hywenklis@hotmail.com

---

## 📚 Mais Recursos

- [Getting Started](./Getting-Started.md) - Guia de início
- [Configuration Guide](./Configuration-Guide.md) - Configurações detalhadas
- [FAQ](./FAQ.md) - Perguntas frequentes

---

**Mantido por**: @hywenklis | **Última atualização**: Dezembro 2024
