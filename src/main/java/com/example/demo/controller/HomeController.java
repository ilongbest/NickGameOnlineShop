package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class HomeController {
    @Autowired
    private UserRepository userRepository;

    @GetMapping("/home")
    public String showHomepage(Model model, Principal principal) {
        if (principal != null) {
            User user = userRepository.findByUsername(principal.getName())
                    .or(() -> userRepository.findFirstByEmailOrderByIdAsc(principal.getName()))
                    .orElse(null);
            if (user != null) {
                model.addAttribute("balance", user.getBalance());
                model.addAttribute("username", user.getUsername());
                model.addAttribute("avatarUrl", user.getAvatarUrl());
                model.addAttribute("currentUserId", user.getId());
            }
        }
        return "homepage";
    }
}
