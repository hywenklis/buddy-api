# 🐾 Buddy API

![Java](https://img.shields.io/badge/java-%23ED8B00.svg?style=flat&logo=openjdk&logoColor=white)
![Spring](https://img.shields.io/badge/spring-%236DB33F.svg?style=flat&logo=spring&logoColor=white)
[![codecov](https://codecov.io/gh/hywenklis/buddy-api/graph/badge.svg?token=LQ5ZANNWWN)](https://codecov.io/gh/hywenklis/buddy-api)
![Build Status](https://github.com/hywenklis/buddy-api/actions/workflows/cicd-pipeline.yml/badge.svg)
[![OpenSSF Scorecard](https://api.scorecard.dev/projects/github.com/hywenklis/buddy-api/badge)](https://scorecard.dev/viewer/?site=github.com/hywenklis/buddy-api)
[![OpenSSF Best Practices](https://www.bestpractices.dev/projects/14251/badge)](https://www.bestpractices.dev/projects/14251)
![Dependabot Status](https://img.shields.io/badge/dependabot-active-brightgreen.svg)
![GitHub](https://img.shields.io/github/license/hywenklis/buddy-api.svg)
![GitHub contributors](https://img.shields.io/github/contributors/hywenklis/buddy-api.svg)
![GitHub issues](https://img.shields.io/github/issues/hywenklis/buddy-api.svg)

> **API para adoção de animais de estimação.** Vamos ajudar animais a encontrar um lar amoroso?

---

## 🚀 Visão Geral

A **Buddy API** é o backend de uma aplicação que conecta abrigos e adotantes para facilitar a adoção responsável de animais de estimação. Desenvolvida com **Java** e **Spring Boot**, a API oferece endpoints para gerenciar contas, perfis, pets, pedidos de adoção e notificações, promovendo um processo seguro e eficiente.

### Links Úteis
- **Swagger**: [Documentação interativa da API](https://buddy.propresto.app/api/swagger-ui/index.html#/)
- **Página Web**: [Aplicação frontend](https://buddyclient.vercel.app/)
- **Repositório do Cliente**: [Buddy Client](https://github.com/genesluna/buddy-client)
- **Wiki**: [Documentação técnica detalhada](https://github.com/hywenklis/buddy-api/wiki)

---

## 💡 Motivação

A **Buddy API** foi criada para transformar o cenário da adoção de animais no Brasil, enfrentando desafios como a falta de visibilidade de animais resgatados e a burocracia nos processos de adoção. Nosso objetivo é:

- **Conectar**: Unir abrigos e adotantes de forma eficiente.
- **Promover**: Incentivar a adoção responsável com fluxos seguros e transparentes.
- **Inovar**: Usar tecnologia (ex.: Spring Boot, Redis, Flyway) para escalar o impacto.
- **Salvar Vidas**: Ajudar milhares de animais a encontrar lares amorosos.

Iniciado como um projeto integrador de faculdade, o projeto ganhou vida própria devido à paixão pela causa e agora é um repositório aberto para contribuições da comunidade.

---

## 🛠 Tecnologias Utilizadas

- **Backend**: Java, Spring Boot, Spring Security, Spring Data JPA
- **Banco de Dados**: PostgreSQL, Flyway (migrações)
- **Integrações**: Feign (clientes HTTP), Redis (cache e controle de taxa)
- **Testes**: JUnit, Mockito, WireMock
- **CI/CD**: GitHub Actions, Dependabot (com automação de merge)
- **Documentação**: Swagger, Markdown, Mermaid (diagramas)

---

## 🤖 Automação de Dependências

O projeto inclui automação completa para gerenciamento de PRs do Dependabot:

- **Atualização Automática**: Branches do Dependabot são atualizados automaticamente com o branch `develop`
- **Merge Automático**: PRs com todos os checks passando são mergeados automaticamente
- **Execução Agendada**: Roda toda segunda-feira às 10:00 UTC
- **Execução Manual**: Disponível via GitHub Actions
---

## 📚 Documentação

Toda a documentação técnica detalhada está disponível na **[Wiki do Buddy API](https://github.com/hywenklis/buddy-api/wiki)**. Lá você encontrará:

- **Arquitetura**: Detalhes sobre a Arquitetura em Camadas por Domínio e inspiração em Arquitetura Hexagonal.
- **Fluxos**: Diagramas de sequência para autenticação, verificação de e-mail e integrações.
- **Modelo de Dados**: Esquemas V1 (legacy) e V2 (atual), com estratégias de migração.
- **Integrações**: Padrões como Anti-Corruption Layer (ACL) para APIs externas.
- **Débitos Técnicos**: Lista de melhorias arquiteturais e como contribuir para resolvê-las.

> **Dica**: Comece pela [Visão Geral da Arquitetura](https://github.com/hywenklis/buddy-api/wiki/Arquitetura-da-Aplica%C3%A7%C3%A3o) para entender a estrutura do projeto.

Estamos continuamente melhorando a wiki. Contribuições para expandir ou esclarecer a documentação são muito bem-vindas!

---

## 🤝 Como Contribuir

Quer ajudar a salvar animais e aprender no processo? **Qualquer contribuição é valiosa**, desde correções de bugs até novas funcionalidades, melhorias na documentação ou sugestões de design.

1. **Leia o Guia**:
   - [Guia Completo para Contribuidores](CONTRIBUTING.md)
   - [Código de Conduta](CODE_OF_CONDUCT.md)
2. **Escolha uma Tarefa**:
   - Veja as [issues abertas](https://github.com/hywenklis/buddy-api/issues) ou os [débitos técnicos](https://github.com/hywenklis/buddy-api/wiki/D%C3%A9bitos-T%C3%A9cnicos-e-Evolu%C3%A7%C3%B5es).
   - Iniciantes podem começar com issues marcadas como `good first issue`.
3. **Siga o Fluxo**:
   - Fork o repositório, crie uma branch, e envie um Pull Request.
   - Siga as diretrizes de código no [CONTRIBUTING.md](CONTRIBUTING.md).
4. **Participe**:
   - Junte-se às discussões no GitHub ou entre em contato com os mantenedores.

**Não importa se você é iniciante ou experiente** – sua contribuição faz a diferença!

---

## 📜 Termos de Uso

Ao contribuir para o **Buddy API**, você concorda em seguir nosso **[Código de Conduta](CODE_OF_CONDUCT.md)**, que promove um ambiente respeitoso e colaborativo.

---

## 👩‍💻👨‍💻 Autores

| **API** | **Página Web** |
|---------|----------------|
| [<img src="https://github.com/hywenklis.png?size=115" width=115><br><sub>@hywenklis</sub>](https://github.com/hywenklis) | [<img src="https://github.com/genesluna.png?size=115" width=115><br><sub>@genesluna</sub>](https://github.com/genesluna) |

---

## 🙌 Contribuidores

Agradecemos a todos que já contribuíram para o projeto! Veja quem faz parte desta jornada:

<a href="https://github.com/hywenklis/buddy-backend/graphs/contributors">
  <img src="https://contrib.rocks/image?repo=hywenklis/buddy-backend" alt="Contribuidores do Buddy API" />
</a>

Feito com [contrib.rocks](https://contrib.rocks).

---

## 📢 Junte-se à Causa!

O **Buddy API** é mais do que um projeto técnico – é uma missão para transformar vidas de animais e adotantes. Contribua, aprenda e faça parte desta mudança!

- **Explorar a Wiki**: [Documentação Técnica](https://github.com/hywenklis/buddy-api/wiki)
- **Abrir uma Issue**: [Reportar bugs ou sugerir ideias](https://github.com/hywenklis/buddy-api/issues/new)
- **Enviar um PR**: [Contribuir com código](https://github.com/hywenklis/buddy-api/pulls)

**Vamos juntos ajudar mais animais a encontrar um lar?** 🐶🐱


Se precisar de mais ajustes, como adicionar seções específicas (ex.: instalação local, exemplos de endpoints), criar diagramas Mermaid para o README, ou adaptar o conteúdo para outro formato, é só avisar!
