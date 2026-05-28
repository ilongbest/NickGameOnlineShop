package com.example.demo.service;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(usernameOrEmail)
                .or(() -> userRepository.findFirstByEmailOrderByIdAsc(usernameOrEmail))
                .orElseThrow(() -> new UsernameNotFoundException("User khong ton tai: " + usernameOrEmail));

        if (user.getEnabled() != null && !user.getEnabled()) {
            throw new UsernameNotFoundException("Tai khoan da bi vo hieu hoa: " + usernameOrEmail);
        }

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .roles(normalizeRole(user.getRole()))
                .build();
    }

    private String normalizeRole(String role) {
        if (role == null || role.isBlank()) {
            return "USER";
        }
        return role.startsWith("ROLE_") ? role.substring("ROLE_".length()) : role;
    }
}
