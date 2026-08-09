package com.inventoaccion.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/*
 * Controlador REST para el modulo educativo de autenticacion.
 * 
 * Este archivo pertenece al nuevo modulo auth del proyecto Invento-Accion.
 * Por ahora solo contiene una ruta de prueba para confirmar que Spring Boot
 * reconoce correctamente este nuevo componente sin afectar los modulos existentes.
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    /*
     * Endpoint de prueba de la API.
     * 
     * Sirve para validar desde el navegador o desde Postman que el modulo
     * de autenticacion esta cargando correctamente.
     * 
     * Ruta:
     * GET /api/auth/prueba
     */
    @GetMapping("/prueba")
    public ResponseEntity<String> prueba() {
        return ResponseEntity.ok("API Auth Invento-Accion funcionando correctamente");
    }
}