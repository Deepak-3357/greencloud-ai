package com.deepak.greencloud.monitoring;

/**
 * POJO containing VM-level execution and utilization metrics.
 */
public class VmMetrics {

    private final long vmId;
    private final long hostId;
    private final double cpuUsage;
    private final double ramUsage;
    private final double bandwidthUsage;
    private final int currentCloudlets;
    private final int finishedCloudlets;
    private final double executionTime;
    private final String status;
    private final double timestamp;

    public VmMetrics(
            long vmId,
            long hostId,
            double cpuUsage,
            double ramUsage,
            double bandwidthUsage,
            int currentCloudlets,
            int finishedCloudlets,
            double executionTime,
            String status,
            double timestamp) {
        this.vmId = vmId;
        this.hostId = hostId;
        this.cpuUsage = cpuUsage;
        this.ramUsage = ramUsage;
        this.bandwidthUsage = bandwidthUsage;
        this.currentCloudlets = currentCloudlets;
        this.finishedCloudlets = finishedCloudlets;
        this.executionTime = executionTime;
        this.status = status;
        this.timestamp = timestamp;
    }

    public long getVmId() {
        return vmId;
    }

    public long getHostId() {
        return hostId;
    }

    public double getCpuUsage() {
        return cpuUsage;
    }

    public double getRamUsage() {
        return ramUsage;
    }

    public double getBandwidthUsage() {
        return bandwidthUsage;
    }

    public int getCurrentCloudlets() {
        return currentCloudlets;
    }

    public int getFinishedCloudlets() {
        return finishedCloudlets;
    }

    public double getExecutionTime() {
        return executionTime;
    }

    public String getStatus() {
        return status;
    }

    public double getTimestamp() {
        return timestamp;
    }
}
