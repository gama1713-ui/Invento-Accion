package com.inventoaccion.auth.model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

/*
 * Modelo de usuario para el modulo educativo de autenticacion.
 *
 * Esta clase representa la informacion que se guardara en MongoDB
 * para permitir registro, validacion de correo e inicio de sesion.
 *
 * Se crea separado del UsuarioMongo existente para no afectar
 * el modulo actual de gestion de usuarios del proyecto Invento-Accion.
 */
@Document(collection = "auth_usuarios")
public class AuthUsuario {

    /*
     * Identificador unico generado automaticamente por MongoDB.
     */
    @Id
    private String id;

    /*
     * Nombre completo de la persona registrada.
     */
    private String nombre;

    /*
     * Correo electronico del usuario.
     * Este correo se usara para enviar la validacion de cuenta.
     */
    private String correo;

    /*
     * Nombre de usuario generado para iniciar sesion.
     */
    private String username;

    /*
     * Contraseña cifrada.
     * Por seguridad, nunca se debe guardar la contraseña real en texto claro.
     */
    private String passwordHash;

    /*
     * Indica si el usuario ya valido su correo electronico.
     * Por defecto inicia en false.
     */
    private boolean correoValidado;

    /*
     * Token temporal usado para validar el correo electronico.
     */
    private String tokenValidacion;

    /*
     * Fecha y hora en la que se creo el usuario.
     */
    private LocalDateTime fechaCreacion;

    /*
     * Constructor vacio requerido por Spring Data MongoDB.
     */
    public AuthUsuario() {
    }

    /*
     * Constructor usado para crear un usuario de autenticacion.
     */
    public AuthUsuario(String nombre, String correo, String username, String passwordHash, String tokenValidacion) {
        this.nombre = nombre;
        this.correo = correo;
        this.username = username;
        this.passwordHash = passwordHash;
        this.tokenValidacion = tokenValidacion;
        this.correoValidado = false;
        this.fechaCreacion = LocalDateTime.now();
    }

    public String getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getCorreo() {
        return correo;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public boolean isCorreoValidado() {
        return correoValidado;
    }

    public String getTokenValidacion() {
        return tokenValidacion;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public void setCorreo(String correo) {
        this.correo = correo;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public void setCorreoValidado(boolean correoValidado) {
        this.correoValidado = correoValidado;
    }

    public void setTokenValidacion(String tokenValidacion) {
        this.tokenValidacion = tokenValidacion;
    }

    public void setFechaCreacion(LocalDateTime fechaCreacion) {
        this.fechaCreacion = fechaCreacion;
    }
}