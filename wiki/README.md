# Wiki do Buddy API

Este diretório contém toda a documentação do projeto Buddy API em formato Markdown, pronta para ser usada no GitHub Wiki.

## 📚 Conteúdo da Wiki

### Páginas Principais

1. **[Home.md](./Home.md)** - Página inicial da wiki com índice completo
2. **[Getting-Started.md](./Getting-Started.md)** - Guia de início rápido e instalação
3. **[Architecture-Overview.md](./Architecture-Overview.md)** - Visão geral da arquitetura do sistema
4. **[API-Documentation.md](./API-Documentation.md)** - Documentação completa da API REST
5. **[Database-Schema.md](./Database-Schema.md)** - Esquema do banco de dados e migrações
6. **[Configuration-Guide.md](./Configuration-Guide.md)** - Guia de configuração da aplicação
7. **[Testing-Guide.md](./Testing-Guide.md)** - Guia de testes e boas práticas
8. **[Troubleshooting.md](./Troubleshooting.md)** - Solução de problemas comuns
9. **[FAQ.md](./FAQ.md)** - Perguntas frequentes
10. **[Glossary.md](./Glossary.md)** - Glossário de termos técnicos

## 🚀 Como Usar Esta Documentação

### Para Desenvolvedores

Se você está começando no projeto:
1. Leia o [Getting Started](./Getting-Started.md)
2. Entenda a [Arquitetura](./Architecture-Overview.md)
3. Explore a [Documentação da API](./API-Documentation.md)
4. Consulte o [Glossário](./Glossary.md) quando necessário

### Para Contribuidores

Antes de contribuir:
1. Leia o [Getting Started](./Getting-Started.md)
2. Estude o [Testing Guide](./Testing-Guide.md)
3. Consulte o [Configuration Guide](./Configuration-Guide.md)
4. Veja o [CONTRIBUTING.md](../CONTRIBUTING.md) no repositório principal

### Para Usuários da API

Se você vai integrar com a API:
1. Comece pela [Documentação da API](./API-Documentation.md)
2. Use o Swagger UI para testes: http://localhost:8080/api/swagger-ui/index.html
3. Consulte o [FAQ](./FAQ.md) para dúvidas comuns
4. Veja o [Troubleshooting](./Troubleshooting.md) se encontrar problemas

## 📖 Como Publicar no GitHub Wiki

Para publicar estas páginas no GitHub Wiki oficial do projeto:

### Método 1: Manual (Recomendado para primeira vez)

1. Vá para https://github.com/hywenklis/buddy-api/wiki
2. Clique em "New Page" ou edite uma página existente
3. Cole o conteúdo do arquivo Markdown correspondente
4. Salve a página

### Método 2: Via Git (Avançado)

```bash
# Clone o repositório wiki
git clone https://github.com/hywenklis/buddy-api.wiki.git

# Copie os arquivos markdown
cp wiki/*.md buddy-api.wiki/

# Commit e push
cd buddy-api.wiki
git add .
git commit -m "Update wiki documentation"
git push origin master
```

### Método 3: Script Automatizado

```bash
#!/bin/bash
# publish-wiki.sh

WIKI_REPO="https://github.com/hywenklis/buddy-api.wiki.git"
TEMP_DIR="temp-wiki"

# Clone wiki repo
git clone $WIKI_REPO $TEMP_DIR

# Copy markdown files
cp wiki/*.md $TEMP_DIR/

# Commit and push
cd $TEMP_DIR
git add .
git commit -m "Update wiki documentation - $(date +%Y-%m-%d)"
git push origin master

# Cleanup
cd ..
rm -rf $TEMP_DIR

echo "Wiki updated successfully!"
```

## 🔄 Manutenção da Documentação

### Atualizando a Documentação

Quando fizer mudanças no código que afetem a documentação:

1. **Atualize os arquivos Markdown relevantes** neste diretório
2. **Commite as mudanças** junto com o código
3. **Publique no GitHub Wiki** usando um dos métodos acima

