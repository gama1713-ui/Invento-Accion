package com.inventoaccion.auth.dto;

/*
 * DTO para recibir la solicitud de registro desde la API.
 *
 * DTO significa Data Transfer Object.
 * En palabras sencillas, esta clase sirve para transportar los datos
 * que llegan desde Postman, un formulario o un frontend hacia el backend.
 *
 * En este primer registro solo pedimos nombre y correo.
 * El username y la contraseña temporal se generarán automáticamente
 * desde el servicio de autenticación.
 */
public class RegistroAuthRequest {

    /*
     * Nombre completo del usuario que se va a registrar.
     */
    private String nombre;

    /*
     * Correo electrónico del usuario.
     * A este correo se enviará la validación de la cuenta.
     */
    private String correo;

    /*
     * Constructor vacío requerido por Spring Boot
     * para poder convertir el JSON recibido en un objeto Java.
     */
    public RegistroAuthRequest() {
    }

    /*
     * Constructor con datos.
     * Puede ser útil para pruebas o para crear objetos manualmente.
     */
    public RegistroAuthRequest(String nombre, String correo) {
        this.nombre = nombre;
        this.correo = correo;
    }

    /*
     * Obtiene el nombre recibido en la solicitud.
     */
    public String getNombre() {
        return nombre;
    }

    /*
     * Asigna el nombre recibido en la solicitud.
     */
    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    /*
     * Obtiene el correo recibido en la solicitud.
     */
    public String getCorreo() {
        return correo;
    }

    /*
     * Asigna el correo recibido en la solicitud.
     */
    public void setCorreo(String correo) {
        this.correo = correo;
    }
}