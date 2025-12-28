# 📖 Glossary - Glossário de Termos

Este glossário define termos técnicos e conceitos importantes usados no Buddy API.

## A

### Account (Conta)
Uma conta de usuário no sistema, contendo credenciais de autenticação (email e senha). Cada conta pode ter um perfil associado.

### Actuator
Módulo do Spring Boot que fornece endpoints para monitoramento e gerenciamento da aplicação (health checks, métricas, etc.).

### Adoption (Adoção)
Processo pelo qual um pet encontra um novo lar. Envolve pedido de adoção, análise, aprovação e acompanhamento.

### Adoption Request (Pedido de Adoção)
Solicitação formal feita por um adotante para adotar um pet específico. Passa por diferentes status até ser concluída ou rejeitada.

### Adoption Questionnaire (Questionário de Adoção)
Formulário preenchido pelo adotante com informações sobre experiência com pets, tipo de moradia, e razões para adoção.

### Anti-Corruption Layer (ACL)
Padrão de design que isola o domínio interno de APIs externas, traduzindo entre diferentes modelos de dados.

### API (Application Programming Interface)
Interface que permite comunicação entre diferentes sistemas de software através de endpoints HTTP/REST.

## B

### BCrypt
Algoritmo de hash usado para criptografar senhas de forma segura, incluindo salt automático.

### Bounded Context
Conceito do DDD que define limites claros entre diferentes áreas de domínio, cada uma com seu próprio modelo.

### Build
Processo de compilação do código-fonte em artefatos executáveis (JAR, Docker image, etc.).

## C

### Cache
Armazenamento temporário de dados frequentemente acessados para melhorar performance. No Buddy API, usa Redis.

### CI/CD (Continuous Integration/Continuous Deployment)
Práticas de automação de integração e deploy de código através de pipelines.

### Controller
Componente da camada web que recebe requisições HTTP, processa através de services e retorna respostas.

### CORS (Cross-Origin Resource Sharing)
Mecanismo que permite que recursos de um domínio sejam acessados por outro domínio.

## D

### DTO (Data Transfer Object)
Objeto usado para transferir dados entre camadas da aplicação, sem lógica de negócio.

### DDD (Domain-Driven Design)
Abordagem de design de software focada no domínio de negócio e suas regras.

### Docker
Plataforma de containerização que empacota aplicações e suas dependências em containers isolados.

### Domain Model (Modelo de Domínio)
Representação orientada a objetos dos conceitos, regras e processos do negócio.

## E

### Entity (Entidade)
Objeto de domínio com identidade única que persiste ao longo do tempo, mapeado para uma tabela no banco de dados.

### Endpoint
URL específica da API que aceita requisições HTTP para realizar uma operação.

## F

### Feign Client
Biblioteca declarativa para criar clientes HTTP que consomem APIs REST.

### Flyway
Ferramenta de versionamento e migração de banco de dados que gerencia mudanças no schema.

## G

### Gradle
Ferramenta de build e gerenciamento de dependências usada no projeto.

## H

### H2 Database
Banco de dados em memória usado em testes para simular PostgreSQL.

### HikariCP
Pool de conexões JDBC de alta performance usado pelo Spring Boot.

### Hibernate
Framework ORM (Object-Relational Mapping) que mapeia objetos Java para tabelas de banco de dados.

## I

### Integration Layer (Camada de Integração)
Camada responsável por comunicação com sistemas externos via APIs, mensageria, etc.

## J

### JaCoCo
Ferramenta de análise de cobertura de código para Java.

### JPA (Java Persistence API)
Especificação Java para mapeamento objeto-relacional (ORM).

### JSON (JavaScript Object Notation)
Formato leve de intercâmbio de dados, usado nas requisições e respostas da API.

### JWT (JSON Web Token)
Padrão aberto para transmissão segura de informações entre partes como um objeto JSON, usado para autenticação.

### JUnit
Framework de testes unitários para Java.

## L

### Lazy Loading
Estratégia de carregamento de dados sob demanda, apenas quando necessário, para otimizar performance.

### Lombok
Biblioteca Java que reduz código boilerplate através de anotações (@Getter, @Setter, etc.).

## M

### MapStruct
Framework de mapeamento entre objetos Java que gera código de conversão em tempo de compilação.

### Migration (Migração)
Script SQL versionado que aplica mudanças incrementais no schema do banco de dados.

### Mock
Objeto simulado usado em testes para substituir dependências reais.

### Mockito
Framework de mocking para testes em Java.

## P

### Pet
Animal disponível para adoção no sistema. Pode ser cachorro, gato, pássaro, etc.

### PostgreSQL
Sistema de gerenciamento de banco de dados relacional open-source usado no projeto.

### Profile (Perfil)
Informações pessoais de um usuário ou abrigo, complementando a conta.

## R

### Rate Limiting
Técnica para limitar o número de requisições que um cliente pode fazer em um período de tempo.

### Redis
Sistema de armazenamento de dados em memória usado para cache e controle de taxa.

### Repository
Padrão de design que abstrai o acesso a dados, encapsulando operações de persistência.

