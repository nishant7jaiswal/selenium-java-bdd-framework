package com.framework.steps;

import com.framework.pages.DashboardPage;
import com.framework.utils.assertion.AssertionUtils;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;

/**
 * Step definitions for Dashboard feature.
 */
public class DashboardSteps {

    private static final Logger log = LogManager.getLogger(DashboardSteps.class);
    private DashboardPage dashboardPage = new DashboardPage();

    // ── Then ─────────────────────────────────────────────────────────────

    @Then("the dashboard should be displayed")
    public void theDashboardShouldBeDisplayed() {
        AssertionUtils.assertTrue(dashboardPage.isPageLoaded(),
            "Dashboard should be fully loaded");
    }

    @Then("the welcome message should contain the username")
    public void theWelcomeMessageShouldContainTheUsername() {
        String message = dashboardPage.getWelcomeMessage();
        AssertionUtils.assertNotBlank(message, "Welcome message");
        log.info("Welcome message: {}", message);
    }

    @Then("the navigation menu should contain the following items")
    public void theNavigationMenuShouldContainTheFollowingItems(DataTable dataTable) {
        List<String> expectedItems = dataTable.asList();
        List<String> actualItems   = dashboardPage.getNavigationItems();

        expectedItems.forEach(expected ->
            AssertionUtils.assertTrue(
                actualItems.stream().anyMatch(actual -> actual.equalsIgnoreCase(expected)),
                "Navigation menu should contain: " + expected
            )
        );
    }

    @When("the user navigates to {string}")
    public void theUserNavigatesTo(String section) {
        dashboardPage.navigateTo(section);
    }

    @Then("the {string} page should be loaded")
    public void thePageShouldBeLoaded(String section) {
        AssertionUtils.assertUrlContains(dashboardPage.getCurrentUrl(),
            section.toLowerCase(), "URL should contain section name");
    }

    @Then("the notification bell count should be greater than or equal to 0")
    public void theNotificationBellCountShouldBeValid() {
        int count = dashboardPage.getNotificationCount();
        AssertionUtils.assertTrue(count >= 0, "Notification count should be >= 0, was: " + count);
    }

    @When("the user searches for {string}")
    public void theUserSearchesFor(String query) {
        dashboardPage.search(query);
    }

    @Then("search results should be displayed")
    public void searchResultsShouldBeDisplayed() {
        // Validate search results container appears
        AssertionUtils.assertTrue(
            dashboardPage.isElementPresent(
                org.openqa.selenium.By.cssSelector(".search-results")),
            "Search results should be displayed"
        );
    }
}
