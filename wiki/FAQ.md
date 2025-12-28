# ❓ FAQ - Perguntas Frequentes

Este documento responde às perguntas mais comuns sobre o Buddy API.

## 🚀 Começando

### Como faço para rodar o projeto localmente?

1. Certifique-se de ter Java 21 e Docker instalados
2. Clone o repositório: `git clone https://github.com/hywenklis/buddy-api.git`
3. Inicie os serviços: `docker-compose up -d`
4. Execute a aplicação: `./gradlew bootRun`

Veja o [Getting Started](./Getting-Started.md) para mais detalhes.

### Preciso de qual versão do Java?

O projeto requer **Java 21** ou superior. Recomendamos usar o Eclipse Temurin JDK.

### Por que não consigo conectar ao banco de dados?

Verifique se:
1. O PostgreSQL está rodando: `docker-compose ps`
2. As credenciais estão corretas no `application-local.yml`
3. A porta 5432 não está em uso por outro processo

```bash
# Reiniciar PostgreSQL
docker-compose restart postgres

# Ver logs
docker-compose logs postgres
```

## 🔐 Autenticação e Segurança

### Como funciona a autenticação?

O Buddy API usa **JWT (JSON Web Tokens)** para autenticação:

1. Usuário faz login com email e senha
2. API valida credenciais e retorna um token JWT
3. Cliente inclui o token em todas as requisições protegidas
4. API valida o token e autoriza o acesso

