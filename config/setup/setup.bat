@echo off

REM Cria o diretório .idea se não existir
if not exist .idea mkdir .idea

REM Cria o arquivo de configuração do Checkstyle no IntelliJ IDEA
echo ^<component name="CheckStyle-IDEA"^> > .idea\checkstyle-idea.xml
echo ^    ^<option name="configuration"^> >> .idea\checkstyle-idea.xml
echo ^        ^<list^> >> .idea\checkstyle-idea.xml
echo ^            ^<option name="Local file" value="%%PROJECT_DIR%%/config/checkstyle/checkstyle.xml" /^> >> .idea\checkstyle-idea.xml
echo ^        ^</list^> >> .idea\checkstyle-idea.xml
echo ^    ^</option^> >> .idea\checkstyle-idea.xml
echo ^</component^> >> .idea\checkstyle-idea.xml

echo Checkstyle configurado no IntelliJ IDEA.
