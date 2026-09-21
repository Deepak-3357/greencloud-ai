package com.deepak.greencloud.monitoring;

import com.deepak.greencloud.config.SimulationConfig;
import com.deepak.greencloud.ai.EnergyCalculator;
import com.deepak.greencloud.ai.EnergySnapshot;
import com.deepak.greencloud.ai.HostScore;
import com.deepak.greencloud.ai.HostScoringEngine;
import com.deepak.greencloud.ai.placement.AIPlacementManager;
import com.deepak.greencloud.ai.placement.PlacementDecision;
import com.deepak.greencloud.ai.placement.PlacementHistory;
import com.deepak.greencloud.ai.migration.MigrationDecision;
import com.deepak.greencloud.ai.migration.MigrationHistory;
import com.deepak.greencloud.ai.migration.MigrationManager;
import com.deepak.greencloud.ai.optimization.OptimizationManager;
import com.deepak.greencloud.ai.optimization.OptimizationDecision;
import com.deepak.greencloud.constants.Constants;
import com.deepak.greencloud.utils.LoggerUtil;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.cloudsimplus.brokers.DatacenterBroker;
import org.cloudsimplus.cloudlets.Cloudlet;
import org.cloudsimplus.core.CloudSimPlus;
import org.cloudsimplus.datacenters.Datacenter;
import org.cloudsimplus.hosts.Host;
import org.cloudsimplus.vms.Vm;

/**
 * Coordinates Module 2 monitoring, event tracking, snapshots, metrics, console reports and CSV exports.
 */
public class MonitoringManager {

    private final EventLogger eventLogger;
    private final ResourceMonitor resourceMonitor;
    private final PerformanceCalculator performanceCalculator;
    private final MetricsExporter metricsExporter;
    private final LiveMonitor liveMonitor;
    private final EnergyCalculator energyCalculator;
    private final HostScoringEngine hostScoringEngine;
    private final MigrationManager migrationManager;
    private final OptimizationManager optimizationManager;
    private AIPlacementManager aiPlacementManager;
    private final List<ResourceSnapshot> snapshots;
    private final List<MonitoringListener> listeners;
    private CloudSimPlus simulation;
    private Datacenter datacenter;
    private DatacenterBroker broker;
    private List<Host> hosts;
    private List<Vm> vms;
    private List<Cloudlet> cloudlets;
    private List<CloudletMetrics> cloudletMetrics;
    private PerformanceSummary performanceSummary;
    private double lastSnapshotTime;
    private double lastMetricsDisplayTime;

    public MonitoringManager() {
        this.eventLogger = new EventLogger();
        this.resourceMonitor = new ResourceMonitor();
        this.performanceCalculator = new PerformanceCalculator();
        this.metricsExporter = new MetricsExporter();
        this.liveMonitor = new LiveMonitor(resourceMonitor);
        this.energyCalculator = new EnergyCalculator();
        this.hostScoringEngine = new HostScoringEngine(energyCalculator.getEnergyModel());
        this.migrationManager = new MigrationManager(hostScoringEngine, energyCalculator);
        this.optimizationManager = new OptimizationManager(migrationManager, energyCalculator, hostScoringEngine);
        this.snapshots = new ArrayList<>();
        this.listeners = new CopyOnWriteArrayList<>();
        this.hosts = List.of();
        this.vms = List.of();
        this.cloudlets = List.of();
        this.cloudletMetrics = List.of();
        this.lastSnapshotTime = -SimulationConfig.MONITORING_SNAPSHOT_INTERVAL;
        this.lastMetricsDisplayTime = -SimulationConfig.MONITORING_INTERVAL;
    }

    /**
     * Initializes monitoring dependencies and listeners.
     *
     * @param simulation CloudSim Plus simulation
     * @param datacenter monitored datacenter
     * @param broker monitored broker
     * @param hosts monitored hosts
     * @param vms monitored VMs
     * @param cloudlets monitored cloudlets
     */
    public void initialize(
            CloudSimPlus simulation,
            Datacenter datacenter,
            DatacenterBroker broker,
            List<Host> hosts,
            List<Vm> vms,
            List<Cloudlet> cloudlets) {
        initialize(simulation, datacenter, broker, hosts, vms, cloudlets, new AIPlacementManager());
    }

