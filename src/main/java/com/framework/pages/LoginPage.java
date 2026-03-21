package com.framework.pages;

import com.framework.base.BasePage;
import io.qameta.allure.Step;
import org.openqa.selenium.By;

/**
 * Page Object for the Login page.
 * Contains locators and actions specific to login flows.
 */
public class LoginPage extends BasePage {

    // ── Locators ─────────────────────────────────────────────────────────
    private final By usernameField    = By.id("username");
    private final By passwordField    = By.id("password");
    private final By loginButton      = By.id("login-btn");
    private final By errorMessage     = By.cssSelector(".error-message");
    private final By forgotPasswordLink = By.linkText("Forgot Password?");
    private final By rememberMeCheckbox = By.id("remember-me");
    private final By pageHeading      = By.cssSelector("h1.login-heading");

    // ── Actions ──────────────────────────────────────────────────────────

    @Step("Enter username: {username}")
    public LoginPage enterUsername(String username) {
        type(usernameField, username);
        return this;
    }

    @Step("Enter password")
    public LoginPage enterPassword(String password) {
        type(passwordField, password);
        return this;
    }

    @Step("Click login button")
    public DashboardPage clickLogin() {
        click(loginButton);
        return new DashboardPage();
    }

    @Step("Login with credentials — user: {username}")
    public DashboardPage loginAs(String username, String password) {
        log.info("Logging in as: {}", username);
        enterUsername(username);
        enterPassword(password);
        return clickLogin();
    }

    @Step("Login and expect failure — user: {username}")
    public LoginPage loginExpectingFailure(String username, String password) {
        enterUsername(username);
        enterPassword(password);
        click(loginButton);
        return this;
    }

    @Step("Click Forgot Password")
    public LoginPage clickForgotPassword() {
        click(forgotPasswordLink);
        return this;
    }

    @Step("Check Remember Me")
    public LoginPage checkRememberMe() {
        if (!isSelected(rememberMeCheckbox)) {
            click(rememberMeCheckbox);
        }
        return this;
    }

    // ── Getters ──────────────────────────────────────────────────────────

    public String getErrorMessage() {
        return getText(errorMessage);
    }

    public boolean isErrorDisplayed() {
        return isDisplayed(errorMessage);
    }

    public String getHeading() {
        return getText(pageHeading);
    }

    // ── Page Validation ──────────────────────────────────────────────────

    @Override
    public boolean isPageLoaded() {
        return isDisplayed(loginButton) && isDisplayed(usernameField);
    }
}
