package com.inventoaccion.auth.service;

import java.security.SecureRandom;
import java.util.UUID;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.inventoaccion.auth.dto.AuthResponse;
import com.inventoaccion.auth.dto.LoginAuthRequest;
import com.inventoaccion.auth.dto.RegistroAuthRequest;
import com.inventoaccion.auth.model.AuthUsuario;
import com.inventoaccion.auth.repository.AuthUsuarioRepository;

/*
 * Servicio principal del modulo educativo de autenticacion.
 *
 * Esta clase contiene la logica para:
 * registrar usuarios,
 * generar username,
 * generar contrasena temporal,
 * cifrar la contrasena con BCrypt,
 * guardar el usuario en MongoDB,
 * validar el correo por token,
 * e iniciar sesion.
 *
 * Por ahora esta en modo educativo/local.
 * Mas adelante se conectara con un servicio de correo real.
 */
@Service
public class AuthService {

    /*
     * Repositorio usado para guardar y consultar usuarios
     * en la coleccion auth_usuarios de MongoDB.
     */
    private final AuthUsuarioRepository authUsuarioRepository;

    /*
     * Objeto usado para cifrar y validar contrasenas.
     * BCrypt permite guardar contrasenas de forma segura.
     */
    private final BCryptPasswordEncoder passwordEncoder;

    /*
     * Constructor del servicio.
     *
     * Spring Boot inyecta automaticamente el repositorio.
     */
    public AuthService(AuthUsuarioRepository authUsuarioRepository) {
        this.authUsuarioRepository = authUsuarioRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }

    /*
     * Registra un usuario nuevo para autenticacion.
     *
     * Recibe nombre y correo.
     * Genera username automaticamente.
     * Genera una contrasena temporal.
     * Cifra la contrasena.
     * Genera un token para validar correo.
     * Guarda todo en MongoDB.
     */
    public AuthResponse registrar(RegistroAuthRequest request) {

        /*
         * Se limpian los datos recibidos para evitar espacios innecesarios.
         */
        String nombreLimpio = request.getNombre() == null ? "" : request.getNombre().trim();
        String correoLimpio = request.getCorreo() == null ? "" : request.getCorreo().trim().toLowerCase();

        /*
         * Validacion basica para evitar registros vacios.
         */
        if (nombreLimpio.isEmpty() || correoLimpio.isEmpty()) {
            return new AuthResponse(false, "El nombre y el correo son obligatorios.", null);
        }

        /*
         * Se valida que el correo no exista previamente.
         */
        if (authUsuarioRepository.existsByCorreo(correoLimpio)) {
            return new AuthResponse(false, "El correo ya se encuentra registrado.", null);
        }

        /*
         * Se genera un username a partir de la parte inicial del correo.
         */
        String usernameBase = correoLimpio.split("@")[0];
        String username = generarUsernameUnico(usernameBase);

        /*
         * Se genera una contrasena temporal para el primer acceso.
         */
        String passwordTemporal = generarPasswordTemporal();

        /*
         * Se cifra la contrasena antes de guardarla en MongoDB.
         */
        String passwordHash = passwordEncoder.encode(passwordTemporal);

        /*
         * Se genera un token unico para validar el correo.
         */
        String tokenValidacion = UUID.randomUUID().toString();

        /*
         * Se crea el objeto que se guardara en MongoDB.
         */
        AuthUsuario usuario = new AuthUsuario(
                nombreLimpio,
                correoLimpio,
                username,
                passwordHash,
                tokenValidacion
        );

        /*
         * Se guarda el usuario en la coleccion auth_usuarios.
         */
        authUsuarioRepository.save(usuario);

        /*
         * Modo educativo:
         * Por ahora se devuelve username, contrasena temporal y token en el mensaje
         * para poder probar con Postman.
         *
         * En la version con correo real, estos datos se enviaran por email
         * y no se mostraran en la respuesta.
         */
        String mensaje = "Usuario creado en modo educativo. "
                + "Username: " + username
                + " | Password temporal: " + passwordTemporal
                + " | Token validacion: " + tokenValidacion;

        return new AuthResponse(true, mensaje, null);
    }