    /**
     * Initializes monitoring dependencies and the shared AI placement manager.
     *
     * @param simulation CloudSim Plus simulation
     * @param datacenter monitored datacenter
     * @param broker monitored broker
     * @param hosts monitored hosts
     * @param vms monitored VMs
     * @param cloudlets monitored cloudlets
     * @param aiPlacementManager manager used for VM host selection
     */
    public void initialize(
            CloudSimPlus simulation,
            Datacenter datacenter,
            DatacenterBroker broker,
            List<Host> hosts,
            List<Vm> vms,
            List<Cloudlet> cloudlets,
            AIPlacementManager aiPlacementManager) {
        this.simulation = simulation;
        this.datacenter = datacenter;
        this.broker = broker;
        this.hosts = List.copyOf(hosts);
        this.vms = List.copyOf(vms);
        this.cloudlets = List.copyOf(cloudlets);
        this.aiPlacementManager = aiPlacementManager;

        liveMonitor.initialize(this.hosts, this.vms, this.cloudlets, broker, eventLogger);

        registerSimulationListeners();
        registerHostListeners();
        registerVmListeners();
        registerCloudletListeners();
        logStaticCreationEvents();
        captureSnapshot(0);
    }

    /**
     * Starts active monitoring.
     */
    public void startMonitoring() {
        eventLogger.log(simulation.clock(), Constants.EVENT_DATACENTER_STARTED, datacenter.getId(), "Datacenter",
                "Datacenter " + datacenter.getId() + " started");
        // Use displayLiveMonitoring so AI sections are printed in sync with the live monitor
        displayLiveMonitoring(simulation.clock());
    }

    /** Registers a consumer for throttled live monitoring snapshots. */
    public void addMonitoringListener(MonitoringListener listener) {
        if (listener != null) listeners.add(listener);
    }

    /**
     * Stops monitoring and generates final in-memory reports.
     */
    public void stopMonitoring() {
        double currentTime = simulation.clock();
        eventLogger.log(currentTime, Constants.EVENT_DATACENTER_FINISHED, datacenter.getId(), "Datacenter",
                "Datacenter " + datacenter.getId() + " finished");
        eventLogger.log(currentTime, Constants.EVENT_SIMULATION_FINISHED, 0, "Simulation",
                Constants.EVENT_SIMULATION_FINISHED);
        logFailedCloudlets();
        captureSnapshot(currentTime);
        cloudletMetrics = resourceMonitor.collectCloudletMetrics(cloudlets);
        performanceSummary = performanceCalculator.calculate(
                snapshots,
                cloudletMetrics,
                vms.size(),
                broker.getVmCreatedList().size());
        publishLiveUpdate();
    }

    /**
     * Generates console and CSV monitoring reports.
     */
    public void generateReports() {
        eventLogger.printTimeline();
        printResourceUtilization();
        printPerformanceSummary();
        printEnergySummary();
        printHostRanking();
        printPlacementSummary();
        printMigrationSummary();
        printOptimizationSummary();
        // Gather additional analytics from managers
        var energyHistory = energyCalculator.getEnergyHistory();
        var migrationHistory = migrationManager.getMigrationHistory();
        var placementHistory = aiPlacementManager.getPlacementHistory();
        var optimizationSummary = optimizationManager.generateSummary();
        var slaMetrics = optimizationManager.getSLAManager().getComplianceMetrics();

        metricsExporter.export(
                eventLogger.getEvents(),
                snapshots,
                cloudletMetrics,
                performanceSummary,
                energyHistory,
                migrationHistory,
                placementHistory,
                optimizationSummary,
                slaMetrics);

    }

