# =========================
# FASE DE BUILD
# =========================
FROM eclipse-temurin:17-jdk AS builder
WORKDIR /app

# 1️⃣ Copiar solo archivos de dependencias
COPY pom.xml .
COPY mvnw .
COPY .mvn .mvn

RUN chmod +x mvnw \
 && ./mvnw dependency:go-offline

# 2️⃣ Copiar el código
COPY src src

# 3️⃣ Compilar
RUN ./mvnw package -DskipTests

# =========================
# FASE DE RUNTIME
# =========================
FROM eclipse-temurin:17-jre
WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

ENV SERVER_PORT=8088
EXPOSE 8088

ENTRYPOINT ["sh", "-c", "java -jar app.jar --server.port=${SERVER_PORT}"]
