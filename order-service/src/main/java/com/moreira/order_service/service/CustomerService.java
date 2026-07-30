package com.moreira.order_service.service;

import com.moreira.order_service.mapper.OrderMapper;
import com.moreira.order_service.models.OrderEntity;
import com.moreira.order_service.models.OrderServiceModel;
import com.moreira.order_service.models.PriceSummaryServiceModel;
import com.moreira.order_service.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

@Service
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private OrderMapper orderMapper;

    public List<OrderServiceModel> getCustomerOrders(Long page, Long size, String email) {

        Pageable pageWithElements = PageRequest.of(page.intValue(), size.intValue());
        Page<OrderEntity> orderPage = customerRepository.findByEmail(email, pageWithElements);

        return orderMapper.orderEntityToOrderServiceModel(orderPage.getContent());

    }

    public PriceSummaryServiceModel calculatePriceSummariesForUser(LocalDate dataInizio, LocalDate dataFine, String email) throws IllegalArgumentException {

        if (dataInizio != null && dataFine != null) {
            if (dataInizio.isAfter(dataFine)) {
                throw new IllegalArgumentException("data-inizio is after data-fine");
            }

            return orderMapper.priceSummaryRecordToPriceSummaryServiceModel(customerRepository.customerExpenseSummary(dataInizio, dataFine, email));
        } else {

            LocalDate today = LocalDate.now();
            LocalDate previousMonthStart = today.minusMonths(1).with(TemporalAdjusters.firstDayOfMonth());
            LocalDate previousMonthEnd = today.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());

            return orderMapper.priceSummaryRecordToPriceSummaryServiceModel(customerRepository.customerExpenseSummary(previousMonthStart, previousMonthEnd, email));

        }

    }
}
