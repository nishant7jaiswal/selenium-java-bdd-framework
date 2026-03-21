package com.framework.driver;

import com.framework.config.ConfigManager;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;

import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Thread-safe WebDriver lifecycle manager using ThreadLocal.
 * Supports: Chrome, Firefox, Edge, Safari, Remote Grid.
 * Auto-manages driver binaries via WebDriverManager.
 */
public final class DriverManager {

    private static final Logger log = LogManager.getLogger(DriverManager.class);
    private static final ThreadLocal<WebDriver> DRIVER_THREAD_LOCAL = new ThreadLocal<>();

    private DriverManager() {}

    // ── Public API ───────────────────────────────────────────────────────

    public static void initDriver() {
        if (DRIVER_THREAD_LOCAL.get() != null) {
            log.warn("Driver already initialised for thread [{}]. Quitting existing.", Thread.currentThread().getName());
            quitDriver();
        }
        WebDriver driver = createDriver();
        applyTimeouts(driver);
        driver.manage().window().maximize();
        DRIVER_THREAD_LOCAL.set(driver);
        log.info("Driver initialised: [{}] on thread [{}]", ConfigManager.get().browser(), Thread.currentThread().getName());
    }

    public static WebDriver getDriver() {
        WebDriver driver = DRIVER_THREAD_LOCAL.get();
        if (driver == null) {
            throw new IllegalStateException("WebDriver not initialised. Call initDriver() before getDriver().");
        }
        return driver;
    }

    public static void quitDriver() {
        WebDriver driver = DRIVER_THREAD_LOCAL.get();
        if (driver != null) {
            try {
                driver.quit();
                log.info("Driver quit on thread [{}]", Thread.currentThread().getName());
            } catch (Exception e) {
                log.error("Error quitting driver: {}", e.getMessage());
            } finally {
                DRIVER_THREAD_LOCAL.remove();
            }
        }
    }

    public static boolean isDriverAlive() {
        try {
            WebDriver driver = DRIVER_THREAD_LOCAL.get();
            return driver != null && ((RemoteWebDriver) driver).getSessionId() != null;
        } catch (Exception e) {
            return false;
        }
    }

    // ── Driver Factory ───────────────────────────────────────────────────

    private static WebDriver createDriver() {
        String browser = ConfigManager.get().browser().toLowerCase().trim();
        boolean remote  = ConfigManager.get().remote();

        if (remote) {
            return createRemoteDriver(browser);
        }

        return switch (browser) {
            case "chrome"  -> createChromeDriver();
            case "firefox" -> createFirefoxDriver();
            case "edge"    -> createEdgeDriver();
            case "safari"  -> new SafariDriver();
            default -> throw new IllegalArgumentException("Unsupported browser: [" + browser + "]. Supported: chrome, firefox, edge, safari");
        };
    }

    // ── Chrome ───────────────────────────────────────────────────────────

    private static ChromeDriver createChromeDriver() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = getChromeOptions();
        return new ChromeDriver(options);
    }

    private static ChromeOptions getChromeOptions() {
        ChromeOptions options = new ChromeOptions();
        boolean headless = ConfigManager.get().headless();

        if (headless) {
            options.addArguments("--headless=new");
        }

        // FAANG-standard Chrome flags for stability in CI
        options.addArguments(
            "--no-sandbox",
            "--disable-dev-shm-usage",
            "--disable-gpu",
            "--disable-extensions",
            "--disable-infobars",
            "--disable-notifications",
            "--disable-popup-blocking",
            "--start-maximized",
            "--remote-allow-origins=*",
            "--window-size=1920,1080"
        );

        // Suppress automation detection
        options.setExperimentalOption("excludeSwitches", new String[]{"enable-automation"});
        options.setExperimentalOption("useAutomationExtension", false);

        // Download preferences
        Map<String, Object> prefs = new HashMap<>();
        prefs.put("download.default_directory", System.getProperty("user.dir") + "/downloads");
        prefs.put("download.prompt_for_download", false);
        prefs.put("plugins.always_open_pdf_externally", true);
        options.setExperimentalOption("prefs", prefs);

        return options;
    }

    // ── Firefox ──────────────────────────────────────────────────────────

    private static FirefoxDriver createFirefoxDriver() {
        WebDriverManager.firefoxdriver().setup();
        FirefoxOptions options = new FirefoxOptions();
        if (ConfigManager.get().headless()) {
            options.addArguments("--headless");
        }
        options.addArguments("--width=1920", "--height=1080");
        return new FirefoxDriver(options);
    }

    // ── Edge ─────────────────────────────────────────────────────────────

    private static EdgeDriver createEdgeDriver() {
        WebDriverManager.edgedriver().setup();
        EdgeOptions options = new EdgeOptions();
        if (ConfigManager.get().headless()) {
            options.addArguments("--headless=new");
        }
        options.addArguments("--no-sandbox", "--disable-dev-shm-usage", "--window-size=1920,1080");
        return new EdgeDriver(options);
    }

    // ── Remote / Grid ────────────────────────────────────────────────────

    private static RemoteWebDriver createRemoteDriver(String browser) {
        try {
            var capabilities = switch (browser) {
                case "chrome"  -> getChromeOptions();
                case "firefox" -> new FirefoxOptions();
                case "edge"    -> new EdgeOptions();
                default -> throw new IllegalArgumentException("Unsupported remote browser: " + browser);
            };
            String gridUrl = ConfigManager.get().gridUrl();
            log.info("Connecting to Selenium Grid at: {}", gridUrl);
            return new RemoteWebDriver(new URL(gridUrl), capabilities);
        } catch (MalformedURLException e) {
            throw new RuntimeException("Invalid Grid URL: " + ConfigManager.get().gridUrl(), e);
        }
    }

    // ── Timeouts ─────────────────────────────────────────────────────────

    private static void applyTimeouts(WebDriver driver) {
        driver.manage().timeouts()
            .implicitlyWait(Duration.ofSeconds(ConfigManager.get().implicitWait()))
            .pageLoadTimeout(Duration.ofSeconds(ConfigManager.get().pageLoadTimeout()))
            .scriptTimeout(Duration.ofSeconds(30));
    }
}
