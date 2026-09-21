package com.deepak.greencloud.ai.optimization;

import com.deepak.greencloud.ai.EnergyCalculator;
import com.deepak.greencloud.ai.HostScore;
import com.deepak.greencloud.ai.HostScoringEngine;
import com.deepak.greencloud.ai.migration.MigrationDecision;
import com.deepak.greencloud.ai.migration.MigrationManager;
import com.deepak.greencloud.config.SimulationConfig;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.cloudsimplus.cloudlets.Cloudlet;
import org.cloudsimplus.datacenters.Datacenter;
import org.cloudsimplus.hosts.Host;

/**
 * Main orchestrator for AI optimization and SLA management.
 * Integrates SLA monitoring, host power management, and consolidation logic
 * while reusing existing migration and energy management components.
 */
public final class OptimizationManager {
    private final SLAManager slaManager;
    private final HostPowerManager hostPowerManager;
    private final MigrationManager migrationManager;
    private final EnergyCalculator energyCalculator;
    private final HostScoringEngine hostScoringEngine;
    private final OptimizationStatistics statistics;

    /**
     * Creates an optimization manager with reusable components.
     *
     * @param migrationManager migration manager for consolidation actions
     * @param energyCalculator energy calculation engine
     * @param hostScoringEngine host scoring for placement decisions
     */
    public OptimizationManager(MigrationManager migrationManager, EnergyCalculator energyCalculator,
                               HostScoringEngine hostScoringEngine) {
        this.slaManager = new SLAManager();
        this.hostPowerManager = new HostPowerManager();
        this.migrationManager = migrationManager;
        this.energyCalculator = energyCalculator;
        this.hostScoringEngine = hostScoringEngine;
        this.statistics = new OptimizationStatistics();
    }

    /**
     * Evaluates the entire system for optimization opportunities.
     * Detects SLA violations, determines power states, and produces recommendations.
     *
     * @param hosts all hosts in datacenter
     * @param cloudlets all cloudlets
     * @param datacenter datacenter for migration requests
     * @param simulationTime current simulation time
     * @return optimization decision with all metrics and recommendations
     */
    public synchronized OptimizationDecision evaluate(List<Host> hosts, List<Cloudlet> cloudlets,
                                                      Datacenter datacenter, double simulationTime) {
        // Detect SLA violations
        List<SLAViolation> violations = slaManager.evaluate(hosts, cloudlets, simulationTime);

        // Update host power states
        Map<Long, HostPowerManager.PowerState> powerStates = hostPowerManager.updatePowerStates(hosts);

        // Evaluate migration opportunities
        List<MigrationDecision> migrationDecisions = migrationManager.evaluate(hosts, datacenter, simulationTime);

        // Calculate optimization scores
        double resourceBalanceScore = calculateResourceBalanceScore(hosts);
        double energyEfficiencyScore = calculateEnergyEfficiencyScore(hosts, powerStates);
        double migrationEfficiencyScore = calculateMigrationEfficiencyScore(migrationDecisions);
        double slaComplianceScore = calculateSLAComplianceScore(violations);

        double overallScore = (resourceBalanceScore * 0.25 + energyEfficiencyScore * 0.35 +
                              migrationEfficiencyScore * 0.20 + slaComplianceScore * 0.20);

        // Generate recommendations
        List<OptimizationDecision.RecommendationType> recommendations = generateRecommendations(
                violations, migrationDecisions, powerStates, hosts, overallScore);

        // Estimate energy savings
        double energySavings = hostPowerManager.estimateEnergySavings(hosts,
                energyCalculator.getEnergyModel().getMaxPowerWatts(),
                SimulationConfig.MONITORING_SNAPSHOT_INTERVAL);

        // Count host states using the already computed powerStates map to ensure consistency
        int poweredOffHosts = (int) powerStates.values().stream()
                .filter(state -> state == HostPowerManager.PowerState.POWER_OFF).count();
        int sleepingHosts = (int) powerStates.values().stream()
                .filter(state -> state == HostPowerManager.PowerState.SLEEP).count();

        // Build and record decision
        OptimizationDecision.Builder builder = new OptimizationDecision.Builder()
                .decisionTime(simulationTime)
                .resourceBalanceScore(resourceBalanceScore)
                .energyEfficiencyScore(energyEfficiencyScore)
                .migrationEfficiencyScore(migrationEfficiencyScore)
                .slaComplianceScore(slaComplianceScore)
                .overallOptimizationScore(overallScore)
                .estimatedEnergySavingsWattHours(energySavings)
                .poweredOffHostsCount(poweredOffHosts)
                .sleepingHostsCount(sleepingHosts)
                .slaViolationsCount(violations.size());

        for (MigrationDecision migDecision : migrationDecisions) {
            builder.addMigrationDecision(migDecision);
        }

        for (OptimizationDecision.RecommendationType rec : recommendations) {
            builder.addRecommendation(rec);
        }

        OptimizationDecision decision = builder.build();

        statistics.recordDecision(decision);
        return decision;
    }

