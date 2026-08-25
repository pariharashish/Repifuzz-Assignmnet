package com.repifuzz.Entity;

import jakarta.persistence.*;
import lombok.*;


@Entity
@Table(name = "users")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String username;
    @Column(nullable = false, unique = true)
    private String email;
    private String phone;
    private String address;
    private String pinCode;
    private String city;
    private String country;
    private String password; // hashed
// getters/setters

}