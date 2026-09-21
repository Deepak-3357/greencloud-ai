package com.deepak.greencloud.monitoring;

import com.deepak.greencloud.ai.EnergySnapshot;
import com.deepak.greencloud.ai.migration.MigrationDecision;
import com.deepak.greencloud.ai.migration.MigrationHistory;
import com.deepak.greencloud.ai.placement.PlacementHistory;
import com.deepak.greencloud.ai.optimization.OptimizationManager;
import com.deepak.greencloud.ai.optimization.SLAManager;
import com.deepak.greencloud.config.SimulationConfig;
import com.deepak.greencloud.constants.Constants;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Exports monitoring data into CSV files for future analysis modules.
 */
public class MetricsExporter {

    /**
     * Exports all monitoring artifacts.
     */
    public void export(
            List<EventRecord> events,
            List<ResourceSnapshot> snapshots,
            List<CloudletMetrics> cloudletMetrics,
            PerformanceSummary summary,
            List<EnergySnapshot> energyHistory,
            MigrationHistory migrationHistory,
            PlacementHistory placementHistory,
            OptimizationManager.OptimizationSummary optimizationSummary,
            SLAManager.SLAComplianceMetrics slaMetrics) {
        try {
            Path reportDirectory = Path.of(SimulationConfig.REPORTS_DIRECTORY);
            Files.createDirectories(reportDirectory);
            exportEvents(reportDirectory.resolve(Constants.EVENT_LOG_FILE), events);
            exportHostMetrics(reportDirectory.resolve(Constants.HOST_METRICS_FILE), snapshots);
            exportVmMetrics(reportDirectory.resolve(Constants.VM_METRICS_FILE), snapshots);
            exportCloudletMetrics(reportDirectory.resolve(Constants.CLOUDLET_METRICS_FILE), cloudletMetrics);
            exportEnergyHistory(reportDirectory.resolve("energy_history.csv"), energyHistory);
            exportMigrationHistory(reportDirectory.resolve("migration_history.csv"), migrationHistory);
            exportHostSummary(reportDirectory.resolve("host_summary.csv"), snapshots);
            exportVmSummary(reportDirectory.resolve("vm_summary.csv"), snapshots, cloudletMetrics);
            exportPlacementSummary(reportDirectory.resolve("placement_summary.csv"), placementHistory, snapshots);
            exportSimulationSummary(reportDirectory.resolve(Constants.SIMULATION_SUMMARY_FILE), summary,
                    optimizationSummary, slaMetrics, energyHistory, migrationHistory);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to export monitoring metrics.", exception);
        }
    }

