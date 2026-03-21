package com.framework.utils.screenshot;

import com.framework.config.ConfigManager;
import com.framework.driver.DriverManager;
import io.qameta.allure.Allure;
import org.apache.commons.io.FileUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Screenshot utility with Allure attachment integration.
 * Captures full-page screenshots and attaches them to the report.
 */
public final class ScreenshotUtils {

    private static final Logger log = LogManager.getLogger(ScreenshotUtils.class);
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss-SSS");

    private ScreenshotUtils() {}

    /**
     * Capture screenshot and attach to Allure report.
     * Returns the file path of the saved screenshot.
     */
    public static String captureAndAttach(String screenshotName) {
        if (!DriverManager.isDriverAlive()) {
            log.warn("Driver not alive — skipping screenshot for: {}", screenshotName);
            return null;
        }

        try {
            WebDriver driver = DriverManager.getDriver();
            byte[] screenshotBytes = ((TakesScreenshot) driver).getScreenshotAs(OutputType.BYTES);

            // Attach to Allure
            String attachmentName = screenshotName + "_" + LocalDateTime.now().format(FORMATTER);
            Allure.addAttachment(attachmentName, new ByteArrayInputStream(screenshotBytes));

            // Save to disk
            String filePath = saveScreenshotToDisk(screenshotBytes, attachmentName);
            log.info("Screenshot captured: {}", filePath);
            return filePath;

        } catch (Exception e) {
            log.error("Failed to capture screenshot for [{}]: {}", screenshotName, e.getMessage());
            return null;
        }
    }

    /**
     * Capture screenshot on test failure — called from Cucumber hooks.
     */
    public static void captureOnFailure(String scenarioName) {
        if (ConfigManager.get().screenshotOnFailure()) {
            captureAndAttach("FAILURE_" + sanitize(scenarioName));
        }
    }

    /**
     * Embed page source in Allure report for debugging.
     */
    public static void attachPageSource() {
        try {
            String pageSource = DriverManager.getDriver().getPageSource();
            Allure.addAttachment("Page Source", "text/html", pageSource, ".html");
        } catch (Exception e) {
            log.error("Failed to attach page source: {}", e.getMessage());
        }
    }

    /**
     * Attach arbitrary text log to Allure report.
     */
    public static void attachLog(String name, String content) {
        Allure.addAttachment(name, "text/plain", content, ".txt");
    }

    // ── Private Helpers ──────────────────────────────────────────────────

    private static String saveScreenshotToDisk(byte[] bytes, String fileName) {
        String dir  = ConfigManager.get().screenshotDir();
        String path = dir + File.separator + fileName + ".png";
        try {
            File file = new File(path);
            FileUtils.forceMkdirParent(file);
            FileUtils.writeByteArrayToFile(file, bytes);
        } catch (IOException e) {
            log.error("Could not save screenshot to disk: {}", e.getMessage());
        }
        return path;
    }

    private static String sanitize(String name) {
        return name.replaceAll("[^a-zA-Z0-9_\\-]", "_").substring(0, Math.min(name.length(), 80));
    }
}
