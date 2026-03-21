package com.framework.utils.api;

import com.framework.config.ConfigManager;
import io.qameta.allure.Step;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;

/**
 * RestAssured-based API utility for UI test frameworks.
 * Common patterns:
 *  - Create test data via API before UI tests (test setup)
 *  - Validate backend state after UI actions (dual assertion)
 *  - Bypass UI for non-SUT flows (login, test teardown)
 *
 * FAANG pattern: API-first for setup/teardown; UI only for what users see.
 */
public final class ApiUtils {

    private static final Logger log = LogManager.getLogger(ApiUtils.class);

    private ApiUtils() {}

    // ── Request Spec Builder ─────────────────────────────────────────────

    public static RequestSpecification baseSpec() {
        return RestAssured.given()
            .baseUri(ConfigManager.get().apiBaseUrl())
            .contentType(ContentType.JSON)
            .accept(ContentType.JSON)
            .log().ifValidationFails();
    }

    public static RequestSpecification authenticatedSpec(String token) {
        return baseSpec().header("Authorization", "Bearer " + token);
    }

    public static RequestSpecification authenticatedSpec(String username, String password) {
        return baseSpec().auth().preemptive().basic(username, password);
    }

    // ── HTTP Methods ─────────────────────────────────────────────────────

    @Step("GET {endpoint}")
    public static Response get(String endpoint) {
        log.info("GET {}", endpoint);
        return baseSpec().get(endpoint);
    }

    @Step("GET {endpoint} with auth")
    public static Response getAuthenticated(String endpoint, String token) {
        log.info("GET (auth) {}", endpoint);
        return authenticatedSpec(token).get(endpoint);
    }

    @Step("POST {endpoint}")
    public static Response post(String endpoint, Object body) {
        log.info("POST {}", endpoint);
        return baseSpec().body(body).post(endpoint);
    }

    @Step("POST {endpoint} with auth")
    public static Response postAuthenticated(String endpoint, Object body, String token) {
        return authenticatedSpec(token).body(body).post(endpoint);
    }

    @Step("PUT {endpoint}")
    public static Response put(String endpoint, Object body) {
        log.info("PUT {}", endpoint);
        return baseSpec().body(body).put(endpoint);
    }

    @Step("PATCH {endpoint}")
    public static Response patch(String endpoint, Object body) {
        return baseSpec().body(body).patch(endpoint);
    }

    @Step("DELETE {endpoint}")
    public static Response delete(String endpoint) {
        log.info("DELETE {}", endpoint);
        return baseSpec().delete(endpoint);
    }

    // ── GET with query params ─────────────────────────────────────────────

    @Step("GET {endpoint} with params")
    public static Response getWithParams(String endpoint, Map<String, ?> params) {
        return baseSpec().queryParams(params).get(endpoint);
    }

    // ── Response Helpers ─────────────────────────────────────────────────

    public static <T> T extractBody(Response response, Class<T> clazz) {
        return response.as(clazz);
    }

    public static String extractField(Response response, String jsonPath) {
        return response.jsonPath().getString(jsonPath);
    }

    public static int getStatusCode(Response response) {
        return response.getStatusCode();
    }

    public static void assertStatusCode(Response response, int expectedCode) {
        int actual = response.getStatusCode();
        if (actual != expectedCode) {
            throw new AssertionError(
                "Expected status " + expectedCode + " but got " + actual + ". Body: " + response.getBody().asString()
            );
        }
        log.info("Status code verified: {}", expectedCode);
    }

    // ── Auth Token Helper ─────────────────────────────────────────────────

    /**
     * Obtain a Bearer token via API login — bypasses UI login.
     * FAANG pattern: UI tests should not test the login flow unless specifically required.
     */
    @Step("API Login — obtain auth token")
    public static String getAuthToken(String loginEndpoint, String username, String password) {
        Response response = baseSpec()
            .body(Map.of("username", username, "password", password))
            .post(loginEndpoint);
        assertStatusCode(response, 200);
        String token = extractField(response, "token");
        log.info("Auth token obtained for user: {}", username);
        return token;
    }
}
