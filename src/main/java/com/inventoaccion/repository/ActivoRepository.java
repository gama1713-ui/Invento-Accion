package com.inventoaccion.repository;

import com.inventoaccion.model.Activo;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ActivoRepository extends JpaRepository<Activo, Long> {
    List<Activo> findByNombreContainingIgnoreCase(String nombre);
}
