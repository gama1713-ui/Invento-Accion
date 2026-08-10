package com.inventoaccion.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/*
 * Controlador web para el modulo visual de autenticacion.
 *
 * Esta clase no procesa directamente el registro ni el login.
 * Su funcion es mostrar una pagina web sencilla donde el usuario
 * pueda probar graficamente la API Auth de Invento-Accion.
 *
 * La pagina consumira los servicios REST reales:
 *
 * POST /api/auth/registro
 * GET  /api/auth/validar?token=...
 * POST /api/auth/login
 *
 * De esta forma el usuario puede ver en pantalla lo que antes
 * probamos solamente con cURL o Postman.
 */
@Controller
public class AuthWebController {

    /*
     * Muestra la pagina visual del modulo de autenticacion.
     *
     * Ruta local:
     * http://localhost:8080/api-auth-web
     *
     * Ruta en produccion:
     * https://invento-accion.onrender.com/api-auth-web
     */
    @GetMapping("/api-auth-web")
    public String mostrarModuloAuthWeb() {
        return "api-auth-web";
    }
}
