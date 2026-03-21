package com.framework.utils.wait;

import com.framework.config.ConfigManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.awaitility.Awaitility;
import org.awaitility.core.ConditionTimeoutException;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * Centralised wait utility.
 * Combines Selenium FluentWait + Awaitility for comprehensive async handling.
 * FAANG pattern: no hardcoded Thread.sleep() anywhere in the framework.
 */
public class WaitUtils {

    private static final Logger log = LogManager.getLogger(WaitUtils.class);
    private final WebDriver driver;
    private final int timeoutSeconds;
    private final int pollingMillis;

    public WaitUtils(WebDriver driver) {
        this.driver         = driver;
        this.timeoutSeconds = ConfigManager.get().explicitWait();
        this.pollingMillis  = ConfigManager.get().pollingInterval();
    }

    // ── FluentWait Factory ───────────────────────────────────────────────

    private FluentWait<WebDriver> fluentWait(int seconds) {
        return new FluentWait<>(driver)
            .withTimeout(Duration.ofSeconds(seconds))
            .pollingEvery(Duration.ofMillis(pollingMillis))
            .ignoring(NoSuchElementException.class)
            .ignoring(StaleElementReferenceException.class)
            .ignoring(ElementClickInterceptedException.class);
    }

    private FluentWait<WebDriver> fluentWait() {
        return fluentWait(timeoutSeconds);
    }

    // ── Element Waits ────────────────────────────────────────────────────

    public WebElement waitForElementVisible(By locator) {
        log.debug("Waiting for element visible: {}", locator);
        return fluentWait().until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public WebElement waitForElementVisible(By locator, int seconds) {
        return fluentWait(seconds).until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public WebElement waitForElementClickable(By locator) {
        log.debug("Waiting for element clickable: {}", locator);
        return fluentWait().until(ExpectedConditions.elementToBeClickable(locator));
    }

    public WebElement waitForElementPresent(By locator) {
        return fluentWait().until(ExpectedConditions.presenceOfElementLocated(locator));
    }

    public List<WebElement> waitForAllElementsVisible(By locator) {
        return fluentWait().until(ExpectedConditions.visibilityOfAllElementsLocatedBy(locator));
    }

    public boolean waitForElementInvisible(By locator) {
        return fluentWait().until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    public boolean waitForElementInvisible(By locator, int seconds) {
        return fluentWait(seconds).until(ExpectedConditions.invisibilityOfElementLocated(locator));
    }

    public boolean waitForTextPresent(By locator, String text) {
        return fluentWait().until(ExpectedConditions.textToBePresentInElementLocated(locator, text));
    }

    public boolean waitForAttributeContains(By locator, String attribute, String value) {
        return fluentWait().until(ExpectedConditions.attributeContains(locator, attribute, value));
    }

    // ── Page / URL Waits ─────────────────────────────────────────────────

    public boolean waitForUrlContains(String urlFragment) {
        return fluentWait().until(ExpectedConditions.urlContains(urlFragment));
    }

    public boolean waitForUrlMatches(String regex) {
        return fluentWait().until(ExpectedConditions.urlMatches(regex));
    }

    public boolean waitForTitleContains(String title) {
        return fluentWait().until(ExpectedConditions.titleContains(title));
    }

    public boolean waitForPageLoad() {
        return fluentWait().until(driver ->
            ((JavascriptExecutor) driver)
                .executeScript("return document.readyState")
                .equals("complete")
        );
    }

    // ── Alert Waits ──────────────────────────────────────────────────────

    public Alert waitForAlert() {
        return fluentWait().until(ExpectedConditions.alertIsPresent());
    }

    // ── Frame Waits ──────────────────────────────────────────────────────

    public WebDriver waitForFrameAndSwitch(By locator) {
        return fluentWait().until(ExpectedConditions.frameToBeAvailableAndSwitchToIt(locator));
    }

    // ── Custom / Awaitility ──────────────────────────────────────────────

    /**
     * Wait for any arbitrary condition using Awaitility.
     * Ideal for non-Selenium conditions (API state, DB records, file system).
     *
     * Example:
     *   waitUntil(() -> apiClient.getOrderStatus().equals("COMPLETED"), 30, "Order status COMPLETED");
     */
    public void waitUntil(Callable<Boolean> condition, int timeoutSeconds, String conditionDescription) {
        log.debug("Waiting for condition: {}", conditionDescription);
        try {
            Awaitility.await()
                .alias(conditionDescription)
                .atMost(timeoutSeconds, TimeUnit.SECONDS)
                .pollInterval(pollingMillis, TimeUnit.MILLISECONDS)
                .until(condition);
        } catch (ConditionTimeoutException e) {
            throw new TimeoutException("Condition not met within " + timeoutSeconds + "s: " + conditionDescription, e);
        }
    }

    /**
     * Wait for a value to be returned (not just boolean true).
     * Useful for waiting until an element has specific text / attribute.
     */
    public <T> T waitForValue(Function<WebDriver, T> condition, int timeoutSeconds) {
        return fluentWait(timeoutSeconds).until(condition);
    }

    /**
     * Hard sleep — use ONLY when dealing with third-party animations
     * that have no detectable DOM change. Requires a comment explaining why.
     */
    public static void hardWait(long millis) {
        log.warn("Hard wait of {}ms — ensure this is necessary", millis);
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    // ── Spinner / Loader ─────────────────────────────────────────────────

    /**
     * Wait for a loading spinner to disappear before proceeding.
     * Pass the spinner locator; default timeout is 30s.
     */
    public void waitForSpinnerToDisappear(By spinnerLocator) {
        try {
            waitForElementInvisible(spinnerLocator, 30);
            log.debug("Spinner disappeared: {}", spinnerLocator);
        } catch (Exception e) {
            log.debug("Spinner not found or already gone: {}", spinnerLocator);
        }
    }
}
