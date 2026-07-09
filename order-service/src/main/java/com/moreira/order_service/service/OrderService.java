package com.moreira.order_service.service;

import com.moreira.order_service.mapper.OrderMapper;
import com.moreira.order_service.model.Order;
import com.moreira.order_service.models.OrderEntity;
import com.moreira.order_service.models.OrderServiceModel;
import com.moreira.order_service.models.PriceSummaryRecord;
import com.moreira.order_service.models.PriceSummaryServiceModel;
import com.moreira.order_service.repository.OrderRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.auditing.CurrentDateTimeProvider;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderMapper orderMapper;

    public List<OrderServiceModel> getOrders(Long page, Long size) {

        Pageable pageWithElements = PageRequest.of(page.intValue(), size.intValue());

        Page<OrderEntity> orderPage = orderRepository.findAll(pageWithElements);

        return orderMapper.orderEntityToOrderServiceModel(orderPage.getContent());
    }

    public OrderServiceModel getOrder(UUID id) {

        Optional<OrderEntity> order = orderRepository.findById(id);

        if (order.isEmpty()) {

            throw new NoSuchElementException("Order not found");

        }

        return orderMapper.orderEntityToOrderServiceModel(order.get());

    }

    public OrderServiceModel createOrder(OrderServiceModel orderServiceModel) {
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setCreatedOn(Instant.now());
        orderEntity.setName(orderServiceModel.getName());
        orderEntity.setCognome(orderServiceModel.getCognome());
        orderEntity.setEmail(orderServiceModel.getEmail());
        orderEntity.setDataOrder(orderServiceModel.getDataOrder());
        orderEntity.setPrice(orderServiceModel.getPrice());

        return orderMapper.orderEntityToOrderServiceModel(orderRepository.saveAndFlush(orderEntity));

    }

    public List<PriceSummaryServiceModel> calculatePriceSummaries(LocalDate startDate, LocalDate endDate) throws IllegalArgumentException {

        if (startDate != null && endDate != null) {
            if (startDate.isAfter(endDate)) {
                throw new IllegalArgumentException("data-inizio is after data-fine");
            }
            return orderMapper.priceSummaryRecordToPriceSummaryServiceModel(orderRepository.countPriceSummaryForCustomer(startDate, endDate));
        } else {

            LocalDate today = LocalDate.now();

            LocalDate previousMonthStart = today.minusMonths(1).with(TemporalAdjusters.firstDayOfMonth());
            LocalDate previousMonthEnd = today.minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());

            return orderMapper.priceSummaryRecordToPriceSummaryServiceModel(orderRepository.countPriceSummaryForCustomer(previousMonthStart, previousMonthEnd));

        }

    }

}
