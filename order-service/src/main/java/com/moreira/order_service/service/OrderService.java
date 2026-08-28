package com.moreira.order_service.service;

import com.moreira.order_service.mapper.OrderMapper;
import com.moreira.order_service.models.OrderEntity;
import com.moreira.order_service.models.OrderServiceModel;
import com.moreira.order_service.models.PriceSummaryServiceModel;
import com.moreira.order_service.repository.OrderRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@AllArgsConstructor
@Service
public class OrderService {

    private OrderRepository orderRepository;
    private OrderMapper orderMapper;

    public List<OrderServiceModel> getOrders(@NonNull Long page, @NonNull Long size) {

        Pageable pageWithElements = PageRequest.of(page.intValue(), size.intValue());

        try {
            Page<OrderEntity> orderPage = orderRepository.findAll(pageWithElements);
            log.info("getOrders - OK");

            return orderMapper.orderEntityToOrderServiceModel(orderPage.getContent());

        } catch (NoSuchElementException e) {
            log.error("getOrders - NoSuchElementException", e);
            throw new NoSuchElementException("getOrders - NoSuchElementException");
        }
    }

    public OrderServiceModel getOrder(UUID id) {

        try {
            Optional<OrderEntity> order = orderRepository.findById(id);

            if (order.isEmpty()) {
                log.warn("Order not found for id {}", id);
                throw new NoSuchElementException("Order not found");
            }
            log.info("Order found for id {}", id);

            return orderMapper.orderEntityToOrderServiceModel(order.get());

        } catch (Exception e) {
            log.error("Error while getting order for id {}", id, e);
            throw new IllegalArgumentException("Order not found");
        }

    }

    public OrderServiceModel createOrder(@NonNull OrderServiceModel orderServiceModel) {

        try {

            OrderEntity orderEntity = new OrderEntity();
            orderEntity.setCreatedOn(Instant.now());
            orderEntity.setName(orderServiceModel.getName());
            orderEntity.setCognome(orderServiceModel.getCognome());
            orderEntity.setEmail(orderServiceModel.getEmail());
            orderEntity.setDataOrder(orderServiceModel.getDataOrder());
            orderEntity.setPrice(orderServiceModel.getPrice());
            log.info("createOrder - OK");

            return orderMapper.orderEntityToOrderServiceModel(orderRepository.saveAndFlush(orderEntity));

        } catch (Exception e) {
            log.error("Fail on create order: {}" , e.getMessage());
            throw new IllegalArgumentException("Order could not be created");
        }

    }

    public List<PriceSummaryServiceModel> calculatePriceSummaries(LocalDate dataInizio, LocalDate dataFine) throws IllegalArgumentException {

        if (dataInizio != null && dataFine != null) {
            if (dataInizio.isAfter(dataFine)) {
                log.error("dataInizio is after dataFine");
                throw new IllegalArgumentException("data-inizio is after data-fine");
            }
            log.info("calculatePriceSummariesForUser - OK");

            return orderMapper.priceSummaryRecordToPriceSummaryServiceModel(orderRepository.countPriceSummaryForCustomer(dataInizio, dataFine));
        } else {

            LocalDate today = LocalDate.now(ZoneId.of("UTC"));
            LocalDate previousMonthStart = today.minusMonths(1).with(TemporalAdjusters.firstDayOfMonth());
            LocalDate previousMonthEnd = today.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
            log.info("Default: previousMonthStart: {}, previousMonthEnd: {}", previousMonthEnd, previousMonthStart);

            return orderMapper.priceSummaryRecordToPriceSummaryServiceModel(orderRepository.countPriceSummaryForCustomer(previousMonthStart, previousMonthEnd));

        }

    }

}
