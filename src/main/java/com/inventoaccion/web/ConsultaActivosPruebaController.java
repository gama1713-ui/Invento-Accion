package com.inventoaccion.web;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Puente web de solo lectura para los activos sintéticos académicos.
 *
 * Spring Boot entrega la página pública y reenvía las consultas
 * hacia FastAPI en el puerto interno 8000.
 *
 * Este controlador no crea, actualiza, reemplaza ni elimina datos.
 */
@Controller
public class ConsultaActivosPruebaController {

    private static final String URL_INTERNA_ACTIVOS_PRUEBA =
            "http://127.0.0.1:8000/api/activos-prueba";

    private static final int PAGINA_PREDETERMINADA = 1;
    private static final int TAMANO_PREDETERMINADO = 25;
    private static final int TAMANO_MAXIMO = 100;

    private final HttpClient clienteHttp = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    /**
     * Entrega la futura página de consulta de activos sintéticos.
     */
    @GetMapping("/activos/consulta-activos-prueba")
    public String mostrarConsultaActivosPrueba() {
        return "consulta-activos-prueba";
    }

    /**
     * Consulta una página controlada de activos sintéticos.
     */
    @GetMapping("/api/activos-prueba")
    @ResponseBody
    public ResponseEntity<byte[]> consultarActivosPrueba(
            @RequestParam(defaultValue = "1") int pagina,
            @RequestParam(defaultValue = "25") int tamano
    ) {
        if (pagina < PAGINA_PREDETERMINADA) {
            return crearRespuestaDeValidacion(
                    "La página debe ser mayor o igual a 1."
            );
        }

        if (
                tamano < 1
                || tamano > TAMANO_MAXIMO
        ) {
            return crearRespuestaDeValidacion(
                    "El tamaño debe estar entre 1 y "
                    + TAMANO_MAXIMO
                    + " registros."
            );
        }

        String url = URL_INTERNA_ACTIVOS_PRUEBA
                + "?pagina="
                + pagina
                + "&tamano="
                + tamano;

        return consultarApiInterna(url);
    }

    /**
     * Consulta el estado del servicio sin descargar activos.
     */
    @GetMapping("/api/activos-prueba/estado")
    @ResponseBody
    public ResponseEntity<byte[]> consultarEstado() {
        return consultarApiInterna(
                URL_INTERNA_ACTIVOS_PRUEBA + "/estado"
        );
    }

    /**
     * Busca mediante código, placa, serial o id numérico.
     */
    @GetMapping("/api/activos-prueba/{identificador}")
    @ResponseBody
    public ResponseEntity<byte[]> consultarActivoPrueba(
            @PathVariable String identificador
    ) {
        String valor = identificador == null
                ? ""
                : identificador.trim();

        if (valor.isBlank()) {
            return crearRespuestaDeValidacion(
                    "El identificador del activo es obligatorio."
            );
        }

        String identificadorSeguro = URLEncoder.encode(
                valor,
                StandardCharsets.UTF_8
        );

        return consultarApiInterna(
                URL_INTERNA_ACTIVOS_PRUEBA
                + "/"
                + identificadorSeguro
        );
    }

    /**
     * Realiza una solicitud GET hacia FastAPI.
     */
    private ResponseEntity<byte[]> consultarApiInterna(
            String url
    ) {
        try {
            HttpRequest solicitud = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(20))
                    .header(
                            HttpHeaders.ACCEPT,
                            MediaType.APPLICATION_JSON_VALUE
                    )
                    .GET()
                    .build();

            HttpResponse<byte[]> respuestaPython = clienteHttp.send(
                    solicitud,
                    HttpResponse.BodyHandlers.ofByteArray()
            );

            HttpStatus estado = HttpStatus.resolve(
                    respuestaPython.statusCode()
            );

            if (estado == null) {
                estado = HttpStatus.INTERNAL_SERVER_ERROR;
            }

            return ResponseEntity
                    .status(estado)
                    .contentType(MediaType.APPLICATION_JSON)
                    .header(
                            HttpHeaders.CACHE_CONTROL,
                            "no-store"
                    )
                    .body(respuestaPython.body());

        } catch (InterruptedException excepcion) {
            Thread.currentThread().interrupt();
            return crearRespuestaDeError();

        } catch (
                IOException
                | IllegalArgumentException excepcion
        ) {
            return crearRespuestaDeError();
        }
    }

    /**
     * Devuelve HTTP 400 para parámetros inválidos.
     */
    private ResponseEntity<byte[]> crearRespuestaDeValidacion(
            String detalle
    ) {
        return crearRespuestaJson(
                HttpStatus.BAD_REQUEST,
                detalle
        );
    }

    /**
     * Devuelve HTTP 503 cuando FastAPI no está disponible.
     */
    private ResponseEntity<byte[]> crearRespuestaDeError() {
        return crearRespuestaJson(
                HttpStatus.SERVICE_UNAVAILABLE,
                "El servicio de activos sintéticos "
                + "no está disponible."
        );
    }

    /**
     * Construye una respuesta JSON controlada.
     */
    private ResponseEntity<byte[]> crearRespuestaJson(
            HttpStatus estado,
            String detalle
    ) {
        String detalleSeguro = detalle
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");

        String mensaje = "{\"detail\":\""
                + detalleSeguro
                + "\"}";

        return ResponseEntity
                .status(estado)
                .contentType(MediaType.APPLICATION_JSON)
                .header(
                        HttpHeaders.CACHE_CONTROL,
                        "no-store"
                )
                .body(
                        mensaje.getBytes(
                                StandardCharsets.UTF_8
                        )
                );
    }
}