    /**
     * Prints the current live monitoring console with all metrics.
     *
     * @param simulationTime current simulation time
     */
    public void displayLiveMonitoring(double simulationTime) {
        if (!SimulationConfig.MONITORING_ENABLED) {
            return;
        }

        boolean printed = liveMonitor.displayIfInterval(simulationTime);
        // Only display the AI and ENERGY sections when the live monitor actually printed
        if (!printed) return;

        // Prevent duplicate metrics display within the same monitoring interval
        if (simulationTime - lastMetricsDisplayTime + Constants.MONITORING_TIME_EPSILON < SimulationConfig.MONITORING_INTERVAL) {
            return;
        }

        publishLiveUpdate();

        displayEnergyMetrics();
        displayPlacementMetrics();
        displayMigrationMetrics();
        displayOptimizationMetrics();
        lastMetricsDisplayTime = simulationTime;
    }

    private void registerSimulationListeners() {
        simulation.addOnSimulationStartListener(info -> eventLogger.log(
                info.getTime(),
                Constants.EVENT_SIMULATION_STARTED,
                0,
                "Simulation",
                Constants.EVENT_SIMULATION_STARTED));
        simulation.addOnClockTickListener(info -> {
            if (info.getTime() - lastSnapshotTime >= SimulationConfig.MONITORING_SNAPSHOT_INTERVAL) {
                captureSnapshot(info.getTime());
            }
        });
    }

    private void registerHostListeners() {
        hosts.forEach(host -> {
            host.addOnStartupListener(info -> eventLogger.log(
                    info.getTime(),
                    Constants.EVENT_DATACENTER_STARTED,
                    info.getHost().getId(),
                    "Host",
                    "Host " + info.getHost().getId() + " started"));
            host.addOnShutdownListener(info -> eventLogger.log(
                    info.getTime(),
                    Constants.EVENT_DATACENTER_FINISHED,
                    info.getHost().getId(),
                    "Host",
                    "Host " + info.getHost().getId() + " shut down"));
            host.addOnUpdateProcessingListener(info -> captureSnapshot(info.getTime()));
        });
    }

    private void registerVmListeners() {
        vms.forEach(vm -> {
            vm.addOnHostAllocationListener(info -> {
                resourceMonitor.recordVmHostAllocation(info.getVm().getId(), info.getHost().getId());
                eventLogger.log(
                        info.getTime(),
                        Constants.EVENT_VM_ALLOCATED,
                        info.getVm().getId(),
                        "Host " + info.getHost().getId(),
                        "VM " + info.getVm().getId() + " allocated to Host " + info.getHost().getId());
            });
            vm.addOnHostDeallocationListener(info -> eventLogger.log(
                    info.getTime(),
                    Constants.EVENT_VM_DESTROYED,
                    info.getVm().getId(),
                    "Host " + info.getHost().getId(),
                    "VM " + info.getVm().getId() + " destroyed from Host " + info.getHost().getId()));
            vm.addOnCreationFailureListener(info -> eventLogger.log(
                    info.getTime(),
                    Constants.EVENT_VM_FAILED,
                    info.getVm().getId(),
                    "Datacenter",
                    "VM " + info.getVm().getId() + " failed to allocate"));
        });
    }

    private void registerCloudletListeners() {
        cloudlets.forEach(cloudlet -> {
            cloudlet.addOnStartListener(info -> eventLogger.log(
                    info.getTime(),
                    Constants.EVENT_CLOUDLET_STARTED,
                    info.getCloudlet().getId(),
                    "VM " + info.getVm().getId(),
                    "Cloudlet " + info.getCloudlet().getId() + " started on VM " + info.getVm().getId()));
            cloudlet.addOnFinishListener(info -> eventLogger.log(
                    info.getTime(),
                    Constants.EVENT_CLOUDLET_FINISHED,
                    info.getCloudlet().getId(),
                    "VM " + info.getVm().getId(),
                    "Cloudlet " + info.getCloudlet().getId() + " completed"));
        });
    }

