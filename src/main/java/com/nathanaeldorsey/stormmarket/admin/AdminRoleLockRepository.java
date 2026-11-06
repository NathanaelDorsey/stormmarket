package com.nathanaeldorsey.stormmarket.admin;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Repository for the admin role lock row. Provides a pessimistic-write lock query to
 * serialize concurrent role-change operations.
 */
public interface AdminRoleLockRepository extends JpaRepository<AdminRoleLock, Long> {

    /**
     * Finds and pessimistically locks the admin role lock row by id for update.
     *
     * @param id the lock row id
     * @return the locked lock row, or empty
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT lockrow FROM AdminRoleLock lockrow WHERE lockrow.id = :id")
    Optional<AdminRoleLock> findByIdForUpdate(@Param("id") Long id);
}
