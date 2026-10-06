package com.nathanaeldorsey.stormmarket.user;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // Additional query methods can be defined here if needed

    //Find by username
    Optional<User> findByUsername(String username);
}
