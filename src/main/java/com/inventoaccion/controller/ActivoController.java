package com.inventoaccion.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.inventoaccion.repository.ActivoRepository;
import com.inventoaccion.model.Activo;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import jakarta.validation.Valid;
import org.springframework.validation.BindingResult;

@Controller
@RequestMapping("/activos")
public class ActivoController {

    private final ActivoRepository activoRepo;

    public ActivoController(ActivoRepository activoRepo) {
        this.activoRepo = activoRepo;
    }

    @GetMapping
    public String listActivos(Model model, @ModelAttribute("msg") String msg) {
        model.addAttribute("activos", activoRepo.findAll());
        if (msg != null && !msg.isBlank()) {
            model.addAttribute("msg", msg);
        }
        return "activos";
    }

    @GetMapping("/nuevo")
    public String nuevoForm(Model model) {
        model.addAttribute("activo", new Activo());
        return "activo-form";
    }

    @GetMapping("/{id}/editar")
    public String editarForm(@PathVariable Long id, Model model, RedirectAttributes ra) {
        return activoRepo.findById(id)
                .map(a -> {
                    model.addAttribute("activo", a);
                    return "activo-form";
                })
                .orElseGet(() -> {
                    ra.addFlashAttribute("msg", "Activo no encontrado: " + id);
                    return "redirect:/activos";
                });
    }

    @PostMapping
    public String crearActivo(@Valid @ModelAttribute Activo activo,
                              BindingResult br,
                              RedirectAttributes ra) {
        if (br.hasErrors()) {
            return "activo-form";
        }
        activoRepo.save(activo);
        ra.addFlashAttribute("msg", "Activo guardado correctamente");
        return "redirect:/activos";
    }

    @PostMapping("/{id}/editar")
    public String actualizarActivo(@PathVariable Long id,
                                   @Valid @ModelAttribute Activo activo,
                                   BindingResult br,
                                   RedirectAttributes ra) {
        if (br.hasErrors()) {
            return "activo-form";
        }
        activo.setId(id);
        activoRepo.save(activo);
        ra.addFlashAttribute("msg", "Activo actualizado");
        return "redirect:/activos";
    }

    @PostMapping("/{id}/delete")
    public String borrarActivo(@PathVariable Long id, RedirectAttributes ra) {
        if (activoRepo.existsById(id)) {
            activoRepo.deleteById(id);
            ra.addFlashAttribute("msg", "Activo eliminado");
        } else {
            ra.addFlashAttribute("msg", "Activo no encontrado: " + id);
        }
        return "redirect:/activos";
    }
}
