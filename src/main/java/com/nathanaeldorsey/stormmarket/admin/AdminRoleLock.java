package com.nathanaeldorsey.stormmarket.admin;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "admin_role_lock")
/**
 * Singleton lock row used to serialize admin role changes and prevent race conditions.
 * Only the id field is used; its existence provides a row to lock against.
 */
public class AdminRoleLock {
    @Id
    private Long id;
}
