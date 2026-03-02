package com.inventoaccion.service;

import com.inventoaccion.model.Activo;
import com.inventoaccion.repository.ActivoRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ActivoService {
    private final ActivoRepository repo;

    public ActivoService(ActivoRepository repo) {
        this.repo = repo;
    }

    public Activo save(Activo a) { return repo.save(a); }
    public List<Activo> findAll() { return repo.findAll(); }
    public Optional<Activo> findById(Long id) { return repo.findById(id); }
    public List<Activo> searchByName(String q) { return repo.findByNombreContainingIgnoreCase(q); }
}
