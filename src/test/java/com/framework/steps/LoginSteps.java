package com.framework.steps;

import com.framework.config.ConfigManager;
import com.framework.driver.DriverManager;
import com.framework.pages.DashboardPage;
import com.framework.pages.LoginPage;
import com.framework.utils.assertion.AssertionUtils;
import io.cucumber.java.en.*;
import io.qameta.allure.Step;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Step definitions for Login feature.
 * Each step is concise — page interaction logic lives in the Page Object.
 */
public class LoginSteps {

    private static final Logger log = LogManager.getLogger(LoginSteps.class);

    private LoginPage loginPage;
    private DashboardPage dashboardPage;

    // ── Given ────────────────────────────────────────────────────────────

    @Given("the user is on the login page")
    public void theUserIsOnTheLoginPage() {
        loginPage = new LoginPage();
        loginPage.navigateToBaseUrl();
        AssertionUtils.assertTrue(loginPage.isPageLoaded(), "Login page should be loaded");
    }

    @Given("the user is logged in as {string}")
    public void theUserIsLoggedInAs(String username) {
        loginPage = new LoginPage();
        loginPage.navigateToBaseUrl();
        // Use API login to bypass UI for non-login test scenarios (FAANG pattern)
        // For demo purposes, using UI login here
        dashboardPage = loginPage.loginAs(username, ConfigManager.get().baseUrl());
        AssertionUtils.assertTrue(dashboardPage.isPageLoaded(), "Dashboard should be loaded after login");
    }

    // ── When ─────────────────────────────────────────────────────────────

    @When("the user enters username {string}")
    public void theUserEntersUsername(String username) {
        loginPage.enterUsername(username);
    }

    @When("the user enters password {string}")
    public void theUserEntersPassword(String password) {
        loginPage.enterPassword(password);
    }

    @When("the user clicks the login button")
    public void theUserClicksTheLoginButton() {
        dashboardPage = loginPage.clickLogin();
    }

    @When("the user checks remember me")
    public void theUserChecksRememberMe() {
        loginPage.checkRememberMe();
    }

    @When("the user clicks logout")
    public void theUserClicksLogout() {
        loginPage = dashboardPage.logout();
    }

    @When("the user refreshes the page")
    public void theUserRefreshesThePage() {
        DriverManager.getDriver().navigate().refresh();
    }

    // ── Then ─────────────────────────────────────────────────────────────

    @Then("the user should be redirected to the dashboard")
    public void theUserShouldBeRedirectedToTheDashboard() {
        AssertionUtils.assertTrue(dashboardPage.isPageLoaded(),
            "User should be redirected to dashboard after login");
        AssertionUtils.assertUrlContains(dashboardPage.getCurrentUrl(), "dashboard");
    }

    @Then("the welcome message should be displayed")
    public void theWelcomeMessageShouldBeDisplayed() {
        String welcome = dashboardPage.getWelcomeMessage();
        AssertionUtils.assertNotBlank(welcome, "Welcome message");
    }

    @Then("an error message {string} should be displayed")
    public void anErrorMessageShouldBeDisplayed(String expectedMessage) {
        AssertionUtils.assertTrue(loginPage.isErrorDisplayed(),
            "Error message should be visible");
        AssertionUtils.assertContains(loginPage.getErrorMessage(), expectedMessage,
            "Error message content mismatch");
    }

    @Then("the user should remain on the login page")
    public void theUserShouldRemainOnTheLoginPage() {
        AssertionUtils.assertTrue(loginPage.isPageLoaded(),
            "User should remain on login page after failed login");
    }

    @Then("the user should be redirected to the login page")
    public void theUserShouldBeRedirectedToTheLoginPage() {
        AssertionUtils.assertTrue(loginPage.isPageLoaded(),
            "User should be on login page after logout");
    }

    @Then("the user should still be logged in")
    public void theUserShouldStillBeLoggedIn() {
        dashboardPage = new DashboardPage();
        AssertionUtils.assertTrue(dashboardPage.isUserLoggedIn(),
            "User should still be logged in after page refresh");
    }

    @Then("the user role displayed should be {string}")
    public void theUserRoleDisplayedShouldBe(String role) {
        String welcome = dashboardPage.getWelcomeMessage();
        AssertionUtils.assertContains(welcome, role, "User role in welcome message");
    }
}
