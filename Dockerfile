FROM eclipse-temurin:21-jre
WORKDIR /app
COPY apps/reqsys-enterprise-api/target/reqsys-enterprise-api-*.jar /app/reqsys-enterprise-api.jar
EXPOSE 8081
ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Dspring.profiles.active=${SPRING_PROFILES_ACTIVE:-prod}", \
  "-jar", "/app/reqsys-enterprise-api.jar"]