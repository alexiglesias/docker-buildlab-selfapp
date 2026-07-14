# ---------- Stage 1: BUILD ----------
FROM maven:3.9-eclipse-temurin-17 AS BUILD

WORKDIR /src

# Cache dependency layer separately from source code
COPY pom.xml .
RUN mvn -B dependency:go-offline

COPY src ./src
RUN mvn -B clean package -DskipTests

# ---------- Stage 2: RUNTIME ----------
# Slim JRE only — no Maven, no JDK compiler, no source tree in the final image
FROM eclipse-temurin:17-jre-jammy AS RUNTIME

LABEL project="docker-buildlab-selfapp"

RUN useradd -r -u 1001 appuser
WORKDIR /app
COPY --from=BUILD /src/target/*.jar app.jar
RUN chown 1001:1001 app.jar
USER 1001

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
    CMD wget -qO- http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar"]
