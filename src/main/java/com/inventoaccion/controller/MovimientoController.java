package com.inventoaccion.controller;

import com.inventoaccion.model.Movimiento;
import com.inventoaccion.service.MovimientoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/movimientos")
public class MovimientoController {
    private final MovimientoService service;
    public MovimientoController(MovimientoService service) { this.service = service; }

    @PostMapping
    public ResponseEntity<?> registrar(@RequestBody Movimiento m) {
        try {
            Movimiento saved = service.registrarMovimiento(m);
            return ResponseEntity.ok(saved);
        } catch (RuntimeException ex) {
            return ResponseEntity.badRequest().body(ex.getMessage());
        }
    }

    @GetMapping("/activo/{activoId}")
    public List<Movimiento> historial(@PathVariable Long activoId) {
        return service.historialPorActivo(activoId);
    }
}
