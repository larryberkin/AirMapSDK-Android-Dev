package com.airmap.airmapsdk;

public interface AnalyticsTracker {
    void logEvent(String section, String action, String label);

    void logEvent(String section, String action, String label, int value);

    void logEvent(String section, String action, String label, String value);

    void report(Throwable t);

    void logDebug(String key, String value);
}