    /**
     * Calculates resource balance score based on host utilization evenness.
     */
    private double calculateResourceBalanceScore(List<Host> hosts) {
        if (hosts.isEmpty()) return 100.0;

        List<Double> utilizationRatios = new ArrayList<>();
        for (Host host : hosts) {
            double cpuUtil = clamp(host.getCpuPercentUtilization());
            double ramUtil = clamp(host.getRam().getPercentUtilization());
            double avgUtil = (cpuUtil + ramUtil) / 2.0;
            utilizationRatios.add(avgUtil);
        }

        double mean = utilizationRatios.stream().mapToDouble(Double::doubleValue).average().orElse(0);
        double variance = utilizationRatios.stream()
                .mapToDouble(x -> Math.pow(x - mean, 2)).average().orElse(0);
        double stdDev = Math.sqrt(variance);

        // Lower standard deviation = better balance
        return Math.max(0, 100.0 - (stdDev * 100.0));
    }

    /**
     * Calculates energy efficiency based on power consumption and utilization.
     */
    private double calculateEnergyEfficiencyScore(List<Host> hosts,
                                                  Map<Long, HostPowerManager.PowerState> powerStates) {
        if (hosts.isEmpty()) return 100.0;

        int activeCount = 0;
        int idleCount = 0;
        int sleepCount = 0;
        int powerOffCount = 0;
        double totalUtilization = 0;
        double totalPotentialPower = 0;
        double totalActualPower = 0;

        for (Host host : hosts) {
            HostPowerManager.PowerState state = powerStates.getOrDefault(host.getId(),
                    HostPowerManager.PowerState.ACTIVE);
            switch (state) {
                case ACTIVE -> activeCount++;
                case IDLE -> idleCount++;
                case SLEEP -> sleepCount++;
                case POWER_OFF -> powerOffCount++;
            }

            double cpuUtil = clamp(host.getCpuPercentUtilization());
            double ramUtil = clamp(host.getRam().getPercentUtilization());
            double avgUtil = (cpuUtil + ramUtil) / 2.0;
            totalUtilization += avgUtil;

            double maxPower = energyCalculator.getEnergyModel().getMaxPowerWatts();
            totalPotentialPower += maxPower;
            double actualPower = hostPowerManager.estimatePowerConsumption(host, maxPower);
            totalActualPower += actualPower;
        }

        // Score based on power state distribution
        double stateScore = (activeCount * 100.0 + idleCount * 70.0 + sleepCount * 30.0) / hosts.size();

        // Score based on actual vs potential power usage
        double powerRatio = totalPotentialPower > 0 ? (totalActualPower / totalPotentialPower) : 0;
        double powerScore = Math.max(0, 100.0 - (powerRatio * 100.0));

        // Combined score: 60% power state distribution, 40% actual power efficiency
        return (stateScore * 0.6 + powerScore * 0.4);
    }

    /**
     * Calculates migration efficiency based on migration decisions quality.
     */
    private double calculateMigrationEfficiencyScore(List<MigrationDecision> migrationDecisions) {
        if (migrationDecisions.isEmpty()) return 100.0;

        long successful = migrationDecisions.stream()
                .filter(d -> d.getStatus() == MigrationDecision.MigrationStatus.SUCCESS).count();
        long recommended = migrationDecisions.stream()
                .filter(d -> d.getStatus() == MigrationDecision.MigrationStatus.RECOMMENDED).count();

        double successRate = (successful + recommended) / (double) migrationDecisions.size();
        return successRate * 100.0;
    }

