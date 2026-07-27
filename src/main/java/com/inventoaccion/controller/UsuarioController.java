package com.inventoaccion.controller;

import com.inventoaccion.model.Usuario;
import com.inventoaccion.repository.UsuarioRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UsuarioController {

    private final UsuarioRepository usuarioRepository;

    public UsuarioController(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    @GetMapping("/gestion/usuarios")
    public String gestionUsuarios(Model model) {
        model.addAttribute("usuarios", usuarioRepository.findAll());
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

        if (usuarioRepository.existsByDocumentoIdentidadOrEmail(documentoIdentidad, email)) {
            redirectAttributes.addFlashAttribute("error", "El documento de identidad o el correo electrónico ya se encuentra registrado.");
            return "redirect:/gestion/usuarios";
        }

        Usuario usuario = new Usuario();
        usuario.setDocumentoIdentidad(documentoIdentidad);
        usuario.setNombre(nombre);
        usuario.setApellidos(apellidos);
        usuario.setEmail(email);
        usuario.setTelefonoContacto(telefonoContacto);
        usuario.setRol("USUARIO");

        usuarioRepository.save(usuario);

        redirectAttributes.addFlashAttribute("success", "Usuario registrado correctamente.");
        return "redirect:/gestion/usuarios";
    }
}
