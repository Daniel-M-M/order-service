package com.moreira.order_service.models;

import java.math.BigDecimal;

public record PriceSummaryRecord (String customer, BigDecimal total){
}
