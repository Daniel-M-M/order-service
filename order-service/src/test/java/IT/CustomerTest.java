package IT;

import com.moreira.order_service.OrderServiceApplication;
import com.moreira.order_service.model.Order;
import com.moreira.order_service.model.PriceSummary;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.TestPropertySource;

import java.time.LocalDate;
import java.util.List;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.tuple;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
@SpringBootTest(classes = {OrderServiceApplication.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.flyway.enabled=false")
@TestPropertySource(locations = {"classpath:IT/application-test.properties"})
class CustomerTest {

    @LocalServerPort
    private int port;

    private String userToken;

    private String obtainAccessToken(String username, String password) {
        Response response = given()
                .contentType(ContentType.URLENC)
                .formParam("client_id", "foodmanager")
                .formParam("client_secret", "lvZ1F3cMfFVVQ4KnbEZzJsAin944FbWwSKZdA8HnF8mBaaE7idG1Gny7hHnpwQYxpjGs4Wx8NitjoGpGmkMh7d")
                .formParam("grant_type", "password")
                .formParam("username", username)
                .formParam("password", password)
                .post("http://localhost:8080/realms/foodmanager/protocol/openid-connect/token");

        return response.jsonPath().getString("access_token");
    }

    @BeforeEach
    void setUp() {
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = port;

        this.userToken  = obtainAccessToken("mbianchi", "MBianchi");
    }

    @Test
    void testGetCustomerOrders() {
        Order requestPayload1 = new Order(
                "Marco",
                "Bianchi",
                "m.bianchi@gmail.com",
                LocalDate.parse("2026-07-20"),
                Double.parseDouble("10.50")
        );

        Order requestPayload2 = new Order(
                "Marco",
                "Bianchi",
                "m.bianchi@gmail.com",
                LocalDate.parse("2026-07-21"),
                Double.parseDouble("10.50")
        );

        Order order1 = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + userToken)
                .body(requestPayload1)
                .post("/orders")
                .then()
                .statusCode(200)
                .log().headers()
                .extract()
                .as(Order.class);

        assertThat(order1.getDataOrder()).isNotNull();

        Order order2 = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + userToken)
                .body(requestPayload2)
                .post("/orders")
                .then()
                .statusCode(200)
                .log().headers()
                .extract()
                .as(Order.class);

        assertThat(order2.getDataOrder()).isNotNull();

        List<Order> ordersResponse = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + userToken)
                .pathParam("userName", "mbianchi" )
                .queryParam("page", 0)
                .queryParam("size", 10)
                .when()
                .get("/customers/{userName}/orders")
                .then()
                .statusCode(200)
                .extract()
                .as(new TypeRef<>() {
                });

        assertThat(ordersResponse.getFirst().getDataOrder()).isNotNull();

        //Assert for each element contains on response list
        assertThat(ordersResponse)
                .extracting(Order::getName, Order::getCognome, Order::getEmail, Order::getPrice, Order::getDataOrder)
                .contains(tuple(
                        order1.getName(),
                        order1.getCognome(),
                        order1.getEmail(),
                        order1.getPrice(),
                        order1.getDataOrder()
                ));

        //Assert for each element contains on response list
        assertThat(ordersResponse)
                .extracting(Order::getName, Order::getCognome, Order::getEmail, Order::getPrice, Order::getDataOrder)
                .contains(tuple(
                        order2.getName(),
                        order2.getCognome(),
                        order2.getEmail(),
                        order2.getPrice(),
                        order2.getDataOrder()
                ));

        //Assert for each element lambda func for order1
        assertTrue(ordersResponse.stream().anyMatch(order1Base -> order1Base.getName().equals(order1.getName())));
        assertTrue(ordersResponse.stream().anyMatch(order1Base -> order1Base.getCognome().equals(order1.getCognome())));
        assertTrue(ordersResponse.stream().anyMatch(order1Base -> order1Base.getDataOrder().equals(order1.getDataOrder())));
        assertTrue(ordersResponse.stream().anyMatch(order1Base -> order1Base.getEmail().equals(order1.getEmail())));
        assertTrue(ordersResponse.stream().anyMatch(order1Base -> order1Base.getPrice().equals(order1.getPrice())));

        //Assert for each element lambda func for order2
        assertTrue(ordersResponse.stream().anyMatch(order2Base -> order2Base.getName().equals(order2.getName())));
        assertTrue(ordersResponse.stream().anyMatch(order2Base -> order2Base.getCognome().equals(order2.getCognome())));
        assertTrue(ordersResponse.stream().anyMatch(order2Base -> order2Base.getDataOrder().equals(order2.getDataOrder())));
        assertTrue(ordersResponse.stream().anyMatch(order2Base -> order2Base.getEmail().equals(order2.getEmail())));
        assertTrue(ordersResponse.stream().anyMatch(order2Base -> order2Base.getPrice().equals(order2.getPrice())));

    }

    @Test
    void testGetCustomerOrdersSummary() {

        Order requestPayload1 = new Order(
                "Marco",
                "Bianchi",
                "m.bianchi@gmail.com",
                LocalDate.parse("2026-08-20"),
                10.50
        );

        Order requestPayload2 = new Order(
                "Marco",
                "Bianchi",
                "m.bianchi@gmail.com",
                LocalDate.parse("2026-08-21"),
                10.50
        );

        Order order1 = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + userToken)
                .body(requestPayload1)
                .post("/orders")
                .then()
                .statusCode(200)
                .log().headers()
                .extract()
                .as(Order.class);

        assertThat(order1.getDataOrder()).isNotNull();

        Order order2 = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + userToken)
                .body(requestPayload2)
                .post("/orders")
                .then()
                .statusCode(200)
                .log().headers()
                .extract()
                .as(Order.class);

        assertThat(order2.getDataOrder()).isNotNull();

        Response response = given()
                .contentType(ContentType.JSON)
                .header("Authorization", "Bearer " + userToken)
                .pathParam("userName", "mbianchi" )
                .queryParam("data-inizio", "2026-08-15")
                .queryParam("data-fine", "2026-08-30")
                .when()
                .get("/customers/{userName}/orders/summary")
                .then()
                .log().ifValidationFails() // Logga solo se il test fallisce
                .statusCode(200)
                .extract()
                .response();

        PriceSummary ordersResponse = response.as(new TypeRef<PriceSummary>() {});

        //Assert for each element contains on response list
        assertThat(ordersResponse)
                .extracting(
                        PriceSummary::getCustomer,
                        PriceSummary::getTotal
                )
                .contains(
                        "Bianchi Marco",
                        21.00
                );

        assertThat(ordersResponse.getCustomer()).isEqualTo(order1.getCognome()+" "+order1.getName());
        assertThat(ordersResponse.getCustomer()).isEqualTo(order2.getCognome()+" "+order2.getName());
        assertThat(ordersResponse.getTotal()).isEqualTo(order1.getPrice() + order2.getPrice());

    }
}
