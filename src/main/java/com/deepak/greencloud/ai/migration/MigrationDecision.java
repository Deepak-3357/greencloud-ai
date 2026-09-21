package com.deepak.greencloud.ai.migration;

/** Immutable record of an AI VM migration recommendation or execution request. */
public final class MigrationDecision {
    /** Represents the outcome of a migration decision. */
    public enum MigrationStatus { SUCCESS, FAILED, RECOMMENDED }

    private final double migrationTime;
    private final long vmId;
    private final long sourceHostId;
    private final long destinationHostId;
    private final String reason;
    private final double migrationScore;
    private final double cpuBefore;
    private final double cpuAfter;
    private final double energySavedEstimateWattHours;
    private final MigrationStatus status;

    /** Creates an immutable migration decision. */
    public MigrationDecision(double migrationTime, long vmId, long sourceHostId, long destinationHostId,
                             String reason, double migrationScore, double cpuBefore, double cpuAfter,
                             double energySavedEstimateWattHours, MigrationStatus status) {
        this.migrationTime = migrationTime;
        this.vmId = vmId;
        this.sourceHostId = sourceHostId;
        this.destinationHostId = destinationHostId;
        this.reason = reason;
        this.migrationScore = migrationScore;
        this.cpuBefore = cpuBefore;
        this.cpuAfter = cpuAfter;
        this.energySavedEstimateWattHours = energySavedEstimateWattHours;
        this.status = status;
    }

    /** @return simulation time when the decision was made */ public double getMigrationTime() { return migrationTime; }
    /** @return migrated VM identifier */ public long getVmId() { return vmId; }
    /** @return source host identifier */ public long getSourceHostId() { return sourceHostId; }
    /** @return selected destination host identifier */ public long getDestinationHostId() { return destinationHostId; }
    /** @return migration rationale */ public String getReason() { return reason; }
    /** @return destination host suitability score */ public double getMigrationScore() { return migrationScore; }
    /** @return source CPU utilization before the migration */ public double getCpuBefore() { return cpuBefore; }
    /** @return estimated source CPU utilization after the migration */ public double getCpuAfter() { return cpuAfter; }
    /** @return estimated energy saved during one monitoring interval in watt-hours */ public double getEnergySavedEstimateWattHours() { return energySavedEstimateWattHours; }
    /** @return migration execution status */ public MigrationStatus getStatus() { return status; }
}
