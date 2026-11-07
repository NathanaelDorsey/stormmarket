package com.nathanaeldorsey.stormmarket.order;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for shipping address entities.
 */
public interface ShippingAddressRepository extends JpaRepository<ShippingAddress, Long> {
}
