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


# Etapa de ejecución con Java 17, PHP y Python.
FROM eclipse-temurin:17-jre-jammy

WORKDIR /app

# Instalar las herramientas necesarias para los módulos secundarios.
#
# PHP CLI ejecuta Transporte SITP.
# Python ejecuta la API REST de Consulta Inventario.
# ca-certificates permite validar conexiones HTTPS y TLS.
RUN apt-get update \
    && apt-get install -y --no-install-recommends \
        php-cli \
        python3 \
        python3-pip \
        python3-venv \
        ca-certificates \
    && rm -rf /var/lib/apt/lists/*

# Crear un entorno virtual exclusivo para la API Python.
RUN python3 -m venv /opt/inventarios-venv

# Copiar primero las dependencias Python para aprovechar la caché.
COPY requirements.txt /app/requirements.txt

# Instalar las dependencias declaradas del módulo.
RUN /opt/inventarios-venv/bin/python -m pip install \
        --no-cache-dir \
        -r /app/requirements.txt

# Copiar la aplicación Spring Boot construida.
COPY --from=build /app/app.jar /app/app.jar

# Copiar el módulo PHP Transporte SITP.
COPY modulo-php-sitp /app/modulo-php-sitp

# Copiar la API REST Python de Consulta Inventario.
#
# El archivo local .env queda fuera del contexto Docker
# porque está protegido mediante .dockerignore.
COPY modulo-python-inventarios /app/modulo-python-inventarios

# Render expone públicamente el puerto utilizado por Spring Boot.
EXPOSE 8080

# Iniciar los tres componentes dentro del mismo contenedor.
#
# FastAPI escucha internamente en el puerto 8000.
# PHP escucha internamente en el puerto 8081.
# Spring Boot utiliza el puerto público entregado por Render.
#
# Java permanece como proceso principal mediante exec.
ENTRYPOINT ["sh", "-c", "(cd /app/modulo-python-inventarios && /opt/inventarios-venv/bin/python -m uvicorn app.main:app --host 127.0.0.1 --port 8000 > /tmp/python-inventarios.log 2>&1) & php -S 127.0.0.1:8081 -t /app/modulo-php-sitp > /tmp/php-sitp.log 2>&1 & exec java -jar /app/app.jar --server.port=${PORT:-8080}"]
