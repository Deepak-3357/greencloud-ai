package com.deepak.greencloud.exceptions;

/**
 * Represents an invalid simulation lifecycle state.
 */
public class SimulationException extends RuntimeException {

    public SimulationException(String message) {
        super(message);
    }
}
