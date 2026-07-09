package com.moreira.order_service.repository;

import com.moreira.order_service.model.PriceSummary;
import com.moreira.order_service.models.OrderEntity;
import com.moreira.order_service.models.PriceSummaryRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, UUID> {

    @Query(value = "SELECT CONCAT_WS(' ', o.cognome, o.name) AS cliente, SUM(o.price) AS totale " +
                    "FROM orders o " +
                    "WHERE o.data_order >= :startDate AND o.data_order < :endDate " +
                    "GROUP BY o.email, o.name, o.cognome " +
                    "ORDER BY o.cognome",
                    nativeQuery = true)
    List<PriceSummaryRecord> countPriceSummaryForCustomer(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
