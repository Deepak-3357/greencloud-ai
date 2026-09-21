package com.deepak.greencloud.ai.optimization;

import com.deepak.greencloud.ai.migration.MigrationDecision;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a complete optimization decision made by OptimizationManager.
 * Contains resource balance, energy efficiency, migration recommendations,
 * and SLA compliance metrics.
 */
public final class OptimizationDecision {
    /**
     * Recommendation types the optimization engine can produce.
     */
    public enum RecommendationType {
        CONSOLIDATION("Host consolidation recommended for underutilized hosts"),
        POWER_OFF("Power off underutilized hosts"),
        MIGRATION("VM migration recommended"),
        SLA_MITIGATION("SLA violation mitigation required"),
        NONE("No optimization needed at this time");

        private final String description;

        RecommendationType(String description) {
            this.description = description;
        }

        public String getDescription() { return description; }
    }

    private final double decisionTime;
    private final double resourceBalanceScore;
    private final double energyEfficiencyScore;
    private final double migrationEfficiencyScore;
    private final double slaComplianceScore;
    private final double overallOptimizationScore;
    private final List<RecommendationType> recommendations;
    private final List<String> details;
    private final double estimatedEnergySavingsWattHours;
    private final List<MigrationDecision> migrationDecisions;
    private final int poweredOffHostsCount;
    private final int sleepingHostsCount;
    private final int slaViolationsCount;

    /**
     * Creates an immutable optimization decision with all metrics and recommendations.
     */
    public OptimizationDecision(double decisionTime,
                                double resourceBalanceScore,
                                double energyEfficiencyScore,
                                double migrationEfficiencyScore,
                                double slaComplianceScore,
                                double overallOptimizationScore,
                                List<RecommendationType> recommendations,
                                List<String> details,
                                double estimatedEnergySavingsWattHours,
                                List<MigrationDecision> migrationDecisions,
                                int poweredOffHostsCount,
                                int sleepingHostsCount,
                                int slaViolationsCount) {
        this.decisionTime = decisionTime;
        this.resourceBalanceScore = resourceBalanceScore;
        this.energyEfficiencyScore = energyEfficiencyScore;
        this.migrationEfficiencyScore = migrationEfficiencyScore;
        this.slaComplianceScore = slaComplianceScore;
        this.overallOptimizationScore = overallOptimizationScore;
        this.recommendations = List.copyOf(recommendations);
        this.details = List.copyOf(details);
        this.estimatedEnergySavingsWattHours = estimatedEnergySavingsWattHours;
        this.migrationDecisions = List.copyOf(migrationDecisions);
        this.poweredOffHostsCount = poweredOffHostsCount;
        this.sleepingHostsCount = sleepingHostsCount;
        this.slaViolationsCount = slaViolationsCount;
    }

    public double getDecisionTime() { return decisionTime; }
    public double getResourceBalanceScore() { return resourceBalanceScore; }
    public double getEnergyEfficiencyScore() { return energyEfficiencyScore; }
    public double getMigrationEfficiencyScore() { return migrationEfficiencyScore; }
    public double getSlaComplianceScore() { return slaComplianceScore; }
    public double getOverallOptimizationScore() { return overallOptimizationScore; }
    public List<RecommendationType> getRecommendations() { return recommendations; }
    public List<String> getDetails() { return details; }
    public double getEstimatedEnergySavingsWattHours() { return estimatedEnergySavingsWattHours; }
    public List<MigrationDecision> getMigrationDecisions() { return migrationDecisions; }
    public int getPoweredOffHostsCount() { return poweredOffHostsCount; }
    public int getSleepingHostsCount() { return sleepingHostsCount; }
    public int getSlaViolationsCount() { return slaViolationsCount; }

    /**
     * Builder for creating OptimizationDecision objects.
     */
    public static final class Builder {
        private double decisionTime = 0;
        private double resourceBalanceScore = 0;
        private double energyEfficiencyScore = 0;
        private double migrationEfficiencyScore = 0;
        private double slaComplianceScore = 0;
        private double overallOptimizationScore = 0;
        private final List<RecommendationType> recommendations = new ArrayList<>();
        private final List<String> details = new ArrayList<>();
        private double estimatedEnergySavingsWattHours = 0;
        private final List<MigrationDecision> migrationDecisions = new ArrayList<>();
        private int poweredOffHostsCount = 0;
        private int sleepingHostsCount = 0;
        private int slaViolationsCount = 0;

        public Builder decisionTime(double time) { this.decisionTime = time; return this; }
        public Builder resourceBalanceScore(double score) { this.resourceBalanceScore = score; return this; }
        public Builder energyEfficiencyScore(double score) { this.energyEfficiencyScore = score; return this; }
        public Builder migrationEfficiencyScore(double score) { this.migrationEfficiencyScore = score; return this; }
        public Builder slaComplianceScore(double score) { this.slaComplianceScore = score; return this; }
        public Builder overallOptimizationScore(double score) { this.overallOptimizationScore = score; return this; }
        public Builder addRecommendation(RecommendationType rec) { this.recommendations.add(rec); return this; }
        public Builder addDetail(String detail) { this.details.add(detail); return this; }
        public Builder estimatedEnergySavingsWattHours(double savings) { this.estimatedEnergySavingsWattHours = savings; return this; }
        public Builder addMigrationDecision(MigrationDecision decision) { this.migrationDecisions.add(decision); return this; }
        public Builder poweredOffHostsCount(int count) { this.poweredOffHostsCount = count; return this; }
        public Builder sleepingHostsCount(int count) { this.sleepingHostsCount = count; return this; }
        public Builder slaViolationsCount(int count) { this.slaViolationsCount = count; return this; }

        public OptimizationDecision build() {
            return new OptimizationDecision(
                    decisionTime, resourceBalanceScore, energyEfficiencyScore,
                    migrationEfficiencyScore, slaComplianceScore, overallOptimizationScore,
                    recommendations, details, estimatedEnergySavingsWattHours,
                    migrationDecisions, poweredOffHostsCount, sleepingHostsCount, slaViolationsCount
            );
        }
    }
}
