package com.moreira.order_service.models;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PriceSummaryServiceModel {
    private String customer;
    private BigDecimal total;
}
