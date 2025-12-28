# Automatização de Merge de PRs do Dependabot

Este documento descreve as soluções implementadas para automatizar o processo de atualização e merge de Pull Requests (PRs) criados pelo Dependabot.

## Visão Geral

O repositório agora possui duas formas de gerenciar PRs do Dependabot:

1. **GitHub Actions Workflow** (Recomendado) - Automação completa via CI/CD
2. **Script Bash** - Para execução manual local

## Problema Resolvido

O Dependabot cria PRs automaticamente para atualizar dependências, mas essas PRs:
- Ficam desatualizadas em relação ao branch `develop`
- Precisam passar por todos os checks do CI/CD
- Requerem merge manual após aprovação

As soluções implementadas automatizam todo esse processo.

## Solução 1: GitHub Actions Workflow (Recomendado)

### Arquivo: `.github/workflows/auto-update-dependabot.yml`

Esta é a solução mais conveniente pois funciona completamente na nuvem, sem necessidade de acesso local.

### Como Funciona

O workflow executa automaticamente:
- **Toda segunda-feira às 10:00 UTC** (via cron schedule)
- **Manualmente** quando você acionar via interface do GitHub

#### Processo de Execução:

1. **Job: update-dependabot-prs**
   - Busca todos os branches do Dependabot
   - Para cada branch:
     - Faz merge do `develop` no branch do Dependabot
     - Push das alterações
     - Adiciona comentário no PR informando sobre a atualização
   - Trata conflitos de merge automaticamente (adiciona comentário de aviso)

2. **Job: auto-merge-dependabot**
   - Aguarda o job anterior completar
   - Verifica todos os PRs do Dependabot
   - Para PRs com todos os checks passando:
     - Habilita auto-merge
     - Adiciona comentário de sucesso
   - PRs são mergeados automaticamente quando aprovados

### Como Usar

#### Execução Manual

1. Acesse o repositório no GitHub
2. Vá para a aba **Actions**
3. Selecione **Auto-Update Dependabot PRs** na lista de workflows
4. Clique no botão **Run workflow**
5. Selecione o branch (geralmente `main`) e confirme

#### Visualizando Resultados

Após a execução, você pode visualizar:
- Logs detalhados de cada step
- Summary com lista de branches atualizados
- Summary com lista de branches que falharam
- Comentários automáticos nos PRs

### Vantagens

✅ Totalmente automatizado
✅ Não requer acesso local
✅ Execução agendada automaticamente
✅ Logs e summaries detalhados
✅ Comentários automáticos nos PRs
✅ Seguro com Harden Runner

## Solução 2: Script Bash Local

### Arquivo: `scripts/update-dependabot-branches.sh`

Script para execução local quando você precisa de mais controle sobre o processo.

### Pré-requisitos

1. **Git** instalado e configurado
2. **GitHub CLI (gh)** instalado e autenticado

#### Instalando GitHub CLI

**macOS:**
```bash
brew install gh
```

**Linux (Debian/Ubuntu):**
```bash
sudo apt install gh
```

**Windows:**
```bash
winget install GitHub.cli
```

#### Autenticando GitHub CLI

```bash
gh auth login
```

### Como Usar

```bash
# Na raiz do repositório
./scripts/update-dependabot-branches.sh
```

O script irá:
1. Buscar todos os branches do Dependabot
2. Atualizar cada um com `develop`
3. Aguardar os checks passarem (até 30 minutos)
4. Fazer merge automaticamente

### Vantagens

✅ Controle total local
✅ Útil para debugging
✅ Funciona offline (após clone)
✅ Output colorido e detalhado

## Branches do Dependabot Atuais

No momento da implementação, os seguintes PRs do Dependabot estão abertos:

1. **PR #253** - `dependabot-github_actions-develop-actions-upload-artifact-6`
   - Atualiza actions/upload-artifact de v5 para v6

2. **PR #252** - `dependabot-gradle-develop-com.github.spotbugs-6.4.8`
   - Atualiza com.github.spotbugs de 6.4.4 para 6.4.8

3. **PR #254** - `dependabot-gradle-develop-org.springframework.boot-4.0.1`
   - Atualiza org.springframework.boot de 3.5.7 para 4.0.1

4. **PR #245** - `dependabot-gradle-develop-com.palantir.git-version-4.2.0`
   - Atualiza com.palantir.git-version de 4.1.0 para 4.2.0

Também existem branches mais antigas que podem ter conflitos ou checks falhando.

## Tratamento de Problemas

### Conflitos de Merge

**Workflow do GitHub Actions:**
- Adiciona comentário no PR indicando o conflito
- Skip do branch com conflito
- Continua processando outros branches

**Script Bash:**
- Mostra aviso no terminal
- Aborta o merge
- Continua processando outros branches

**Resolução Manual:**
```bash
git checkout <branch-do-dependabot>
git merge origin/develop
# Resolva conflitos em seu editor
git add .
git commit
git push origin <branch-do-dependabot>
```

### Checks Falhando

Se os checks do CI/CD falharem:

1. Visualize os logs de falha:
   ```bash
   gh pr checks <PR_NUMBER>
   ```

2. Acesse os detalhes completos no GitHub Actions

3. Corrija o problema no branch do Dependabot (se necessário)

4. Os checks serão re-executados automaticamente

### Permissões

O workflow do GitHub Actions requer as seguintes permissões:
- `contents: write` - Para fazer push em branches
- `pull-requests: write` - Para comentar e fazer merge de PRs
- `checks: read` - Para verificar status dos checks

Estas permissões são configuradas no arquivo do workflow.

## Monitoramento

### Via GitHub Actions

1. Vá para a aba **Actions**
2. Veja o histórico de execuções do workflow
3. Cada execução mostra um summary com:
   - Branches atualizados com sucesso
   - Branches com falha
   - PRs mergeados

### Via Command Line

```bash
# Listar PRs do Dependabot abertos
gh pr list --author "dependabot[bot]" --base develop

# Ver status de um PR específico
gh pr view <PR_NUMBER>

# Ver checks de um PR
gh pr checks <PR_NUMBER>
```

## Segurança

### Harden Runner

Ambas as soluções usam o `step-security/harden-runner` para aumentar a segurança:
- Monitora chamadas de rede
- Gera audit logs
- Previne execução de código não autorizado

### Autenticação

- O workflow usa `GITHUB_TOKEN` (gerado automaticamente)
- O script requer autenticação via `gh auth login`
- Nenhum secret adicional é necessário

## Próximos Passos

Após a implementação destas soluções:

1. **Teste o workflow** - Execute manualmente para validar
2. **Monitore a execução automática** - Aguarde a próxima segunda-feira
3. **Ajuste o cron schedule** - Se necessário, modifique no arquivo do workflow
4. **Configure notificações** - Para ser alertado sobre execuções do workflow

## Contribuindo

Se você encontrar problemas ou tiver sugestões de melhorias:

1. Abra uma issue descrevendo o problema
2. Sugira alterações ao workflow ou script
3. Teste suas alterações localmente antes de fazer PR

## Referências

- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [GitHub CLI Documentation](https://cli.github.com/)
- [Dependabot Documentation](https://docs.github.com/en/code-security/dependabot)
- [Git Flow Branching Model](https://nvie.com/posts/a-successful-git-branching-model/)
