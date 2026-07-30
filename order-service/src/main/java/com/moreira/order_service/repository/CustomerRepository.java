package com.moreira.order_service.repository;

import com.moreira.order_service.models.OrderEntity;
import com.moreira.order_service.models.PriceSummaryRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<OrderEntity, UUID> {

    @Query(value = "SELECT CONCAT_WS(' ', o.cognome, o.name) AS customer, SUM(o.price) AS total " +
            "FROM orders o " +
            "WHERE o.data_order >= :startDate AND o.data_order < :endDate AND o.email = :email " +
            "GROUP BY o.name, o.cognome ",
            nativeQuery = true)
    PriceSummaryRecord customerExpenseSummary(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("email") String email
    );

    Page<OrderEntity> findByEmail(String email, Pageable pageable);

}
