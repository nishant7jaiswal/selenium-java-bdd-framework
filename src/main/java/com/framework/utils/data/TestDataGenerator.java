package com.framework.utils.data;

import com.github.javafaker.Faker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Dynamic test data generator using JavaFaker.
 * Generates realistic, locale-aware test data at runtime.
 * FAANG pattern: never hardcode test data — generate it fresh each run.
 */
public final class TestDataGenerator {

    private static final Logger log = LogManager.getLogger(TestDataGenerator.class);
    private static final Faker faker = new Faker(new Locale("en-IN"));

    private TestDataGenerator() {}

    // ── Identity ─────────────────────────────────────────────────────────

    public static String firstName()     { return faker.name().firstName(); }
    public static String lastName()      { return faker.name().lastName(); }
    public static String fullName()      { return faker.name().fullName(); }
    public static String username()      { return faker.name().username() + randomInt(100, 999); }

    public static String email() {
        return faker.internet().emailAddress()
            .replace(" ", "")
            .toLowerCase();
    }

    public static String uniqueEmail() {
        return "test_" + UUID.randomUUID().toString().substring(0, 8) + "@qa.automation.com";
    }

    public static String password() {
        return "Test@" + randomInt(1000, 9999) + faker.letterify("??");
    }

    // ── Phone / Address ──────────────────────────────────────────────────

    public static String phoneNumber()   { return faker.phoneNumber().phoneNumber(); }
    public static String streetAddress() { return faker.address().streetAddress(); }
    public static String city()          { return faker.address().city(); }
    public static String state()         { return faker.address().state(); }
    public static String zipCode()       { return faker.address().zipCode(); }
    public static String country()       { return faker.address().country(); }

    // ── Company / Finance ────────────────────────────────────────────────

    public static String companyName()   { return faker.company().name(); }
    public static String creditCard()    { return faker.finance().creditCard(); }
    public static String iban()          { return faker.finance().iban(); }

    // ── Text / Content ───────────────────────────────────────────────────

    public static String sentence()      { return faker.lorem().sentence(); }
    public static String paragraph()     { return faker.lorem().paragraph(); }
    public static String word()          { return faker.lorem().word(); }

    public static String alphanumeric(int length) {
        return faker.bothify("?".repeat(length)).replaceAll("[^a-zA-Z0-9]", "X");
    }

    // ── Numbers / Dates ──────────────────────────────────────────────────

    public static int randomInt(int min, int max) {
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }

    public static double randomDouble(double min, double max) {
        return Math.round(ThreadLocalRandom.current().nextDouble(min, max) * 100.0) / 100.0;
    }

    public static String futureDate(int daysAhead, String format) {
        return LocalDate.now().plusDays(daysAhead).format(DateTimeFormatter.ofPattern(format));
    }

    public static String pastDate(int daysBack, String format) {
        return LocalDate.now().minusDays(daysBack).format(DateTimeFormatter.ofPattern(format));
    }

    public static String today(String format) {
        return LocalDate.now().format(DateTimeFormatter.ofPattern(format));
    }

    // ── Unique IDs ───────────────────────────────────────────────────────

    public static String uuid()          { return UUID.randomUUID().toString(); }
    public static String shortUuid()     { return UUID.randomUUID().toString().substring(0, 8).toUpperCase(); }
    public static String timestamp()     { return String.valueOf(System.currentTimeMillis()); }

    // ── Product / Commerce ───────────────────────────────────────────────

    public static String productName()   { return faker.commerce().productName(); }
    public static String department()    { return faker.commerce().department(); }
    public static String price()         { return faker.commerce().price(); }

    // ── Internet ─────────────────────────────────────────────────────────

    public static String url()           { return faker.internet().url(); }
    public static String ipAddress()     { return faker.internet().ipV4Address(); }
    public static String userAgent()     { return faker.internet().userAgentAny(); }
}
