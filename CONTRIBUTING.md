## Guia Completo para Contribuidores

Caso você esteja lendo esta versão de README, você está pegando o projeto num estágio extremamente inicial, porém empolgante, pois há várias coisas a serem definidas. Então, caso queira contribuir, utilize as issues para entender quais pontos ainda não foram resolvidos, conversar conosco e contribuir.

Contribuições são bem-vindas! Siga os passos abaixo para contribuir:

#### Realizando PRs para o repositório raiz

1. **Faça um fork desse repositório no GitHub:**
   - Clique no botão "Fork" no canto superior direito. Isso criará uma cópia do repositório em sua conta do GitHub.

2. **Clone o fork para o seu computador:**
   - Abra o terminal e navegue até o diretório onde deseja clonar o repositório.
   - Execute o seguinte comando:
     ```bash
     git clone https://github.com/<SEU_USUARIO>/buddy-backend.git
     ```
     *Substitua `<SEU_USUARIO>` pelo seu nome de usuário no GitHub.*
3.  **Tipos de branchs:**
   - **main:** *Branch principal do projeto, onde o código estável é armazenado.*
   - **develop:** *Branch de desenvolvimento, onde as novas features são criadas e testadas.*
   - **feature:** *Branches criadas a partir da develop para implementação de novas features.*
   - **fix:** *Branches criadas a partir da develop para correção de bugs.*
   - **hotfix:** *Branches criadas a partir da main para correções urgentes de bugs.*

4. **Crie uma branch para sua contribuição:**
   - Crie uma branch para cada feature ou correção que você for implementar. Por exemplo:
     ```bash
     git checkout -b minha-feature
     ```
     *Substitua `minha-feature` pelo nome da sua branch. tenha em mente de sempre ter o prefixo do tipo de branchs ex: `feature/nome-relacionado`*

5. **Implemente sua contribuição:**
   - Faça as alterações no código de acordo com sua feature ou correção.
   - Adicione e commite as suas mudanças:
     ```bash
     git add <arquivos_modificados>
     git commit -m "Mensagem do seu commit"
     ```

6. **Envie sua branch para o GitHub:**
   - Envie sua branch para o seu fork remoto:
     ```bash
     git push origin minha-feature
     ```

7. **Crie uma pull request:**
   - No GitHub, acesse seu fork do repositório.
   - Clique na aba "Pull requests".
   - Clique no botão "New pull request".
   - Selecione sua branch como branch de origem e a branch `develop` como branch de destino.
   - Adicione um título e uma descrição detalhada da sua contribuição.
   - Clique no botão "Create pull request".

#### Revise e finalize o merge

- Aguarde a revisão da sua pull request por outros colaboradores.
- Responda a comentários e faça as alterações solicitadas.
- Quando sua pull request for aprovada, você poderá fazer o merge na branch `develop`.

### Opções de merge:

- **Squash and merge:** Combina todas as alterações da sua branch em um único commit antes de mesclar na branch `develop`. Utilize este método para merge de pull requests em branches de desenvolvimento (feature e fix).
- **Merge commit:** Cria um novo commit de merge que inclui todas as alterações da sua branch. Utilize este método para merge da branch develop na main.

Escolha a opção de merge de acordo com as diretrizes do projeto.

## Boas Práticas de Contribuição

- Faça commits claros e descritivos.
- Teste seu código antes de enviar uma pull request.
- Respeite o código de conduta do projeto.
- Participe das discussões e revisões de código.
- Exclua a branch feature ou fix após o merge na develop.
- Não exclua a branch develop após o merge na main.

## Recursos Adicionais

- [Documentação do Git](https://git-scm.com/doc)
- [Guia de contribuição do GitHub](https://docs.github.com/en/pull-requests/collaborating-with-pull-requests/proposing-changes-to-your-work-with-pull-requests/creating-a-pull-request)

Agradecemos sua contribuição para o Buddy!
