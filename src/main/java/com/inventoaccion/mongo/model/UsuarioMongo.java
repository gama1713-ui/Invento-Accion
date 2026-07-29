package com.inventoaccion.mongo.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "usuarios")
public class UsuarioMongo {

    @Id
    private String id;

    private String documentoIdentidad;
    private String nombre;
    private String apellidos;
    private String email;
    private String telefonoContacto;
    private String rol;

    public UsuarioMongo() {
    }

    public UsuarioMongo(String documentoIdentidad, String nombre, String apellidos, String email, String telefonoContacto, String rol) {
        this.documentoIdentidad = documentoIdentidad;
        this.nombre = nombre;
        this.apellidos = apellidos;
        this.email = email;
        this.telefonoContacto = telefonoContacto;
        this.rol = rol;
    }

    public String getId() {
        return id;
    }

    public String getDocumentoIdentidad() {
        return documentoIdentidad;
    }

    public void setDocumentoIdentidad(String documentoIdentidad) {
        this.documentoIdentidad = documentoIdentidad;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getApellidos() {
        return apellidos;
    }

    public void setApellidos(String apellidos) {
        this.apellidos = apellidos;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefonoContacto() {
        return telefonoContacto;
    }

    public void setTelefonoContacto(String telefonoContacto) {
        this.telefonoContacto = telefonoContacto;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }
}
