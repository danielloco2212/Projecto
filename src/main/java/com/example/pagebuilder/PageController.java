package com.example.pagebuilder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class PageController {

    @Autowired
    private ClubRepository clubRepository;
    @Autowired
    private TeamRepository teamRepository;
    @Autowired
    private PostRepository postRepository;

    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("club", clubRepository.findAll().stream().findFirst().orElse(new ClubInfo()));
        model.addAttribute("teams", teamRepository.findAll());
        model.addAttribute("posts", postRepository.findAll());
        return "index";
    }

    @GetMapping("/team/{id}")
    public String teamDetail(@PathVariable Long id, Model model) {
        Team team = teamRepository.findById(id).orElseThrow(() -> new RuntimeException("Equipo no encontrado"));
        model.addAttribute("team", team);
        model.addAttribute("club", clubRepository.findAll().stream().findFirst().orElse(new ClubInfo()));
        model.addAttribute("teams", teamRepository.findAll());
        return "team";
    }
}