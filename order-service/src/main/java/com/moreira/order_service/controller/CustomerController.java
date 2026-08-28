package com.moreira.order_service.controller;

import com.moreira.order_service.mapper.OrderMapper;
import com.moreira.order_service.model.Order;
import com.moreira.order_service.model.PriceSummary;
import com.moreira.order_service.service.CustomerService;
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
public class CustomerController implements CustomerApi{

    private CustomerService customerService;
    private OrderMapper orderMapper;

    @Override
    public ResponseEntity<List<Order>> getCustomerOrders(String userName, Long page, Long size) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof JwtAuthenticationToken jwtAuth)) {
            log.error("Invalid JWT Token - customer orders");
            throw new AccessDeniedException("Access denied: Invalid Token.");
        }

        Jwt jwt = jwtAuth.getToken();

        if (!Objects.equals(jwt.getClaimAsString("preferred_username"), userName)) {
            log.error("Insufficient Access to User - customer orders");
            throw new AccessDeniedException("Access denied: Insufficient role or permissions.");
        }

        String email = jwt.getClaimAsString("email");

        return ResponseEntity.ok(orderMapper.orderServiceModelToOrder(customerService.getCustomerOrders(page, size, email)));
    }

    @Override
    public ResponseEntity<PriceSummary> getCustomerSummary(String userName, LocalDate dataInizio, LocalDate dataFine) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof JwtAuthenticationToken jwtAuth)) {
            log.error("Invalid JWT Token - customer summary");
            throw new AccessDeniedException("Access denied: Invalid Token.");
        }

        Jwt jwt = jwtAuth.getToken();

        if (!Objects.equals(jwt.getClaimAsString("preferred_username"), userName)) {
            log.error("Insufficient Access to User - customer summary");
            throw new AccessDeniedException("Access denied: Insufficient role or permissions.");
        }

        String email = jwt.getClaimAsString("email");

        return ResponseEntity.ok(orderMapper.priceSummaryServiceModelToPriceSummary(customerService.calculatePriceSummariesForUser(dataInizio, dataFine, email)));
    }
}
