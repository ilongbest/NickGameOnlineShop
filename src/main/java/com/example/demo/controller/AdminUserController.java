package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {
    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public String showUserManagement(Model model) {
        List<User> users = userRepository.findAll();
        model.addAttribute("users", users);
        return "user-management";
    }

    @GetMapping("/toggle/{id}")
    public String toggleUserStatus(@PathVariable Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Boolean currentEnabled = user.getEnabled();
        if (currentEnabled == null) {
            currentEnabled = true;
        }
        user.setEnabled(!currentEnabled);
        userRepository.save(user);
        return "redirect:/admin/users?toggleSuccess";
    }

    @GetMapping("/detail/{id}")
    public String showUserDetail(@PathVariable Long id, Model model) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        model.addAttribute("user", user);
        return "user-detail-management";
    }

    public static boolean isUserOnline(User user) {
        if (user.getLastActiveAt() == null) {
            return false;
        }
        long minutes = ChronoUnit.MINUTES.between(user.getLastActiveAt(), LocalDateTime.now());
        return minutes < 5;
    }
}
