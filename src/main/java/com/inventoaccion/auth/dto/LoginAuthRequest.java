package com.inventoaccion.auth.dto;

/*
 * DTO para recibir la solicitud de inicio de sesion desde la API.
 *
 * Esta clase transporta los datos que el usuario enviara desde Postman,
 * un formulario web o un frontend para autenticarse en Invento-Accion.
 *
 * En este caso se reciben dos datos:
 * username: usuario generado por el sistema.
 * password: contraseña temporal recibida por correo.
 */
public class LoginAuthRequest {

    /*
     * Nombre de usuario usado para iniciar sesion.
     */
    private String username;

    /*
     * Contraseña enviada por el usuario.
     *
     * Esta contraseña se comparara contra la contraseña cifrada
     * que esta guardada en MongoDB.
     */
    private String password;

    /*
     * Constructor vacio requerido por Spring Boot
     * para convertir el JSON recibido en un objeto Java.
     */
    public LoginAuthRequest() {
    }

    /*
     * Constructor con datos.
     * Puede servir para pruebas o para crear objetos manualmente.
     */
    public LoginAuthRequest(String username, String password) {
        this.username = username;
        this.password = password;
    }

    /*
     * Obtiene el username recibido en la solicitud.
     */
    public String getUsername() {
        return username;
    }

    /*
     * Asigna el username recibido en la solicitud.
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /*
     * Obtiene la contraseña recibida en la solicitud.
     */
    public String getPassword() {
        return password;
    }

    /*
     * Asigna la contraseña recibida en la solicitud.
     */
    public void setPassword(String password) {
        this.password = password;
    }
}