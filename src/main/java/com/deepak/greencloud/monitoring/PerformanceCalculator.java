package com.deepak.greencloud.monitoring;

import com.deepak.greencloud.constants.Constants;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Calculates performance indicators from captured monitoring metrics.
 */
public class PerformanceCalculator {

    /**
     * Calculates a performance summary.
     *
     * @param snapshots resource snapshots
     * @param cloudletMetrics cloudlet metrics
     * @param totalVms total requested VMs
     * @param allocatedVms allocated VMs
     * @return performance summary
     */
    public PerformanceSummary calculate(
            List<ResourceSnapshot> snapshots,
            List<CloudletMetrics> cloudletMetrics,
            int totalVms,
            int allocatedVms) {
        List<HostMetrics> hostMetrics = uniqueHostMetrics(snapshots);
        List<VmMetrics> vmMetrics = uniqueVmMetrics(snapshots);
        List<CloudletMetrics> completedCloudlets = cloudletMetrics.stream()
                .filter(metric -> "SUCCESS".equals(metric.getStatus()))
                .toList();

        double makespan = calculateMakespan(completedCloudlets);
        double averageCpuUsage = average(hostMetrics.stream().mapToDouble(HostMetrics::getCpuUtilization).toArray());
        double averageRamUsage = average(hostMetrics.stream().mapToDouble(HostMetrics::getRamUtilization).toArray());
        double averageBandwidthUsage = average(hostMetrics.stream().mapToDouble(HostMetrics::getBandwidthUtilization).toArray());
        double averageStorageUsage = average(hostMetrics.stream().mapToDouble(HostMetrics::getStorageUtilization).toArray());
        double averageHostUtilization = average(new double[] {
                averageCpuUsage,
                averageRamUsage,
                averageBandwidthUsage,
                averageStorageUsage
        });
        double averageVmUtilization = average(vmMetrics.stream().mapToDouble(VmMetrics::getCpuUsage).toArray());
        double throughput = makespan == 0 ? 0 : completedCloudlets.size() / makespan;
        double completionRate = calculatePercentage(completedCloudlets.size(), cloudletMetrics.size());
        double allocationSuccessRate = calculatePercentage(allocatedVms, totalVms);
        double resourceEfficiency = average(new double[] { completionRate, allocationSuccessRate, averageHostUtilization });

        return new PerformanceSummary(
                averageHostUtilization,
                averageVmUtilization,
                averageCpuUsage,
                averageRamUsage,
                averageBandwidthUsage,
                averageStorageUsage,
                throughput,
                makespan,
                average(completedCloudlets.stream().mapToDouble(CloudletMetrics::getExecutionTime).toArray()),
                completionRate,
                allocationSuccessRate,
                resourceEfficiency,
                averageHostUtilization);
    }

    private List<HostMetrics> uniqueHostMetrics(List<ResourceSnapshot> snapshots) {
        Map<SampleKey, HostMetrics> unique = new LinkedHashMap<>();
        for (ResourceSnapshot snapshot : snapshots) {
            for (HostMetrics metric : snapshot.getHostMetrics()) {
                unique.putIfAbsent(new SampleKey(metric.getTimestamp(), metric.getHostId()), metric);
            }
        }
        return new ArrayList<>(unique.values());
    }

    private List<VmMetrics> uniqueVmMetrics(List<ResourceSnapshot> snapshots) {
        Map<SampleKey, VmMetrics> unique = new LinkedHashMap<>();
        for (ResourceSnapshot snapshot : snapshots) {
            for (VmMetrics metric : snapshot.getVmMetrics()) {
                unique.putIfAbsent(new SampleKey(metric.getTimestamp(), metric.getVmId()), metric);
            }
        }
        return new ArrayList<>(unique.values());
    }

    private record SampleKey(long timestampBucket, long resourceId) {
        SampleKey(double timestamp, long resourceId) {
            this(Math.round(timestamp / Constants.MONITORING_TIME_EPSILON), resourceId);
        }
    }

    private double calculateMakespan(List<CloudletMetrics> cloudletMetrics) {
        if (cloudletMetrics.isEmpty()) {
            return 0;
        }

        double earliestStart = cloudletMetrics.stream()
                .mapToDouble(CloudletMetrics::getStartTime)
                .min()
                .orElse(0);
        double latestFinish = cloudletMetrics.stream()
                .mapToDouble(CloudletMetrics::getFinishTime)
                .max()
                .orElse(0);
        return Math.max(0, latestFinish - earliestStart);
    }

    private double calculatePercentage(double value, double total) {
        if (total == 0) {
            return 0;
        }
        return value / total;
    }

    private double average(double[] values) {
        if (values.length == 0) {
            return 0;
        }

        double total = 0;
        for (double value : values) {
            total += value;
        }
        return total / values.length;
    }
}