    private void logStaticCreationEvents() {
        hosts.forEach(host -> eventLogger.log(simulation.clock(), Constants.EVENT_HOST_CREATED, host.getId(), "Datacenter",
                "Host " + host.getId() + " created"));
        vms.forEach(vm -> eventLogger.log(simulation.clock(), Constants.EVENT_VM_CREATED, vm.getId(), "Broker",
                "VM " + vm.getId() + " created"));
        cloudlets.forEach(cloudlet -> eventLogger.log(simulation.clock(), Constants.EVENT_CLOUDLET_SUBMITTED,
                cloudlet.getId(), "Broker", "Cloudlet " + cloudlet.getId() + " submitted"));
    }

    private void logFailedCloudlets() {
        cloudlets.stream()
                .filter(cloudlet -> cloudlet.getStatus().name().startsWith("FAILED"))
                .forEach(cloudlet -> eventLogger.log(
                        simulation.clock(),
                        Constants.EVENT_CLOUDLET_FAILED,
                        cloudlet.getId(),
                        "Cloudlet",
                        "Cloudlet " + cloudlet.getId() + " failed with status " + cloudlet.getStatus()));
    }

    private void captureSnapshot(double timestamp) {
        if (timestamp < lastSnapshotTime - Constants.MONITORING_TIME_EPSILON) {
            return;
        }
        if (Math.abs(timestamp - lastSnapshotTime) <= Constants.MONITORING_TIME_EPSILON) {
            // Preserve the existing Module 2 evaluation cadence while preventing
            // duplicate host/VM and energy records for the same timestamp.
            evaluatePolicies(timestamp);
            return;
        }
        DatacenterMetrics datacenterMetrics = resourceMonitor.collectDatacenterMetrics(hosts, vms, cloudlets, broker, timestamp);
        snapshots.add(new ResourceSnapshot(
                timestamp,
                resourceMonitor.collectHostMetrics(hosts, timestamp),
                resourceMonitor.collectVmMetrics(vms, timestamp),
                datacenterMetrics));
        energyCalculator.capture(hosts, timestamp);
        evaluatePolicies(timestamp);
        publishLiveUpdate();
        lastSnapshotTime = timestamp;
    }

    private void publishLiveUpdate() {
        if (listeners.isEmpty() || snapshots.isEmpty()) return;
        ResourceSnapshot snapshot = snapshots.get(snapshots.size() - 1);
        listeners.forEach(listener -> listener.onMonitoringUpdate(new LiveMonitoringData(
                snapshot,
                energyCalculator.getTotalEnergy(),
                energyCalculator.getEnergyHistory(),
                migrationManager.getMigrationHistory().getAll(),
                optimizationManager.getSLAManager().getComplianceMetrics().getTotalViolations())));
    }

    private void evaluatePolicies(double timestamp) {
        migrationManager.evaluate(hosts, datacenter, timestamp).forEach(this::logMigrationDecision);
        optimizationManager.evaluate(hosts, cloudlets, datacenter, timestamp);
    }

    private void printResourceUtilization() {
        LoggerUtil.info(Constants.SEPARATOR);
        LoggerUtil.info(Constants.RESOURCE_UTILIZATION_TITLE);
        LoggerUtil.info(Constants.SEPARATOR);
        if (snapshots.isEmpty()) {
            return;
        }

        ResourceSnapshot busiestSnapshot = snapshots.stream()
                .max((first, second) -> Double.compare(
                        calculateSnapshotHostUtilization(first),
                        calculateSnapshotHostUtilization(second)))
                .orElse(snapshots.get(snapshots.size() - 1));
        busiestSnapshot.getHostMetrics().forEach(metric -> LoggerUtil.info(String.format(
                Constants.HOST_RESOURCE_BLOCK_FORMAT,
                metric.getHostId(),
                toPercent(metric.getCpuUtilization()),
                toPercent(metric.getRamUtilization()),
                toPercent(metric.getBandwidthUtilization()),
                toPercent(metric.getStorageUtilization()),
                metric.getRunningVmCount())));
    }

    private double calculateSnapshotHostUtilization(ResourceSnapshot snapshot) {
        if (snapshot.getHostMetrics().isEmpty()) {
            return 0;
        }

        return snapshot.getHostMetrics().stream()
                .mapToDouble(metric -> (metric.getCpuUtilization()
                        + metric.getRamUtilization()
                        + metric.getBandwidthUtilization()
                        + metric.getStorageUtilization()) / Constants.MONITORED_HOST_RESOURCE_DIMENSIONS)
                .average()
                .orElse(0);
    }

