package com.inventoaccion.web;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

/*
 * Controlador web para el módulo Transporte SITP.
 *
 * Esta clase permite abrir la página PHP desde la ruta:
 *
 * /gestion/transporte-sitp
 *
 * Spring Boot y PHP funcionan dentro del mismo contenedor.
 *
 * PHP escucha únicamente en el puerto interno 8081.
 * El navegador nunca es enviado hacia localhost.
 *
 * Spring Boot consulta internamente la página PHP y devuelve
 * el resultado al usuario desde la dirección principal de
 * Invento-Acción.
 *
 * Este controlador no modifica usuarios, ubicaciones, activos,
 * préstamos, MongoDB Atlas ni la API de autenticación.
 */
@Controller
public class TransporteSitpController {

    /*
     * Dirección interna del módulo PHP.
     *
     * Esta dirección solo se utiliza dentro del contenedor.
     * No se muestra ni se envía al navegador del usuario.
     */
    private static final String URL_INTERNA_SITP =
            "http://127.0.0.1:8081/paraderos_sitp.php";

    /*
     * Cliente HTTP utilizado para comunicarse con PHP.
     *
     * El tiempo máximo de conexión evita que la solicitud
     * quede esperando indefinidamente.
     */
    private final HttpClient clienteHttp = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    /*
     * Muestra la página principal del módulo Transporte SITP.
     *
     * También recibe el parámetro localidad cuando el usuario
     * envía el formulario.
     */
    @GetMapping("/gestion/transporte-sitp")
    public ResponseEntity<byte[]> mostrarTransporteSitp(
            @RequestParam(
                    name = "localidad",
                    required = false
            ) String localidad
    ) {

        /*
         * Se inicia con la dirección interna de la página PHP.
         */
        String urlConsulta = URL_INTERNA_SITP;

        /*
         * Si el usuario seleccionó una localidad, se agrega el
         * parámetro de manera segura a la consulta interna.
         */
        if (localidad != null) {
            urlConsulta += "?localidad=" + codificarParametro(localidad);
        }

        try {

            /*
             * Se prepara la solicitud hacia el servidor PHP interno.
             */
            HttpRequest solicitud = HttpRequest.newBuilder()
                    .uri(URI.create(urlConsulta))
                    .timeout(Duration.ofSeconds(40))
                    .header(
                            "Accept",
                            "text/html,application/xhtml+xml"
                    )
                    .GET()
                    .build();

            /*
             * Se ejecuta la solicitud y se recibe el HTML generado
             * por paraderos_sitp.php.
             */
            HttpResponse<byte[]> respuestaPhp =
                    clienteHttp.send(
                            solicitud,
                            HttpResponse.BodyHandlers.ofByteArray()
                    );

            /*
             * Se conserva el código HTTP entregado por PHP.
             *
             * Por ejemplo:
             * 200 para una consulta correcta.
             * 400 para una localidad incorrecta.
             * 404 cuando no existen resultados.
             * 500 cuando ocurre una falla.
             */
            HttpStatus estado = HttpStatus.resolve(
                    respuestaPhp.statusCode()
            );

            if (estado == null) {
                estado = HttpStatus.INTERNAL_SERVER_ERROR;
            }

            /*
             * Se devuelve el HTML al navegador desde la ruta
             * /gestion/transporte-sitp.
             */
            return ResponseEntity
                    .status(estado)
                    .contentType(
                            MediaType.parseMediaType(
                                    "text/html;charset=UTF-8"
                            )
                    )
                    .header(
                            HttpHeaders.CACHE_CONTROL,
                            "no-store"
                    )
                    .body(respuestaPhp.body());

        } catch (InterruptedException excepcion) {

            /*
             * Se restaura la señal de interrupción del proceso.
             */
            Thread.currentThread().interrupt();

            return crearRespuestaDeError();

        } catch (IOException | IllegalArgumentException excepcion) {

            /*
             * Si PHP no responde o la dirección no es válida,
             * se muestra un mensaje sencillo y controlado.
             */
            return crearRespuestaDeError();
        }
    }

    /*
     * Codifica el nombre de la localidad para enviarlo de forma
     * segura en la dirección interna.
     */
    private String codificarParametro(String valor) {
        return java.net.URLEncoder.encode(
                valor,
                java.nio.charset.StandardCharsets.UTF_8
        );
    }

    /*
     * Crea una respuesta amigable cuando el servidor PHP interno
     * no está disponible.
     */
    private ResponseEntity<byte[]> crearRespuestaDeError() {

        String paginaError = """
                <!DOCTYPE html>
                <html lang="es">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport"
                          content="width=device-width, initial-scale=1.0">
                    <title>Transporte SITP | Invento-Acción</title>
                    <style>
                        body {
                            margin: 0;
                            font-family: Arial, Helvetica, sans-serif;
                            background: #f4f6f9;
                            color: #212529;
                        }

                        .encabezado {
                            padding: 20px 28px;
                            background: linear-gradient(
                                135deg,
                                #0d6efd,
                                #084298
                            );
                            color: white;
                        }

                        .contenido {
                            max-width: 760px;
                            margin: 50px auto;
                            padding: 28px;
                            background: white;
                            border-radius: 14px;
                            box-shadow: 0 4px 16px
                                rgba(0, 0, 0, 0.10);
                        }

                        h1 {
                            color: #0d6efd;
                        }

                        .mensaje {
                            padding: 16px;
                            border: 1px solid #f1aeb5;
                            border-radius: 10px;
                            background: #f8d7da;
                            color: #58151c;
                            line-height: 1.6;
                        }

                        a {
                            display: inline-block;
                            margin-top: 20px;
                            color: #0d6efd;
                        }
                    </style>
                </head>
                <body>
                    <header class="encabezado">
                        <strong>Invento-Acción | Transporte SITP</strong>
                    </header>

                    <main class="contenido">
                        <h1>Servicio temporalmente no disponible</h1>

                        <div class="mensaje">
                            No fue posible iniciar la consulta de paraderos.
                            Por favor, inténtalo nuevamente.
                        </div>

                        <a href="/">Regresar a Invento-Acción</a>
                    </main>
                </body>
                </html>
                """;

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(
                        MediaType.parseMediaType(
                                "text/html;charset=UTF-8"
                        )
                )
                .header(
                        HttpHeaders.CACHE_CONTROL,
                        "no-store"
                )
                .body(
                        paginaError.getBytes(
                                java.nio.charset.StandardCharsets.UTF_8
                        )
                );
    }
}
