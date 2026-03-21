package com.framework.utils.report;

import io.qameta.allure.Allure;
import io.qameta.allure.model.Status;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Allure report enrichment utility.
 * Provides helper methods to add rich context to test reports.
 * Used in hooks, step definitions, and listeners.
 */
public final class ReportUtils {

    private static final Logger log = LogManager.getLogger(ReportUtils.class);

    private ReportUtils() {}

    // ── Allure Metadata ──────────────────────────────────────────────────

    public static void setTestDescription(String description) {
        Allure.description(description);
    }

    public static void addLabel(String name, String value) {
        Allure.label(name, value);
    }

    public static void addLink(String name, String url) {
        Allure.link(name, url);
    }

    public static void addIssueLink(String issueId) {
        Allure.issue(issueId, "https://jira.yourcompany.com/browse/" + issueId);
    }

    public static void addTestCaseLink(String testCaseId) {
        Allure.tms(testCaseId, "https://testmanager.yourcompany.com/tc/" + testCaseId);
    }

    // ── Step Logging ─────────────────────────────────────────────────────

    /**
     * Log a custom step to Allure with PASSED status.
     * Use when a step doesn't map to a @Step annotation.
     */
    public static void logStep(String stepName) {
        log.info("[STEP] {}", stepName);
        Allure.step(stepName);
    }

    public static void logStep(String stepName, Status status) {
        Allure.step(stepName, status);
    }

    // ── Attachments ──────────────────────────────────────────────────────

    public static void attachText(String name, String content) {
        Allure.addAttachment(name, "text/plain", content, ".txt");
    }

    public static void attachJson(String name, String json) {
        Allure.addAttachment(name, "application/json", json, ".json");
    }

    public static void attachHtml(String name, String html) {
        Allure.addAttachment(name, "text/html", html, ".html");
    }

    public static void attachFile(String name, String filePath) {
        try (InputStream is = Files.newInputStream(Paths.get(filePath))) {
            String mimeType = Files.probeContentType(Paths.get(filePath));
            Allure.addAttachment(name, mimeType != null ? mimeType : "application/octet-stream", is, "");
        } catch (Exception e) {
            log.error("Failed to attach file [{}]: {}", filePath, e.getMessage());
        }
    }

    // ── Environment ──────────────────────────────────────────────────────

    /**
     * Adds environment info to the Allure report (shown on the Overview page).
     * Call once at suite start — typically in TestListener.
     */
    public static void addEnvironmentInfo(String browser, String env, String baseUrl) {
        try {
            String envPropertiesContent = String.format(
                "Browser=%s%nEnvironment=%s%nBase.URL=%s%nOS=%s%nJava.Version=%s",
                browser, env, baseUrl,
                System.getProperty("os.name"),
                System.getProperty("java.version")
            );
            Files.writeString(
                Paths.get("target/allure-results/environment.properties"),
                envPropertiesContent
            );
        } catch (Exception e) {
            log.warn("Could not write Allure environment.properties: {}", e.getMessage());
        }
    }
}
