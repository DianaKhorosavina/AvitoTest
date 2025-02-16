import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.UUID;
import java.util.stream.Stream;

import static API.ConstantsForTests.*;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.*;

public class ApiTest {

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = BASE_URI;
    }

    @Test
    public void creatingAnAdPositiveTest() {
        String requestBody = """
                {
                  "sellerID": 1234349231,
                  "name": "Велосипед",
                  "price": 1000,
                  "statistics": {
                    "contacts": 2,
                    "likes": 68,
                    "viewCount": 13
                  }
                }
                """;

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post("/api/1/item")
                .then()
                .statusCode(200)
                .log().body()
                .body("status", matchesPattern("Сохранили объявление - [a-f0-9\\-]+"))
                .extract().response();

    } //баг, имя товара и его id меняются местами

    @ParameterizedTest
    @MethodSource("provideNullRequests")
    public void creatingAnAdWithEmptyFields(Integer sellerID, String name,
                                            Integer price) {

        String requestBody = String.format("""
                        {
                          "sellerID": %s,
                          "name": "%s",
                          "price": %s,
                          "statistics": {
                            "contacts": 3,
                            "likes": 120,
                            "viewCount": 7
                          }
                        }
                        """,
                sellerID == null ? "null" : sellerID,
                name == null ? "null" : name,
                price == null ? "null" : price
        );

        given()
                .contentType(ContentType.JSON)
                .body(requestBody)
                .log().body()
                .when()
                .post("/api/1/item")
                .then()
                .statusCode(400)
                .log().body()
                .extract()
                .response();
        //баг - поля явно обязательные, сервер возвращает 200
    }

    @Test
    public void getPromoById() {
        given()
                .pathParam("id", PROMO_ID)
                .when()
                .get("/api/1/item/{id}")
                .then()
                .statusCode(200)
                .log().body()
                .body("[0].id", equalTo(PROMO_ID))
                .extract()
                .response();
    }

    @Test
    public void getPromoByNonExistentId() {
        String randomUUID = UUID.randomUUID().toString();
        given()
                .pathParam("id", randomUUID)
                .when()
                .get("/api/1/item/{id}")
                .then()
                .statusCode(404)
                .log().body()
                .extract()
                .response(); // баг в ответе, сообщение: null
    }

    @Test
    public void getPromoByInvalidIdForm() {
        given()
                .pathParam("id", INVALID_ID_FORM)
                .when()
                .get("/api/1/item/{id}")
                .then()
                .statusCode(400)
                .log().body()
                .extract()
                .response();
    }

    @Test
    public void getPromosBySellerID() {
        given()
                .pathParam("sellerID", SELLER_ID)
                .when()
                .get("/api/1/{sellerID}/item")
                .then()
                .statusCode(200)
                .log().body()
                .body("[0].sellerId", equalTo(SELLER_ID))
                .extract()
                .response();
    }

    @Test
    public void getPromosByInvalidID() {
        given()
                .pathParam("sellerID", "idhere")
                .when()
                .get("/api/1/{sellerID}/item")
                .then()
                .statusCode(400)
                .log().body()
                .extract()
                .response();
    }

    @Test
    public void getStatisticsByPromoID() {
        given()
                .pathParam("id", PROMO_ID)
                .when()
                .get("/api/1/statistic/{id}")
                .then()
                .statusCode(200)
                .log().body()
                .extract()
                .response();
    }

    @Test
    public void getStatisticsByNonExistentId() {
        String randomUUID = UUID.randomUUID().toString();
        given()
                .pathParam("id", randomUUID)
                .when()
                .get("/api/1/statistic/{id}")
                .then()
                .statusCode(404)
                .log().body()
                .extract()
                .response();
        // баг в сообщении null
    }

    @Test
    public void getStatisticsByInvalidId() {
        given()
                .pathParam("id", INVALID_ID_FORM)
                .when()
                .get("/api/1/statistic/{id}")
                .then()
                .statusCode(400)
                .log().body()
                .extract()
                .response();
    }

    static Stream<Arguments> provideNullRequests() {
        return Stream.of(
                Arguments.of(null, "Велосипед", 1000, 2, 68, 13),
                Arguments.of(1234349231, null, 1000, 2, 68, 13),
                Arguments.of(1234349231, "Велосипед", null, 2, 68, 13)
        );
    }

}
