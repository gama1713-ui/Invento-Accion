package com.inventoaccion.mongo.service;

import com.inventoaccion.mongo.model.SolicitudContacto;
import com.inventoaccion.mongo.repository.SolicitudContactoRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class SolicitudContactoService {
    private final SolicitudContactoRepository repository;
    public SolicitudContactoService(SolicitudContactoRepository repository){this.repository=repository;}

    public SolicitudContacto registrar(String nombre,String email,String tipo,String asunto,String mensaje,boolean autorizacion){
        SolicitudContacto solicitud=new SolicitudContacto();
        solicitud.setNombre(nombre.trim()); solicitud.setEmail(email.trim().toLowerCase());
        solicitud.setTipo(tipo.trim()); solicitud.setAsunto(asunto.trim()); solicitud.setMensaje(mensaje.trim());
        solicitud.setAutorizacion(autorizacion); solicitud.setFechaCreacion(LocalDateTime.now());
        solicitud.setCorreoConfirmacionEnviado(false);
        return repository.save(solicitud);
    }
}
