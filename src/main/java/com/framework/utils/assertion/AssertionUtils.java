package com.framework.utils.assertion;

import io.qameta.allure.Step;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.assertj.core.api.SoftAssertions;
import org.testng.Assert;

import java.util.List;

/**
 * Centralised assertion utility combining TestNG hard assertions
 * and AssertJ soft assertions, all Allure-annotated.
 *
 * FAANG pattern: use soft assertions for UI validation steps,
 * hard assertions for critical control-flow gates.
 */
public final class AssertionUtils {

    private static final Logger log = LogManager.getLogger(AssertionUtils.class);

    private AssertionUtils() {}

    // ── Hard Assertions ──────────────────────────────────────────────────

    @Step("Assert equals — Expected: '{expected}' | Actual: '{actual}'")
    public static void assertEquals(Object actual, Object expected, String message) {
        log.info("Asserting equals — Expected: [{}] | Actual: [{}]", expected, actual);
        Assert.assertEquals(actual, expected, message);
    }

    @Step("Assert not equals — Actual: '{actual}' should not equal '{unexpected}'")
    public static void assertNotEquals(Object actual, Object unexpected, String message) {
        Assert.assertNotEquals(actual, unexpected, message);
    }

    @Step("Assert true — {message}")
    public static void assertTrue(boolean condition, String message) {
        log.info("Asserting true: {}", message);
        Assert.assertTrue(condition, message);
    }

    @Step("Assert false — {message}")
    public static void assertFalse(boolean condition, String message) {
        Assert.assertFalse(condition, message);
    }

    @Step("Assert not null — {message}")
    public static void assertNotNull(Object object, String message) {
        Assert.assertNotNull(object, message);
    }

    @Step("Assert null — {message}")
    public static void assertNull(Object object, String message) {
        Assert.assertNull(object, message);
    }

    @Step("Assert contains — '{text}' should contain '{substring}'")
    public static void assertContains(String text, String substring, String message) {
        log.info("Asserting [{}] contains [{}]", text, substring);
        Assert.assertTrue(text != null && text.contains(substring),
            message + " | Expected to contain: [" + substring + "] | Actual: [" + text + "]");
    }

    @Step("Assert list size — Expected: {expectedSize}")
    public static void assertListSize(List<?> list, int expectedSize, String message) {
        Assert.assertEquals(list.size(), expectedSize,
            message + " | Expected size: " + expectedSize + " | Actual: " + list.size());
    }

    @Step("Assert list not empty — {message}")
    public static void assertListNotEmpty(List<?> list, String message) {
        Assert.assertFalse(list == null || list.isEmpty(), message);
    }

    // ── Soft Assertions ──────────────────────────────────────────────────

    /**
     * Execute a block of soft assertions.
     * All assertions run even if some fail; failures reported together.
     *
     * Usage:
     *   AssertionUtils.softAssert(soft -> {
     *       soft.assertThat(page.getTitle()).isEqualTo("Dashboard");
     *       soft.assertThat(page.getUserName()).contains("Nishant");
     *   });
     */
    @Step("Soft assertion block")
    public static void softAssert(SoftAssertionBlock block) {
        SoftAssertions soft = new SoftAssertions();
        block.execute(soft);
        soft.assertAll();
    }

    @FunctionalInterface
    public interface SoftAssertionBlock {
        void execute(SoftAssertions soft);
    }

    // ── URL / Page Assertions ─────────────────────────────────────────────

    @Step("Assert URL contains: '{expectedFragment}'")
    public static void assertUrlContains(String actualUrl, String expectedFragment) {
        log.info("Asserting URL [{}] contains [{}]", actualUrl, expectedFragment);
        Assert.assertTrue(actualUrl.contains(expectedFragment),
            "URL should contain [" + expectedFragment + "] but was [" + actualUrl + "]");
    }

    @Step("Assert page title: '{expectedTitle}'")
    public static void assertPageTitle(String actualTitle, String expectedTitle) {
        assertEquals(actualTitle, expectedTitle, "Page title mismatch");
    }

    // ── String Assertions ─────────────────────────────────────────────────

    @Step("Assert not blank — {fieldName}")
    public static void assertNotBlank(String value, String fieldName) {
        Assert.assertFalse(value == null || value.isBlank(),
            fieldName + " should not be blank but was: [" + value + "]");
    }

    @Step("Assert matches regex — pattern: {pattern}")
    public static void assertMatchesRegex(String value, String pattern, String message) {
        Assert.assertTrue(value != null && value.matches(pattern),
            message + " | Pattern: [" + pattern + "] | Value: [" + value + "]");
    }
}