    private void exportEvents(Path path, List<EventRecord> events) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write("simulation_time,event_type,resource_id,related_resource,description");
            writer.newLine();
            for (EventRecord event : events) {
                writer.write(csv(event.getSimulationTime(), event.getEventType(), event.getResourceId(),
                        event.getRelatedResource(), event.getDescription()));
                writer.newLine();
            }
        }
    }

    private void exportHostMetrics(Path path, List<ResourceSnapshot> snapshots) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write("timestamp,host_id,cpu_utilization,ram_utilization,bandwidth_utilization,storage_utilization,available_ram,available_pes,available_bandwidth,available_storage,running_vm_count,status");
            writer.newLine();
            for (ResourceSnapshot snapshot : snapshots) {
                for (HostMetrics metric : snapshot.getHostMetrics()) {
                    writer.write(csv(metric.getTimestamp(), metric.getHostId(), metric.getCpuUtilization(),
                            metric.getRamUtilization(), metric.getBandwidthUtilization(), metric.getStorageUtilization(),
                            metric.getAvailableRam(), metric.getAvailablePes(), metric.getAvailableBandwidth(),
                            metric.getAvailableStorage(), metric.getRunningVmCount(), metric.getStatus()));
                    writer.newLine();
                }
            }
        }
    }

    private void exportVmMetrics(Path path, List<ResourceSnapshot> snapshots) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write("timestamp,vm_id,host_id,cpu_usage,ram_usage,bandwidth_usage,current_cloudlets,finished_cloudlets,execution_time,status");
            writer.newLine();
            for (ResourceSnapshot snapshot : snapshots) {
                for (VmMetrics metric : snapshot.getVmMetrics()) {
                    writer.write(csv(metric.getTimestamp(), metric.getVmId(), metric.getHostId(), metric.getCpuUsage(),
                            metric.getRamUsage(), metric.getBandwidthUsage(), metric.getCurrentCloudlets(),
                            metric.getFinishedCloudlets(), metric.getExecutionTime(), metric.getStatus()));
                    writer.newLine();
                }
            }
        }
    }

    private void exportCloudletMetrics(Path path, List<CloudletMetrics> cloudletMetrics) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write("cloudlet_id,vm_id,host_id,submission_time,start_time,finish_time,waiting_time,execution_time,cpu_time,status");
            writer.newLine();
            for (CloudletMetrics metric : cloudletMetrics) {
                writer.write(csv(metric.getCloudletId(), metric.getVmId(), metric.getHostId(), metric.getSubmissionTime(),
                        metric.getStartTime(), metric.getFinishTime(), metric.getWaitingTime(), metric.getExecutionTime(),
                        metric.getCpuTime(), metric.getStatus()));
                writer.newLine();
            }
        }
    }

    private void exportEnergyHistory(Path path, List<EnergySnapshot> energyHistory) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write("timestamp,host_id,cpu_utilization,ram_utilization,current_power_watts,accumulated_energy_wh,host_state");
            writer.newLine();
            for (EnergySnapshot s : energyHistory) {
                writer.write(csv(s.getSimulationTime(), s.getHostId(), s.getCpuUtilization(), s.getRamUtilization(),
                        s.getCurrentPowerWatts(), s.getAccumulatedEnergyWattHours(), s.getHostState()));
                writer.newLine();
            }
        }
    }

    private void exportMigrationHistory(Path path, MigrationHistory migrationHistory) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write("migration_time,vm_id,source_host,destination_host,reason,score,cpu_before,cpu_after,energy_saved_wh,status");
            writer.newLine();
            for (MigrationDecision d : migrationHistory.getAll()) {
                writer.write(csv(d.getMigrationTime(), d.getVmId(), d.getSourceHostId(), d.getDestinationHostId(),
                        d.getReason(), d.getMigrationScore(), d.getCpuBefore(), d.getCpuAfter(), d.getEnergySavedEstimateWattHours(), d.getStatus()));
                writer.newLine();
            }
        }
    }

    private void exportHostSummary(Path path, List<ResourceSnapshot> snapshots) throws IOException {
        // Aggregate per-host metrics across snapshots
        Map<Long, HostAggregate> agg = new HashMap<>();
        List<Double> times = new ArrayList<>();
        for (ResourceSnapshot s : snapshots) times.add(s.getSimulationTime());
        List<Double> sortedTimes = times.stream().distinct().sorted().toList();
        double prev = sortedTimes.isEmpty() ? 0 : sortedTimes.get(0);

        for (int i = 0; i < sortedTimes.size(); i++) {
            double t = sortedTimes.get(i);
            double dt = (i + 1 < sortedTimes.size()) ? (sortedTimes.get(i + 1) - t) : SimulationConfig.MONITORING_SNAPSHOT_INTERVAL;
            // find snapshot with this timestamp
            snapshots.stream().filter(s -> s.getSimulationTime() == t).findFirst().ifPresent(snapshot -> {
                for (HostMetrics m : snapshot.getHostMetrics()) {
                    HostAggregate a = agg.computeIfAbsent(m.getHostId(), k -> new HostAggregate());
                    a.sampleCount++;
                    a.cpuSum += m.getCpuUtilization();
                    a.cpuPeak = Math.max(a.cpuPeak, m.getCpuUtilization());
                    a.ramSum += m.getRamUtilization();
                    a.ramPeak = Math.max(a.ramPeak, m.getRamUtilization());
                    a.vmCountSum += m.getRunningVmCount();
                    if ("ACTIVE".equalsIgnoreCase(m.getStatus())) {
                        a.activeTime += dt;
                    } else {
                        a.idleTime += dt;
                    }
                    a.lastStatus = m.getStatus();
                }
            });
        }

        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write("host_id,avg_cpu_pct,peak_cpu_pct,avg_ram_pct,peak_ram_pct,avg_vm_count,active_time_s,idle_time_s,last_status");
            writer.newLine();
            for (Map.Entry<Long, HostAggregate> e : agg.entrySet()) {
                HostAggregate a = e.getValue();
                double avgCpu = a.sampleCount == 0 ? 0 : (a.cpuSum / a.sampleCount) * 100.0;
                double peakCpu = a.cpuPeak * 100.0;
                double avgRam = a.sampleCount == 0 ? 0 : (a.ramSum / a.sampleCount) * 100.0;
                double peakRam = a.ramPeak * 100.0;
                double avgVm = a.sampleCount == 0 ? 0 : (double) a.vmCountSum / a.sampleCount;
                writer.write(csv(e.getKey(), String.format("%.2f", avgCpu), String.format("%.2f", peakCpu),
                        String.format("%.2f", avgRam), String.format("%.2f", peakRam), String.format("%.2f", avgVm),
                        String.format("%.2f", a.activeTime), String.format("%.2f", a.idleTime), a.lastStatus));
                writer.newLine();
            }
        }
    }

    private void exportVmSummary(Path path, List<ResourceSnapshot> snapshots, List<CloudletMetrics> cloudletMetrics) throws IOException {
        Map<Long, VmAggregate> agg = new HashMap<>();
        for (ResourceSnapshot s : snapshots) {
            for (VmMetrics m : s.getVmMetrics()) {
                VmAggregate a = agg.computeIfAbsent(m.getVmId(), k -> new VmAggregate());
                a.sampleCount++;
                a.cpuSum += m.getCpuUsage();
                a.cpuPeak = Math.max(a.cpuPeak, m.getCpuUsage());
                a.lastHost = m.getHostId();
                a.lastStatus = m.getStatus();
            }
        }
        // aggregate cloudlet stats per VM
        Map<Long, List<CloudletMetrics>> cloudletsByVm = new HashMap<>();
        for (CloudletMetrics c : cloudletMetrics) cloudletsByVm.computeIfAbsent(c.getVmId(), k -> new ArrayList<>()).add(c);

        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write("vm_id,host_id_last,avg_cpu_pct,peak_cpu_pct,total_execution_time_s,total_waiting_time_s,completed_cloudlets,failed_cloudlets,last_status");
            writer.newLine();
            for (Map.Entry<Long, VmAggregate> e : agg.entrySet()) {
                long vmId = e.getKey();
                VmAggregate a = e.getValue();
                double avgCpu = a.sampleCount == 0 ? 0 : (a.cpuSum / a.sampleCount) * 100.0;
                double peakCpu = a.cpuPeak * 100.0;
                double totalExec = 0.0;
                double totalWait = 0.0;
                int completed = 0;
                int failed = 0;
                List<CloudletMetrics> list = cloudletsByVm.getOrDefault(vmId, List.of());
                for (CloudletMetrics c : list) {
                    totalExec += c.getExecutionTime();
                    totalWait += c.getWaitingTime();
                    if ("SUCCESS".equalsIgnoreCase(c.getStatus()) || c.getFinishTime() > 0) completed++;
                    else failed++;
                }
                writer.write(csv(vmId, a.lastHost, String.format("%.2f", avgCpu), String.format("%.2f", peakCpu),
                        String.format("%.4f", totalExec), String.format("%.4f", totalWait), completed, failed, a.lastStatus));
                writer.newLine();
            }
        }
    }

    private void exportPlacementSummary(Path path, PlacementHistory placementHistory, List<ResourceSnapshot> snapshots) throws IOException {
        // compute host average loads from snapshots
        Map<Long, List<Double>> hostLoads = new HashMap<>();
        for (ResourceSnapshot s : snapshots) {
            for (HostMetrics m : s.getHostMetrics()) {
                hostLoads.computeIfAbsent(m.getHostId(), k -> new ArrayList<>()).add(m.getCpuUtilization());
            }
        }
        Map<Long, Double> hostAvg = new HashMap<>();
        for (Map.Entry<Long, List<Double>> e : hostLoads.entrySet()) {
            double avg = e.getValue().stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
            hostAvg.put(e.getKey(), avg);
        }
        double overallAvg = hostAvg.values().stream().mapToDouble(Double::doubleValue).average().orElse(0.0);
        double variance = hostAvg.values().stream().mapToDouble(v -> Math.pow(v - overallAvg, 2)).average().orElse(0.0);
        double stdDev = Math.sqrt(variance);
        long balanced = hostAvg.values().stream().filter(v -> Math.abs(v - overallAvg) <= 0.10).count();
        long overloaded = hostAvg.values().stream().filter(v -> v >= 0.85).count();
        long underloaded = hostAvg.values().stream().filter(v -> v <= 0.20).count();

        var stats = placementHistory.getStatistics();

        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write("metric,value"); writer.newLine();
            writer.write(csv("average_load", overallAvg)); writer.newLine();
            writer.write(csv("std_dev", stdDev)); writer.newLine();
            writer.write(csv("balanced_hosts", balanced)); writer.newLine();
            writer.write(csv("overloaded_hosts", overloaded)); writer.newLine();
            writer.write(csv("underloaded_hosts", underloaded)); writer.newLine();
            if (hostAvg.size() > 1 && stdDev > 0.05) {
                writer.write(csv("most_loaded_host", stats.getMostSelectedHostId())); writer.newLine();
                writer.write(csv("least_loaded_host", stats.getLeastSelectedHostId())); writer.newLine();
            } else {
                writer.write(csv("allocation_balance", "Balanced Allocation")); writer.newLine();
            }
        }
    }

    private void exportSimulationSummary(Path path, PerformanceSummary summary,
                                         OptimizationManager.OptimizationSummary optimizationSummary,
                                         SLAManager.SLAComplianceMetrics slaMetrics,
                                         List<EnergySnapshot> energyHistory,
                                         MigrationHistory migrationHistory) throws IOException {
        // compute total energy using latest accumulated energy per host
        Map<Long, Double> latestEnergy = new HashMap<>();
        for (EnergySnapshot s : energyHistory) {
            latestEnergy.merge(s.getHostId(), s.getAccumulatedEnergyWattHours(), Math::max);
        }
        double totalEnergy = latestEnergy.values().stream().mapToDouble(Double::doubleValue).sum();
        int totalMigrations = migrationHistory.getAll().size();

        try (BufferedWriter writer = Files.newBufferedWriter(path)) {
            writer.write("metric,value"); writer.newLine();
            writer.write(csv("simulation_duration_s", summary.getMakespan())); writer.newLine();
            writer.write(csv("hosts", latestEnergy.size())); writer.newLine();
            writer.write(csv("vms", "N/A")); writer.newLine();
            writer.write(csv("cloudlets", "N/A")); writer.newLine();
            writer.write(csv("energy_consumed_wh", String.format("%.4f", totalEnergy))); writer.newLine();
            // Backwards compatibility keys expected by the Dashboard
            writer.write(csv("total_energy", String.format("%.4f", totalEnergy))); writer.newLine();
            writer.write(csv("average_host_utilization", summary.getAverageHostUtilization())); writer.newLine();
            writer.write(csv("average_vm_utilization", summary.getAverageVmUtilization())); writer.newLine();
            writer.write(csv("total_migrations", totalMigrations)); writer.newLine();
            writer.write(csv("total_sla_violations", slaMetrics.getTotalViolations())); writer.newLine();
            writer.write(csv("sla_violations", slaMetrics.getTotalViolations())); writer.newLine();
            writer.write(csv("placement_efficiency", optimizationSummary.getAverageOptimizationScore())); writer.newLine();
            writer.write(csv("optimization_efficiency", optimizationSummary.getAverageOptimizationScore())); writer.newLine();
        }
    }

    private String csv(Object... values) {
        StringBuilder builder = new StringBuilder();
        for (int index = 0; index < values.length; index++) {
            if (index > 0) {
                builder.append(',');
            }
            builder.append(escape(String.valueOf(values[index])));
        }
        return builder.toString();
    }

    private String escape(String value) {
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }

    private static final class HostAggregate {
        int sampleCount = 0;
        double cpuSum = 0.0;
        double cpuPeak = 0.0;
        double ramSum = 0.0;
        double ramPeak = 0.0;
        int vmCountSum = 0;
        double activeTime = 0.0;
        double idleTime = 0.0;
        String lastStatus = "N/A";
    }

    private static final class VmAggregate {
        int sampleCount = 0;
        double cpuSum = 0.0;
        double cpuPeak = 0.0;
        long lastHost = -1;
        String lastStatus = "N/A";
    }
}
