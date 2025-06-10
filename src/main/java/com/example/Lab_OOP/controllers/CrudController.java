package com.example.Lab_OOP.controllers;

import com.example.Lab_OOP.models.Flower;
import com.example.Lab_OOP.repo.FlowerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Optional;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.GrantedAuthority;

@Controller
public class CrudController {

    @Autowired
    private FlowerRepository FlowerRepository;

    @GetMapping("/whoami")
    @ResponseBody
    public String whoAmI(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated() ||
                authentication.getPrincipal().equals("anonymousUser")) {
            return "Пользователь не аутентифицирован.";
        }

        String username = authentication.getName();
        StringBuilder roles = new StringBuilder();

        for (GrantedAuthority authority : authentication.getAuthorities()) {
            roles.append(authority.getAuthority()).append(" ");
        }

        return "Пользователь: " + username + "<br>Роли: " + roles;
    }

    @PreAuthorize("hasAnyRole('USER', 'MODERATOR', 'ADMIN')")
    @GetMapping("/crud")
    public String crud(Model model) {
        Iterable<Flower> flowers = FlowerRepository.findAll();
        flowers.forEach(f -> System.out.println(f.getName())); // простая проверка
        model.addAttribute("flowers", flowers);
        return "crud";
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @GetMapping("/crud/add")
    public String crudAdd(Model model) {
        return "crud-new";
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @PostMapping("/crud/add")
    public String crudFlowerAdd(@RequestParam String name,
                              @RequestParam Integer price,
                              @RequestParam Integer quantity,
                              Model model) {
        Flower flower = new Flower(name, price, quantity);
        FlowerRepository.save(flower);
        return "redirect:/crud";
    }

    @PreAuthorize("hasAnyRole('USER', 'MODERATOR', 'ADMIN')")
    @GetMapping("/crud/{id}")
    public String crudDetails(@PathVariable(value = "id") int id, Model model) {

        if (!FlowerRepository.existsById(id)) {
            return "redirect:/crud";
        }
        Optional<Flower> flower = FlowerRepository.findById(id);
        ArrayList<Flower> res = new ArrayList<>();
        flower.ifPresent(res::add);
        model.addAttribute("flower", res);
        return "crud-details";
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @GetMapping("/crud/{id}/edit")
    public String crudEdit(@PathVariable(value = "id") int id, Model model) {

        if (!FlowerRepository.existsById(id)) {
            return "redirect:/crud";
        }
        Optional<Flower> flower = FlowerRepository.findById(id);
        ArrayList<Flower> res = new ArrayList<>();
        flower.ifPresent(res::add);
        model.addAttribute("flower", res);
        return "crud-edit";
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @PostMapping("/crud/{id}/edit")
    public String crudFlowerEdit(@PathVariable Integer id,
                                @RequestParam String name,
                                @RequestParam Integer price,
                                @RequestParam Integer quantity,
                                Model model) {
        Flower flower = FlowerRepository.findById(id).orElseThrow();
        flower.setName(name);
        flower.setPrice(price);
        flower.setQuantity(quantity);
        FlowerRepository.save(flower);
        return "redirect:/crud";
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'MODERATOR')")
    @PostMapping("/crud/{id}/remove")
    public String crudFlowerDelete(@PathVariable Integer id, Model model) {
        Flower flower = FlowerRepository.findById(id).orElseThrow();
        FlowerRepository.delete(flower);
        return "redirect:/crud";
    }

}
