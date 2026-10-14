package com.inventoaccion.web;

import com.inventoaccion.mongo.model.SolicitudContacto;
import com.inventoaccion.mongo.service.SolicitudContactoService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class ContactController {
    private final SolicitudContactoService solicitudContactoService;
    public ContactController(SolicitudContactoService solicitudContactoService){this.solicitudContactoService=solicitudContactoService;}

    @PostMapping("/contacto/enviar")
    public String enviarContacto(@RequestParam String nombre,@RequestParam String email,@RequestParam String tipo,
        @RequestParam String asunto,@RequestParam String mensaje,
        @RequestParam(name="autorizacion",defaultValue="false") boolean autorizacion,RedirectAttributes ra){
        if(!autorizacion){ra.addFlashAttribute("error","Debes autorizar el uso de los datos para gestionar la solicitud.");return "redirect:/contacto";}
        if(!valido(nombre,2,100)||!valido(email,5,150)||!email.contains("@")||!valido(tipo,3,50)||!valido(asunto,3,150)||!valido(mensaje,10,2000)){
            ra.addFlashAttribute("error","Revisa los campos del formulario y vuelve a intentarlo.");return "redirect:/contacto";
        }
        try{
            SolicitudContacto solicitud=solicitudContactoService.registrar(nombre,email,tipo,asunto,mensaje,true);
            ra.addFlashAttribute("msg","Solicitud registrada correctamente. Identificador: "+solicitud.getId());
        }catch(Exception ex){
            ra.addFlashAttribute("error","No fue posible registrar la solicitud. No se confirmó su recepción.");
        }
        return "redirect:/contacto";
    }

    @PostMapping("/suscribir")
    public String suscribir(@RequestParam String email,RedirectAttributes ra){
        ra.addFlashAttribute("subscribeMessage","La suscripción a novedades todavía no está habilitada.");
        return "redirect:/";
    }

    private boolean valido(String valor,int minimo,int maximo){
        if(valor==null)return false; int longitud=valor.trim().length(); return longitud>=minimo&&longitud<=maximo;
    }
}
