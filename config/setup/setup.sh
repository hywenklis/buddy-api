#!/bin/bash

# Cria o arquivo de configuração do Checkstyle no IntelliJ IDEA
mkdir -p .idea

cat <<EOT > .idea/checkstyle-idea.xml
<component name="CheckStyle-IDEA">
    <option name="configuration">
        <list>
            <option name="Local file" value="\$PROJECT_DIR\$/config/checkstyle/checkstyle.xml" />
        </list>
    </option>
</component>
EOT

echo "Checkstyle configurado no IntelliJ IDEA."
