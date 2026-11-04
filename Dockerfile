# syntax=docker/dockerfile:1
# Build multi-stage: o jar final não carrega JDK/Gradle, só o JRE.
FROM eclipse-temurin:21-jdk-alpine AS build
WORKDIR /app

# git real (não JGit): com.palantir.git-version resolve a versão pela tag.
RUN apk add --no-cache git

# Camada isolada com o que o Gradle precisa pra resolver dependências, cacheada entre builds.
COPY gradlew ./
COPY gradle ./gradle
COPY *.gradle* ./
RUN ./gradlew --no-daemon help > /dev/null 2>&1 || true

COPY . .
RUN ./gradlew --no-daemon bootJar -x test

# ---- Runtime ----
# Mesma base e mesmo contrato da imagem que o deploy_gradle.sh gerava pro ramo OpenJ9
# (IBM Semeru): o serviço no Swarm já traz JAVA_OPTS=-javaagent:/app/otel-agent.jar ... e
# SERVER_PORT, então o Enxame só troca a imagem e o resto do spec continua valendo.
FROM icr.io/appcafe/ibm-semeru-runtimes:open-21-jre-ubi-minimal

# A imagem da IBM roda como 1001; root só pra criar as pastas e ajustar permissões.
USER root
RUN mkdir -p /app/storage /app/scc && chown -R 1001:0 /app && chmod -R g+rw /app

ENV TZ=America/Recife \
    SPRING_MAIN_BANNER_MODE=off \
    SPRING_PROFILES_ACTIVE=prod \
    CONFIG_SERVER_LABEL=master \
    CONFIG_SERVER_URI=http://propresto-jaiminho:21050/api \
    OPENJ9_OPTS="-Xquickstart -Xtune:virtualized -Xshareclasses:cacheDir=/app/scc,name=springcache -Xscmx256M -XX:+IdleTuningGcOnIdle -XX:+IdleTuningCompactOnIdle" \
    JAVA_OPTS="-javaagent:/app/otel-agent.jar"

COPY --from=build --chown=1001:0 /app/build/libs/app.jar /app/app.jar
# Versão fixa — bump deliberado, nunca "latest" (mesma do deploy_gradle.sh).
ADD --chown=1001:0 --chmod=644 https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/v2.26.1/opentelemetry-javaagent.jar /app/otel-agent.jar
WORKDIR /app
USER 1001

# Warm-up do shared class cache do OpenJ9 (mesmo passo do deploy_gradle.sh).
RUN timeout 15s java ${OPENJ9_OPTS} -jar app.jar --spring.main.web-application-type=NONE --spring.cloud.config.enabled=false --spring.cloud.bootstrap.enabled=false || true

EXPOSE 8080
# SERVER_PORT vem do serviço no Swarm (21060); o actuator do buddy fica fora do context-path.
HEALTHCHECK --interval=5s --timeout=3s --retries=5 --start-period=300s \
  CMD curl -sS "http://localhost:${SERVER_PORT:-8080}/actuator/health" 2>/dev/null | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["sh", "-c", "exec java ${OPENJ9_OPTS} ${JAVA_OPTS} -jar app.jar"]
