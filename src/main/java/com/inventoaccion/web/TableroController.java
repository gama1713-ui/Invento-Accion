package com.inventoaccion.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/tablero")
public class TableroController {

    @GetMapping("/resumen")
    public String resumen() {
        return "tablero-resumen"; // plantilla tablero-resumen.html
    }

    @GetMapping("/alertas")
    public String alertas() {
        return "tablero-alertas"; // plantilla tablero-alertas.html
    }

    @GetMapping("/reportes")
    public String reportes() {
        return "tablero-reportes"; // plantilla tablero-reportes.html
    }
}