Veja: [API Documentation - Authentication](./API-Documentation.md#authentication)

### Como obtenho um token JWT?

Faça uma requisição POST para `/api/authentication`:

```bash
curl -X POST http://localhost:8080/api/authentication \
  -H "Content-Type: application/json" \
  -d '{
    "email": "seu@email.com",
    "password": "sua-senha"
  }'
```

A resposta incluirá o token no campo `token`.

### Quanto tempo dura um token JWT?

Por padrão:
- **Desenvolvimento**: 24 horas
- **Produção**: 1 hora

Isso é configurável em `application.yml`:

```yaml
buddy:
  security:
    jwt:
      expiration: 3600000  # 1 hora em millisegundos
```

### Como renovar um token expirado?

Atualmente, é necessário fazer login novamente. No futuro, implementaremos refresh tokens.

### Esqueci minha senha, como recupero?

A funcionalidade de recuperação de senha está em desenvolvimento. Por enquanto, entre em contato com o suporte.

## 🐾 Gerenciamento de Pets

### Como cadastro um novo pet?

1. Você precisa estar autenticado como um **abrigo**
2. Faça uma requisição POST para `/api/pets`:

```bash
curl -X POST http://localhost:8080/api/pets \
  -H "Authorization: Bearer SEU_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Rex",
    "species": "DOG",
    "breed": "Labrador",
    "gender": "MALE",
    "birthDate": "2020-05-15",
    "size": "LARGE"
  }'
```

### Posso cadastrar pets sem ser um abrigo?

Não. Apenas usuários com role de **SHELTER** podem cadastrar pets. Para se tornar um abrigo, você precisa:
1. Criar uma conta normal
2. Registrar um abrigo através de `/api/shelters`

### Como adiciono fotos ao pet?

Após criar o pet, use o endpoint de upload de imagens (em desenvolvimento).

Por enquanto, as imagens são referenciadas por URL:

```json
{
  "images": [
    {
      "url": "https://example.com/pet-photo.jpg",
      "isPrimary": true
    }
  ]
}
```

### Como faço para pesquisar pets por localização?

Use o parâmetro `location` na busca:

```bash
curl "http://localhost:8080/api/pets?location=São Paulo"
```

### Quais são os status possíveis de um pet?

- `AVAILABLE`: Disponível para adoção
- `PENDING`: Pedido de adoção em análise
- `ADOPTED`: Já foi adotado
- `UNAVAILABLE`: Não disponível (ex: tratamento médico)

## 📝 Pedidos de Adoção

### Como solicito a adoção de um pet?

1. Crie uma conta e complete seu perfil
2. Navegue pelos pets disponíveis
3. Faça uma requisição POST para `/api/adoption-requests`:

```bash
curl -X POST http://localhost:8080/api/adoption-requests \
  -H "Authorization: Bearer SEU_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "petId": "uuid-do-pet",
    "message": "Gostaria de adotar este pet...",
    "questionnaire": {
      "hasExperience": true,
      "hasPets": false,
      "housingType": "HOUSE"
    }
  }'
```

### Quanto tempo leva para um pedido ser aprovado?

Depende do abrigo. Normalmente de 2 a 7 dias úteis. Você pode acompanhar o status através de `/api/adoption-requests/me`.

### Posso solicitar a adoção de múltiplos pets?

Sim, você pode criar múltiplos pedidos de adoção, mas cada pedido é avaliado separadamente pelo abrigo.

### O que acontece após meu pedido ser aprovado?

1. Você receberá uma notificação (quando implementado)
2. O abrigo entrará em contato para agendar uma visita
3. Após a visita e documentação, a adoção é concluída
4. O status do pedido muda para `COMPLETED`

## 🏗️ Desenvolvimento

### Como contribuo para o projeto?

1. Fork o repositório
2. Crie uma branch para sua feature: `git checkout -b feature/minha-feature`
3. Faça commit das mudanças: `git commit -m 'feat: adiciona nova feature'`
4. Push para o fork: `git push origin feature/minha-feature`
5. Abra um Pull Request

Veja: [Contributing Guidelines](./Contributing.md)

### Qual é o fluxo de trabalho do Git?

Usamos Gitflow:
- `main`: Código em produção
- `develop`: Desenvolvimento ativo
- `feature/*`: Novas features
- `hotfix/*`: Correções urgentes
- `release/*`: Preparação de releases

### Como executo os testes?

```bash
# Todos os testes
./gradlew test

# Com relatório de cobertura
./gradlew test jacocoTestReport

# Apenas testes unitários
./gradlew test --tests *Test
```

### Como verifico a qualidade do código?

```bash
# Checkstyle
./gradlew checkstyleMain

# SpotBugs
./gradlew spotbugsMain

# PMD
./gradlew pmdMain

# Todos de uma vez
./gradlew check
```

### Qual IDE devo usar?

Recomendamos **IntelliJ IDEA**, mas você pode usar:
- IntelliJ IDEA (Community ou Ultimate)
- Eclipse com Spring Tools
- VS Code com extensões Java

## 🐳 Docker e Deploy

### Como faço build da imagem Docker?

```bash
# Build da aplicação
./gradlew clean build

# Build da imagem Docker
docker build -t buddy-api:latest .

# Executar
docker run -p 8080:8080 buddy-api:latest
```

### Como configuro para produção?

1. Configure as variáveis de ambiente:
   - `DATABASE_URL`
   - `DATABASE_USERNAME`
   - `DATABASE_PASSWORD`
   - `JWT_SECRET`
   - `REDIS_HOST`

2. Use o perfil de produção:
   ```bash
   java -jar app.jar --spring.profiles.active=prod
   ```

Veja: [Deployment Guide](./Deployment-Guide.md)

### Posso usar outro banco de dados que não seja PostgreSQL?

O projeto está otimizado para PostgreSQL, mas você pode adaptar para outros bancos:
1. Adicione o driver no `build.gradle`
2. Ajuste as configurações de datasource
3. Revise as migrations do Flyway (podem haver incompatibilidades)

⚠️ **Nota**: Isso não é oficialmente suportado e pode causar problemas.

## 🔧 Troubleshooting

### Erro: "Port 8080 already in use"

Outro processo está usando a porta 8080:

```bash
# Encontrar o processo (Linux/Mac)
lsof -i :8080

# Encontrar o processo (Windows)
netstat -ano | findstr :8080

# Ou mude a porta no application.yml
server:
  port: 8081
```

### Erro: "Connection refused" ao conectar no PostgreSQL

1. Verifique se o container está rodando:
   ```bash
   docker-compose ps
   ```

2. Verifique as credenciais no `application-local.yml`

3. Tente reiniciar:
   ```bash
   docker-compose restart postgres
   ```

### Erro: "Flyway migration failed"

1. Verifique se há migrations pendentes:
   ```sql
   SELECT * FROM flyway_schema_history;
   ```

2. Se necessário, limpe o banco (⚠️ CUIDADO: apaga todos os dados):
   ```bash
   docker-compose down -v
   docker-compose up -d
   ```

### Testes estão falhando

1. Limpe e reconstrua:
   ```bash
   ./gradlew clean test
   ```

2. Verifique se o Redis está disponível para testes

3. Se usar Windows, verifique line endings (CRLF vs LF)

### Como habilito logs mais detalhados?

Em `application-local.yml`:

```yaml
logging:
  level:
    com.buddy.api: DEBUG
    org.springframework: DEBUG
    org.hibernate.SQL: DEBUG
```

## 🌐 API e Integrações

### Existe um limite de requisições (rate limit)?

Sim, por padrão:
- **100 requisições por minuto** por IP

Quando excedido, você receberá `429 Too Many Requests`.

### A API suporta CORS?

Sim, CORS está configurado para permitir requisições de:
- `http://localhost:3000` (desenvolvimento)
- `https://buddyclient.vercel.app` (produção)

Para adicionar mais origens, veja [Configuration Guide](./Configuration-Guide.md#cors).

### Como testo a API sem código?

Use o **Swagger UI**:
```
http://localhost:8080/api/swagger-ui/index.html
```

Ou ferramentas como:
- **Postman**
- **Insomnia**
- **cURL**

### A API está versionada?

Ainda não. Todas as mudanças breaking changes serão comunicadas com antecedência e período de depreciação.

### Como reporto um bug ou sugiro uma feature?

1. Verifique se já não existe uma [issue similar](https://github.com/hywenklis/buddy-api/issues)
2. Abra uma nova issue com:
   - Descrição clara do problema/sugestão
   - Steps to reproduce (para bugs)
   - Screenshots se aplicável
   - Ambiente (OS, Java version, etc.)

## 📊 Performance e Monitoramento

### Como monitoro a saúde da aplicação?

Use os endpoints do Actuator:

```bash
# Health check
curl http://localhost:8080/api/actuator/health

# Métricas
curl http://localhost:8080/api/actuator/metrics

# Informações da build
curl http://localhost:8080/api/actuator/info
```

### Como otimizo o desempenho?

1. **Use cache** - Redis está configurado
2. **Pagination** - Sempre pagine listagens
3. **Lazy loading** - Entidades JPA usam lazy loading
4. **Índices** - Verifique se os índices estão otimizados

### Onde vejo os logs em produção?

Depende do ambiente:
- **Local**: Console
- **Docker**: `docker-compose logs -f buddy-api`
- **Produção**: Configurado em `/var/log/buddy-api/`

## 📱 Cliente Web

### Existe um frontend para a API?

Sim! O [Buddy Client](https://github.com/genesluna/buddy-client) é o frontend oficial:
- **Demo**: https://buddyclient.vercel.app/
- **Repositório**: https://github.com/genesluna/buddy-client

### Posso criar meu próprio cliente?

Sim! A API é pública e documentada. Use o Swagger para entender os endpoints.

## 🆘 Suporte

### Como obtenho ajuda?

1. **Documentação**: Verifique a [Wiki completa](./Home.md)
2. **Issues**: Procure nas [issues existentes](https://github.com/hywenklis/buddy-api/issues)
3. **Discussões**: Participe das [GitHub Discussions](https://github.com/hywenklis/buddy-api/discussions)
4. **Email**: hywenklis@hotmail.com

### Posso usar o Buddy API comercialmente?

Sim! O projeto é licenciado sob a **GNU General Public License v3.0**. Veja [LICENSE](https://github.com/hywenklis/buddy-api/blob/main/LICENSE) para detalhes.

### Como cito o projeto?

```
Buddy API - Pet Adoption Management System
Desenvolvido por: @hywenklis e colaboradores
GitHub: https://github.com/hywenklis/buddy-api
Licença: GPL-3.0
```

## 🔄 Roadmap

### Quais são as próximas features?

Veja nosso [roadmap](https://github.com/hywenklis/buddy-api/issues?q=is%3Aissue+is%3Aopen+label%3Aenhancement):

- [ ] Upload de imagens de pets
- [ ] Notificações por email
- [ ] Refresh tokens
- [ ] Sistema de mensagens entre abrigos e adotantes
- [ ] Dashboard de estatísticas
- [ ] Suporte a múltiplos idiomas
- [ ] App mobile

### Como sugiro uma nova feature?

Abra uma [issue](https://github.com/hywenklis/buddy-api/issues/new) com a label `enhancement` e descreva:
- O problema que a feature resolve
- Como você imagina que deveria funcionar
- Benefícios para os usuários

---

## 🤔 Sua pergunta não está aqui?

Abra uma [discussão](https://github.com/hywenklis/buddy-api/discussions) ou [issue](https://github.com/hywenklis/buddy-api/issues)!

---

**Mantido por**: @hywenklis | **Última atualização**: Dezembro 2024
