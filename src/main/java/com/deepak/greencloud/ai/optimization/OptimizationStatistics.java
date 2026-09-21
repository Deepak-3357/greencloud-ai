package com.deepak.greencloud.ai.optimization;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores aggregate statistics about optimization decisions and actions
 * across the simulation lifecycle.
 */
public final class OptimizationStatistics {
    private final List<OptimizationDecision> decisions = new ArrayList<>();
    private double totalEnergySavedWattHours = 0;
    private int totalConsolidationRecommendations = 0;
    private int totalMigrationRecommendations = 0;
    private int totalSlaViolations = 0;
    private int maxPoweredOffHosts = 0;
    private int maxSleepingHosts = 0;
    private double averageOptimizationScore = 0;

    /**
     * Records a new optimization decision.
     *
     * @param decision decision to record
     */
    public synchronized void recordDecision(OptimizationDecision decision) {
        decisions.add(decision);
        totalEnergySavedWattHours += decision.getEstimatedEnergySavingsWattHours();
        totalSlaViolations += decision.getSlaViolationsCount();
        maxPoweredOffHosts = Math.max(maxPoweredOffHosts, decision.getPoweredOffHostsCount());
        maxSleepingHosts = Math.max(maxSleepingHosts, decision.getSleepingHostsCount());

        for (OptimizationDecision.RecommendationType rec : decision.getRecommendations()) {
            if (rec == OptimizationDecision.RecommendationType.CONSOLIDATION) {
                totalConsolidationRecommendations++;
            } else if (rec == OptimizationDecision.RecommendationType.MIGRATION) {
                totalMigrationRecommendations++;
            }
        }

        recalculateAverageScore();
    }

    /**
     * Gets all recorded optimization decisions.
     *
     * @return immutable list of decisions
     */
    public synchronized List<OptimizationDecision> getDecisions() {
        return List.copyOf(decisions);
    }

    /**
     * Gets the latest optimization decision if one exists.
     *
     * @return latest decision or null
     */
    public synchronized OptimizationDecision getLatestDecision() {
        return decisions.isEmpty() ? null : decisions.get(decisions.size() - 1);
    }

    /**
     * Gets total energy saved across all optimization decisions.
     *
     * @return energy saved in watt-hours
     */
    public synchronized double getTotalEnergySavedWattHours() {
        return totalEnergySavedWattHours;
    }

    /**
     * Gets total consolidation recommendations made.
     *
     * @return count of consolidation recommendations
     */
    public synchronized int getTotalConsolidationRecommendations() {
        return totalConsolidationRecommendations;
    }

    /**
     * Gets total migration recommendations made.
     *
     * @return count of migration recommendations
     */
    public synchronized int getTotalMigrationRecommendations() {
        return totalMigrationRecommendations;
    }

    /**
     * Gets total SLA violations detected.
     *
     * @return count of violations
     */
    public synchronized int getTotalSlaViolations() {
        return totalSlaViolations;
    }

    /**
     * Gets the maximum number of hosts powered off in any single decision.
     *
     * @return max powered off host count
     */
    public synchronized int getMaxPoweredOffHosts() {
        return maxPoweredOffHosts;
    }

    /**
     * Gets the maximum number of hosts in sleep mode in any single decision.
     *
     * @return max sleeping host count
     */
    public synchronized int getMaxSleepingHosts() {
        return maxSleepingHosts;
    }

    /**
     * Gets the average optimization score across all decisions.
     *
     * @return average score from 0-100
     */
    public synchronized double getAverageOptimizationScore() {
        return averageOptimizationScore;
    }

    /**
     * Gets the overall optimization statistics summary.
     *
     * @return immutable summary metrics
     */
    public synchronized OptimizationSummary getSummary() {
        return new OptimizationSummary(
                decisions.size(),
                totalEnergySavedWattHours,
                totalConsolidationRecommendations,
                totalMigrationRecommendations,
                totalSlaViolations,
                maxPoweredOffHosts,
                maxSleepingHosts,
                averageOptimizationScore
        );
    }

    /**
     * Gets the count of optimization decisions made.
     *
     * @return number of decisions
     */
    public synchronized int getDecisionCount() {
        return decisions.size();
    }

    private void recalculateAverageScore() {
        if (decisions.isEmpty()) {
            averageOptimizationScore = 0;
            return;
        }
        double sum = decisions.stream()
                .mapToDouble(OptimizationDecision::getOverallOptimizationScore)
                .sum();
        averageOptimizationScore = sum / decisions.size();
    }

    /**
     * Immutable summary of optimization statistics.
     */
    public static final class OptimizationSummary {
        private final int totalDecisions;
        private final double totalEnergySavedWattHours;
        private final int totalConsolidationRecommendations;
        private final int totalMigrationRecommendations;
        private final int totalSlaViolations;
        private final int maxPoweredOffHosts;
        private final int maxSleepingHosts;
        private final double averageOptimizationScore;

        public OptimizationSummary(int totalDecisions, double totalEnergySavedWattHours,
                                   int totalConsolidationRecommendations, int totalMigrationRecommendations,
                                   int totalSlaViolations, int maxPoweredOffHosts,
                                   int maxSleepingHosts, double averageOptimizationScore) {
            this.totalDecisions = totalDecisions;
            this.totalEnergySavedWattHours = totalEnergySavedWattHours;
            this.totalConsolidationRecommendations = totalConsolidationRecommendations;
            this.totalMigrationRecommendations = totalMigrationRecommendations;
            this.totalSlaViolations = totalSlaViolations;
            this.maxPoweredOffHosts = maxPoweredOffHosts;
            this.maxSleepingHosts = maxSleepingHosts;
            this.averageOptimizationScore = averageOptimizationScore;
        }

        public int getTotalDecisions() { return totalDecisions; }
        public double getTotalEnergySavedWattHours() { return totalEnergySavedWattHours; }
        public int getTotalConsolidationRecommendations() { return totalConsolidationRecommendations; }
        public int getTotalMigrationRecommendations() { return totalMigrationRecommendations; }
        public int getTotalSlaViolations() { return totalSlaViolations; }
        public int getMaxPoweredOffHosts() { return maxPoweredOffHosts; }
        public int getMaxSleepingHosts() { return maxSleepingHosts; }
        public double getAverageOptimizationScore() { return averageOptimizationScore; }
    }
}