### Estrutura de Commits

Use commits semânticos:

```bash
# Nova página
git commit -m "docs: add deployment guide to wiki"

# Atualização de página existente
git commit -m "docs: update API documentation with new endpoints"

# Correção de typo
git commit -m "docs: fix typo in getting started guide"
```

### Revisão de Documentação

- Revise a documentação a cada release
- Mantenha exemplos de código atualizados
- Verifique se os links estão funcionando
- Atualize screenshots se a UI mudou

## 📝 Convenções de Escrita

### Formatação

- Use **Markdown** padrão GitHub
- Inclua emojis para melhor visualização (opcional)
- Use blocos de código com syntax highlighting:
  ```java
  public class Example { }
  ```
- Use tabelas para comparações
- Use listas para passos sequenciais

### Estrutura das Páginas

Toda página deve ter:

```markdown
# 🎯 Título da Página

Breve descrição do conteúdo.

## Seção 1

Conteúdo...

## Seção 2

Conteúdo...

---

**Mantido por**: @hywenklis | **Última atualização**: Mês Ano
```

### Estilo

- **Tom**: Profissional mas acessível
- **Idioma**: Português (Brasil)
- **Código**: Comentários em português, código em inglês
- **Exemplos**: Sempre inclua exemplos práticos
- **Links**: Use links relativos entre páginas da wiki

## 🌐 Links Úteis

### Documentação Oficial

- **GitHub Wiki**: https://github.com/hywenklis/buddy-api/wiki
- **Swagger UI**: https://buddy.propresto.app/api/swagger-ui/index.html
- **Repositório**: https://github.com/hywenklis/buddy-api

### Referências Externas

- [GitHub Markdown Guide](https://guides.github.com/features/mastering-markdown/)
- [Spring Boot Documentation](https://docs.spring.io/spring-boot/docs/current/reference/html/)
- [Mermaid Diagram Syntax](https://mermaid-js.github.io/mermaid/)

## 🤝 Contribuindo para a Documentação

Encontrou um erro? Quer adicionar conteúdo?

1. Faça um fork do repositório
2. Edite os arquivos em `wiki/`
3. Abra um Pull Request
4. Descreva suas mudanças

Ou simplesmente abra uma [issue](https://github.com/hywenklis/buddy-api/issues) reportando o problema.

## 📋 Checklist de Qualidade

Antes de publicar nova documentação, verifique:

- [ ] Markdown está formatado corretamente
- [ ] Código de exemplo foi testado
- [ ] Links estão funcionando
- [ ] Imagens/diagramas estão incluídos (se necessário)
- [ ] Revisão ortográfica feita
- [ ] Informações técnicas estão corretas
- [ ] Exemplos são relevantes e claros
- [ ] Rodapé com autor e data está presente

## 📊 Estatísticas da Documentação

- **Total de páginas**: 10
- **Última atualização completa**: Dezembro 2024
- **Idioma**: Português (Brasil)
- **Cobertura**: ~90% das funcionalidades
- **Exemplos de código**: 50+

## 🎯 Roadmap da Documentação

### Próximas Adições

- [ ] Guia de Deploy detalhado
- [ ] Guia de Performance e Monitoramento
- [ ] Tutorial passo-a-passo para iniciantes
- [ ] Guia de Integração com serviços externos
- [ ] Exemplos de uso em diferentes linguagens
- [ ] Vídeos tutoriais (futuro)

### Melhorias Contínuas

- [ ] Adicionar mais diagramas Mermaid
- [ ] Incluir screenshots da aplicação
- [ ] Tradução para inglês (futuro)
- [ ] Versão em PDF para download

## 📧 Contato

Dúvidas sobre a documentação?

- **Maintainer**: @hywenklis
- **Email**: hywenklis@hotmail.com
- **Issues**: https://github.com/hywenklis/buddy-api/issues
- **Discussions**: https://github.com/hywenklis/buddy-api/discussions

---

**Obrigado por contribuir para melhorar a documentação do Buddy API!** 🐾
