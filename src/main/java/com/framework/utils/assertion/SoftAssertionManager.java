package com.framework.utils.assertion;

import org.assertj.core.api.SoftAssertions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Thread-safe soft assertion manager for Cucumber step definitions.
 *
 * Problem: Cucumber steps are separate methods — a shared SoftAssertions
 * object needs thread-safe lifecycle management.
 *
 * Solution: ThreadLocal<SoftAssertions> initialised per scenario in hooks,
 * flushed (assertAll) at the end of each scenario.
 *
 * Usage in steps:
 *   SoftAssertionManager.get().assertThat(actual).isEqualTo(expected);
 *
 * Usage in @After hook:
 *   SoftAssertionManager.assertAll();
 *   SoftAssertionManager.reset();
 */
public final class SoftAssertionManager {

    private static final Logger log = LogManager.getLogger(SoftAssertionManager.class);
    private static final ThreadLocal<SoftAssertions> SOFT = ThreadLocal.withInitial(SoftAssertions::new);

    private SoftAssertionManager() {}

    public static SoftAssertions get() {
        return SOFT.get();
    }

    /**
     * Call at the END of each scenario (@After hook) to flush all soft assertions.
     * If any assertions failed, this throws a single aggregated error.
     */
    public static void assertAll() {
        try {
            SOFT.get().assertAll();
        } finally {
            reset();
        }
    }

    /**
     * Call at the start of each scenario (@Before hook) to ensure a clean state.
     */
    public static void reset() {
        SOFT.remove();
        log.debug("SoftAssertions reset for thread: {}", Thread.currentThread().getName());
    }

    // ── Convenience Methods ──────────────────────────────────────────────

    public static void assertEqualsS(Object actual, Object expected, String message) {
        SOFT.get().assertThat(actual).as(message).isEqualTo(expected);
    }

    public static void assertTrueS(boolean condition, String message) {
        SOFT.get().assertThat(condition).as(message).isTrue();
    }

    public static void assertContainsS(String actual, String expected, String message) {
        SOFT.get().assertThat(actual).as(message).contains(expected);
    }

    public static void assertNotBlankS(String value, String fieldName) {
        SOFT.get().assertThat(value).as(fieldName + " should not be blank").isNotBlank();
    }

    public static void assertNotNullS(Object value, String message) {
        SOFT.get().assertThat(value).as(message).isNotNull();
    }
}
