package com.deepak.greencloud.monitoring;

/**
 * POJO containing calculated performance indicators.
 */
public class PerformanceSummary {

    private final double averageHostUtilization;
    private final double averageVmUtilization;
    private final double averageCpuUsage;
    private final double averageRamUsage;
    private final double averageBandwidthUsage;
    private final double averageStorageUsage;
    private final double throughput;
    private final double makespan;
    private final double averageCloudletExecutionTime;
    private final double cloudletCompletionRate;
    private final double vmAllocationSuccessRate;
    private final double resourceEfficiency;
    private final double hostUtilizationEfficiency;

    public PerformanceSummary(
            double averageHostUtilization,
            double averageVmUtilization,
            double averageCpuUsage,
            double averageRamUsage,
            double averageBandwidthUsage,
            double averageStorageUsage,
            double throughput,
            double makespan,
            double averageCloudletExecutionTime,
            double cloudletCompletionRate,
            double vmAllocationSuccessRate,
            double resourceEfficiency,
            double hostUtilizationEfficiency) {
        this.averageHostUtilization = averageHostUtilization;
        this.averageVmUtilization = averageVmUtilization;
        this.averageCpuUsage = averageCpuUsage;
        this.averageRamUsage = averageRamUsage;
        this.averageBandwidthUsage = averageBandwidthUsage;
        this.averageStorageUsage = averageStorageUsage;
        this.throughput = throughput;
        this.makespan = makespan;
        this.averageCloudletExecutionTime = averageCloudletExecutionTime;
        this.cloudletCompletionRate = cloudletCompletionRate;
        this.vmAllocationSuccessRate = vmAllocationSuccessRate;
        this.resourceEfficiency = resourceEfficiency;
        this.hostUtilizationEfficiency = hostUtilizationEfficiency;
    }

    public double getAverageHostUtilization() {
        return averageHostUtilization;
    }

    public double getAverageVmUtilization() {
        return averageVmUtilization;
    }

    public double getAverageCpuUsage() {
        return averageCpuUsage;
    }

    public double getAverageRamUsage() {
        return averageRamUsage;
    }

    public double getAverageBandwidthUsage() {
        return averageBandwidthUsage;
    }

    public double getAverageStorageUsage() {
        return averageStorageUsage;
    }

    public double getThroughput() {
        return throughput;
    }

    public double getMakespan() {
        return makespan;
    }

    public double getAverageCloudletExecutionTime() {
        return averageCloudletExecutionTime;
    }

    public double getCloudletCompletionRate() {
        return cloudletCompletionRate;
    }

    public double getVmAllocationSuccessRate() {
        return vmAllocationSuccessRate;
    }

    public double getResourceEfficiency() {
        return resourceEfficiency;
    }

    public double getHostUtilizationEfficiency() {
        return hostUtilizationEfficiency;
    }
}
