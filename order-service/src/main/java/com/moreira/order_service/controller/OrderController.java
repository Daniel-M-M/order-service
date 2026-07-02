package com.moreira.order_service.controller;

import com.moreira.order_service.mapper.OrderMapper;
import com.moreira.order_service.model.Order;
import com.moreira.order_service.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
public class OrderController implements OrderApi {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderMapper orderMapper;

    @Override
    public ResponseEntity<Order> createOrder(Order order) {
        return ResponseEntity.ok(orderMapper.orderServiceModelToOrder(orderService.createOrder(orderMapper.orderToOrderServiceModel(order))));
    }

    @Override
    public ResponseEntity<Order> getOrder(UUID uuidOrder) {
        return ResponseEntity.ok(orderMapper.orderServiceModelToOrder(orderService.getOrder(uuidOrder)));
    }

    @Override
    public ResponseEntity<List<Order>> getOrders(Long page, Long size) {
        return ResponseEntity.ok(orderMapper.orderServiceModelToOrder(orderService.getOrders(page, size)));
    }

}
