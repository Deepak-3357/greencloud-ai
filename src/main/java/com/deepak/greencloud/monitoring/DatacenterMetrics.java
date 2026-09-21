package com.deepak.greencloud.monitoring;

/**
 * POJO containing aggregate datacenter metrics.
 */
public class DatacenterMetrics {

    private final int totalHosts;
    private final int totalVms;
    private final int allocatedVms;
    private final int runningVms;
    private final int idleVms;
    private final int completedCloudlets;
    private final int failedCloudlets;
    private final int waitingCloudlets;
    private final double averageCpuUtilization;
    private final double averageRamUtilization;
    private final double averageBandwidthUtilization;
    private final double simulationTime;

    public DatacenterMetrics(
            int totalHosts,
            int totalVms,
            int allocatedVms,
            int runningVms,
            int idleVms,
            int completedCloudlets,
            int failedCloudlets,
            int waitingCloudlets,
            double averageCpuUtilization,
            double averageRamUtilization,
            double averageBandwidthUtilization,
            double simulationTime) {
        this.totalHosts = totalHosts;
        this.totalVms = totalVms;
        this.allocatedVms = allocatedVms;
        this.runningVms = runningVms;
        this.idleVms = idleVms;
        this.completedCloudlets = completedCloudlets;
        this.failedCloudlets = failedCloudlets;
        this.waitingCloudlets = waitingCloudlets;
        this.averageCpuUtilization = averageCpuUtilization;
        this.averageRamUtilization = averageRamUtilization;
        this.averageBandwidthUtilization = averageBandwidthUtilization;
        this.simulationTime = simulationTime;
    }

    public int getTotalHosts() {
        return totalHosts;
    }

    public int getTotalVms() {
        return totalVms;
    }

    public int getAllocatedVms() {
        return allocatedVms;
    }

    public int getRunningVms() {
        return runningVms;
    }

    public int getIdleVms() {
        return idleVms;
    }

    public int getCompletedCloudlets() {
        return completedCloudlets;
    }

    public int getFailedCloudlets() {
        return failedCloudlets;
    }

    public int getWaitingCloudlets() {
        return waitingCloudlets;
    }

    public double getAverageCpuUtilization() {
        return averageCpuUtilization;
    }

    public double getAverageRamUtilization() {
        return averageRamUtilization;
    }

    public double getAverageBandwidthUtilization() {
        return averageBandwidthUtilization;
    }

    public double getSimulationTime() {
        return simulationTime;
    }
}
