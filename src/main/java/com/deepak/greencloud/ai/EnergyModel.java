package com.deepak.greencloud.ai;

/**
 * Defines the linear power model used to estimate host energy consumption.
 */
public final class EnergyModel {

    /** Default idle server power in watts. */
    public static final double DEFAULT_IDLE_POWER_WATTS = 150.0;
    /** Default maximum server power in watts. */
    public static final double DEFAULT_MAX_POWER_WATTS = 300.0;

    private final double idlePowerWatts;
    private final double maxPowerWatts;

    /** Creates a model with the framework default power limits. */
    public EnergyModel() {
        this(DEFAULT_IDLE_POWER_WATTS, DEFAULT_MAX_POWER_WATTS);
    }

    /**
     * Creates a model with custom power limits.
     *
     * @param idlePowerWatts power consumed by an idle host in watts
     * @param maxPowerWatts power consumed by a fully utilized host in watts
     */
    public EnergyModel(double idlePowerWatts, double maxPowerWatts) {
        if (idlePowerWatts < 0 || maxPowerWatts < idlePowerWatts) {
            throw new IllegalArgumentException("Power limits must be non-negative and max power must not be below idle power.");
        }
        this.idlePowerWatts = idlePowerWatts;
        this.maxPowerWatts = maxPowerWatts;
    }

    /**
     * Calculates host power from CPU utilization using the linear server power model.
     *
     * @param cpuUtilization CPU utilization from 0.0 to 1.0
     * @return current power in watts
     */
    public double calculatePower(double cpuUtilization) {
        double utilization = clamp(cpuUtilization);
        return idlePowerWatts + (maxPowerWatts - idlePowerWatts) * utilization;
    }

    /**
     * Calculates energy usage in watt-hours.
     *
     * @param powerWatts measured power in watts
     * @param durationSeconds elapsed simulation time in seconds
     * @return consumed energy in watt-hours
     */
    public double calculateEnergy(double powerWatts, double durationSeconds) {
        return Math.max(0, powerWatts) * Math.max(0, durationSeconds) / 3600.0;
    }

    /**
     * Calculates average power from an accumulated energy value.
     *
     * @param energyWattHours accumulated energy in watt-hours
     * @param durationSeconds elapsed simulation time in seconds
     * @return average power in watts
     */
    public double calculateAveragePower(double energyWattHours, double durationSeconds) {
        if (durationSeconds <= 0) {
            return 0;
        }
        return Math.max(0, energyWattHours) * 3600.0 / durationSeconds;
    }

    /**
     * Returns the configured idle power.
     *
     * @return idle power in watts
     */
    public double getIdlePowerWatts() { return idlePowerWatts; }

    /**
     * Returns the configured maximum power.
     *
     * @return maximum power in watts
     */
    public double getMaxPowerWatts() { return maxPowerWatts; }

    private double clamp(double value) { return Math.max(0, Math.min(1, value)); }
}
