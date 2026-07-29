package com.inventoaccion.mongo.service;

import com.inventoaccion.mongo.model.UsuarioMongo;
import com.inventoaccion.mongo.repository.UsuarioMongoRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UsuarioMongoService {

    private final UsuarioMongoRepository usuarioMongoRepository;

    public UsuarioMongoService(UsuarioMongoRepository usuarioMongoRepository) {
        this.usuarioMongoRepository = usuarioMongoRepository;
    }

    public List<UsuarioMongo> listarUsuarios() {
        return usuarioMongoRepository.findAll();
    }

    public boolean existeUsuario(String documentoIdentidad, String email) {
        return usuarioMongoRepository.existsByDocumentoIdentidadOrEmail(documentoIdentidad, email);
    }

    public UsuarioMongo registrarUsuario(String documentoIdentidad, String nombre, String apellidos, String email, String telefonoContacto) {
        UsuarioMongo usuario = new UsuarioMongo();
        usuario.setDocumentoIdentidad(documentoIdentidad);
        usuario.setNombre(nombre);
        usuario.setApellidos(apellidos);
        usuario.setEmail(email);
        usuario.setTelefonoContacto(telefonoContacto);
        usuario.setRol("USUARIO");

        return usuarioMongoRepository.save(usuario);
    }
}
