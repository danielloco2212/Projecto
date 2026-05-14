package com.example.pagebuilder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.data.domain.Sort;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class ControladorPagina {

    @Autowired
    private ClubRepositorio clubRepositorio;
    @Autowired
    private EquipoRepositorio equipoRepositorio;
    @Autowired
    private NoticiaRepositorio noticiaRepositorio;
    @Autowired
    private UsuarioRepositorio usuarioRepositorio;

    @GetMapping("/")
    public String index(Model modelo) {
        ClubInformacion club = obtenerClubPrincipal();
        modelo.addAttribute("club", club);
        
        // Obtenemos todo ordenado por ID descendente 
        var equipos = equipoRepositorio.findAll(Sort.by(Sort.Direction.DESC, "id"));
        var noticias = noticiaRepositorio.findAll(Sort.by(Sort.Direction.DESC, "id"));

        modelo.addAttribute("equipos", equipos);
        modelo.addAttribute("noticias", noticias);
        modelo.addAttribute("teams", equipos); 
        modelo.addAttribute("posts", noticias); 
        return "index";
    }

    @GetMapping("/team/{id}")
    public String detalleEquipo(@PathVariable Long id, Model modelo) {
        Equipo equipo = equipoRepositorio.findById(id).orElseThrow(() -> new RuntimeException("Equipo no encontrado"));
        var equipos = equipoRepositorio.findAll(Sort.by(Sort.Direction.DESC, "id"));
        modelo.addAttribute("team", equipo);
        modelo.addAttribute("club", obtenerClubPrincipal());
        modelo.addAttribute("teams", equipos);
        modelo.addAttribute("equipos", equipos);
        return "team";
    }

    private ClubInformacion obtenerClubPrincipal() {
        return clubRepositorio.findAll().stream()
                .reduce((primero, segundo) -> segundo) // Esto selecciona el último elemento de la lista
                .orElse(new ClubInformacion());
    }
}