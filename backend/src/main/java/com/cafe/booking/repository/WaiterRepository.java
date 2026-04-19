package com.cafe.booking.repository;

import com.cafe.booking.entity.Waiter;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface WaiterRepository extends JpaRepository<Waiter, Long> {
    Optional<Waiter> findByNameIgnoreCase(String name);
}