    private void printPerformanceSummary() {
        LoggerUtil.info(Constants.SEPARATOR);
        LoggerUtil.info(Constants.PERFORMANCE_SUMMARY_TITLE);
        LoggerUtil.info(Constants.SEPARATOR);
        LoggerUtil.summary("Total Hosts", hosts.size());
        LoggerUtil.summary("Total VMs", vms.size());
        LoggerUtil.summary("Completed Cloudlets", broker.getCloudletFinishedList().size());
        LoggerUtil.summary("Failed Cloudlets", cloudlets.stream()
                .filter(cloudlet -> cloudlet.getStatus().name().startsWith("FAILED"))
                .count());
        LoggerUtil.summary("Average CPU Usage", formatPercent(performanceSummary.getAverageCpuUsage()));
        LoggerUtil.summary("Average RAM Usage", formatPercent(performanceSummary.getAverageRamUsage()));
        LoggerUtil.summary("Average Bandwidth Usage", formatPercent(performanceSummary.getAverageBandwidthUsage()));
        LoggerUtil.summary("Average Storage Usage", formatPercent(performanceSummary.getAverageStorageUsage()));
        LoggerUtil.summary("Throughput", String.format("%.4f cloudlets/second", performanceSummary.getThroughput()));
        LoggerUtil.summary("Makespan", String.format(Constants.SECONDS_FORMAT, performanceSummary.getMakespan()));
        LoggerUtil.summary("Average Execution Time", String.format(Constants.SECONDS_FORMAT,
                performanceSummary.getAverageCloudletExecutionTime()));
        LoggerUtil.summary("Resource Efficiency", formatPercent(performanceSummary.getResourceEfficiency()));
    }

    private void displayEnergyMetrics() {
        LoggerUtil.info("ENERGY");
        LoggerUtil.summary("Current Total Power", String.format("%.2f W", energyCalculator.getCurrentTotalPower()));
        LoggerUtil.summary("Current Total Energy", String.format("%.4f Wh", energyCalculator.getTotalEnergy()));
        LoggerUtil.summary("Average Host Power", String.format("%.2f W", energyCalculator.getAverageHostPower()));
        LoggerUtil.info(Constants.LIVE_SECTION_SEPARATOR);
    }

    private void printEnergySummary() {
        LoggerUtil.info(Constants.SEPARATOR);
        LoggerUtil.info("ENERGY SUMMARY");
        LoggerUtil.info(Constants.SEPARATOR);
        LoggerUtil.summary("Total Energy Consumed", String.format("%.4f Wh", energyCalculator.getTotalEnergy()));
        LoggerUtil.summary("Average Host Energy", String.format("%.4f Wh", energyCalculator.getAverageEnergy()));
        LoggerUtil.summary("Peak Host Energy", String.format("%.4f Wh", energyCalculator.getPeakEnergy()));
        LoggerUtil.summary("Lowest Energy Host", formatEnergyHost(energyCalculator.getLowestEnergyHost()));
        LoggerUtil.summary("Highest Energy Host", formatEnergyHost(energyCalculator.getHighestEnergyHost()));
        LoggerUtil.info(Constants.SEPARATOR);
    }

    private void printHostRanking() {
        LoggerUtil.info("HOST RANKING");
        hostScoringEngine.evaluate(hosts).forEach(score -> {
            LoggerUtil.info("Host " + score.getHostId());
            LoggerUtil.summary("Score", String.format("%.2f", score.getFinalScore()));
            LoggerUtil.summary("CPU", String.format("%.2f", score.getCpuScore()));
            LoggerUtil.summary("RAM", String.format("%.2f", score.getRamScore()));
            LoggerUtil.summary("Energy", String.format("%.2f", score.getEnergyScore()));
            LoggerUtil.summary("Reason", score.getReason());
        });
    }

