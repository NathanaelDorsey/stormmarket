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
public class AdminRoleLock {
    @Id
    private Long id;
}
