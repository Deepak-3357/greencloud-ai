package com.deepak.greencloud.ai;

import java.time.Instant;

/** Immutable energy reading for a single host at a simulation instant. */
public final class EnergySnapshot {
    private final double simulationTime;
    private final long hostId;
    private final double cpuUtilization;
    private final double ramUtilization;
    private final double currentPowerWatts;
    private final double accumulatedEnergyWattHours;
    private final String hostState;
    private final Instant timestamp;

    /**
     * Creates an immutable host-energy snapshot.
     *
     * @param simulationTime current CloudSim time in seconds
     * @param hostId host identifier
     * @param cpuUtilization actual host CPU utilization
     * @param ramUtilization actual host RAM utilization
     * @param currentPowerWatts current modeled power in watts
     * @param accumulatedEnergyWattHours energy consumed by this host in watt-hours
     * @param hostState current host state
     * @param timestamp wall-clock time when the reading was captured
     */
    public EnergySnapshot(double simulationTime, long hostId, double cpuUtilization, double ramUtilization,
                          double currentPowerWatts, double accumulatedEnergyWattHours,
                          String hostState, Instant timestamp) {
        this.simulationTime = simulationTime;
        this.hostId = hostId;
        this.cpuUtilization = cpuUtilization;
        this.ramUtilization = ramUtilization;
        this.currentPowerWatts = currentPowerWatts;
        this.accumulatedEnergyWattHours = accumulatedEnergyWattHours;
        this.hostState = hostState;
        this.timestamp = timestamp;
    }

    /** @return simulation time in seconds */
    public double getSimulationTime() { return simulationTime; }
    /** @return host identifier */
    public long getHostId() { return hostId; }
    /** @return actual CPU utilization */
    public double getCpuUtilization() { return cpuUtilization; }
    /** @return actual RAM utilization */
    public double getRamUtilization() { return ramUtilization; }
    /** @return modeled current power in watts */
    public double getCurrentPowerWatts() { return currentPowerWatts; }
    /** @return accumulated energy in watt-hours */
    public double getAccumulatedEnergyWattHours() { return accumulatedEnergyWattHours; }
    /** @return host state */
    public String getHostState() { return hostState; }
    /** @return wall-clock capture timestamp */
    public Instant getTimestamp() { return timestamp; }
}
