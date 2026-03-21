package com.framework.utils;

import com.framework.config.ConfigManager;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Pre-suite environment health check.
 * Validates the target application is reachable before tests begin.
 *
 * FAANG pattern: fail fast if the environment is down,
 * rather than running 500 tests and getting 500 meaningless failures.
 *
 * Usage in @BeforeSuite or Cucumber @BeforeAll hook:
 *   EnvironmentValidator.validate();
 */
public final class EnvironmentValidator {

    private static final Logger log = LogManager.getLogger(EnvironmentValidator.class);
    private static final int TIMEOUT_MS = 10_000;

    private EnvironmentValidator() {}

    /**
     * Validate the target environment is reachable.
     * Throws RuntimeException if the URL is unreachable — suite will abort.
     */
    public static void validate() {
        String baseUrl = ConfigManager.get().baseUrl();
        log.info("Validating environment: {}", baseUrl);

        try {
            HttpURLConnection connection = (HttpURLConnection) new URL(baseUrl).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(TIMEOUT_MS);
            connection.setReadTimeout(TIMEOUT_MS);

            int responseCode = connection.getResponseCode();
            if (responseCode >= 200 && responseCode < 400) {
                log.info("✅ Environment reachable: {} → HTTP {}", baseUrl, responseCode);
            } else {
                throw new RuntimeException(
                    "Environment returned unexpected status: HTTP " + responseCode + " for " + baseUrl
                );
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(
                "Environment unreachable: " + baseUrl + " | Error: " + e.getMessage(), e
            );
        }
    }

    /**
     * Validate API base URL is reachable.
     */
    public static void validateApi() {
        String apiUrl = ConfigManager.get().apiBaseUrl();
        if (apiUrl == null || apiUrl.isBlank()) {
            log.warn("No API base URL configured — skipping API health check");
            return;
        }

        try {
            Response response = RestAssured.given()
                .baseUri(apiUrl)
                .get("/health")
                .andReturn();

            if (response.statusCode() < 400) {
                log.info("✅ API healthy: {} → HTTP {}", apiUrl, response.statusCode());
            } else {
                log.warn("⚠️  API health check returned HTTP {} for {}", response.statusCode(), apiUrl);
            }
        } catch (Exception e) {
            log.warn("⚠️  API health check failed: {} | {}", apiUrl, e.getMessage());
        }
    }

    /**
     * Validate all configured environments and log a summary.
     */
    public static void validateAll() {
        log.info("Running pre-suite environment validation...");
        validate();
        validateApi();
        log.info("Environment validation complete");
    }
}
