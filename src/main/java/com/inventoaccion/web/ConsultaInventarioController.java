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
 * Controlador web para el módulo Consulta Inventario.
 *
 * Spring Boot entrega la página y funciona como intermediario
 * entre el navegador y la API interna desarrollada con FastAPI.
 *
 * FastAPI escucha únicamente en el puerto interno 8000.
 * El navegador nunca se conecta directamente a ese puerto.
 *
 * Este controlador realiza solamente consultas.
 * No crea, modifica ni elimina información de MongoDB Atlas.
 */
@Controller
public class ConsultaInventarioController {

    /**
     * Dirección interna del módulo de inventarios en FastAPI.
     */
    private static final String URL_INTERNA_INVENTARIOS =
            "http://127.0.0.1:8000/api/inventarios";

    /**
     * Límites aplicados también en Spring Boot.
     *
     * FastAPI vuelve a validar estos valores para mantener
     * la protección aunque se invoque directamente la API interna.
     */
    private static final int PAGINA_PREDETERMINADA = 1;
    private static final int TAMANO_PREDETERMINADO = 25;
    private static final int TAMANO_MAXIMO = 100;

    /**
     * Cliente utilizado para comunicarse con FastAPI.
     */
    private final HttpClient clienteHttp = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    /**
     * Entrega la página dinámica de Consulta Inventario.
     */
    @GetMapping("/activos/consulta-inventario")
    public String mostrarConsultaInventario() {
        return "consulta-inventario";
    }

    /**
     * Consulta una página controlada de inventarios.
     *
     * Nunca permite solicitar más de 100 registros.
     */
    @GetMapping("/api/inventarios")
    @ResponseBody
    public ResponseEntity<byte[]> consultarInventarios(
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

        String url = URL_INTERNA_INVENTARIOS
                + "?pagina="
                + pagina
                + "&tamano="
                + tamano;

        return consultarApiInterna(url);
    }

    /**
     * Consulta el estado del servicio sin descargar inventarios.
     */
    @GetMapping("/api/inventarios/estado")
    @ResponseBody
    public ResponseEntity<byte[]> consultarEstadoInventarios() {
        return consultarApiInterna(
                URL_INTERNA_INVENTARIOS + "/estado"
        );
    }

    /**
     * Consulta un inventario utilizando su código.
     */
    @GetMapping("/api/inventarios/{codigo}")
    @ResponseBody
    public ResponseEntity<byte[]> consultarInventarioPorCodigo(
            @PathVariable String codigo
    ) {
        String codigoLimpio = codigo == null
                ? ""
                : codigo.trim();

        if (codigoLimpio.isBlank()) {
            return crearRespuestaDeValidacion(
                    "El código de inventario es obligatorio."
            );
        }

        String codigoSeguro = URLEncoder.encode(
                codigoLimpio,
                StandardCharsets.UTF_8
        );

        return consultarApiInterna(
                URL_INTERNA_INVENTARIOS
                + "/"
                + codigoSeguro
        );
    }

    /**
     * Realiza una solicitud GET hacia FastAPI y conserva
     * el código HTTP recibido desde Python.
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
        String mensaje = crearJsonDetalle(detalle);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
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

    /**
     * Devuelve un mensaje controlado cuando FastAPI
     * no está disponible.
     */
    private ResponseEntity<byte[]> crearRespuestaDeError() {
        String mensaje = crearJsonDetalle(
                "El servicio de consulta de inventarios "
                + "no está disponible."
        );

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
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

    /**
     * Construye una respuesta JSON controlada.
     *
     * El detalle proviene únicamente de textos internos
     * definidos por la aplicación.
     */
    private String crearJsonDetalle(String detalle) {
        String detalleSeguro = detalle
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");

        return "{\"detail\":\""
                + detalleSeguro
                + "\"}";
    }
}
