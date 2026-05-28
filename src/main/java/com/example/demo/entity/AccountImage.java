package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "account_images")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class AccountImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String url;

    @ManyToOne
    @JoinColumn(name = "account_id")
    @JsonBackReference
    private AccountGame account;
}
