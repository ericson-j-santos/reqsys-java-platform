FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /workspace
COPY pom.xml ./
COPY libs ./libs
COPY apps ./apps
RUN mvn -B -DskipTests package -pl apps/reqsys-enterprise-api -am

FROM eclipse-temurin:21-jre-jammy

LABEL org.opencontainers.image.title="ReqSys Enterprise API" \
      org.opencontainers.image.description="API corporativa ReqSys com gates de producao" \
      org.opencontainers.image.source="https://github.com/ericson-j-santos/reqsys-java-platform"

RUN apt-get update \
    && apt-get install -y --no-install-recommends curl ca-certificates \
    && rm -rf /var/lib/apt/lists/* \
    && groupadd --system reqsys \
    && useradd --system --gid reqsys --home-dir /app --shell /usr/sbin/nologin reqsys

WORKDIR /app
COPY --from=build /workspace/apps/reqsys-enterprise-api/target/reqsys-enterprise-api-*.jar /app/reqsys-enterprise-api.jar

RUN chown -R reqsys:reqsys /app

USER reqsys
EXPOSE 8081

ENV SPRING_PROFILES_ACTIVE=prod
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

HEALTHCHECK --interval=30s --timeout=5s --start-period=60s --retries=3 \
  CMD curl -fsS http://localhost:8081/actuator/health || exit 1

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/reqsys-enterprise-api.jar"]