    private String formatEnergyHost(java.util.Optional<EnergySnapshot> snapshot) {
        return snapshot.map(value -> String.format("Host %d (%.4f Wh)", value.getHostId(),
                value.getAccumulatedEnergyWattHours())).orElse("N/A");
    }

    private void displayPlacementMetrics() {
        PlacementHistory.PlacementStatistics statistics = aiPlacementManager.getPlacementHistory().getStatistics();
        LoggerUtil.info("AI PLACEMENT");
        LoggerUtil.summary("Total Decisions", statistics.getTotalDecisions());
        LoggerUtil.summary("Latest Placement", formatLatestPlacement(aiPlacementManager.getPlacementHistory().getLatest()));
        LoggerUtil.summary("Best Score", String.format("%.2f", statistics.getBestPlacementScore()));
        LoggerUtil.summary("Average Score", String.format("%.2f", statistics.getAveragePlacementScore()));
        LoggerUtil.info(Constants.LIVE_SECTION_SEPARATOR);
    }

    private void printPlacementSummary() {
        PlacementHistory.PlacementStatistics statistics = aiPlacementManager.getPlacementHistory().getStatistics();
        LoggerUtil.info(Constants.SEPARATOR);
        LoggerUtil.info("AI PLACEMENT SUMMARY");
        LoggerUtil.info(Constants.SEPARATOR);
        LoggerUtil.summary("Total Placement Decisions", statistics.getTotalDecisions());
        LoggerUtil.summary("Average Placement Score", String.format("%.2f", statistics.getAveragePlacementScore()));
        LoggerUtil.summary("Best Placement Score", String.format("%.2f", statistics.getBestPlacementScore()));
        LoggerUtil.summary("Worst Placement Score", String.format("%.2f", statistics.getWorstPlacementScore()));
        LoggerUtil.summary("Most Selected Host", formatHostId(statistics.getMostSelectedHostId()));
        LoggerUtil.summary("Least Selected Host", formatHostId(statistics.getLeastSelectedHostId()));
        LoggerUtil.info(Constants.SEPARATOR);
    }

    private String formatLatestPlacement(java.util.Optional<PlacementDecision> decision) {
        return decision.map(value -> String.format("VM %d -> Host %d (%.2f)", value.getVmId(),
                value.getSelectedHostId(), value.getPlacementScore())).orElse("N/A");
    }

    private String formatHostId(long hostId) {
        return hostId < 0 ? "N/A" : "Host " + hostId;
    }

    private void displayMigrationMetrics() {
        MigrationHistory.MigrationStatistics statistics = migrationManager.getMigrationHistory().getStatistics();
        LoggerUtil.info("AI MIGRATION");
        LoggerUtil.summary("Total Migrations", statistics.getTotalDecisions());
        LoggerUtil.summary("Latest Migration", formatLatestMigration(migrationManager.getMigrationHistory().getLatest()));
        LoggerUtil.summary("Successful", statistics.getSuccessfulMigrations());
        LoggerUtil.summary("Recommended", statistics.getRecommendedMigrations());
        LoggerUtil.summary("Failed", statistics.getFailedMigrations());
        LoggerUtil.summary("Energy Saved", String.format("%.4f Wh", statistics.getEstimatedEnergySavedWattHours()));
        LoggerUtil.info(Constants.LIVE_SECTION_SEPARATOR);
    }

    private void printMigrationSummary() {
        MigrationHistory.MigrationStatistics statistics = migrationManager.getMigrationHistory().getStatistics();
        LoggerUtil.info(Constants.SEPARATOR);
        LoggerUtil.info("AI MIGRATION SUMMARY");
        LoggerUtil.info(Constants.SEPARATOR);
        LoggerUtil.summary("Total Migration Decisions", statistics.getTotalDecisions());
        LoggerUtil.summary("Successful Migrations", statistics.getSuccessfulMigrations());
        LoggerUtil.summary("Recommended Migrations", statistics.getRecommendedMigrations());
        LoggerUtil.summary("Failed Migrations", statistics.getFailedMigrations());
        LoggerUtil.summary("Estimated Energy Saved", String.format("%.4f Wh", statistics.getEstimatedEnergySavedWattHours()));
        LoggerUtil.summary("Most Loaded Host", formatHostId(migrationManager.getMostLoadedHostId()));
        LoggerUtil.summary("Least Loaded Host", formatHostId(migrationManager.getLeastLoadedHostId()));
        LoggerUtil.summary("Average Host Utilization", formatPercent(migrationManager.getAverageHostUtilization()));
        LoggerUtil.info(Constants.SEPARATOR);
    }

