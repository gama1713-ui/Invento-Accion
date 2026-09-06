package com.inventoaccion.web;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.view.RedirectView;

/*
 * Controlador web para el módulo Transporte SITP.
 *
 * Su función es crear una ruta dentro de la categoría Gestión
 * de Invento-Acción.
 *
 * La ruta creada será:
 *
 * /gestion/transporte-sitp
 *
 * Cuando el usuario seleccione Transporte SITP desde el menú,
 * este controlador lo dirigirá hacia la página PHP encargada
 * de consultar los paraderos.
 *
 * El controlador no modifica usuarios, ubicaciones, activos,
 * préstamos, MongoDB Atlas ni la API de autenticación.
 */
@Controller
public class TransporteSitpController {

    /*
     * Dirección donde se encuentra ejecutándose el módulo PHP.
     *
     * En el entorno local se usa:
     *
     * http://localhost:8081/paraderos_sitp.php
     *
     * Para producción se podrá configurar una dirección diferente
     * mediante la propiedad sitp.module-url.
     *
     * De esta manera no será necesario cambiar nuevamente el código
     * del controlador cuando el módulo PHP sea publicado.
     */
    @Value("${sitp.module-url:http://localhost:8081/paraderos_sitp.php}")
    private String moduloSitpUrl;

    /*
     * Muestra el módulo Transporte SITP desde la sección Gestión.
     *
     * La opción del menú principal utilizará esta ruta:
     *
     * /gestion/transporte-sitp
     *
     * RedirectView realiza la redirección hacia la dirección
     * configurada en sitp.module-url.
     */
    @GetMapping("/gestion/transporte-sitp")
    public RedirectView mostrarTransporteSitp() {

        /*
         * Se crea la redirección hacia el módulo PHP.
         */
        RedirectView redireccion = new RedirectView();

        /*
         * Se asigna la dirección local o de producción
         * configurada para el módulo Transporte SITP.
         */
        redireccion.setUrl(moduloSitpUrl);

        /*
         * false indica que la dirección del módulo puede ser externa
         * a la aplicación principal de Spring Boot.
         */
        redireccion.setContextRelative(false);

        /*
         * Se devuelve la redirección al navegador.
         */
        return redireccion;
    }
}