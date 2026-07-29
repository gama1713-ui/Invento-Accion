package com.inventoaccion.controller;

import com.inventoaccion.mongo.service.UsuarioMongoService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UsuarioController {

    private final UsuarioMongoService usuarioMongoService;

    public UsuarioController(UsuarioMongoService usuarioMongoService) {
        this.usuarioMongoService = usuarioMongoService;
    }

    @GetMapping("/gestion/usuarios")
    public String gestionUsuarios(Model model) {
        model.addAttribute("usuarios", usuarioMongoService.listarUsuarios());
        return "gestion-usuarios";
    }

    @PostMapping("/gestion/usuarios/registrar")
    public String registrarUsuario(
            @RequestParam String documentoIdentidad,
            @RequestParam String nombre,
            @RequestParam String apellidos,
            @RequestParam String email,
            @RequestParam String telefonoContacto,
            RedirectAttributes redirectAttributes) {

        String documentoLimpio = documentoIdentidad.trim();
        String nombreLimpio = nombre.trim();
        String apellidosLimpio = apellidos.trim();
        String emailLimpio = email.trim().toLowerCase();
        String telefonoLimpio = telefonoContacto.trim();

        if (usuarioMongoService.existeUsuario(documentoLimpio, emailLimpio)) {
            redirectAttributes.addFlashAttribute("error", "El documento de identidad o el correo electrónico ya se encuentra registrado.");
            return "redirect:/gestion/usuarios";
        }

        usuarioMongoService.registrarUsuario(
                documentoLimpio,
                nombreLimpio,
                apellidosLimpio,
                emailLimpio,
                telefonoLimpio
        );

        redirectAttributes.addFlashAttribute("success", "Usuario registrado correctamente en MongoDB Atlas.");
        return "redirect:/gestion/usuarios";
    }
}
