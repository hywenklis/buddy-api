# AGENTS.md — Buddy API

## Padrão SDD Oficial: OpenSpec

Este repositório adota exclusivamente o **OpenSpec (`openspec/`)** como fluxo padrão de Spec-Driven Development (SDD) para desenvolvimento assistido por IA.

- **Fonte única de regras e contexto**: [openspec/config.yaml](openspec/config.yaml)
- **Specs canônicas do sistema**: `openspec/specs/<dominio>/<capacidade>/spec.md`
- **Changes ativas e arquivadas**: `openspec/changes/`
- **Proibido criar SDDs paralelos**: não crie nem utilize `.specs/`, `.planning/`, `.sentinel/` ou arquivos avulsos `task-*.md` na raiz do repositório.

## Fluxo Padrão de Trabalho

1. **Explorar** (`openspec explore`): investigar o código, dúvidas de domínio e decisões arquiteturais antes de propor mudanças.
2. **Propor** (`openspec propose`): criar `proposal.md`, delta `specs/`, `design.md` e `tasks.md` sob `openspec/changes/<nome-kebab-case>/` (sem editar código de produção nesta etapa).
3. **Revisar**: validar o plano com o mantenedor antes de implementar.
4. **Implementar** (`openspec apply`): executar as tasks em ordem, mantendo os checkboxes atualizados.
5. **Validar Quality Gate**: rodar `./gradlew build` (testes, Checkstyle, SpotBugs e PMD devem passar com 0 falhas/warnings).
6. **Arquivar** (`openspec archive`): sincronizar delta specs para `openspec/specs/`, rodar `openspec validate --specs --strict` e mover a change para `openspec/changes/archive/`.

## Setup Local para Contribuidores

As skills de IA por ferramenta/IDE ficam fora do Git (conforme `.gitignore`). Para habilitar o OpenSpec no seu ambiente local:

```bash
openspec init --scope global --tools <sua-ferramenta>
# ou para atualizar skills já instaladas:
openspec update
```

## Guardrails Inegociáveis do Projeto

- **Arquitetura DDD**: bounded contexts em `com.buddy.api.domains.<context>`; nunca acessar repositories de outro domínio diretamente (use apenas interfaces de Service). Controllers ficam em `com.buddy.api.web.<resource>` sem lógica de negócio; integrações externas usam ACL em `com.buddy.api.integrations.clients`.
- **DTOs & Validação**: DTOs são Java `record`s puros; conversões ficam em mappers MapStruct; validações de entrada ficam na borda com Bean Validation; enums usam parsing defensivo (`Locale.ROOT` + fallback `UNKNOWN`).
- **Segurança & Transações**: `TermsAcceptanceEntity` é recibo legal imutável (LGPD); eventos de segurança vão para `SecurityAuditEventEntity` com `IpAddressEncryptor`; efeitos colaterais pós-transação (Redis/auditoria/e-mail) usam `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)`.
- **Qualidade & Testes**: Checkstyle (`maxWarnings = 0`, 100 colunas, `final` nos parâmetros), SpotBugs (`Effort.MAX` + FindSecBugs) e PMD 7.22.0. Testes unitários em `units/` e testes de integração/API em `integrations/` estendendo `IntegrationTestAbstract`, usando AssertJ, `@DisplayName` e sem comentários no código de teste.
- **Gitflow & PRs**: branches `feature/*` ou `fix/*` a partir de `develop`, escopo de um único concern coeso por PR e Conventional Commits.
