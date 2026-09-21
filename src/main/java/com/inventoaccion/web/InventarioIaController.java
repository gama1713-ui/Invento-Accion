package com.inventoaccion.web;

import java.io.IOException;
import java.net.URI;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Controlador web para Inventario IA.
 *
 * Spring Boot entrega la futura pagina web y funciona como intermediario
 * entre el navegador y la API interna desarrollada con FastAPI.
 *
 * El navegador nunca se conecta directamente al puerto interno 8000.
 */
@Controller
public class InventarioIaController {

    private static final String URL_INTERNA_INVENTARIO_IA =
            "http://127.0.0.1:8000/api/inventario-ia";

    private final HttpClient clienteHttp = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    /**
     * Entrega la futura pagina de Inventario IA.
     */
    @GetMapping("/activos/inventario-ia")
    public String mostrarInventarioIa() {
        return "inventario-ia";
    }

    /**
     * Consulta el estado del componente interno.
     */
    @GetMapping("/api/inventario-ia/health")
    @ResponseBody
    public ResponseEntity<byte[]> consultarEstado() {
        return enviarGet(
                URL_INTERNA_INVENTARIO_IA + "/health"
        );
    }

    /**
     * Consulta la informacion general y limitaciones del modelo.
     */
    @GetMapping("/api/inventario-ia/modelo")
    @ResponseBody
    public ResponseEntity<byte[]> consultarModelo() {
        return enviarGet(
                URL_INTERNA_INVENTARIO_IA + "/modelo"
        );
    }

    /**
     * Envia una propuesta de compra a la API interna para evaluacion.
     */
    @PostMapping(
            value = "/api/inventario-ia/evaluar",
            consumes = MediaType.APPLICATION_JSON_VALUE
    )
    @ResponseBody
    public ResponseEntity<byte[]> evaluarCompra(
            @RequestBody byte[] cuerpo
    ) {
        return enviarPost(
                URL_INTERNA_INVENTARIO_IA + "/evaluar",
                cuerpo
        );
    }

    /**
     * Realiza una solicitud GET hacia FastAPI.
     */
    private ResponseEntity<byte[]> enviarGet(String url) {
        try {
            HttpRequest solicitud = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(30))
                    .header(
                            HttpHeaders.ACCEPT,
                            MediaType.APPLICATION_JSON_VALUE
                    )
                    .GET()
                    .build();

            return enviarSolicitud(solicitud);

        } catch (IllegalArgumentException excepcion) {
            return crearRespuestaDeError();
        }
    }

    /**
     * Realiza una solicitud POST hacia FastAPI.
     */
    private ResponseEntity<byte[]> enviarPost(
            String url,
            byte[] cuerpo
    ) {
        try {
            HttpRequest solicitud = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(30))
                    .header(
                            HttpHeaders.ACCEPT,
                            MediaType.APPLICATION_JSON_VALUE
                    )
                    .header(
                            HttpHeaders.CONTENT_TYPE,
                            MediaType.APPLICATION_JSON_VALUE
                    )
                    .POST(
                            HttpRequest.BodyPublishers.ofByteArray(cuerpo)
                    )
                    .build();

            return enviarSolicitud(solicitud);

        } catch (IllegalArgumentException excepcion) {
            return crearRespuestaDeError();
        }
    }

    /**
     * Envia la solicitud y conserva el codigo HTTP recibido desde Python.
     */
    private ResponseEntity<byte[]> enviarSolicitud(
            HttpRequest solicitud
    ) {
        try {
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
                    .header(HttpHeaders.CACHE_CONTROL, "no-store")
                    .body(respuestaPython.body());

        } catch (InterruptedException excepcion) {
            Thread.currentThread().interrupt();
            return crearRespuestaDeError();

        } catch (IOException excepcion) {
            return crearRespuestaDeError();
        }
    }

    /**
     * Devuelve un mensaje controlado cuando FastAPI no esta disponible.
     */
    private ResponseEntity<byte[]> crearRespuestaDeError() {
        String mensaje =
                "{\"detail\":\"El servicio de Inventario IA "
                + "no esta disponible.\"}";

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(mensaje.getBytes(StandardCharsets.UTF_8));
    }
}
