package com.deepak.greencloud.ai.optimization;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.cloudsimplus.hosts.Host;

/**
 * Manages host power states and determines state based on current utilization.
 * Supports: ACTIVE, IDLE, SLEEP, POWER_OFF states.
 */
public final class HostPowerManager {
    /**
     * Host power states representing different operational modes.
     */
    public enum PowerState {
        ACTIVE("Active - High utilization, all components active"),
        IDLE("Idle - Low utilization, minimal processing"),
        SLEEP("Sleep - Very low utilization, reduced power state"),
        POWER_OFF("Power Off - Host disabled");

        private final String description;

        PowerState(String description) {
            this.description = description;
        }

        public String getDescription() { return description; }
    }

    private static final double ACTIVE_THRESHOLD = 0.50;      // CPU > 50% = ACTIVE
    private static final double IDLE_THRESHOLD = 0.20;        // 20% <= CPU <= 50% = IDLE
    private static final double SLEEP_THRESHOLD = 0.05;       // 5% <= CPU < 20% = SLEEP
    private static final double POWER_OFF_THRESHOLD = 0.01;   // CPU < 1% AND no VMs = POWER_OFF

    private final Map<Long, PowerState> hostPowerStates = new HashMap<>();
    private final Map<Long, Long> timeInState = new HashMap<>();

    /**
     * Evaluates host utilization and determines the appropriate power state.
     *
     * @param host host to evaluate
     * @return determined power state
     */
    public synchronized PowerState determinePowerState(Host host) {
        if (!host.isActive()) {
            return PowerState.POWER_OFF;
        }

        double cpuUtilization = clamp(host.getCpuPercentUtilization());
        boolean hasVms = !host.getVmList().isEmpty();

        PowerState state;
        if (cpuUtilization > ACTIVE_THRESHOLD) {
            state = PowerState.ACTIVE;
        } else if (cpuUtilization > IDLE_THRESHOLD) {
            state = PowerState.IDLE;
        } else if (cpuUtilization > SLEEP_THRESHOLD) {
            state = PowerState.SLEEP;
        } else if (cpuUtilization < POWER_OFF_THRESHOLD && !hasVms && cpuUtilization > 0) {
            // Only power off if CPU is near zero AND no VMs AND there's at least been some activity
            state = PowerState.POWER_OFF;
        } else if (hasVms) {
            // If there are VMs, default to ACTIVE or IDLE based on utilization
            state = cpuUtilization > IDLE_THRESHOLD ? PowerState.IDLE : PowerState.ACTIVE;
        } else {
            // Default to SLEEP for idle hosts without VMs
            state = PowerState.SLEEP;
        }

        hostPowerStates.put(host.getId(), state);
        return state;
    }

    /**
     * Updates power states for all hosts.
     *
     * @param hosts hosts to evaluate
     * @return map of host IDs to their power states
     */
    public synchronized Map<Long, PowerState> updatePowerStates(List<Host> hosts) {
        Map<Long, PowerState> states = new HashMap<>();
        for (Host host : hosts) {
            PowerState state = determinePowerState(host);
            states.put(host.getId(), state);
        }
        return Map.copyOf(states);
    }

    /**
     * Gets the current power state for a host.
     *
     * @param hostId host identifier
     * @return power state or POWER_OFF if unknown
     */
    public synchronized PowerState getPowerState(long hostId) {
        return hostPowerStates.getOrDefault(hostId, PowerState.POWER_OFF);
    }

    /**
     * Calculates estimated power consumption for a host based on its power state.
     *
     * @param host host to calculate power for
     * @param maxPowerWatts maximum power consumption in active state
     * @return estimated power consumption in watts
     */
    public synchronized double estimatePowerConsumption(Host host, double maxPowerWatts) {
        PowerState state = getPowerState(host.getId());

        return switch (state) {
            case ACTIVE -> {
                double cpuUtil = clamp(host.getCpuPercentUtilization());
                yield maxPowerWatts * (0.30 + 0.70 * cpuUtil); // 30% baseline + 70% variable
            }
            case IDLE -> maxPowerWatts * 0.40; // 40% of max power
            case SLEEP -> maxPowerWatts * 0.10; // 10% of max power
            case POWER_OFF -> 0.0;
        };
    }

    /**
     * Counts hosts in each power state.
     *
     * @param hosts hosts to count
     * @return map of power state to count
     */
    public synchronized Map<PowerState, Integer> countHostsByPowerState(List<Host> hosts) {
        Map<PowerState, Integer> counts = new HashMap<>();
        for (PowerState state : PowerState.values()) {
            counts.put(state, 0);
        }

        for (Host host : hosts) {
            PowerState state = getPowerState(host.getId());
            counts.put(state, counts.getOrDefault(state, 0) + 1);
        }

        return Map.copyOf(counts);
    }

    /**
     * Estimates energy savings if hosts transition to lower power states.
     *
     * @param hosts hosts to evaluate
     * @param maxPowerWatts maximum power per host
     * @param durationSeconds monitoring interval in seconds
     * @return estimated energy savings in watt-hours
     */
    public synchronized double estimateEnergySavings(List<Host> hosts, double maxPowerWatts, double durationSeconds) {
        double currentEnergy = 0;
        double optimizedEnergy = 0;

        for (Host host : hosts) {
            double cpuUtil = clamp(host.getCpuPercentUtilization());
            double currentPower = maxPowerWatts * (0.30 + 0.70 * cpuUtil);
            currentEnergy += currentPower;

            PowerState state = getPowerState(host.getId());
            double optimizedPower = estimatePowerConsumption(host, maxPowerWatts);
            optimizedEnergy += optimizedPower;
        }

        double savingsPower = currentEnergy - optimizedEnergy;
        return Math.max(0, savingsPower * (durationSeconds / 3600.0)); // Convert to watt-hours
    }

    /**
     * Resets the power state tracking for a new monitoring cycle.
     */
    public synchronized void resetStates() {
        hostPowerStates.clear();
        timeInState.clear();
    }

    private double clamp(double value) {
        return Math.max(0, Math.min(1, value));
    }
}
