package com.moreira.order_service.controller;

import com.moreira.order_service.mapper.OrderMapper;
import com.moreira.order_service.model.Order;
import com.moreira.order_service.model.PriceSummary;
import com.moreira.order_service.service.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.*;

@RestController
public class CustomerController implements CustomerApi{

    @Autowired
    private CustomerService customerService;

    @Autowired
    private OrderMapper orderMapper;

    @Override
    public ResponseEntity<List<Order>> getCustomerOrders(String userName, Long page, Long size) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof JwtAuthenticationToken jwtAuth)) {
            throw new AccessDeniedException("Access denied: Invalid Token.");
        }

        Jwt jwt = jwtAuth.getToken();

        if (!Objects.equals(jwt.getClaimAsString("preferred_username"), userName)) {
            throw new AccessDeniedException("Access denied: Insufficient role or permissions.");
        }

        String email = jwt.getClaimAsString("email");

        return ResponseEntity.ok(orderMapper.orderServiceModelToOrder(customerService.getCustomerOrders(page, size, email)));
    }

    @Override
    public ResponseEntity<PriceSummary> getCustomerSummary(String userName, LocalDate dataInizio, LocalDate dataFine) {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof JwtAuthenticationToken jwtAuth)) {
            throw new AccessDeniedException("Access denied: Invalid Token.");
        }

        Jwt jwt = jwtAuth.getToken();

        if (!Objects.equals(jwt.getClaimAsString("preferred_username"), userName)) {
            throw new AccessDeniedException("Access denied: Insufficient role or permissions.");
        }

        String email = jwt.getClaimAsString("email");

        return ResponseEntity.ok(orderMapper.priceSummaryServiceModelToPriceSummary(customerService.calculatePriceSummariesForUser(dataInizio, dataFine, email)));
    }
}
