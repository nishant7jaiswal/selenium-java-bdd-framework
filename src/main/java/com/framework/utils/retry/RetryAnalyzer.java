package com.framework.utils.retry;

import com.framework.config.ConfigManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

/**
 * TestNG retry analyzer for handling transient failures.
 * Configured via config.properties: retry.count
 *
 * Usage: @Test(retryAnalyzer = RetryAnalyzer.class)
 * Or applied globally via RetryTransformer listener.
 */
public class RetryAnalyzer implements IRetryAnalyzer {

    private static final Logger log = LogManager.getLogger(RetryAnalyzer.class);
    private int retryCount = 0;
    private final int maxRetryCount = ConfigManager.get().retryCount();

    @Override
    public boolean retry(ITestResult result) {
        if (retryCount < maxRetryCount) {
            retryCount++;
            log.warn("Retrying test [{}] — attempt {}/{} | Failure: {}",
                result.getName(), retryCount, maxRetryCount, result.getThrowable().getMessage());
            return true;
        }
        log.error("Test [{}] failed after {} retry attempt(s)", result.getName(), maxRetryCount);
        return false;
    }
}