    /**
     * Calculates SLA compliance score based on violations severity.
     */
    private double calculateSLAComplianceScore(List<SLAViolation> violations) {
        if (violations.isEmpty()) return 100.0;

        long critical = violations.stream()
                .filter(v -> v.getSeverity() == SLAViolation.Severity.CRITICAL).count();
        long high = violations.stream()
                .filter(v -> v.getSeverity() == SLAViolation.Severity.HIGH).count();
        long medium = violations.stream()
                .filter(v -> v.getSeverity() == SLAViolation.Severity.MEDIUM).count();
        long low = violations.stream()
                .filter(v -> v.getSeverity() == SLAViolation.Severity.LOW).count();

        double penalty = (critical * 10.0 + high * 5.0 + medium * 2.0 + low * 0.5);
        return Math.max(0, 100.0 - penalty);
    }

    /**
     * Generates optimization recommendations based on current state.
     */
    private List<OptimizationDecision.RecommendationType> generateRecommendations(
            List<SLAViolation> violations,
            List<MigrationDecision> migrationDecisions,
            Map<Long, HostPowerManager.PowerState> powerStates,
            List<Host> hosts,
            double overallScore) {

        List<OptimizationDecision.RecommendationType> recommendations = new ArrayList<>();

        // SLA mitigation
        if (!violations.isEmpty()) {
            long criticalOrHigh = violations.stream()
                    .filter(v -> v.getSeverity() == SLAViolation.Severity.CRITICAL ||
                               v.getSeverity() == SLAViolation.Severity.HIGH).count();
            if (criticalOrHigh > 0) {
                recommendations.add(OptimizationDecision.RecommendationType.SLA_MITIGATION);
            }
        }

        // Migration recommendations
        if (!migrationDecisions.isEmpty()) {
            recommendations.add(OptimizationDecision.RecommendationType.MIGRATION);
        }

        // Consolidation opportunities
        long underloadedHostCount = hosts.stream()
                .filter(h -> clamp(h.getCpuPercentUtilization()) < 0.20 && !h.getVmList().isEmpty())
                .count();
        if (underloadedHostCount > 0) {
            recommendations.add(OptimizationDecision.RecommendationType.CONSOLIDATION);
        }

        // Power off recommendations
        long powerOffCandidates = powerStates.values().stream()
                .filter(state -> state == HostPowerManager.PowerState.POWER_OFF).count();
        if (powerOffCandidates > 0) {
            recommendations.add(OptimizationDecision.RecommendationType.POWER_OFF);
        }

        // No optimization needed
        if (recommendations.isEmpty() && overallScore > 90.0) {
            recommendations.add(OptimizationDecision.RecommendationType.NONE);
        }

        return recommendations;
    }

    /**
     * Gets the SLA manager instance.
     */
    public SLAManager getSLAManager() {
        return slaManager;
    }

    /**
     * Gets the host power manager instance.
     */
    public HostPowerManager getHostPowerManager() {
        return hostPowerManager;
    }

    /**
     * Gets optimization statistics.
     */
    public OptimizationStatistics getStatistics() {
        return statistics;
    }

    /**
     * Gets the reusable migration manager.
     */
    public MigrationManager getMigrationManager() {
        return migrationManager;
    }

    /**
     * Generates final optimization summary for reporting.
     */
    public synchronized OptimizationSummary generateSummary() {
        return new OptimizationSummary(statistics.getSummary());
    }

    private double clamp(double value) {
        return Math.max(0, Math.min(1, value));
    }

    /**
     * Final summary for reporting to users.
     */
    public static final class OptimizationSummary {
        private final OptimizationStatistics.OptimizationSummary stats;

        public OptimizationSummary(OptimizationStatistics.OptimizationSummary stats) {
            this.stats = stats;
        }

        public int getTotalDecisions() { return stats.getTotalDecisions(); }
        public double getTotalEnergySavedWattHours() { return stats.getTotalEnergySavedWattHours(); }
        public int getTotalSlaViolations() { return stats.getTotalSlaViolations(); }
        public int getMaxPoweredOffHosts() { return stats.getMaxPoweredOffHosts(); }
        public int getMaxSleepingHosts() { return stats.getMaxSleepingHosts(); }
        public double getAverageOptimizationScore() { return stats.getAverageOptimizationScore(); }
        public int getTotalConsolidationRecommendations() { return stats.getTotalConsolidationRecommendations(); }
        public int getTotalMigrationRecommendations() { return stats.getTotalMigrationRecommendations(); }
    }
}
