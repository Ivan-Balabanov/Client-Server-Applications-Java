package practice5;

import io.restassured.RestAssured;
import org.junit.jupiter.api.*;

import static io.restassured.RestAssured.*;
import static org.hamcrest.Matchers.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class StoreHttpServerTest {

    private static StoreHttpServer server;
    private static String token;
    private static int createdProductId;

    @BeforeAll
    static void startServer() throws Exception {
        server = new StoreHttpServer(8182);
        server.start();
        RestAssured.baseURI = "http://localhost";
        RestAssured.port = 8182;
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @Test
    @Order(1)
    void loginSuccess() {
        token = given()
                .contentType("application/json")
                .body("{\"login\":\"admin\",\"password\":\"password123\"}")
                .when()
                .post("/login")
                .then()
                .statusCode(200)
                .body("token", notNullValue())
                .extract().path("token");
    }

    @Test
    @Order(2)
    void loginFailure() {
        given()
                .contentType("application/json")
                .body("{\"login\":\"admin\",\"password\":\"wrong\"}")
                .when()
                .post("/login")
                .then()
                .statusCode(401);
    }

    @Test
    @Order(3)
    void accessWithoutTokenIsRejected() {
        given()
                .when()
                .get("/products/1")
                .then()
                .statusCode(401);
    }

    @Test
    @Order(4)
    void createProduct() {
        createdProductId = given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body("{\"name\":\"buckwheat\",\"price\":25.5,\"quantity\":100}")
                .when()
                .put("/products")
                .then()
                .statusCode(201)
                .body("name",     equalTo("buckwheat"))
                .body("price",    equalTo(25.5f))
                .body("quantity", equalTo(100))
                .extract().path("id");
    }

    @Test
    @Order(5)
    void createDuplicateProductFails() {
        given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body("{\"name\":\"buckwheat\",\"price\":10.0,\"quantity\":50}")
                .when()
                .put("/products")
                .then()
                .statusCode(409);
    }

    @Test
    @Order(6)
    void getProduct() {
        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/products/" + createdProductId)
                .then()
                .statusCode(200)
                .body("name", equalTo("buckwheat"));
    }

    @Test
    @Order(7)
    void getProductNotFound() {
        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/products/99999")
                .then()
                .statusCode(404);
    }

    @Test
    @Order(8)
    void updateProduct() {
        given()
                .header("Authorization", "Bearer " + token)
                .contentType("application/json")
                .body("{\"name\":\"buckwheat\",\"price\":30.0,\"quantity\":200}")
                .when()
                .post("/products/" + createdProductId)
                .then()
                .statusCode(200)
                .body("price",    equalTo(30.0f))
                .body("quantity", equalTo(200));
    }

    @Test
    @Order(9)
    void deleteProduct() {
        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .delete("/products/" + createdProductId)
                .then()
                .statusCode(200)
                .body("ok", equalTo(true));
    }

    @Test
    @Order(10)
    void getDeletedProductReturns404() {
        given()
                .header("Authorization", "Bearer " + token)
                .when()
                .get("/products/" + createdProductId)
                .then()
                .statusCode(404);
    }
}