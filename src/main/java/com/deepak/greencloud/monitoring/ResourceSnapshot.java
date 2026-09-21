package com.deepak.greencloud.monitoring;

import java.util.List;

/**
 * Represents a point-in-time snapshot of the simulated cloud state.
 */
public class ResourceSnapshot {

    private final double simulationTime;
    private final List<HostMetrics> hostMetrics;
    private final List<VmMetrics> vmMetrics;
    private final DatacenterMetrics datacenterMetrics;

    public ResourceSnapshot(
            double simulationTime,
            List<HostMetrics> hostMetrics,
            List<VmMetrics> vmMetrics,
            DatacenterMetrics datacenterMetrics) {
        this.simulationTime = simulationTime;
        this.hostMetrics = List.copyOf(hostMetrics);
        this.vmMetrics = List.copyOf(vmMetrics);
        this.datacenterMetrics = datacenterMetrics;
    }

    public double getSimulationTime() {
        return simulationTime;
    }

    public List<HostMetrics> getHostMetrics() {
        return hostMetrics;
    }

    public List<VmMetrics> getVmMetrics() {
        return vmMetrics;
    }

    public DatacenterMetrics getDatacenterMetrics() {
        return datacenterMetrics;
    }
}
