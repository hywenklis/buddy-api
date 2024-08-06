# Configuração do Checkstyle (IMPORTANTE)

Para garantir que todos os colaboradores utilizem as mesmas regras de formatação e verificação de código, configure o Checkstyle no IntelliJ IDEA seguindo as instruções abaixo.

## Passo 1: Instalar o Plugin Checkstyle-IDEA

1. Abra o IntelliJ IDEA.
2. Vá para `File > Settings > Plugins` (ou `Preferences > Plugins` no macOS).
3. Busque por "Checkstyle-IDEA" e instale o plugin.
4. Reinicie o IntelliJ IDEA, se necessário.

## Passo 2: Executar o Script de Configuração

### Linux e macOS:

1. Abra um terminal.
2. Navegue até o diretório do projeto.
3. Execute o seguinte comando:

    ```bash
    ./setup.sh
    ```

### Windows:

1. Abra o Prompt de Comando.
2. Navegue até o diretório do projeto.
3. Execute o seguinte comando:

    ```batch
    setup.bat
    ```

Esses scripts irão criar automaticamente a configuração do Checkstyle no IntelliJ IDEA, apontando para o arquivo de configuração localizado em `config/checkstyle/checkstyle.xml`.

## Passo 3: Configurar o Checkstyle no IntelliJ IDEA

1. No IntelliJ IDEA, vá para `File > Settings > Tools > Checkstyle` (ou `Preferences > Tools > Checkstyle` no macOS).
2. Verifique se a configuração do Checkstyle está apontando para o arquivo `config/checkstyle/checkstyle.xml`. A configuração deve ter sido criada automaticamente pelo script.
3. Clique em "OK" para salvar as configurações.

## Passo 4: Verificar e Corrigir o Código

1. Para verificar o código com o Checkstyle, vá para `Code > Analyze Code > Checkstyle > Check Whole Project`.
2. Corrija quaisquer violações de estilo de acordo com as regras definidas.

Seguindo esses passos, você garantirá que o código esteja sempre em conformidade com as regras de formatação e estilo definidas pelo projeto.

## Importante

Lembre-se de executar o script de configuração (`setup.sh` ou `setup.bat`) sempre que clonar o repositório ou fizer uma nova configuração do ambiente de desenvolvimento.

Se tiver dúvidas ou encontrar problemas, consulte a documentação do plugin Checkstyle-IDEA ou entre em contato com o administrador do projeto.
