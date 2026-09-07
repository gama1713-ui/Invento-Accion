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
import org.springframework.web.bind.annotation.ResponseBody;

/*
 * Controlador web para el módulo Consulta Inventario.
 *
 * Spring Boot entrega la página web y funciona como intermediario
 * entre el navegador y la API interna desarrollada en Python.
 *
 * FastAPI escucha únicamente en el puerto interno 8000.
 * El navegador nunca es enviado hacia una dirección localhost.
 *
 * Este controlador realiza solamente consultas.
 * No crea, modifica ni elimina información de MongoDB Atlas.
 */
@Controller
public class ConsultaInventarioController {

    /*
     * Dirección interna de la API desarrollada con FastAPI.
     *
     * Esta dirección se utiliza únicamente dentro del equipo local
     * o del contenedor de Render.
     */
    private static final String URL_INTERNA_INVENTARIOS =
            "http://127.0.0.1:8000/api/inventarios";

    /*
     * Cliente utilizado para comunicarse con FastAPI.
     *
     * El tiempo máximo de conexión evita esperas indefinidas
     * cuando el proceso interno de Python no está disponible.
     */
    private final HttpClient clienteHttp = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    /*
     * Entrega la página dinámica de Consulta Inventario.
     */
    @GetMapping("/activos/consulta-inventario")
    public String mostrarConsultaInventario() {
        return "consulta-inventario";
    }

    /*
     * Consulta todos los inventarios mediante la API interna.
     */
    @GetMapping("/api/inventarios")
    @ResponseBody
    public ResponseEntity<byte[]> consultarInventarios() {
        return consultarApiInterna(URL_INTERNA_INVENTARIOS);
    }

    /*
     * Consulta un inventario utilizando su código.
     *
     * El código se codifica antes de añadirlo a la dirección interna.
     */
    @GetMapping("/api/inventarios/{codigo}")
    @ResponseBody
    public ResponseEntity<byte[]> consultarInventarioPorCodigo(
            @PathVariable String codigo
    ) {
        String codigoSeguro = URLEncoder.encode(
                codigo,
                StandardCharsets.UTF_8
        );

        return consultarApiInterna(
                URL_INTERNA_INVENTARIOS + "/" + codigoSeguro
        );
    }

    /*
     * Realiza una solicitud GET hacia FastAPI y conserva
     * el código HTTP recibido desde Python.
     */
    private ResponseEntity<byte[]> consultarApiInterna(String url) {
        try {
            HttpRequest solicitud = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(20))
                    .header("Accept", "application/json")
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
                    .header(HttpHeaders.CACHE_CONTROL, "no-store")
                    .body(respuestaPython.body());

        } catch (InterruptedException excepcion) {
            Thread.currentThread().interrupt();
            return crearRespuestaDeError();

        } catch (IOException | IllegalArgumentException excepcion) {
            return crearRespuestaDeError();
        }
    }

    /*
     * Devuelve un mensaje JSON controlado cuando FastAPI
     * no está disponible.
     */
    private ResponseEntity<byte[]> crearRespuestaDeError() {
        String mensaje =
                "{\"detail\":\"El servicio de consulta de inventarios "
                + "no está disponible.\"}";

        return ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(mensaje.getBytes(StandardCharsets.UTF_8));
    }
}