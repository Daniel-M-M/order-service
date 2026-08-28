package com.moreira.order_service.service;

import com.moreira.order_service.mapper.OrderMapper;
import com.moreira.order_service.models.OrderEntity;
import com.moreira.order_service.models.OrderServiceModel;
import com.moreira.order_service.models.PriceSummaryServiceModel;
import com.moreira.order_service.repository.CustomerRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.NoSuchElementException;

@Slf4j
@AllArgsConstructor
@Service
public class CustomerService {

    private CustomerRepository customerRepository;
    private OrderMapper orderMapper;

    public List<OrderServiceModel> getCustomerOrders(@NonNull Long page, @NonNull Long size, String email) {

        Pageable pageWithElements = PageRequest.of(page.intValue(), size.intValue());
        try {

            Page<OrderEntity> orderPage = customerRepository.findByEmail(email, pageWithElements);
            log.info("getCustomerOrders - OK - Orders found");

            return orderMapper.orderEntityToOrderServiceModel(orderPage.getContent());

        } catch (Exception e) {
            log.error("Error while fetching orders from customer", e);
            throw new NoSuchElementException("Element not found");
        }

    }

    public PriceSummaryServiceModel calculatePriceSummariesForUser(LocalDate dataInizio, LocalDate dataFine, String email) throws IllegalArgumentException {

        if (dataInizio != null && dataFine != null) {
            if (dataInizio.isAfter(dataFine)) {
                log.error("dataInizio is after dataFine");
                throw new IllegalArgumentException("data-inizio is after data-fine");
            }
            log.info("calculatePriceSummariesForUser - OK");

            return orderMapper.priceSummaryRecordToPriceSummaryServiceModel(customerRepository.customerExpenseSummary(dataInizio, dataFine, email));
        } else {

            LocalDate today = LocalDate.now(ZoneId.of("UTC"));
            LocalDate previousMonthStart = today.minusMonths(1).with(TemporalAdjusters.firstDayOfMonth());
            LocalDate previousMonthEnd = today.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
            log.info("Default: previousMonthStart: {}, previousMonthEnd: {}", previousMonthEnd, previousMonthStart);

            return orderMapper.priceSummaryRecordToPriceSummaryServiceModel(customerRepository.customerExpenseSummary(previousMonthStart, previousMonthEnd, email));

        }

    }
}
