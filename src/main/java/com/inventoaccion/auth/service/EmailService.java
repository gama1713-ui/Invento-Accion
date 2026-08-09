package com.inventoaccion.auth.service;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/*
 * Servicio de correo para el modulo educativo de autenticacion.
 *
 * Esta clase se encarga de enviar al usuario:
 * username generado,
 * contrasena temporal,
 * enlace de validacion de correo.
 *
 * Se usa ObjectProvider para evitar que la aplicacion falle
 * si todavia no estan configuradas las variables SMTP.
 */
@Service
public class EmailService {

    /*
     * Proveedor opcional de JavaMailSender.
     *
     * Si el servidor SMTP esta configurado, Spring Boot entrega
     * un JavaMailSender disponible.
     *
     * Si el servidor SMTP no esta configurado, la aplicacion puede
     * seguir levantando sin romper el arranque.
     */
    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    /*
     * URL base de la aplicacion.
     *
     * Si no se configura app.base-url, el servicio usara una URL local
     * construida en el metodo obtenerBaseUrl().
     */
    @Value("${app.base-url:}")
    private String appBaseUrl;

    /*
     * Constructor del servicio.
     *
     * Spring Boot inyecta el proveedor de JavaMailSender si esta disponible.
     */
    public EmailService(ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.mailSenderProvider = mailSenderProvider;
    }

    /*
     * Verifica si el servicio de correo esta disponible.
     *
     * Retorna true si existe configuracion SMTP.
     * Retorna false si todavia no se ha configurado correo.
     */
    public boolean correoDisponible() {
        return mailSenderProvider.getIfAvailable() != null;
    }

    /*
     * Envia el correo con las credenciales temporales.
     *
     * Este metodo se usara desde AuthService cuando el usuario
     * se registre correctamente.
     */
    public void enviarCredenciales(
            String correoDestino,
            String nombre,
            String username,
            String passwordTemporal,
            String tokenValidacion
    ) {

        /*
         * Se obtiene el servicio de envio de correo.
         * Si no existe, se lanza un error controlado.
         */
        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();

        if (mailSender == null) {
            throw new IllegalStateException("El servicio de correo no esta configurado.");
        }

        /*
         * Se construye el enlace de validacion.
         */
        String enlaceValidacion = obtenerBaseUrl() + "/api/auth/validar?token=" + tokenValidacion;

        /*
         * Se construye el cuerpo del mensaje.
         */
        String mensaje = construirMensaje(
                nombre,
                username,
                passwordTemporal,
                enlaceValidacion
        );

        /*
         * Se crea el mensaje simple de correo.
         */
        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(correoDestino);
        email.setSubject("Credenciales de acceso - Invento-Accion");
        email.setText(mensaje);

        /*
         * Se envia el correo.
         */
        mailSender.send(email);
    }

    /*
     * Obtiene la URL base de la aplicacion.
     *
     * Si app.base-url esta vacio, se usa localhost en el puerto 8080.
     * Se arma por partes para evitar que el chat convierta la URL en enlace.
     */
    private String obtenerBaseUrl() {
        if (appBaseUrl == null || appBaseUrl.trim().isEmpty()) {
            return "http" + "://localhost:8080";
        }

        return appBaseUrl.trim();
    }

    /*
     * Construye el texto del correo.
     *
     * Se separa en un metodo privado para mantener el codigo ordenado
     * y facil de entender.
     */
    private String construirMensaje(
            String nombre,
            String username,
            String passwordTemporal,
            String enlaceValidacion
    ) {

        StringBuilder mensaje = new StringBuilder();

        mensaje.append("Hola ").append(nombre).append(",\n\n");
        mensaje.append("Tu usuario para Invento-Accion fue creado correctamente.\n\n");
        mensaje.append("Datos de acceso:\n\n");
        mensaje.append("Usuario: ").append(username).append("\n");
        mensaje.append("Contrasena temporal: ").append(passwordTemporal).append("\n\n");
        mensaje.append("Para activar tu cuenta, abre el siguiente enlace:\n\n");
        mensaje.append(enlaceValidacion).append("\n\n");
        mensaje.append("Importante:\n");
        mensaje.append("Si no solicitaste este registro, puedes ignorar este mensaje.\n\n");
        mensaje.append("Atentamente,\n");
        mensaje.append("Equipo Invento-Accion");

        return mensaje.toString();
    }
}