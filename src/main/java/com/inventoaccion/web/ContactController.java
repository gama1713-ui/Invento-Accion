package com.inventoaccion.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ContactController {

    @PostMapping("/contacto/enviar")
    public String enviarContacto(@RequestParam String nombre,
                                 @RequestParam String email,
                                 @RequestParam String mensaje,
                                 RedirectAttributes ra) {
        // Aquí podrías persistir el mensaje en BD o enviar correo.
        String safe = "Gracias " + nombre + ", hemos recibido tu mensaje y responderemos pronto.";
        ra.addFlashAttribute("msg", safe);
        return "redirect:/contacto";
    }

    @PostMapping("/suscribir")
    public String suscribir(@RequestParam String email, RedirectAttributes ra) {
        // Aquí podrías persistir el email en una tabla de suscripciones.
        String safe = "Gracias, " + email + " ha sido suscrito a nuestras novedades.";
        ra.addFlashAttribute("subscribeMessage", safe);
        return "redirect:/";
    }
}
