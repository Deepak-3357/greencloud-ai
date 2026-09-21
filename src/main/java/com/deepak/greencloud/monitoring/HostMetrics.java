package com.deepak.greencloud.monitoring;

/**
 * POJO containing host-level resource metrics.
 */
public class HostMetrics {

    private final long hostId;
    private final double cpuUtilization;
    private final double ramUtilization;
    private final double bandwidthUtilization;
    private final double storageUtilization;
    private final long availableRam;
    private final int availablePes;
    private final long availableBandwidth;
    private final long availableStorage;
    private final int runningVmCount;
    private final String status;
    private final double timestamp;

    public HostMetrics(
            long hostId,
            double cpuUtilization,
            double ramUtilization,
            double bandwidthUtilization,
            double storageUtilization,
            long availableRam,
            int availablePes,
            long availableBandwidth,
            long availableStorage,
            int runningVmCount,
            String status,
            double timestamp) {
        this.hostId = hostId;
        this.cpuUtilization = cpuUtilization;
        this.ramUtilization = ramUtilization;
        this.bandwidthUtilization = bandwidthUtilization;
        this.storageUtilization = storageUtilization;
        this.availableRam = availableRam;
        this.availablePes = availablePes;
        this.availableBandwidth = availableBandwidth;
        this.availableStorage = availableStorage;
        this.runningVmCount = runningVmCount;
        this.status = status;
        this.timestamp = timestamp;
    }

    public long getHostId() {
        return hostId;
    }

    public double getCpuUtilization() {
        return cpuUtilization;
    }

    public double getRamUtilization() {
        return ramUtilization;
    }

    public double getBandwidthUtilization() {
        return bandwidthUtilization;
    }

    public double getStorageUtilization() {
        return storageUtilization;
    }

    public long getAvailableRam() {
        return availableRam;
    }

    public int getAvailablePes() {
        return availablePes;
    }

    public long getAvailableBandwidth() {
        return availableBandwidth;
    }

    public long getAvailableStorage() {
        return availableStorage;
    }

    public int getRunningVmCount() {
        return runningVmCount;
    }

    public String getStatus() {
        return status;
    }

    public double getTimestamp() {
        return timestamp;
    }
}
