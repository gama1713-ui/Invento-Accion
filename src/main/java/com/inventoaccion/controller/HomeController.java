package com.inventoaccion.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import com.inventoaccion.repository.ActivoRepository;
import com.inventoaccion.repository.MovimientoRepository;
import com.inventoaccion.repository.UsuarioRepository;
import com.inventoaccion.model.Activo;
import com.inventoaccion.model.Movimiento;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class HomeController {

    private final ActivoRepository activoRepo;
    private final MovimientoRepository movimientoRepo;
    private final UsuarioRepository usuarioRepo;
    private static final int PAGE_SIZE = 5;

    public HomeController(ActivoRepository activoRepo, 
                         MovimientoRepository movimientoRepo,
                         UsuarioRepository usuarioRepo) {
        this.activoRepo = activoRepo;
        this.movimientoRepo = movimientoRepo;
        this.usuarioRepo = usuarioRepo;
    }

    @GetMapping("/")
    public String home(Model model, 
                      @RequestParam(defaultValue = "0") int page) {
        try {
            // Total de registros
            long totalActivos = activoRepo.findAll().size();
            long totalMovimientos = movimientoRepo.findAll().size();
            long usuariosActivos = usuarioRepo.findAll().size();
            
            model.addAttribute("totalActivos", totalActivos);
            model.addAttribute("totalMovimientos", totalMovimientos);
            model.addAttribute("usuariosActivos", usuariosActivos);
            
            // Últimos movimientos (paginado)
            var ultimosMovimientos = movimientoRepo.findAll()
                    .stream()
                    .sorted((m1, m2) -> {
                        if (m1.getId() == null || m2.getId() == null) return 0;
                        return m2.getId().compareTo(m1.getId());
                    })
                    .limit(PAGE_SIZE)
                    .collect(Collectors.toList());
            model.addAttribute("ultimosMovimientos", ultimosMovimientos);
            
            // Activos críticos (sin especificar estado, mostrar todos)
            // Puedes filtrar por estado cuando agregues esa propiedad
            var activosCriticos = activoRepo.findAll()
                    .stream()
                    .limit(5)
                    .collect(Collectors.toList());
            model.addAttribute("activosCriticos", activosCriticos);
            
            // Info de paginación
            int totalPages = (int) Math.ceil((double) totalMovimientos / PAGE_SIZE);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", totalPages);
            model.addAttribute("hasPrevious", page > 0);
            model.addAttribute("hasNext", page < totalPages - 1);
            
        } catch (Exception e) {
            model.addAttribute("error", "Error al cargar datos: " + e.getMessage());
            model.addAttribute("totalActivos", 0);
            model.addAttribute("totalMovimientos", 0);
            model.addAttribute("usuariosActivos", 0);
            model.addAttribute("ultimosMovimientos", List.of());
            model.addAttribute("activosCriticos", List.of());
        }
        
        return "index";
    }
}
