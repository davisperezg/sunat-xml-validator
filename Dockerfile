# =========================
# FASE DE BUILD
# =========================
FROM eclipse-temurin:17-jdk AS builder
WORKDIR /app

# Copiar solo archivos de dependencias
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

RUN chmod +x mvnw \
 && ./mvnw dependency:resolve -B

# Copiar el código
COPY src src

# Compilar
RUN ./mvnw package -DskipTests -B

# =========================
# FASE DE RUNTIME
# =========================
FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

ENV SERVER_PORT=8088
EXPOSE 8088

ENTRYPOINT ["sh", "-c", "java -jar app.jar --server.port=${SERVER_PORT}"]
