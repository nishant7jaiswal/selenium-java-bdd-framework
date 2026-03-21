package com.framework.hooks;

import com.framework.config.ConfigManager;
import com.framework.driver.DriverManager;
import com.framework.utils.report.ReportUtils;
import com.framework.utils.screenshot.ScreenshotUtils;
import io.cucumber.java.After;
import io.cucumber.java.AfterAll;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.Scenario;
import io.qameta.allure.Allure;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Cucumber lifecycle hooks.
 * Controls driver init/teardown, screenshot capture, and Allure enrichment.
 *
 * Order values:
 *   Lower = runs first in @Before / runs last in @After
 */
public class Hooks {

    private static final Logger log = LogManager.getLogger(Hooks.class);

    // ── Suite Level ──────────────────────────────────────────────────────

    @BeforeAll
    public static void globalSetup() {
        log.info("=== TEST SUITE STARTING ===");
        log.info("Browser:     {}", ConfigManager.get().browser());
        log.info("Environment: {}", ConfigManager.get().env());
        log.info("Base URL:    {}", ConfigManager.get().baseUrl());
        log.info("Headless:    {}", ConfigManager.get().headless());
        log.info("Remote:      {}", ConfigManager.get().remote());

        ReportUtils.addEnvironmentInfo(
            ConfigManager.get().browser(),
            ConfigManager.get().env(),
            ConfigManager.get().baseUrl()
        );
    }

    @AfterAll
    public static void globalTeardown() {
        log.info("=== TEST SUITE COMPLETE ===");
    }

    // ── Scenario Level ───────────────────────────────────────────────────

    @Before(order = 0)
    public void setUp(Scenario scenario) {
        log.info("──────────────────────────────────────────");
        log.info("STARTING: {}", scenario.getName());
        log.info("Tags:     {}", scenario.getSourceTagNames());

        DriverManager.initDriver();

        // Enrich Allure report with scenario metadata
        Allure.label("feature",  scenario.getUri().toString());
        Allure.label("scenario", scenario.getName());
        scenario.getSourceTagNames().forEach(tag -> {
            if (tag.startsWith("@JIRA-")) {
                ReportUtils.addIssueLink(tag.replace("@JIRA-", ""));
            }
            if (tag.startsWith("@TC-")) {
                ReportUtils.addTestCaseLink(tag.replace("@TC-", ""));
            }
        });
    }

    @After(order = 0)
    public void tearDown(Scenario scenario) {
        try {
            if (scenario.isFailed()) {
                log.error("FAILED: {}", scenario.getName());
                ScreenshotUtils.captureOnFailure(scenario.getName());
                ScreenshotUtils.attachPageSource();
                ReportUtils.attachText("Failure Reason",
                    scenario.getName() + " failed at: " + scenario.getStatus());
            } else {
                log.info("PASSED: {}", scenario.getName());
            }
        } finally {
            DriverManager.quitDriver();
            log.info("──────────────────────────────────────────");
        }
    }

    // ── Tagged Hooks ─────────────────────────────────────────────────────

    /**
     * For @api-setup tagged scenarios: log in via API instead of UI.
     * Bypasses the login page for non-login test scenarios.
     */
    @Before(value = "@api-setup", order = 1)
    public void apiLogin(Scenario scenario) {
        log.info("API setup hook triggered for: {}", scenario.getName());
        // Inject auth token into browser cookies or local storage
        // ApiUtils.getAuthToken("/api/auth/login", "testuser", "testpass");
    }

    /**
     * For @cleanup tagged scenarios: reset test data after run.
     */
    @After("@cleanup")
    public void cleanupTestData(Scenario scenario) {
        log.info("Cleanup hook triggered for: {}", scenario.getName());
        // Add API-based cleanup calls here
    }
}
