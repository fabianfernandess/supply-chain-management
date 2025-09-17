package com.fabian.supplychain.repository;

import com.fabian.supplychain.entity.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShipmentRepository extends JpaRepository<Shipment,Long> {
}
