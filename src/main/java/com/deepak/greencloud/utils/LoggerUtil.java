package com.deepak.greencloud.utils;

import com.deepak.greencloud.constants.Constants;

/**
 * Provides simple console logging helpers for simulation output.
 */
public final class LoggerUtil {

    private LoggerUtil() {
    }

    /**
     * Prints the application banner.
     */
    public static void printBanner() {
        info(Constants.SEPARATOR);
        info(Constants.PROJECT_NAME);
        info(Constants.SIMULATION_NAME);
        info(Constants.SEPARATOR);
    }

    /**
     * Prints the simulation summary header.
     */
    public static void printSummaryHeader() {
        info(Constants.SEPARATOR);
        info(Constants.SUMMARY_TITLE);
        info(Constants.SEPARATOR);
    }

    /**
     * Prints the resource validation header.
     */
    public static void printResourceValidationHeader() {
        info(Constants.SEPARATOR);
        info(Constants.RESOURCE_VALIDATION_TITLE);
        info(Constants.SEPARATOR);
    }

    /**
     * Prints a formatted summary item.
     *
     * @param label summary label
     * @param value summary value
     */
    public static void summary(String label, Object value) {
        info(label + ": " + value);
    }

    /**
     * Prints a console message.
     *
     * @param message message to print
     */
    public static void info(String message) {
        System.out.println(message);
    }
}
