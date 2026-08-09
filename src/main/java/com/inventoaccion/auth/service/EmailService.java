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
 * Importante:
 * El envio de correo solo se activa si las variables SMTP estan configuradas.
 * Si no estan configuradas, el sistema sigue funcionando en modo educativo.
 */
@Service
public class EmailService {

    /*
     * Proveedor opcional de JavaMailSender.
     *
     * Spring puede crear este objeto aunque las credenciales SMTP esten vacias.
     * Por eso no basta con validar si existe JavaMailSender.
     */
    private final ObjectProvider<JavaMailSender> mailSenderProvider;

    /*
     * URL base de la aplicacion.
     *
     * En local puede quedar vacia.
     * Si queda vacia, el metodo obtenerBaseUrl usa localhost internamente.
     */
    @Value("${app.base-url:}")
    private String appBaseUrl;

    /*
     * Host SMTP.
     *
     * Ejemplo en Render:
     * MAIL_HOST=smtp.gmail.com
     */
    @Value("${spring.mail.host:}")
    private String mailHost;

    /*
     * Usuario SMTP.
     *
     * Normalmente corresponde al correo remitente.
     */
    @Value("${spring.mail.username:}")
    private String mailUsername;

    /*
     * Password SMTP.
     *
     * Debe configurarse por variable de entorno.
     * No debe guardarse en GitHub.
     */
    @Value("${spring.mail.password:}")
    private String mailPassword;

    /*
     * Constructor del servicio.
     */
    public EmailService(ObjectProvider<JavaMailSender> mailSenderProvider) {
        this.mailSenderProvider = mailSenderProvider;
    }

    /*
     * Verifica si el correo esta realmente disponible.
     *
     * No valida solamente JavaMailSender, porque Spring puede crearlo
     * aunque MAIL_HOST, MAIL_USERNAME y MAIL_PASSWORD esten vacios.
     *
     * Retorna true solo si:
     * - existe JavaMailSender
     * - mailHost tiene valor
     * - mailUsername tiene valor
     * - mailPassword tiene valor
     */
    public boolean correoDisponible() {
        return mailSenderProvider.getIfAvailable() != null
                && tieneTexto(mailHost)
                && tieneTexto(mailUsername)
                && tieneTexto(mailPassword);
    }

    /*
     * Envia el correo con las credenciales temporales.
     */
    public void enviarCredenciales(
            String correoDestino,
            String nombre,
            String username,
            String passwordTemporal,
            String tokenValidacion
    ) {

        JavaMailSender mailSender = mailSenderProvider.getIfAvailable();

        if (!correoDisponible() || mailSender == null) {
            throw new IllegalStateException("El servicio de correo no esta configurado.");
        }

        String enlaceValidacion = obtenerBaseUrl() + "/api/auth/validar?token=" + tokenValidacion;

        String mensaje = construirMensaje(
                nombre,
                username,
                passwordTemporal,
                enlaceValidacion
        );

        SimpleMailMessage email = new SimpleMailMessage();
        email.setTo(correoDestino);
        email.setSubject("Credenciales de acceso - Invento-Accion");
        email.setText(mensaje);

        mailSender.send(email);
    }

    /*
     * Obtiene la URL base de la aplicacion.
     *
     * Si app.base-url esta vacia, usa localhost en puerto 8080.
     * Se arma por partes para evitar problemas de pegado con enlaces.
     */
    private String obtenerBaseUrl() {
        if (!tieneTexto(appBaseUrl)) {
            return "http" + "://localhost:8080";
        }

        return appBaseUrl.trim();
    }

    /*
     * Construye el mensaje que recibira el usuario.
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

    /*
     * Valida si un texto tiene contenido real.
     */
    private boolean tieneTexto(String valor) {
        return valor != null && !valor.trim().isEmpty();
    }
}