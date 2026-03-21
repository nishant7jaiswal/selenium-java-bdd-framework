# selenium-java-bdd-framework

![Java](https://img.shields.io/badge/Java-17-ED8B00?style=flat&logo=openjdk&logoColor=white)
![Selenium](https://img.shields.io/badge/Selenium-4.18-43B02A?style=flat&logo=selenium&logoColor=white)
![Cucumber](https://img.shields.io/badge/Cucumber-BDD-23D96C?style=flat&logo=cucumber&logoColor=white)
![TestNG](https://img.shields.io/badge/TestNG-7.9-FF6600?style=flat)
![Allure](https://img.shields.io/badge/Allure-2.25-orange?style=flat)
![Jenkins](https://img.shields.io/badge/Jenkins-Pipeline-D24939?style=flat&logo=jenkins&logoColor=white)
![Docker](https://img.shields.io/badge/Docker-Grid-2496ED?style=flat&logo=docker&logoColor=white)
![GitHub Actions](https://img.shields.io/badge/GitHub%20Actions-CI-2088FF?style=flat&logo=githubactions&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-3.9-C71A36?style=flat&logo=apachemaven&logoColor=white)

> **Production-grade Selenium Java BDD test automation framework** — built to enterprise standards with parallel execution, AI-assisted test generation, containerised grid support, and rich Allure reporting.

---

## Architecture

```
selenium-java-bdd-framework/
│
├── src/main/java/com/framework/
│   ├── base/                    # BasePage — all page objects extend this
│   ├── config/                  # Type-safe config (Owner library)
│   ├── driver/                  # ThreadLocal DriverManager — Chrome/Firefox/Edge/Remote
│   ├── pages/                   # Page Object Model classes
│   └── utils/
│       ├── api/                 # RestAssured helpers for API setup/teardown
│       ├── assertion/           # Fluent hard + soft assertions (AssertJ + TestNG)
│       ├── data/                # JSON, Excel, Faker test data utilities
│       ├── report/              # Allure report enrichment
│       ├── retry/               # TestNG retry analyzer + global transformer
│       ├── screenshot/          # Screenshot capture with Allure attachment
│       └── wait/                # FluentWait + Awaitility — zero Thread.sleep()
│
├── src/test/java/com/framework/
│   ├── hooks/                   # Cucumber Before/After hooks — driver + Allure
│   ├── runners/                 # TestNG Cucumber runner (parallel DataProvider)
│   └── steps/                   # Step definitions — thin, delegate to Page Objects
│
├── src/test/resources/
│   ├── features/                # Gherkin .feature files (login, dashboard, checkout)
│   └── allure.properties
│
├── src/main/resources/
│   ├── config/                  # config.properties, qa.properties, staging.properties
│   └── log4j2.xml               # Structured logging with rolling files
│
├── test-data/
│   ├── json/                    # JSON test data files
│   └── excel/                   # Excel data-driven test files
│
├── docker/
│   ├── docker-compose.yml       # Selenium Grid 4 + test runner + Allure server
│   └── entrypoint.sh
│
├── .github/workflows/
│   └── ci.yml                   # GitHub Actions — build → test → Allure → GH Pages
│
├── Dockerfile                   # Containerised test runner (Java 17 + Chrome + Maven)
├── Jenkinsfile                  # Declarative Jenkins pipeline with parameterised build
├── testng.xml                   # Parallel execution config
└── pom.xml                      # All dependencies — Selenium, Cucumber, Allure, etc.
```

---

## Key Design Decisions

| Concern | Solution | Why |
|---|---|---|
| Thread safety | `ThreadLocal<WebDriver>` in `DriverManager` | Safe parallel execution across TestNG threads |
| Config management | Owner library with `@LoadPolicy(MERGE)` | Type-safe, multi-env, override via system properties |
| Waiting strategy | `FluentWait` + `Awaitility` | Zero `Thread.sleep()`, handles async UI and non-UI conditions |
| Assertions | `AssertionUtils` wrapping AssertJ + TestNG | Fluent soft assertions for UI validation; hard gates for control flow |
| Test data | `JsonUtils` + `ExcelUtils` + `TestDataGenerator` | Fresh data per run via Faker; file-based for fixed datasets |
| Flaky tests | `RetryAnalyzer` + `RetryTransformer` | Configurable retry applied globally via listener |
| Reporting | Allure with `@Step`, screenshots, page source | Rich failure context; auto-published to GitHub Pages |
| API in UI tests | `ApiUtils` (RestAssured) | API-first setup/teardown; UI tests focus only on UI |
| CI/CD | Jenkins Declarative + GitHub Actions | Nightly schedule, branch triggers, parameterised builds |
| Containerisation | Docker + Selenium Grid 4 | Consistent, scalable, environment-agnostic execution |

---

## Quick Start

### Prerequisites
- Java 17+
- Maven 3.9+
- Chrome / Firefox / Edge

### Clone & Run

```bash
git clone https://github.com/nishant7jaiswal/selenium-java-bdd-framework.git
cd selenium-java-bdd-framework

# Run smoke tests (Chrome, QA env, headed)
mvn test -Dtags=@smoke

# Run regression headless on Firefox
mvn test -Dtags=@regression -Dbrowser=firefox -Dheadless=true

# Run specific environment
mvn test -Denv=staging -Dtags=@smoke

# Run with Maven profile
mvn test -Pregression
```

### Generate Allure Report

```bash
# After test run
mvn allure:report

# Serve report locally
mvn allure:serve
```

### Run via Docker

```bash
# Start Selenium Grid + run smoke tests
docker-compose -f docker/docker-compose.yml up tests

# Run with custom tags
TAGS=@regression BROWSER=chrome docker-compose -f docker/docker-compose.yml up tests

# View Selenium Grid UI
open http://localhost:4444

# View live browser via noVNC
open http://localhost:7900

# View Allure report server
open http://localhost:5050
```

---

## Configuration

All config is in `src/main/resources/config/`. Override any property via system property:

```bash
mvn test -Dbrowser=firefox -Denv=staging -Dheadless=true -Dretry.count=2
```

| Property | Default | Description |
|---|---|---|
| `browser` | `chrome` | chrome / firefox / edge / safari |
| `headless` | `false` | Run browser in headless mode |
| `env` | `qa` | qa / staging / prod |
| `base.url` | *(set in env file)* | Application base URL |
| `explicit.wait` | `20` | Explicit wait timeout (seconds) |
| `remote` | `false` | Use Selenium Grid |
| `grid.url` | `http://localhost:4444/wd/hub` | Grid hub URL |
| `retry.count` | `1` | Number of retries on failure |
| `screenshot.on.failure` | `true` | Capture screenshot on test failure |

---

## Running Specific Tags

```bash
mvn test -Dtags=@smoke          # Smoke suite
mvn test -Dtags=@regression     # Full regression
mvn test -Dtags=@login          # Login feature only
mvn test -Dtags="@smoke and @login"   # AND condition
mvn test -Dtags="@smoke or @sanity"   # OR condition
mvn test -Dtags="not @wip"      # Exclude WIP tests
```

---

## Parallel Execution

Configured in `testng.xml`. Thread count matches available CPU cores / Grid nodes:

```xml
<suite parallel="methods" thread-count="4" data-provider-thread-count="4">
```

Each thread gets its own `WebDriver` instance via `ThreadLocal` — no shared state.

---

## CI/CD Integration

### Jenkins
- Declarative pipeline in `Jenkinsfile`
- Parameterised: browser, environment, tags, headless, email
- Nightly schedule at 1 AM IST
- Allure report published post-execution
- Email notification on failure

### GitHub Actions
- Triggers: push to `main`/`develop`, PR, nightly cron, manual dispatch
- Allure report auto-published to GitHub Pages on `main` branch
- Test results published via `dorny/test-reporter`

---

## Utilities Reference

```java
// Wait for spinner to disappear before interacting
wait.waitForSpinnerToDisappear(By.cssSelector(".loader"));

// Generate fresh test data
String email    = TestDataGenerator.uniqueEmail();
String fullName = TestDataGenerator.fullName();

// Read JSON test data
User user = JsonUtils.readFromClasspath("test-data/json/user.json", User.class);

// Read Excel test data
List<Map<String,String>> rows = ExcelUtils.readSheet("test-data/excel/login.xlsx", "LoginData");

// Soft assertions
AssertionUtils.softAssert(soft -> {
    soft.assertThat(page.getTitle()).isEqualTo("Dashboard");
    soft.assertThat(page.getUserName()).contains("Nishant");
});

// API call in UI test (setup/teardown bypass)
String token = ApiUtils.getAuthToken("/api/auth/login", "user", "pass");

// Attach custom data to Allure report
ReportUtils.attachJson("API Response", responseBody);
ReportUtils.addIssueLink("JIRA-1234");
```

---

## Author

**Nishant Jaiswal** — Senior Lead Test Automation Engineer

[![LinkedIn](https://img.shields.io/badge/LinkedIn-nishantjaiswal007-0A66C2?style=flat&logo=linkedin)](https://linkedin.com/in/nishantjaiswal007)
[![Email](https://img.shields.io/badge/Email-nishant7jaiswal%40gmail.com-D14836?style=flat&logo=gmail)](mailto:nishant7jaiswal@gmail.com)
