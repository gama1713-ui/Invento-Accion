# Build stage
FROM maven:3.8.8-openjdk-17 AS build
WORKDIR /app

# Copiar wrapper y pom primero para aprovechar cache
COPY .mvn .mvn
COPY mvnw .
COPY pom.xml .

RUN chmod +x mvnw

# Copiar código fuente y construir
COPY src ./src
RUN ./mvnw -B -DskipTests package
# Normalizar nombre del jar para la siguiente etapa
RUN cp target/*.jar app.jar

# Runtime stage
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copiar jar ya renombrado
COPY --from=build /app/app.jar /app/app.jar

# Exponer puerto por defecto
EXPOSE 8080

# Usar PORT si Render lo provee, fallback 8080
ENTRYPOINT ["sh","-c","java -jar /app/app.jar --server.port=${PORT:-8080}"]
