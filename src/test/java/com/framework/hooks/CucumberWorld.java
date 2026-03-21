package com.framework.hooks;

import com.framework.pages.CheckoutPage;
import com.framework.pages.DashboardPage;
import com.framework.pages.LoginPage;
import lombok.Getter;
import lombok.Setter;

/**
 * Cucumber World — shared state container between step definition classes.
 * Injected via PicoContainer (cucumber-picocontainer dependency).
 *
 * Instead of using static fields to share page objects between steps,
 * inject World into each step class constructor.
 *
 * FAANG pattern: avoid static shared state in parallel execution.
 *
 * Usage in step class:
 *
 *   public class LoginSteps {
 *       private final CucumberWorld world;
 *
 *       public LoginSteps(CucumberWorld world) {
 *           this.world = world;
 *       }
 *
 *       @When("the user logs in")
 *       public void login() {
 *           world.setDashboardPage(world.getLoginPage().loginAs("user", "pass"));
 *       }
 *   }
 *
 *   public class DashboardSteps {
 *       private final CucumberWorld world;
 *
 *       public DashboardSteps(CucumberWorld world) {
 *           this.world = world;
 *       }
 *
 *       @Then("the dashboard is shown")
 *       public void dashboardShown() {
 *           assertTrue(world.getDashboardPage().isPageLoaded());
 *       }
 *   }
 */
@Getter
@Setter
public class CucumberWorld {

    // ── Page References ───────────────────────────────────────────────────
    private LoginPage    loginPage    = new LoginPage();
    private DashboardPage dashboardPage;
    private CheckoutPage  checkoutPage;

    // ── Test Context ──────────────────────────────────────────────────────
    private String  authToken;
    private String  currentOrderNumber;
    private String  testRunId;
    private boolean apiSetupComplete;

    // ── Scenario Metadata ─────────────────────────────────────────────────
    private String scenarioName;
    private String scenarioTags;

    public CucumberWorld() {
        this.testRunId = java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
