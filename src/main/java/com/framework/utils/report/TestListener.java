package com.framework.utils.report;

import com.framework.config.ConfigManager;
import com.framework.utils.screenshot.ScreenshotUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.*;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * TestNG listener for cross-cutting concerns:
 * - Logs test start/end with timing
 * - Captures screenshots on failure
 * - Tracks pass/fail/skip counts for email summary
 * - Writes Allure environment.properties on suite start
 *
 * Register in testng.xml:
 *   <listener class-name="com.framework.utils.report.TestListener"/>
 */
public class TestListener implements ITestListener, ISuiteListener {

    private static final Logger log = LogManager.getLogger(TestListener.class);

    private static final AtomicInteger passed  = new AtomicInteger(0);
    private static final AtomicInteger failed  = new AtomicInteger(0);
    private static final AtomicInteger skipped = new AtomicInteger(0);

    private long suiteStartTime;

    // ── Suite ────────────────────────────────────────────────────────────

    @Override
    public void onStart(ISuite suite) {
        suiteStartTime = System.currentTimeMillis();
        log.info("╔══════════════════════════════════════════════════════╗");
        log.info("║         AUTOMATION SUITE STARTING                   ║");
        log.info("╠══════════════════════════════════════════════════════╣");
        log.info("║  Suite:   {}", suite.getName());
        log.info("║  Browser: {}", ConfigManager.get().browser());
        log.info("║  Env:     {}", ConfigManager.get().env());
        log.info("║  URL:     {}", ConfigManager.get().baseUrl());
        log.info("╚══════════════════════════════════════════════════════╝");

        ReportUtils.addEnvironmentInfo(
            ConfigManager.get().browser(),
            ConfigManager.get().env(),
            ConfigManager.get().baseUrl()
        );
    }

    @Override
    public void onFinish(ISuite suite) {
        long duration = (System.currentTimeMillis() - suiteStartTime) / 1000;
        int p = passed.get(), f = failed.get(), s = skipped.get();
        int total = p + f + s;

        log.info("╔══════════════════════════════════════════════════════╗");
        log.info("║         AUTOMATION SUITE COMPLETE                   ║");
        log.info("╠══════════════════════════════════════════════════════╣");
        log.info("║  Total:   {} | ✅ Passed: {} | ❌ Failed: {} | ⏭ Skipped: {}", total, p, f, s);
        log.info("║  Pass Rate: {}%", total > 0 ? String.format("%.1f", p * 100.0 / total) : "N/A");
        log.info("║  Duration: {}s", duration);
        log.info("╚══════════════════════════════════════════════════════╝");
    }

    // ── Test ─────────────────────────────────────────────────────────────

    @Override
    public void onTestStart(ITestResult result) {
        log.info("▶ START  [{}] — {}", result.getTestClass().getRealClass().getSimpleName(), result.getName());
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        passed.incrementAndGet();
        long duration = result.getEndMillis() - result.getStartMillis();
        log.info("✅ PASS   [{}] — {} ({}ms)", result.getTestClass().getRealClass().getSimpleName(),
            result.getName(), duration);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        failed.incrementAndGet();
        long duration = result.getEndMillis() - result.getStartMillis();
        log.error("❌ FAIL   [{}] — {} ({}ms)", result.getTestClass().getRealClass().getSimpleName(),
            result.getName(), duration);
        log.error("   Cause: {}", result.getThrowable() != null
            ? result.getThrowable().getMessage() : "unknown");

        ScreenshotUtils.captureOnFailure(result.getName());
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        skipped.incrementAndGet();
        log.warn("⏭ SKIP   [{}] — {}", result.getTestClass().getRealClass().getSimpleName(),
            result.getName());
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        log.warn("⚠️ PARTIAL [{}] — {}", result.getTestClass().getRealClass().getSimpleName(),
            result.getName());
    }

    // ── Counters (accessible for email summary) ──────────────────────────

    public static int getPassed()  { return passed.get(); }
    public static int getFailed()  { return failed.get(); }
    public static int getSkipped() { return skipped.get(); }

    public static void resetCounters() {
        passed.set(0);
        failed.set(0);
        skipped.set(0);
    }
}
