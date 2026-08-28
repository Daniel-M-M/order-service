package com.moreira.order_service.controller;

import com.moreira.order_service.mapper.OrderMapper;
import com.moreira.order_service.model.Order;
import com.moreira.order_service.model.PriceSummary;
import com.moreira.order_service.service.OrderService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@AllArgsConstructor
@RestController
public class OrderController implements OrderApi {

    private OrderService orderService;
    private OrderMapper orderMapper;

    @Override
    public ResponseEntity<Order> createOrder(Order order) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof JwtAuthenticationToken jwtAuth)) {
            log.error("Access denied: Invalid Token. - create order");
            throw new AccessDeniedException("Access denied: Invalid Token.");
        }

        Jwt jwt = jwtAuth.getToken();
        order.setName(jwt.getClaimAsString("given_name"));
        order.setCognome(jwt.getClaimAsString("family_name"));
        order.setEmail(jwt.getClaimAsString("email"));

        return ResponseEntity.ok(orderMapper.orderServiceModelToOrder(orderService.createOrder(orderMapper.orderToOrderServiceModel(order))));
    }

    @Override
    public ResponseEntity<Order> getOrder(UUID uuidOrder) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof JwtAuthenticationToken)) {
            log.error("Access denied: Invalid Token. - get order uuid");
            throw new AccessDeniedException("Access denied: Invalid Token.");
        }

        return ResponseEntity.ok(orderMapper.orderServiceModelToOrder(orderService.getOrder(uuidOrder)));
    }

    @Override
    public ResponseEntity<List<Order>> getOrders(Long page, Long size) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof JwtAuthenticationToken jwtAuth)) {
            log.error("Access denied: Invalid Token. - Get orders");
            throw new AccessDeniedException("Access denied: Invalid Token.");
        }

        Jwt jwt = jwtAuth.getToken();
        String role = Optional.ofNullable((Map<String, Object>) jwt.getClaim("resource_access"))
                .map(resourceAccess -> (Map<String, Object>) resourceAccess.get("foodmanager"))
                .map(foodmanager -> (Collection<String>) foodmanager.get("roles"))
                .flatMap(roles -> roles.stream().findFirst()).orElse(null);

        if (Objects.equals(role, "ROLE_FOODMANAGER_USER")){
            log.error("Access denied: Insufficient role or permissions. - Get orders");
            throw new AccessDeniedException("Access denied: Insufficient role or permissions.");
        }

        return ResponseEntity.ok(orderMapper.orderServiceModelToOrder(orderService.getOrders(page, size)));

    }

    @Override
    public ResponseEntity<List<PriceSummary>> getSummaryForEachCustomer(LocalDate dataInizio, LocalDate dataFine) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof JwtAuthenticationToken jwtAuth)) {
            log.error("Access denied: Invalid Token. - Summary all customers");
            throw new AccessDeniedException("Access denied: Invalid Token.");
        }

        Jwt jwt = jwtAuth.getToken();
        String role = Optional.ofNullable((Map<String, Object>) jwt.getClaim("resource_access"))
                .map(resourceAccess -> (Map<String, Object>) resourceAccess.get("foodmanager"))
                .map(foodmanager -> (Collection<String>) foodmanager.get("roles"))
                .flatMap(roles -> roles.stream().findFirst()).orElse(null);

        if (Objects.equals(role, "ROLE_FOODMANAGER_USER")) {
            log.error("Access denied: Insufficient role or permissions. - Summary all customers");
            throw new AccessDeniedException("Access denied: Insufficient role or permissions.");
        }

        return ResponseEntity.ok(orderMapper.priceSummaryServiceModelToPriceSummary(orderService.calculatePriceSummaries(dataInizio, dataFine)));

    }

}
