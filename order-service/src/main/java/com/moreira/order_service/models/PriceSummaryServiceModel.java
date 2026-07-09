package com.moreira.order_service.models;

import lombok.Data;

@Data
public class PriceSummaryServiceModel {
    private String customer;
    private Double total;
}
