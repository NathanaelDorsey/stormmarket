package com.nathanaeldorsey.stormmarket.user;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    // Additional query methods can be defined here if needed

    Optional<User> findByUsername(String username);
    // Check duplicate usernames
    boolean existsByUsername(String username);
    // Check duplicate emails
    boolean existsByEmail(String email);
    // Count active users by role
    long countActiveByRole(Role role);
}
