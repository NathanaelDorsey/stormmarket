package com.nathanaeldorsey.stormmarket.admin;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AdminRoleLockRepository extends JpaRepository<AdminRoleLock, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT lockrow FROM AdminRoleLock lockrow WHERE lockrow.id = :id")
    Optional<AdminRoleLock> findByIdForUpdate(@Param("id") Long id);
}
