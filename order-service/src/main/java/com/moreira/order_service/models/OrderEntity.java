package com.moreira.order_service.models;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;
import java.util.UUID;

import java.time.Instant;

@Entity
@Getter
@Setter
@Table(name = "orders")
public class OrderEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID orderId;

    private String name;

    @CreationTimestamp
    private Instant createdOn;

    private String email;

    private String cognome;

    private Date dataOrder;

    private Double price;

}