    private void logMigrationDecision(MigrationDecision decision) {
        eventLogger.log(decision.getMigrationTime(), "VM Migration", decision.getVmId(),
                "Host " + decision.getSourceHostId() + " -> Host " + decision.getDestinationHostId(),
                String.format("VM %d migration %s: Host %d -> Host %d (%s, score %.2f)", decision.getVmId(),
                        decision.getStatus(), decision.getSourceHostId(), decision.getDestinationHostId(),
                        decision.getReason(), decision.getMigrationScore()));
    }

    private String formatLatestMigration(java.util.Optional<MigrationDecision> decision) {
        return decision.map(value -> String.format("VM %d: Host %d -> Host %d (%s)", value.getVmId(),
                value.getSourceHostId(), value.getDestinationHostId(), value.getStatus())).orElse("N/A");
    }

    private void displayOptimizationMetrics() {
        OptimizationDecision latest = optimizationManager.getStatistics().getLatestDecision();
        if (latest == null) return;

        LoggerUtil.info("AI OPTIMIZATION");
        LoggerUtil.summary("Optimization Score", String.format("%.2f", latest.getOverallOptimizationScore()));
        LoggerUtil.summary("Resource Balance", String.format("%.2f", latest.getResourceBalanceScore()));
        LoggerUtil.summary("Energy Efficiency", String.format("%.2f", latest.getEnergyEfficiencyScore()));
        LoggerUtil.summary("SLA Compliance", String.format("%.2f", latest.getSlaComplianceScore()));
        LoggerUtil.summary("Powered Off Hosts", latest.getPoweredOffHostsCount());
        LoggerUtil.summary("Sleeping Hosts", latest.getSleepingHostsCount());
        LoggerUtil.summary("SLA Violations", latest.getSlaViolationsCount());
        LoggerUtil.summary("Estimated Energy Saved", String.format("%.4f Wh", latest.getEstimatedEnergySavingsWattHours()));
        LoggerUtil.info(Constants.LIVE_SECTION_SEPARATOR);
    }

    private void printOptimizationSummary() {
        OptimizationManager.OptimizationSummary summary = optimizationManager.generateSummary();
        LoggerUtil.info(Constants.SEPARATOR);
        LoggerUtil.info("AI OPTIMIZATION SUMMARY");
        LoggerUtil.info(Constants.SEPARATOR);
        LoggerUtil.summary("Total Optimization Decisions", summary.getTotalDecisions());
        LoggerUtil.summary("Overall Optimization Score", String.format("%.2f", summary.getAverageOptimizationScore()));
        LoggerUtil.summary("Total Energy Saved", String.format("%.4f Wh", summary.getTotalEnergySavedWattHours()));
        LoggerUtil.summary("Total SLA Violations", summary.getTotalSlaViolations());
        LoggerUtil.summary("Max Hosts Powered Off", summary.getMaxPoweredOffHosts());
        LoggerUtil.summary("Max Hosts Sleeping", summary.getMaxSleepingHosts());
        LoggerUtil.summary("Consolidation Recommendations", summary.getTotalConsolidationRecommendations());
        LoggerUtil.summary("Migration Recommendations", summary.getTotalMigrationRecommendations());
        LoggerUtil.info(Constants.SEPARATOR);
    }

    private String formatPercent(double value) {
        return String.format(Constants.PERCENT_FORMAT, toPercent(value));
    }

    private double toPercent(double value) {
        return value * Constants.PERCENT_SCALE;
    }
}
