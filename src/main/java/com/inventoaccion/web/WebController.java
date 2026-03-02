package com.inventoaccion.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping("/")
    public String index() {
        return "index"; // plantilla principal
    }

    @GetMapping("/nosotros")
    public String nosotros() {
        return "nosotros"; // plantilla nosotros.html
    }

    @GetMapping("/datos")
    public String datos() {
        return "datos"; // plantilla datos.html
    }

    @GetMapping("/terminos")
    public String terminos() {
        return "terminos"; // plantilla terminos.html
    }

    @GetMapping("/privacidad")
    public String privacidad() {
        return "privacidad"; // plantilla privacidad.html
    }

    @GetMapping("/soporte")
    public String soporte() {
        return "soporte"; // plantilla soporte.html
    }

    @GetMapping("/contacto")
    public String contacto() {
        return "contacto"; // plantilla contacto.html
    }
}
