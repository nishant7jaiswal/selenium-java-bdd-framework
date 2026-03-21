package com.framework.config;

import org.aeonbits.owner.Config;
import org.aeonbits.owner.Config.Sources;
import org.aeonbits.owner.Config.LoadPolicy;
import org.aeonbits.owner.Config.LoadType;

/**
 * Type-safe configuration interface backed by properties files.
 * Priority: System properties > env.properties > config.properties
 *
 * Usage: ConfigFactory.create(FrameworkConfig.class)
 */
@LoadPolicy(LoadType.MERGE)
@Sources({
    "system:properties",
    "system:env",
    "file:src/main/resources/config/${env}.properties",
    "file:src/main/resources/config/config.properties"
})
public interface FrameworkConfig extends Config {

    // ── Browser ──────────────────────────────────────────────────────────
    @Key("browser")
    @DefaultValue("chrome")
    String browser();

    @Key("headless")
    @DefaultValue("false")
    boolean headless();

    @Key("browser.version")
    @DefaultValue("latest")
    String browserVersion();

    // ── Environment ──────────────────────────────────────────────────────
    @Key("env")
    @DefaultValue("qa")
    String env();

    @Key("base.url")
    String baseUrl();

    @Key("api.base.url")
    String apiBaseUrl();

    // ── Timeouts ─────────────────────────────────────────────────────────
    @Key("implicit.wait")
    @DefaultValue("10")
    int implicitWait();

    @Key("explicit.wait")
    @DefaultValue("20")
    int explicitWait();

    @Key("page.load.timeout")
    @DefaultValue("30")
    int pageLoadTimeout();

    @Key("polling.interval")
    @DefaultValue("500")
    int pollingInterval();

    // ── Grid / Remote ─────────────────────────────────────────────────────
    @Key("remote")
    @DefaultValue("false")
    boolean remote();

    @Key("grid.url")
    @DefaultValue("http://localhost:4444/wd/hub")
    String gridUrl();

    // ── Retry ────────────────────────────────────────────────────────────
    @Key("retry.count")
    @DefaultValue("1")
    int retryCount();

    // ── Screenshot ───────────────────────────────────────────────────────
    @Key("screenshot.on.failure")
    @DefaultValue("true")
    boolean screenshotOnFailure();

    @Key("screenshot.dir")
    @DefaultValue("target/screenshots")
    String screenshotDir();

    // ── Reporting ────────────────────────────────────────────────────────
    @Key("allure.results.dir")
    @DefaultValue("target/allure-results")
    String allureResultsDir();

    // ── Database ─────────────────────────────────────────────────────────
    @Key("db.url")
    String dbUrl();

    @Key("db.username")
    String dbUsername();

    @Key("db.password")
    String dbPassword();

    // ── Email ────────────────────────────────────────────────────────────
    @Key("email.enabled")
    @DefaultValue("false")
    boolean emailEnabled();

    @Key("email.recipients")
    String emailRecipients();
}
