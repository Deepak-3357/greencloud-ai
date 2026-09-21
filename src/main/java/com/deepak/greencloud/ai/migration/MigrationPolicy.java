package com.deepak.greencloud.ai.migration;

import org.cloudsimplus.hosts.Host;

/** Defines overload and underload thresholds from actual CloudSim host metrics. */
public final class MigrationPolicy {
    /** CPU utilization threshold above which a host is overloaded. */
    public static final double CPU_OVERLOAD_THRESHOLD = 0.80;
    /** RAM utilization threshold above which a host is overloaded. */
    public static final double RAM_OVERLOAD_THRESHOLD = 0.85;
    /** CPU utilization threshold below which a host can be consolidated. */
    public static final double CPU_UNDERLOAD_THRESHOLD = 0.20;

    /**
     * Checks actual host CPU and RAM use against overload thresholds.
     *
     * @param host host to inspect
     * @return true when CPU is above 80% or RAM is above 85%
     */
    public boolean isOverloaded(Host host) {
        return finite(host.getCpuPercentUtilization()) > CPU_OVERLOAD_THRESHOLD
                || finite(host.getRam().getPercentUtilization()) > RAM_OVERLOAD_THRESHOLD;
    }

    /**
     * Checks whether a host has VMs but low enough CPU use to be consolidated.
     *
     * @param host host to inspect
     * @return true when CPU is below 20% and at least one VM is running on the host
     */
    public boolean isUnderloaded(Host host) {
        return finite(host.getCpuPercentUtilization()) < CPU_UNDERLOAD_THRESHOLD && !host.getVmList().isEmpty();
    }

    /**
     * Calculates a balanced host-utilization value from actual CPU, RAM, bandwidth, and storage use.
     *
     * @param host host to inspect
     * @return average utilization from 0.0 to 1.0
     */
    public double getAverageUtilization(Host host) {
        return (finite(host.getCpuPercentUtilization()) + finite(host.getRam().getPercentUtilization())
                + finite(host.getBw().getPercentUtilization()) + finite(host.getStorage().getPercentUtilization())) / 4.0;
    }

    private double finite(double value) { return Double.isFinite(value) ? Math.max(0, Math.min(1, value)) : 0; }
}
