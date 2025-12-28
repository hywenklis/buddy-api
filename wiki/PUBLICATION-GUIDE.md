# 📚 Guia de Publicação da Wiki no GitHub

Este guia explica como publicar a documentação da pasta `wiki/` no GitHub Wiki oficial do projeto.

## 📋 Pré-requisitos

- Acesso de escrita ao repositório hywenklis/buddy-api
- Git instalado localmente
- Conta GitHub configurada

## 🚀 Método 1: Clone e Push (Recomendado)

Este é o método mais simples e direto:

```bash
# 1. Clone o repositório wiki (separado do repositório principal)
git clone https://github.com/hywenklis/buddy-api.wiki.git

# 2. Entre no diretório
cd buddy-api.wiki

# 3. Copie todos os arquivos markdown da pasta wiki/ do projeto principal
cp ../buddy-api/wiki/*.md .

# 4. Adicione os arquivos ao git
git add .

# 5. Commit com mensagem descritiva
git commit -m "Add comprehensive wiki documentation"

# 6. Push para o GitHub
git push origin master

# 7. Acesse a wiki e verifique
# https://github.com/hywenklis/buddy-api/wiki
```

## 🌐 Método 2: Interface Web do GitHub

Para quem prefere usar a interface gráfica:

1. Acesse https://github.com/hywenklis/buddy-api/wiki
2. Clique em "New Page" ou edite uma página existente
3. Copie e cole o conteúdo de cada arquivo .md
4. Salve cada página

### Ordem Sugerida de Criação

1. Home (da Home.md)
2. Getting-Started
3. Architecture-Overview
4. API-Documentation
5. Database-Schema
6. Configuration-Guide
7. Testing-Guide
8. Troubleshooting
9. FAQ
10. Glossary

## 🔄 Script Automatizado

Crie um script `publish-wiki.sh` para automatizar:

```bash
#!/bin/bash

echo "🚀 Publishing Buddy API Wiki..."

# Configurações
WIKI_REPO="https://github.com/hywenklis/buddy-api.wiki.git"
TEMP_DIR="temp-wiki-publish"
SOURCE_DIR="wiki"

# Clone wiki repository
echo "📥 Cloning wiki repository..."
git clone $WIKI_REPO $TEMP_DIR

# Copy markdown files
echo "📋 Copying wiki files..."
cp $SOURCE_DIR/*.md $TEMP_DIR/

# Enter wiki directory
cd $TEMP_DIR

# Git operations
echo "💾 Committing changes..."
git add .
git commit -m "Update wiki documentation - $(date +%Y-%m-%d)"

echo "📤 Pushing to GitHub..."
git push origin master

# Cleanup
cd ..
rm -rf $TEMP_DIR

echo "✅ Wiki published successfully!"
echo "📖 View at: https://github.com/hywenklis/buddy-api/wiki"
```

Execute:
```bash
chmod +x publish-wiki.sh
./publish-wiki.sh
```

## 📝 Estrutura da Wiki no GitHub

Após publicação, a estrutura será:

```
Buddy API Wiki
├── Home (página inicial)
├── Getting Started
├── Architecture Overview
├── API Documentation
├── Database Schema
├── Configuration Guide
├── Testing Guide
├── Troubleshooting
├── FAQ
└── Glossary
```

## 🔗 Links entre Páginas

O GitHub Wiki converte automaticamente:
- `[Link](./Page.md)` → `[Link](Page)`
- Remova `.md` dos links internos após publicação

## ✅ Checklist Pós-Publicação

Após publicar, verifique:

- [ ] Todas as 10 páginas foram criadas
- [ ] Links entre páginas funcionam
- [ ] Blocos de código estão formatados
- [ ] Diagramas Mermaid renderizam corretamente
- [ ] Imagens (se houver) estão acessíveis
- [ ] Sidebar está configurada (opcional)

## 🎨 Configurar Sidebar (Opcional)

Crie uma página `_Sidebar.md` para navegação rápida:

```markdown
## 🐾 Buddy API Wiki

### 🚀 Começando
- [Home](Home)
- [Getting Started](Getting-Started)

### 🏗️ Arquitetura
- [Architecture Overview](Architecture-Overview)
- [Database Schema](Database-Schema)

### 📚 Guias
- [API Documentation](API-Documentation)
- [Configuration Guide](Configuration-Guide)
- [Testing Guide](Testing-Guide)

### 🔧 Ajuda
- [Troubleshooting](Troubleshooting)
- [FAQ](FAQ)
- [Glossary](Glossary)
```

## 🔄 Atualizações Futuras

Para atualizar a wiki:

1. Edite os arquivos em `wiki/` do repositório principal
2. Commit e push no repositório principal
3. Re-execute o script de publicação ou copie manualmente

## 📞 Problemas Comuns

### Erro: "Permission denied"

**Solução**: Verifique se você tem permissão de escrita no repositório.

### Erro: "Already exists"

**Solução**: Se o diretório temp já existe, remova:
```bash
rm -rf temp-wiki-publish
```

### Links não funcionam

**Solução**: No GitHub Wiki, remova `.md` dos links:
- ❌ `[Link](./Page.md)`
- ✅ `[Link](Page)`

### Diagramas Mermaid não renderizam

**Solução**: O GitHub Wiki suporta Mermaid. Verifique a sintaxe:
```markdown
\`\`\`mermaid
graph TD
    A[Start] --> B[End]
\`\`\`
```

## 📊 Verificação Final

Acesse cada página e verifique:
1. ✅ Título está correto
2. ✅ Formatação está OK
3. ✅ Links funcionam
4. ✅ Código renderiza corretamente
5. ✅ Diagramas aparecem

## 🎉 Pronto!

Sua wiki está publicada e acessível em:
**https://github.com/hywenklis/buddy-api/wiki**

Compartilhe com a comunidade! 🚀

---

**Dúvidas?** Abra uma issue no repositório.
