package com.inventoaccion.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class WebController {

    @GetMapping("/inicio")
    public String index() {
        return "index";
    }

    @GetMapping("/nosotros")
    public String nosotros() {
        return "nosotros";
    }

    @GetMapping("/datos")
    public String datos() {
        return "datos";
    }

    @GetMapping("/terminos")
    public String terminos() {
        return "terminos";
    }

    @GetMapping("/privacidad")
    public String privacidad() {
        return "privacidad";
    }

    @GetMapping("/soporte")
    public String soporte() {
        return "soporte";
    }

    @GetMapping("/contacto")
    public String contacto() {
        return "contacto";
    }

    @GetMapping("/configuracion/sistema")
    public String configuracionSistema() {
        return "configuracion-sistema";
    }

    @GetMapping("/configuracion/seguridad")
    public String configuracionSeguridad() {
        return "configuracion-seguridad";
    }

    @GetMapping("/configuracion/notificaciones")
    public String configuracionNotificaciones() {
        return "configuracion-notificaciones";
    }

    @GetMapping("/configuracion/integraciones")
    public String configuracionIntegraciones() {
        return "configuracion-integraciones";
    }

    @GetMapping("/administracion/logs")
    public String administracionLogs() {
        return "administracion-logs";
    }

    @GetMapping("/administracion/backup")
    public String administracionBackup() {
        return "administracion-backup";
    }

    @GetMapping("/administracion/mantenimiento")
    public String administracionMantenimiento() {
        return "administracion-mantenimiento";
    }

    @GetMapping("/administracion/auditoria")
    public String administracionAuditoria() {
        return "administracion-auditoria";
    }

    @GetMapping("/gestion/categorias")
    public String gestionCategorias() {
        return "gestion-categorias";
    }

    @GetMapping("/gestion/proveedores")
    public String gestionProveedores() {
        return "gestion-proveedores";
    }

    @GetMapping("/gestion/ubicaciones")
    public String gestionUbicaciones() {
        return "gestion-ubicaciones";
    }
}
