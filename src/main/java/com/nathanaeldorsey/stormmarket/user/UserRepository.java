package com.nathanaeldorsey.stormmarket.user;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for user entities. Provides standard CRUD plus lookups by username and
 * duplicate-check methods for username and email, plus a count of active users by role.
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by username.
     *
     * @param username the username to look up
     * @return the user, or empty if not found
     */
    Optional<User> findByUsername(String username);

    /**
     * Checks whether a username already exists.
     *
     * @param username the username to check
     * @return true if a user with this username exists
     */
    boolean existsByUsername(String username);

    /**
     * Checks whether an email already exists (case-insensitive).
     *
     * @param email the email to check
     * @return true if a user with this email exists
     */
    boolean existsByEmail(String email);

    /**
     * Counts active users with the given role.
     *
     * @param role the role to count
     * @return the number of active users with that role
     */
    long countActiveByRole(Role role);
}
