package IT;

import com.moreira.order_service.OrderServiceApplication;
import com.moreira.order_service.mapper.OrderMapper;
import com.moreira.order_service.model.Order;
import com.moreira.order_service.service.OrderService;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.junit.jupiter.api.Assertions.*;
import static org.assertj.core.api.Assertions.assertThat;

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

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;
    }

    @Test
    public void OrderTestPost(){

        Order requestPayload = new Order(
            "Francesco",
            "Nero",
            "nero.f@gmail.com",
            LocalDate.parse("2026-06-26"),
            Double.parseDouble("78.92")
        );

        Order responseOrder = given()
            .contentType(ContentType.JSON)
            .body(requestPayload)
            .post("/orders")
                .then()
                .statusCode(200)
                .extract()
                .as(Order.class);

        assertNotNull(responseOrder.getDataOrder(), "La data dell'ordine non deve essere null");
        assertEquals("Francesco", responseOrder.getName());
        assertEquals("Nero", responseOrder.getCognome());
        assertEquals("2026-06-26", responseOrder.getDataOrder().toString());
        assertEquals("nero.f@gmail.com", responseOrder.getEmail());
        assertEquals(Double.parseDouble("78.92"), responseOrder.getPrice());

    }

    @Test
    public void OrderTestGet(){

        Order requestPayload = new Order(
                "Mario",
                "Rossi",
                "rossi.m@gmail.com",
                LocalDate.parse("2026-06-26"),
                Double.parseDouble("89.53")
        );

        Order order = given()
                .contentType(ContentType.JSON)
                .body(requestPayload)
                .post("/orders")
                .then()
                .statusCode(200)
                .extract()
                .as(Order.class);

        assertThat(order.getDataOrder()).isNotNull();

        List<Order> ordersResponse = given()
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/orders")
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<List<Order>>() {});

        assertThat(order.getDataOrder()).isNotNull();
        assertThat(ordersResponse.getFirst().getDataOrder()).isNotNull();

        //Assert for each element contains on response list
        assertThat(ordersResponse)
                .extracting(Order::getName, Order::getCognome, Order::getEmail, Order::getPrice, Order::getDataOrder)
                .contains(tuple(
                        order.getName(),
                        order.getCognome(),
                        order.getEmail(),
                        order.getPrice(),
                        order.getDataOrder()
                ));

        //Assert for each element lambda func TODO: rifare nel modo che era prima cosi non sai cosa falisce
        assertTrue(ordersResponse.stream().anyMatch(order1 -> order1.getName().equals(order.getName())
                && order1.getCognome().equals(order.getCognome())
                && order1.getDataOrder().equals(order.getDataOrder())
                && order1.getEmail().equals(order.getEmail())
                && order1.getPrice().equals(order.getPrice())));

    }

    @Test
    public void OrderTestGetWithUUID(){

        Order requestPayload = new Order(
                "Marco",
                "Bianchi",
                "bianchi.m@gmail.com",
                LocalDate.parse("2026-06-26"),
                Double.parseDouble("25.53")
        );

        Order order = given()
                .contentType(ContentType.JSON)
                .body(requestPayload)
                .post("/orders")
                .then()
                .statusCode(200)
                .extract()
                .as(Order.class);

        assertThat(order.getDataOrder()).isNotNull();

        Order orderResponse = RestAssured
                .given()
                .contentType(ContentType.JSON)
                .pathParam("uuidOrder", order.getUuid())
                .get("/orders/{uuidOrder}")
                .then()
                .statusCode(200)
                .extract()
                .as(Order.class);

        assertThat(order.getDataOrder()).isNotNull();
        assertThat(orderResponse.getDataOrder()).isNotNull();

        //Assert for each parameter
        assertThat(orderResponse)
                .extracting(Order::getName, Order::getCognome, Order::getEmail, Order::getPrice, Order::getDataOrder)
                .containsExactly(
                        order.getName(),
                        order.getCognome(),
                        order.getEmail(),
                        order.getPrice(),
                        order.getDataOrder()
                );

        //Assert for object and ignoring fields
        assertThat(orderResponse)
                .usingRecursiveComparison()
                .ignoringFields("uuid")
                .isEqualTo(order);

    }

}
