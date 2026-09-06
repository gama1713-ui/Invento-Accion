# Imagen oficial de PHP para ejecutar el módulo Transporte SITP.
FROM php:8.4-cli

# Carpeta interna donde funcionará la aplicación PHP.
WORKDIR /app

# Copia únicamente los archivos del módulo SITP.
# El contexto de construcción será la raíz del repositorio.
COPY modulo-php-sitp/ /app/

# Render proporciona el puerto mediante la variable PORT.
# Se documenta 10000 como puerto predeterminado del contenedor.
EXPOSE 10000

# Inicia el servidor PHP escuchando conexiones externas.
# Si PORT no existe, utiliza el puerto 10000.
CMD ["sh", "-c", "php -S 0.0.0.0:${PORT:-10000} -t /app"]