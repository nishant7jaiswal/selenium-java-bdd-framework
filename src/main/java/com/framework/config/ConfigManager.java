package com.framework.config;

import org.aeonbits.owner.ConfigFactory;

/**
 * Singleton config provider.
 * Thread-safe via initialization-on-demand holder pattern.
 */
public final class ConfigManager {

    private ConfigManager() {}

    private static final class Holder {
        static final FrameworkConfig INSTANCE = ConfigFactory.create(FrameworkConfig.class, System.getProperties(), System.getenv());
    }

    public static FrameworkConfig get() {
        return Holder.INSTANCE;
    }
}
