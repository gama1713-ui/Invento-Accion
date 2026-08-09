package com.inventoaccion.auth.repository;

import com.inventoaccion.auth.model.AuthUsuario;
import java.util.Optional;
import org.springframework.data.mongodb.repository.MongoRepository;

/*
 * Repositorio MongoDB para el modulo educativo de autenticacion.
 *
 * Esta interfaz permite consultar y guardar usuarios de autenticacion
 * en la coleccion auth_usuarios de MongoDB.
 *
 * Spring Data MongoDB genera automaticamente las consultas
 * a partir del nombre de cada metodo.
 */
public interface AuthUsuarioRepository extends MongoRepository<AuthUsuario, String> {

    /*
     * Busca un usuario por correo electronico.
     *
     * Se usara para validar si un correo ya fue registrado.
     */
    Optional<AuthUsuario> findByCorreo(String correo);

    /*
     * Busca un usuario por nombre de usuario.
     *
     * Se usara durante el inicio de sesion.
     */
    Optional<AuthUsuario> findByUsername(String username);

    /*
     * Busca un usuario por token de validacion.
     *
     * Se usara cuando el usuario abra el enlace recibido por correo.
     */
    Optional<AuthUsuario> findByTokenValidacion(String tokenValidacion);

    /*
     * Verifica si ya existe un usuario con ese correo.
     *
     * Evita registros duplicados.
     */
    boolean existsByCorreo(String correo);

    /*
     * Verifica si ya existe un usuario con ese username.
     *
     * Ayuda a generar nombres de usuario unicos.
     */
    boolean existsByUsername(String username);
}