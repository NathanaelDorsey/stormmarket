package com.nathanaeldorsey.stormmarket.user;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
/**
 * Represents a registered user. Holds name, username, hashed password, email, phone,
 * address, role (USER or ADMIN), and active flag.
 *
 * Defaults to role USER and active true.
 */
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "phone_number", length = 20)
    private String phone;

    @Column(columnDefinition = "TEXT")
    private String address;

    @Column(nullable = false, length = 20)
    @Enumerated(EnumType.STRING) // Stores enum as its name (string) in database
    private Role role = Role.USER; // Default - USER

    @Column(name = "is_active", nullable = false)
    private boolean active = true; // Default - active
}
