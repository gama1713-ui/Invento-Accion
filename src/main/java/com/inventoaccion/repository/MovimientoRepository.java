package com.inventoaccion.repository;

import com.inventoaccion.model.Movimiento;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface MovimientoRepository extends JpaRepository<Movimiento, Long> {
    List<Movimiento> findByActivoIdOrderByFechaDesc(Long activoId);
}
