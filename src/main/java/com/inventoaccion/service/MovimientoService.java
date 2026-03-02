package com.inventoaccion.service;

import com.inventoaccion.model.Movimiento;
import com.inventoaccion.model.Activo;
import com.inventoaccion.repository.MovimientoRepository;
import com.inventoaccion.repository.ActivoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class MovimientoService {
    private final MovimientoRepository movRepo;
    private final ActivoRepository activoRepo;

    public MovimientoService(MovimientoRepository movRepo, ActivoRepository activoRepo) {
        this.movRepo = movRepo;
        this.activoRepo = activoRepo;
    }

    @Transactional
    public Movimiento registrarMovimiento(Movimiento m) {
        if (m.getActivo() == null || m.getActivo().getId() == null) {
            throw new RuntimeException("Activo requerido");
        }
        Activo a = activoRepo.findById(m.getActivo().getId())
                .orElseThrow(() -> new RuntimeException("Activo no encontrado"));
        if ("SALIDA".equalsIgnoreCase(m.getTipo())) {
            if (a.getCantidadActual() == null) a.setCantidadActual(0);
            if (a.getCantidadActual() - m.getCantidad() < 0) {
                throw new RuntimeException("Stock insuficiente");
            }
            a.setCantidadActual(a.getCantidadActual() - m.getCantidad());
        } else if ("ENTRADA".equalsIgnoreCase(m.getTipo())) {
            if (a.getCantidadActual() == null) a.setCantidadActual(0);
            a.setCantidadActual(a.getCantidadActual() + m.getCantidad());
        }
        activoRepo.save(a);
        return movRepo.save(m);
    }

    public List<Movimiento> historialPorActivo(Long activoId) {
        return movRepo.findByActivoIdOrderByFechaDesc(activoId);
    }
}