    /*
     * Valida el correo electronico usando el token generado en el registro.
     *
     * Si el token existe, marca el correo como validado.
     */
    public AuthResponse validarCorreo(String token) {

        if (token == null || token.trim().isEmpty()) {
            return new AuthResponse(false, "El token de validacion es obligatorio.", null);
        }

        return authUsuarioRepository.findByTokenValidacion(token.trim())
                .map(usuario -> {

                    /*
                     * Se marca el correo como validado.
                     */
                    usuario.setCorreoValidado(true);

                    /*
                     * Se elimina el token para que no pueda reutilizarse.
                     */
                    usuario.setTokenValidacion(null);

                    /*
                     * Se guarda el cambio en MongoDB.
                     */
                    authUsuarioRepository.save(usuario);

                    return new AuthResponse(
                            true,
                            "Correo validado correctamente. Ya puedes iniciar sesion.",
                            null
                    );
                })
                .orElseGet(() -> new AuthResponse(
                        false,
                        "Token de validacion no valido o ya utilizado.",
                        null
                ));
    }

    /*
     * Valida el inicio de sesion del usuario.
     *
     * Primero busca el username.
     * Luego valida si el correo ya fue confirmado.
     * Despues compara la contrasena enviada contra la contrasena cifrada.
     */
    public AuthResponse login(LoginAuthRequest request) {

        String usernameLimpio = request.getUsername() == null ? "" : request.getUsername().trim().toLowerCase();
        String passwordRecibido = request.getPassword() == null ? "" : request.getPassword().trim();

        if (usernameLimpio.isEmpty() || passwordRecibido.isEmpty()) {
            return new AuthResponse(false, "El usuario y la contrasena son obligatorios.", null);
        }

        return authUsuarioRepository.findByUsername(usernameLimpio)
                .map(usuario -> {

                    /*
                     * No se permite iniciar sesion si el correo no ha sido validado.
                     */
                    if (!usuario.isCorreoValidado()) {
                        return new AuthResponse(
                                false,
                                "El correo aun no ha sido validado.",
                                null
                        );
                    }

                    /*
                     * Se compara la contrasena enviada con la contrasena cifrada.
                     */
                    boolean passwordCorrecto = passwordEncoder.matches(
                            passwordRecibido,
                            usuario.getPasswordHash()
                    );

                    if (!passwordCorrecto) {
                        return new AuthResponse(
                                false,
                                "Error de autenticacion. Usuario o contrasena incorrectos.",
                                null
                        );
                    }

                    /*
                     * En esta fase todavia no generamos JWT.
                     * Por ahora devolvemos un token educativo temporal.
                     * Luego reemplazaremos esto por un JWT real.
                     */
                    String tokenEducativo = "AUTH_OK_" + UUID.randomUUID();

                    return new AuthResponse(
                            true,
                            "Autenticacion satisfactoria.",
                            tokenEducativo
                    );
                })
                .orElseGet(() -> new AuthResponse(
                        false,
                        "Error de autenticacion. Usuario o contrasena incorrectos.",
                        null
                ));
    }

    /*
     * Genera un username unico.
     *
     * Si ya existe, agrega un numero al final.
     * Ejemplo:
     * jonathan
     * jonathan1
     * jonathan2
     */
    private String generarUsernameUnico(String usernameBase) {

        String username = usernameBase.toLowerCase();
        int consecutivo = 1;

        while (authUsuarioRepository.existsByUsername(username)) {
            username = usernameBase.toLowerCase() + consecutivo;
            consecutivo++;
        }

        return username;
    }

    /*
     * Genera una contrasena temporal de 10 caracteres.
     *
     * Usa letras mayusculas, minusculas y numeros.
     */
    private String generarPasswordTemporal() {

        String caracteres = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder password = new StringBuilder();

        for (int i = 0; i < 10; i++) {
            int posicion = random.nextInt(caracteres.length());
            password.append(caracteres.charAt(posicion));
        }

        return password.toString();
    }
}