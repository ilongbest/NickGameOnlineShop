package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "users")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    private String email;

    @Column(precision = 19, scale = 2)
    private BigDecimal balance = BigDecimal.ZERO;

    private String role;// e.g., ROLE_USER, ROLE_ADMIN

    @Column(columnDefinition = "TEXT")
    private String avatarUrl;

    @OneToMany(mappedBy = "user")
    @JsonManagedReference
    private List<Order> orders;

    private Boolean enabled = true;

    private LocalDateTime lastActiveAt;
}
