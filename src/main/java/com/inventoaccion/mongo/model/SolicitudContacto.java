package com.inventoaccion.mongo.model;

import java.time.LocalDateTime;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "solicitudes_contacto")
public class SolicitudContacto {
    @Id private String id;
    private String nombre;
    private String email;
    private String tipo;
    private String asunto;
    private String mensaje;
    private boolean autorizacion;
    private LocalDateTime fechaCreacion;
    private boolean correoConfirmacionEnviado;

    public String getId(){return id;} public void setId(String id){this.id=id;}
    public String getNombre(){return nombre;} public void setNombre(String nombre){this.nombre=nombre;}
    public String getEmail(){return email;} public void setEmail(String email){this.email=email;}
    public String getTipo(){return tipo;} public void setTipo(String tipo){this.tipo=tipo;}
    public String getAsunto(){return asunto;} public void setAsunto(String asunto){this.asunto=asunto;}
    public String getMensaje(){return mensaje;} public void setMensaje(String mensaje){this.mensaje=mensaje;}
    public boolean isAutorizacion(){return autorizacion;} public void setAutorizacion(boolean autorizacion){this.autorizacion=autorizacion;}
    public LocalDateTime getFechaCreacion(){return fechaCreacion;} public void setFechaCreacion(LocalDateTime fechaCreacion){this.fechaCreacion=fechaCreacion;}
    public boolean isCorreoConfirmacionEnviado(){return correoConfirmacionEnviado;} public void setCorreoConfirmacionEnviado(boolean valor){this.correoConfirmacionEnviado=valor;}
}
