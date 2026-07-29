package com.inventoaccion.mongo.repository;

import com.inventoaccion.mongo.model.UsuarioMongo;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface UsuarioMongoRepository extends MongoRepository<UsuarioMongo, String> {

    boolean existsByDocumentoIdentidad(String documentoIdentidad);

    boolean existsByEmail(String email);

    boolean existsByDocumentoIdentidadOrEmail(String documentoIdentidad, String email);
}
