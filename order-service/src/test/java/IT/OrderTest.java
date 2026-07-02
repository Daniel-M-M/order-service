package IT;

import com.moreira.order_service.OrderServiceApplication;
import com.moreira.order_service.mapper.OrderMapper;
import com.moreira.order_service.model.Order;
import com.moreira.order_service.service.OrderService;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static tools.jackson.databind.type.LogicalType.Map;

@Slf4j
@SpringBootTest(classes = {OrderServiceApplication.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.flyway.enabled=false")
@TestPropertySource(locations = {"classpath:IT/application-test.properties"})
public class OrderTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private OrderMapper orderMapper;

    @LocalServerPort
    private int port;

    @Test
    public void OrderTestPost(){
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        Response response = RestAssured
        .given()
        .contentType(ContentType.JSON)
        .body("""
                {
                  "name": "Francesco",
                  "cognome": "Nero",
                  "email": "nero.f@gmail.com",
                  "dataOrder": "2026-06-26",
                  "price": "78.92"
                }""") // Send request payload
        .post("/orders");

        String jsonOrder = response.getBody().asString();

        ObjectMapper objectMapper = new ObjectMapper();
        Order order = objectMapper.readValue(jsonOrder, Order.class);

        assertEquals(200, response.getStatusCode());
        assertEquals("Francesco", order.getName());
        assertEquals("Nero", order.getCognome());
        assert order.getDataOrder() != null;
        assertEquals("2026-06-26", order.getDataOrder().toString());
        assertEquals("nero.f@gmail.com", order.getEmail());
        assertEquals("78.92", order.getPrice().toString());

    }

    @Test
    public void OrderTestGet(){
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
        Response responsePost = RestAssured
                .given()
                .contentType(ContentType.JSON)
                .filter(new io.restassured.filter.log.RequestLoggingFilter())
                .filter(new io.restassured.filter.log.ResponseLoggingFilter())
                .body("""
                        {
                          "name": "Marco",
                          "cognome": "Bianchi",
                          "email": "bianchi.m@gmail.com",
                          "dataOrder": "2026-06-27",
                          "price": "90.92"
                        }""") // Send request payload
                .post("/orders");
        String jsonOrder = responsePost.getBody().asString();
        ObjectMapper objectMapper = new ObjectMapper();
        Order order = objectMapper.readValue(jsonOrder, Order.class);
        //Chiamare HTTP
        Response responseGet = RestAssured
                .given()
                .queryParam("page", 0)
                .queryParam("size", 5)
                .filter(new io.restassured.filter.log.RequestLoggingFilter())
                .filter(new io.restassured.filter.log.ResponseLoggingFilter())
                .get("/orders");

        assertEquals(200, responseGet.getStatusCode());

        String jsonOrder1 = responseGet.getBody().asString();

        ObjectMapper objectMapper1 = new ObjectMapper();
        List<Order> ordersResponse = objectMapper1.readValue(jsonOrder1, new TypeReference<>() {});

        //List<Order> ordersResponse = objectMapper.readValue(jsonOrder1, new TypeReference<List<Order>>() {});

        //Fare per tutti i campi
        assertTrue(ordersResponse.stream().anyMatch(order1 -> order1.getName().equals(order.getName())));
        assertTrue(ordersResponse.stream().anyMatch(order1 -> order1.getCognome().equals(order.getCognome())));
        assert order.getDataOrder() != null;
        assert ordersResponse.getFirst().getDataOrder() != null;
        assertTrue(ordersResponse.stream().anyMatch(order1 -> order1.getDataOrder().equals(order.getDataOrder())));
        assertTrue(ordersResponse.stream().anyMatch(order1 -> order1.getEmail().equals(order.getEmail())));
        assertTrue(ordersResponse.stream().anyMatch(order1 -> order1.getPrice().equals(order.getPrice())));

    }

//    @Test
//    public void OrderTestGetWithUUID(){
//        RestAssured.baseURI = "http://localhost";
//        RestAssured.port = port;
//        Response response = RestAssured
//                .given()
//                .contentType(ContentType.JSON)
//                //+.pathParam("idRubrica", UUID.fromString("12fea6b9-34ba-4e63-a1d5-4d20f528c2d8"))
//                //.headers(generateHeaders())
//                .body("""
//                        {
//                          "name": "Mario",
//                          "cognome": "Rossi",
//                          "email": "rossi.m@gmail.com",
//                          "dataOrder": "2026-06-28",
//                          "price": "50.32"
//                        }""") // Send request payload
//                .post("/orders");
//
//        String jsonOrder = response.getBody().asString();
//
//        ObjectMapper objectMapper = new ObjectMapper();
//        Order order = objectMapper.readValue(jsonOrder, Order.class);
//
//        List<Order> getOrder = orderMapper.orderServiceModelToOrder(orderService.getOrders(0L, 5L));
//
//        Order getOrderWithUuid = orderMapper.orderServiceModelToOrder(orderService.getOrder(getOrder.getFirst().getUuid()));
//
//        assertEquals(order.getName(), getOrderWithUuid.getName());
//        assertEquals(order.getCognome(), getOrderWithUuid.getCognome());
//        assert order.getDataOrder() != null;
//        assert getOrderWithUuid.getDataOrder() != null;
//        assertEquals(order.getDataOrder().toString(), getOrderWithUuid.getDataOrder().toString());
//        assertEquals(order.getEmail(), getOrderWithUuid.getEmail());
//        assertEquals(order.getPrice().toString(), getOrderWithUuid.getPrice().toString());
//
//    }

}
