# Etapa de construcción de Spring Boot.
FROM maven:3.8.8 AS build

WORKDIR /app

# Copiar primero Maven Wrapper y pom.xml para aprovechar la caché.
COPY .mvn .mvn
COPY mvnw .
COPY pom.xml .

RUN chmod +x mvnw

# Copiar el código fuente y construir el archivo JAR.
COPY src ./src

RUN sed -i 's/\r$//' mvnw \
    && chmod +x mvnw \
    && mvn -B -Dmaven.repo.local=/root/.m2 -DskipTests package

# Normalizar el nombre del archivo JAR.
RUN cp target/*.jar app.jar


# Etapa de ejecución con Java 17 y PHP en el mismo contenedor.
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Instalar únicamente PHP CLI y certificados HTTPS.
# PHP se utiliza para ejecutar el módulo Transporte SITP.
RUN apt-get update \
    && apt-get install -y --no-install-recommends php-cli ca-certificates \
    && rm -rf /var/lib/apt/lists/*

# Copiar la aplicación Spring Boot construida.
COPY --from=build /app/app.jar /app/app.jar

# Copiar únicamente el módulo PHP Transporte SITP.
COPY modulo-php-sitp /app/modulo-php-sitp

# Render expone públicamente el puerto utilizado por Spring Boot.
EXPOSE 8080

# Iniciar PHP solamente dentro del contenedor en el puerto interno 8081.
# Después iniciar Spring Boot en el puerto público entregado por Render.
# exec mantiene a Java como proceso principal del contenedor.
ENTRYPOINT ["sh", "-c", "php -S 127.0.0.1:8081 -t /app/modulo-php-sitp > /tmp/php-sitp.log 2>&1 & exec java -jar /app/app.jar --server.port=${PORT:-8080}"]
