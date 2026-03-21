package com.framework.runners;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;
import org.testng.annotations.DataProvider;

/**
 * Main Cucumber TestNG runner.
 * Supports parallel scenario execution via TestNG @DataProvider.
 *
 * Run profiles:
 *   mvn test                          → all tests
 *   mvn test -Ptags=@smoke            → smoke only
 *   mvn test -Dtags=@regression       → regression
 *   mvn test -Dbrowser=firefox        → specific browser
 */
@CucumberOptions(
    features  = "src/test/resources/features",
    glue      = {"com.framework.hooks", "com.framework.steps"},
    tags      = "${tags:not @wip}",
    plugin    = {
        "pretty",
        "io.qameta.allure.cucumber7jvm.AllureCucumber7Jvm",
        "json:target/cucumber-reports/cucumber.json",
        "html:target/cucumber-reports/cucumber.html",
        "rerun:target/rerun.txt"
    },
    monochrome = true,
    publish    = false
)
public class TestRunner extends AbstractTestNGCucumberTests {

    /**
     * Enable parallel scenario execution.
     * Thread count controlled by testng.xml data-provider-thread-count.
     */
    @Override
    @DataProvider(parallel = true)
    public Object[][] scenarios() {
        return super.scenarios();
    }
}
