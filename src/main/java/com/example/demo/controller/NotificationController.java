package com.example.demo.controller;

import com.example.demo.entity.Notification;
import com.example.demo.entity.User;
import com.example.demo.repository.NotificationRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/notifications")
public class NotificationController {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public ResponseEntity<?> getNotifications(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body("Chua dang nhap");
        }
        Optional<User> userOpt = findCurrentUser(principal);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body("Chua dang nhap");
        }
        User user = userOpt.get();
        List<Notification> list = notificationRepository.findByUserIdOrderByCreatedAtDesc(user.getId());
        return ResponseEntity.ok(list);
    }

    @PostMapping("/mark-read")
    public ResponseEntity<?> markAllAsRead(Principal principal) {
        if (principal == null) {
            return ResponseEntity.status(401).body("Chua dang nhap");
        }
        Optional<User> userOpt = findCurrentUser(principal);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body("Chua dang nhap");
        }
        User user = userOpt.get();
        List<Notification> list = notificationRepository.findByUserAndReadStatusOrderByCreatedAtDesc(user, false);
        for (Notification n : list) {
            n.setReadStatus(true);
        }
        notificationRepository.saveAll(list);
        return ResponseEntity.ok().build();
    }

    private Optional<User> findCurrentUser(Principal principal) {
        return userRepository.findByUsername(principal.getName())
                .or(() -> userRepository.findFirstByEmailOrderByIdAsc(principal.getName()));
    }
}
