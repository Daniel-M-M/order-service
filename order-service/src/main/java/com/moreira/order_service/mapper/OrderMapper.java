package com.moreira.order_service.mapper;

import com.moreira.order_service.model.Order;
import com.moreira.order_service.model.PriceSummary;
import com.moreira.order_service.models.OrderEntity;
import com.moreira.order_service.models.OrderServiceModel;
import com.moreira.order_service.models.PriceSummaryRecord;
import com.moreira.order_service.models.PriceSummaryServiceModel;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.List;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "date", source = "createdOn")
    @Mapping(target = "id", source = "orderId")
    public OrderServiceModel orderEntityToOrderServiceModel(OrderEntity order);

    @Mapping(target = "date", source = "createdOn")
    @Mapping(target = "id", source = "orderId")
    public List<OrderServiceModel> orderEntityToOrderServiceModel(List<OrderEntity> order);

    @Mapping(target = "createdOn", source = "date")
    @Mapping(target = "orderId", source = "id")
    public OrderEntity orderServiceModelToOrderEntity(OrderServiceModel order);

    @Mapping(target = "createdOn", source = "date")
    @Mapping(target = "orderId", source = "id")
    public List<OrderEntity> orderServiceModelToOrderEntity(List<OrderServiceModel> order);

    @Mapping(target = "data", source = "date")
    @Mapping(target = "uuid", source = "id")
    public Order orderServiceModelToOrder(OrderServiceModel order);

    @Mapping(target = "data", source = "date")
    @Mapping(target = "uuid", source = "id")
    public List<Order> orderServiceModelToOrder(List<OrderServiceModel> order);

    @Mapping(target = "date", source = "data")
    @Mapping(target = "id", source = "uuid")
    public OrderServiceModel orderToOrderServiceModel(Order order);

    @Mapping(target = "date", source = "data")
    @Mapping(target = "id", source = "uuid")
    public List<OrderServiceModel> orderToOrderServiceModel(List<Order> order);

    @Mapping(target = "total", source = "total", qualifiedByName = "bigDecimalToDouble")
    public PriceSummaryServiceModel priceSummaryRecordToPriceSummaryServiceModel(PriceSummaryRecord priceSummaryRecord);

    public List<PriceSummaryServiceModel> priceSummaryRecordToPriceSummaryServiceModel(List<PriceSummaryRecord> priceSummaryRecord);
    public PriceSummary priceSummaryServiceModelToPriceSummary(PriceSummaryServiceModel priceSummaryServiceModel);
    public List<PriceSummary> priceSummaryServiceModelToPriceSummary(List<PriceSummaryServiceModel> priceSummaryServiceModel);

    default LocalDate map(Instant instant) {
        if (instant == null) {
            return null;
        }
        return instant.atZone(ZoneId.systemDefault()).toLocalDate();
    }

    default Instant map(LocalDate value) {
        if (value == null) {
            return null;
        }
        // Converts the LocalDate to midnight (00:00) at UTC
        return value.atStartOfDay(ZoneOffset.UTC).toInstant();
    }

    @Named("bigDecimalToDouble")
    default double bigDecimalToDouble(BigDecimal bigDecimal) {
        if (bigDecimal == null) {
            return 0.0; // Oppure gestisci un valore di default
        }
        return bigDecimal.doubleValue();
    }

}
