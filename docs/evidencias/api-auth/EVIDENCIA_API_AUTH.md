# Evidencia tecnica API Auth - Invento-Accion

## 1. Objetivo del modulo

Se incorporo un modulo de API REST para el proyecto Invento-Accion con el objetivo de validar el registro de usuarios, la validacion por correo y el inicio de sesion mediante usuario y contrasena.

La API fue desarrollada dentro del backend Spring Boot, conectada a MongoDB Atlas mediante la variable de entorno MONGODB_URI.

## 2. Rama de trabajo

Rama utilizada:

feature/api-auth

Estado validado:

- Rama sincronizada con origin/feature/api-auth.
- Proyecto compilado correctamente.
- API ejecutada localmente en el puerto 8080.
- Conexion correcta contra MongoDB Atlas.
- Pruebas realizadas con cURL como preparacion para Postman.

## 3. Componentes desarrollados

El modulo Auth contiene los siguientes componentes:

- AuthController.java
- AuthService.java
- EmailService.java
- AuthUsuario.java
- AuthUsuarioRepository.java
- RegistroAuthRequest.java
- LoginAuthRequest.java
- AuthResponse.java

## 4. Configuracion utilizada

La conexion a MongoDB Atlas se realiza mediante:

spring.data.mongodb.uri=${MONGODB_URI:mongodb://localhost:27017/invento_accion}

Esto permite que el proyecto use MongoDB Atlas cuando existe la variable MONGODB_URI y use MongoDB local como respaldo si la variable no esta configurada.

El servicio de correo queda preparado mediante variables de entorno:

- APP_BASE_URL
- MAIL_HOST
- MAIL_PORT
- MAIL_USERNAME
- MAIL_PASSWORD

Durante las pruebas locales, SMTP no estaba configurado, por lo tanto la API funciono en modo educativo.

## 5. Prueba de compilacion

Comando ejecutado:

./mvnw -DskipTests compile

Resultado obtenido:

BUILD SUCCESS

Conclusion:

El proyecto compila correctamente y el modulo Auth no presenta errores de codigo ni dependencias faltantes.

## 6. Arranque de la API contra MongoDB Atlas

Comando utilizado:

MONGODB_URI='URI_PRIVADA_DE_MONGODB_ATLAS' ./mvnw spring-boot:run

Resultado observado:

- Spring Boot inicio correctamente.
- Tomcat inicio en el puerto 8080.
- MongoDB driver detecto el cluster de Atlas.
- Se establecio conexion con el replica set de MongoDB Atlas.
- La aplicacion quedo disponible localmente.

Conclusion:

La API Auth logra conectarse correctamente a MongoDB Atlas usando MONGODB_URI.

## 7. Prueba 1 - Endpoint de prueba

Metodo:

GET

Ruta:

/api/auth/prueba

Comando ejecutado:

curl -i http://localhost:8080/api/auth/prueba

Resultado obtenido:

HTTP/1.1 200

Respuesta:

API Auth Invento-Accion funcionando correctamente

Conclusion:

El endpoint de prueba responde correctamente y confirma que el modulo Auth esta activo.

## 8. Prueba 2 - Registro de usuario

Metodo:

POST

Ruta:

/api/auth/registro

Body utilizado:

{
  "nombre": "Carmenza Prueba-1-API",
  "correo": "4test@pruebas.com"
}

Resultado obtenido:

HTTP/1.1 200

Respuesta resumida:

{
  "exitoso": true,
  "mensaje": "Usuario creado en modo educativo. Servicio SMTP no configurado; se mantiene modo educativo.",
  "token": null
}

Conclusion:

La API registro correctamente el usuario en MongoDB Atlas, genero username, contrasena temporal y token de validacion.

Nota:

Por seguridad, la contrasena temporal y el token completo no se documentan en esta evidencia.

## 9. Prueba 3 - Validacion de correo

Metodo:

GET

Ruta:

/api/auth/validar?token=TOKEN_GENERADO

Resultado obtenido:

HTTP/1.1 200

Respuesta:

{
  "exitoso": true,
  "mensaje": "Correo validado correctamente. Ya puedes iniciar sesion.",
  "token": null
}

Conclusion:

La API valido correctamente el token generado durante el registro y marco el correo como validado.

## 10. Prueba 4 - Login exitoso

Metodo:

POST

Ruta:

/api/auth/login

Body utilizado:

{
  "username": "4test",
  "password": "PASSWORD_TEMPORAL"
}

Resultado obtenido:

HTTP/1.1 200

Respuesta:

{
  "exitoso": true,
  "mensaje": "Autenticacion satisfactoria.",
  "token": "AUTH_OK_TOKEN_GENERADO"
}

Conclusion:

La API autentico correctamente al usuario cuando el username y la contrasena fueron validos y el correo ya estaba confirmado.

## 11. Prueba 5 - Login con contrasena incorrecta

Metodo:

POST

Ruta:

/api/auth/login

Body utilizado:

{
  "username": "4test",
  "password": "passwordIncorrecta123"
}

Resultado obtenido:

HTTP/1.1 401

Respuesta:

{
  "exitoso": false,
  "mensaje": "Error de autenticacion. Usuario o contrasena incorrectos.",
  "token": null
}

Conclusion:

La API rechazo correctamente el inicio de sesion cuando la contrasena fue incorrecta.

## 12. Prueba 6 - Registro con correo duplicado

Metodo:

POST

Ruta:

/api/auth/registro

Body utilizado:

{
  "nombre": "Carmenza Prueba Duplicada",
  "correo": "4test@pruebas.com"
}

Resultado obtenido:

HTTP/1.1 400

Respuesta:

{
  "exitoso": false,
  "mensaje": "El correo ya se encuentra registrado.",
  "token": null
}

Conclusion:

La API valido correctamente que no se pueda registrar dos veces el mismo correo.

## 13. Prueba 7 - Token falso

Metodo:

GET

Ruta:

/api/auth/validar?token=token-falso-prueba

Resultado obtenido:

HTTP/1.1 400

Respuesta:

{
  "exitoso": false,
  "mensaje": "Token de validacion no valido o ya utilizado.",
  "token": null
}

Conclusion:

La API rechazo correctamente un token falso o invalido.

## 14. Resultado general

El flujo principal del modulo Auth quedo validado correctamente:

1. API activa.
2. Registro de usuario.
3. Escritura en MongoDB Atlas.
4. Validacion de correo por token.
5. Inicio de sesion exitoso.
6. Error de autenticacion con contrasena incorrecta.
7. Rechazo de correo duplicado.
8. Rechazo de token falso.

## 15. Observacion sobre Microsoft Entra ID

Para esta primera version academica se implemento autenticacion propia con Spring Boot y MongoDB Atlas.

Microsoft Entra ID se puede considerar como una fase empresarial posterior, especialmente si se requiere integracion con cuentas corporativas de Microsoft 365, inicio de sesion organizacional o control centralizado de identidades.

## 16. Estado final

La API Auth queda lista para:

- Documentar pruebas en Postman.
- Subir evidencia a GitHub.
- Preparar configuracion posterior en Render.
- Continuar con una version futura usando JWT real o Microsoft Entra ID.
