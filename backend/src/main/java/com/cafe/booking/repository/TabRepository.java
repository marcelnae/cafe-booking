package com.cafe.booking.repository;

import com.cafe.booking.entity.Tab;
import com.cafe.booking.entity.TabStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

public interface TabRepository extends JpaRepository<Tab, Long> {

    List<Tab> findByStatusOrderByCreatedAtDesc(TabStatus status);

    Optional<Tab> findByTableIdAndStatus(Long tableId, TabStatus status);

    @Query("""
        SELECT t FROM Tab t
        WHERE (:status IS NULL OR t.status = :status)
          AND (:tableId IS NULL OR t.table.id = :tableId)
          AND (:waiterId IS NULL OR t.waiter.id = :waiterId)
          AND (:q IS NULL OR LOWER(COALESCE(t.guestName, '')) LIKE LOWER(CONCAT('%', :q, '%')))
        ORDER BY t.createdAt DESC
        """)
    List<Tab> search(@Param("status") TabStatus status,
                     @Param("tableId") Long tableId,
                     @Param("waiterId") Long waiterId,
                     @Param("q") String q);
}
