package com.inventoaccion.mongo.repository;

import com.inventoaccion.mongo.model.SolicitudContacto;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface SolicitudContactoRepository extends MongoRepository<SolicitudContacto, String> {
}
