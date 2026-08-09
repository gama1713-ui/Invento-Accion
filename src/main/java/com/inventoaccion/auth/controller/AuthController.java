package com.inventoaccion.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.inventoaccion.auth.dto.AuthResponse;
import com.inventoaccion.auth.dto.LoginAuthRequest;
import com.inventoaccion.auth.dto.RegistroAuthRequest;
import com.inventoaccion.auth.service.AuthService;

/*
 * Controlador REST para el modulo educativo de autenticacion.
 *
 * Este controlador expone las rutas de la API Auth de Invento-Accion.
 * Desde Postman, navegador o un frontend se podran consumir estos endpoints.
 *
 * Rutas disponibles:
 * GET  /api/auth/prueba
 * POST /api/auth/registro
 * GET  /api/auth/validar?token=...
 * POST /api/auth/login
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    /*
     * Servicio principal de autenticacion.
     *
     * En este servicio esta la logica para registrar usuarios,
     * validar correo e iniciar sesion.
     */
    private final AuthService authService;

    /*
     * Constructor del controlador.
     *
     * Spring Boot inyecta automaticamente AuthService.
     */
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /*
     * Endpoint de prueba de la API.
     *
     * Sirve para validar desde navegador o Postman
     * que el modulo de autenticacion esta activo.
     *
     * Metodo:
     * GET /api/auth/prueba
     */
    @GetMapping("/prueba")
    public ResponseEntity<String> prueba() {
        return ResponseEntity.ok("API Auth Invento-Accion funcionando correctamente");
    }

    /*
     * Endpoint para registrar un usuario.
     *
     * Recibe un JSON con nombre y correo.
     * Luego el servicio genera username, contrasena temporal,
     * token de validacion y guarda el usuario en MongoDB.
     *
     * Metodo:
     * POST /api/auth/registro
     */
    @PostMapping("/registro")
    public ResponseEntity<AuthResponse> registrar(@RequestBody RegistroAuthRequest request) {
        AuthResponse response = authService.registrar(request);

        if (!response.isExitoso()) {
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.ok(response);
    }

    /*
     * Endpoint para validar el correo del usuario.
     *
     * Recibe el token generado durante el registro.
     * Si el token es correcto, marca el correo como validado.
     *
     * Metodo:
     * GET /api/auth/validar?token=TOKEN
     */
    @GetMapping("/validar")
    public ResponseEntity<AuthResponse> validarCorreo(@RequestParam String token) {
        AuthResponse response = authService.validarCorreo(token);

        if (!response.isExitoso()) {
            return ResponseEntity.badRequest().body(response);
        }

        return ResponseEntity.ok(response);
    }

    /*
     * Endpoint para iniciar sesion.
     *
     * Recibe username y password.
     * Si los datos son correctos y el correo esta validado,
     * devuelve autenticacion satisfactoria.
     *
     * Metodo:
     * POST /api/auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginAuthRequest request) {
        AuthResponse response = authService.login(request);

        if (!response.isExitoso()) {
            return ResponseEntity.status(401).body(response);
        }

        return ResponseEntity.ok(response);
    }
}