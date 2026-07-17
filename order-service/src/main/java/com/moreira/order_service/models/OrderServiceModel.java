package com.moreira.order_service.models;

import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;

@Data
public class OrderServiceModel {

    private UUID id;
    private String name;
    private Instant date;
    private String cognome;
    private String email;
    private Date dataOrder;
    private BigDecimal price;

}