### REST (Representational State Transfer)
Estilo arquitetural para APIs baseado em HTTP, usando verbos (GET, POST, PUT, DELETE).

## S

### Schema
Estrutura do banco de dados, incluindo tabelas, colunas, relacionamentos e constraints.

### Service
Componente da camada de domínio que contém lógica de negócio.

### Shelter (Abrigo)
Organização que resgata, cuida e facilita a adoção de animais.

### Spring Boot
Framework Java para criar aplicações stand-alone baseadas em Spring.

### Spring Data JPA
Projeto Spring que simplifica o acesso a dados usando JPA.

### Spring Security
Framework para autenticação e controle de acesso em aplicações Spring.

### Swagger/OpenAPI
Especificação para documentação de APIs REST, com interface interativa.

## T

### Token
String criptografada que representa credenciais de autenticação, usado em cada requisição autenticada.

### Transaction (Transação)
Unidade de trabalho que deve ser executada completamente ou não ser executada, garantindo consistência dos dados.

## U

### UUID (Universally Unique Identifier)
Identificador único de 128 bits usado como chave primária nas tabelas.

## V

### Value Object
Objeto imutável definido apenas por seus atributos, sem identidade única (ex: endereço, data).

### Validation (Validação)
Processo de verificar se os dados atendem às regras de negócio antes de serem processados.

## W

### WebMvcTest
Anotação do Spring para testes focados na camada web (controllers).

### WireMock
Ferramenta para criar mocks de APIs HTTP, usada em testes de integração.

---

## Conceitos de Domínio Específicos do Buddy API

### Adoption Status
Status de um pedido de adoção:
- **PENDING**: Aguardando análise
- **UNDER_REVIEW**: Em análise pelo abrigo
- **APPROVED**: Aprovado
- **REJECTED**: Rejeitado
- **CANCELLED**: Cancelado pelo adotante
- **COMPLETED**: Adoção concluída

### Pet Species
Espécies de animais suportadas:
- **DOG**: Cachorro
- **CAT**: Gato
- **BIRD**: Pássaro
- **RABBIT**: Coelho
- **RODENT**: Roedor
- **OTHER**: Outros

### Pet Size
Porte do animal:
- **SMALL**: Pequeno (até 10kg)
- **MEDIUM**: Médio (10-25kg)
- **LARGE**: Grande (25-45kg)
- **EXTRA_LARGE**: Extra grande (>45kg)

### Pet Age Category
Categoria de idade:
- **PUPPY**: Filhote (0-1 ano)
- **YOUNG**: Jovem (1-3 anos)
- **ADULT**: Adulto (3-8 anos)
- **SENIOR**: Idoso (>8 anos)

### Housing Type
Tipo de moradia do adotante:
- **HOUSE**: Casa
- **APARTMENT**: Apartamento
- **FARM**: Fazenda/Sítio
- **OTHER**: Outro

### User Roles
Papéis de usuário no sistema:
- **USER**: Usuário comum (adotante)
- **SHELTER**: Abrigo/organização
- **ADMIN**: Administrador do sistema

---

## Termos Técnicos de Arquitetura

### Layered Architecture (Arquitetura em Camadas)
Organização do código em camadas com responsabilidades distintas: Web, Domain, Data, Integration.

### Aggregate Root
Entidade raiz que controla acesso a um grupo de entidades relacionadas, garantindo invariantes.

### Entity Lifecycle
Estados pelos quais uma entidade passa: New, Managed, Detached, Removed.

### Dependency Injection (DI)
Padrão onde dependências são fornecidas externamente em vez de criadas pela classe.

### Inversion of Control (IoC)
Princípio onde o framework controla o fluxo da aplicação, não o código do desenvolvedor.

---

## Termos de Teste

### Unit Test (Teste Unitário)
Teste de um componente isolado de suas dependências.

### Integration Test (Teste de Integração)
Teste da interação entre múltiplos componentes.

### End-to-End Test (Teste E2E)
Teste do fluxo completo da aplicação, do início ao fim.

### Test Coverage (Cobertura de Testes)
Métrica que indica a porcentagem do código executada pelos testes.

### Given-When-Then
Padrão de estruturação de testes: preparação, ação e verificação.

---

## Siglas Comuns

| Sigla | Significado |
|-------|-------------|
| API | Application Programming Interface |
| ACL | Anti-Corruption Layer |
| CRUD | Create, Read, Update, Delete |
| DDD | Domain-Driven Design |
| DTO | Data Transfer Object |
| HTTP | Hypertext Transfer Protocol |
| JDBC | Java Database Connectivity |
| JPA | Java Persistence API |
| JSON | JavaScript Object Notation |
| JWT | JSON Web Token |
| ORM | Object-Relational Mapping |
| REST | Representational State Transfer |
| SQL | Structured Query Language |
| UUID | Universally Unique Identifier |

---

**Sugestões de novos termos?** Abra uma [issue](https://github.com/hywenklis/buddy-api/issues) ou [pull request](https://github.com/hywenklis/buddy-api/pulls)!

---

**Mantido por**: @hywenklis | **Última atualização**: Dezembro 2024
