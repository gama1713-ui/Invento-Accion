package com.inventoaccion.auth.dto;

/*
 * DTO de respuesta para la API de autenticacion.
 *
 * Esta clase se usa para devolver respuestas claras desde el backend
 * hacia Postman, un formulario web o un frontend.
 *
 * Permite informar si una operacion fue exitosa, mostrar un mensaje
 * entendible y devolver un token cuando el inicio de sesion sea correcto.
 */
public class AuthResponse {

    /*
     * Indica si la operacion fue exitosa o no.
     *
     * true significa que la operacion se realizo correctamente.
     * false significa que ocurrio algun error o validacion fallida.
     */
    private boolean exitoso;

    /*
     * Mensaje que explica el resultado de la operacion.
     *
     * Ejemplos:
     * Usuario creado correctamente.
     * Autenticacion satisfactoria.
     * Error de autenticacion.
     */
    private String mensaje;

    /*
     * Token de autenticacion.
     *
     * Por ahora puede ir vacio o null.
     * Mas adelante se usara para devolver el token JWT cuando el login sea correcto.
     */
    private String token;

    /*
     * Constructor vacio requerido por Spring Boot.
     */
    public AuthResponse() {
    }

    /*
     * Constructor con datos para crear respuestas de forma sencilla.
     */
    public AuthResponse(boolean exitoso, String mensaje, String token) {
        this.exitoso = exitoso;
        this.mensaje = mensaje;
        this.token = token;
    }

    /*
     * Obtiene si la operacion fue exitosa.
     */
    public boolean isExitoso() {
        return exitoso;
    }

    /*
     * Asigna si la operacion fue exitosa.
     */
    public void setExitoso(boolean exitoso) {
        this.exitoso = exitoso;
    }

    /*
     * Obtiene el mensaje de respuesta.
     */
    public String getMensaje() {
        return mensaje;
    }

    /*
     * Asigna el mensaje de respuesta.
     */
    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }

    /*
     * Obtiene el token de autenticacion.
     */
    public String getToken() {
        return token;
    }

    /*
     * Asigna el token de autenticacion.
     */
    public void setToken(String token) {
        this.token = token;
    }
}