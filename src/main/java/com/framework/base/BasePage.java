package com.framework.base;

import com.framework.config.ConfigManager;
import com.framework.driver.DriverManager;
import com.framework.utils.wait.WaitUtils;
import com.framework.utils.screenshot.ScreenshotUtils;
import io.qameta.allure.Step;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.Select;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Base class for all Page Objects.
 * Provides reusable, stabilised UI interaction methods.
 * All interactions are logged and Allure-annotated.
 */
public abstract class BasePage {

    protected final Logger log = LogManager.getLogger(this.getClass());
    protected WebDriver driver;
    protected WaitUtils wait;
    protected Actions actions;

    protected BasePage() {
        this.driver  = DriverManager.getDriver();
        this.wait    = new WaitUtils(driver);
        this.actions = new Actions(driver);
        PageFactory.initElements(driver, this);
    }

    // ── Navigation ───────────────────────────────────────────────────────

    @Step("Navigate to URL: {url}")
    public void navigateTo(String url) {
        log.info("Navigating to: {}", url);
        driver.get(url);
    }

    public void navigateToBaseUrl() {
        navigateTo(ConfigManager.get().baseUrl());
    }

    public String getCurrentUrl() {
        return driver.getCurrentUrl();
    }

    public String getPageTitle() {
        return driver.getTitle();
    }

    // ── Element Interactions ─────────────────────────────────────────────

    @Step("Click element: {locator}")
    public void click(By locator) {
        log.debug("Clicking element: {}", locator);
        wait.waitForElementClickable(locator).click();
    }

    @Step("Click element (JS fallback): {locator}")
    public void clickWithJS(By locator) {
        log.debug("JS clicking element: {}", locator);
        WebElement element = wait.waitForElementVisible(locator);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", element);
    }

    @Step("Type '{text}' into: {locator}")
    public void type(By locator, String text) {
        log.debug("Typing '{}' into: {}", text, locator);
        WebElement element = wait.waitForElementVisible(locator);
        element.clear();
        element.sendKeys(text);
    }

    @Step("Clear and type '{text}' into: {locator}")
    public void clearAndType(By locator, String text) {
        WebElement element = wait.waitForElementVisible(locator);
        element.sendKeys(Keys.CONTROL + "a");
        element.sendKeys(Keys.DELETE);
        element.sendKeys(text);
    }

    @Step("Get text from: {locator}")
    public String getText(By locator) {
        return wait.waitForElementVisible(locator).getText().trim();
    }

    @Step("Get attribute '{attribute}' from: {locator}")
    public String getAttribute(By locator, String attribute) {
        return wait.waitForElementVisible(locator).getAttribute(attribute);
    }

    @Step("Select dropdown by visible text '{text}': {locator}")
    public void selectByVisibleText(By locator, String text) {
        new Select(wait.waitForElementVisible(locator)).selectByVisibleText(text);
    }

    @Step("Select dropdown by value '{value}': {locator}")
    public void selectByValue(By locator, String value) {
        new Select(wait.waitForElementVisible(locator)).selectByValue(value);
    }

    @Step("Hover over element: {locator}")
    public void hover(By locator) {
        actions.moveToElement(wait.waitForElementVisible(locator)).perform();
    }

    @Step("Double click: {locator}")
    public void doubleClick(By locator) {
        actions.doubleClick(wait.waitForElementVisible(locator)).perform();
    }

    @Step("Right click: {locator}")
    public void rightClick(By locator) {
        actions.contextClick(wait.waitForElementVisible(locator)).perform();
    }

    @Step("Drag '{source}' to '{target}'")
    public void dragAndDrop(By source, By target) {
        actions.dragAndDrop(
            wait.waitForElementVisible(source),
            wait.waitForElementVisible(target)
        ).perform();
    }

    // ── State Checks ─────────────────────────────────────────────────────

    public boolean isDisplayed(By locator) {
        try {
            return driver.findElement(locator).isDisplayed();
        } catch (NoSuchElementException | StaleElementReferenceException e) {
            return false;
        }
    }

    public boolean isEnabled(By locator) {
        try {
            return driver.findElement(locator).isEnabled();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public boolean isSelected(By locator) {
        try {
            return driver.findElement(locator).isSelected();
        } catch (NoSuchElementException e) {
            return false;
        }
    }

    public boolean isElementPresent(By locator) {
        return !driver.findElements(locator).isEmpty();
    }

    // ── Lists ────────────────────────────────────────────────────────────

    public List<String> getTextList(By locator) {
        return driver.findElements(locator)
            .stream()
            .map(el -> el.getText().trim())
            .collect(Collectors.toList());
    }

    public int getElementCount(By locator) {
        return driver.findElements(locator).size();
    }

    // ── Scroll ───────────────────────────────────────────────────────────

    @Step("Scroll to element: {locator}")
    public void scrollToElement(By locator) {
        WebElement element = driver.findElement(locator);
        ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView({behavior:'smooth', block:'center'});", element);
    }

    public void scrollToTop() {
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, 0);");
    }

    public void scrollToBottom() {
        ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");
    }

    // ── Alerts ───────────────────────────────────────────────────────────

    @Step("Accept alert")
    public String acceptAlert() {
        Alert alert = wait.waitForAlert();
        String message = alert.getText();
        alert.accept();
        return message;
    }

    @Step("Dismiss alert")
    public void dismissAlert() {
        wait.waitForAlert().dismiss();
    }

    // ── Frames ───────────────────────────────────────────────────────────

    public void switchToFrame(By locator) {
        driver.switchTo().frame(wait.waitForElementVisible(locator));
    }

    public void switchToDefaultContent() {
        driver.switchTo().defaultContent();
    }

    // ── Windows ──────────────────────────────────────────────────────────

    public void switchToNewTab() {
        String currentHandle = driver.getWindowHandle();
        driver.getWindowHandles().stream()
            .filter(h -> !h.equals(currentHandle))
            .findFirst()
            .ifPresent(h -> driver.switchTo().window(h));
    }

    public void closeCurrentTab() {
        driver.close();
    }

    // ── JS Utilities ─────────────────────────────────────────────────────

    public Object executeJS(String script, Object... args) {
        return ((JavascriptExecutor) driver).executeScript(script, args);
    }

    public void highlightElement(By locator) {
        WebElement element = driver.findElement(locator);
        ((JavascriptExecutor) driver).executeScript(
            "arguments[0].style.border='3px solid red'", element
        );
    }

    // ── Abstract ─────────────────────────────────────────────────────────

    /**
     * Each page must verify it is correctly loaded.
     * Called after navigation in hooks for page validation.
     */
    public abstract boolean isPageLoaded();
}
