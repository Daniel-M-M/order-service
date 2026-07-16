package IT;

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.moreira.order_service.OrderServiceApplication;
import com.moreira.order_service.mapper.OrderMapper;
import com.moreira.order_service.model.Order;
import com.moreira.order_service.model.PriceSummary;
import com.moreira.order_service.service.OrderService;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.config.ObjectMapperConfig;
import io.restassured.config.RestAssuredConfig;
import io.restassured.http.ContentType;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
                .as(new TypeRef<>() {
                });

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

        //Assert for each element lambda func
        assertTrue(ordersResponse.stream().anyMatch(order1 -> order1.getName().equals(order.getName())));
        assertTrue(ordersResponse.stream().anyMatch(order1 -> order1.getCognome().equals(order.getCognome())));
        assertTrue(ordersResponse.stream().anyMatch(order1 -> order1.getDataOrder().equals(order.getDataOrder())));
        assertTrue(ordersResponse.stream().anyMatch(order1 -> order1.getEmail().equals(order.getEmail())));
        assertTrue(ordersResponse.stream().anyMatch(order1 -> order1.getPrice().equals(order.getPrice())));

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

    @Test
    public void PriceSummaryTestGet(){

        Order requestPayload = new Order(
                "Filippo",
                "Verdi",
                "verdi.f@gmail.com",
                LocalDate.parse("2026-07-09"),
                Double.parseDouble("10.00")
        );

        Order order = given()
                .contentType(ContentType.JSON)
                .body(requestPayload)
                .post("/orders")
                .then()
                .statusCode(200)
                .extract()
                .as(Order.class);

        Order requestPayload2 = new Order(
                "Filippo",
                "Verdi",
                "verdi.f@gmail.com",
                LocalDate.parse("2026-07-09"),
                Double.parseDouble("10.00")
        );

        Order order2 = given()
                .contentType(ContentType.JSON)
                .body(requestPayload2)
                .post("/orders")
                .then()
                .statusCode(200)
                .extract()
                .as(Order.class);

//        ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
//
//        RestAssured.config = RestAssuredConfig.config().objectMapperConfig(
//                ObjectMapperConfig.objectMapperConfig().jackson2ObjectMapperFactory((type, s) -> objectMapper)
//        );
        LocalDate inizio = LocalDate.of(2026, 6, 1);
        LocalDate fine = LocalDate.of(2026, 7, 10);

        List<PriceSummary> ordersResponse = given()
                .contentType(ContentType.JSON)
//                .param("data-inizio", inizio.format(DateTimeFormatter.ISO_DATE))
//                .param("data-fine", fine.format(DateTimeFormatter.ISO_DATE))
                .when()
                .get("/orders/summary?data-inizio=2026-06-01&data-fine=2026-07-01")
                .then()
                .log().all()
                .statusCode(200)
                .extract()
                .as(new TypeRef<List<PriceSummary>>() {
                });

        System.out.println("RISPOSTA SERVER: " + ordersResponse);

        //Assert for each element contains on response list
        assertThat(ordersResponse)
                .extracting(
                        PriceSummary::getCustomer,
                        PriceSummary::getTotal // Assicurati che il nome del metodo sia corretto nella tua classe PriceSummary
                )
                .contains(tuple(
                        "Verdi Filippo",
                        20.00
                ));

        //Assert for each element lambda func
        assertTrue(ordersResponse.stream().anyMatch(order1 -> {
            assert order1.getCustomer() != null;
            return order1.getCustomer().equals(order.getCognome().concat(" ").concat(order.getName()));
        }));
        assertTrue(ordersResponse.stream().anyMatch(order1 -> {
            assert order1.getCustomer() != null;
            return order1.getCustomer().equals(order2.getCognome().concat(" ").concat(order2.getName()));
        }));
        assertTrue(ordersResponse.stream().anyMatch(order1 -> {
            assert order1.getTotal() != null;
            return order1.getTotal().equals(order.getPrice() + order2.getPrice());
        }));

    }

}
