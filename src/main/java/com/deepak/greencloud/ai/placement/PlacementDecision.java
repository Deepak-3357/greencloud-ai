package com.deepak.greencloud.ai.placement;

/** Immutable record of an AI host-selection decision for a VM. */
public final class PlacementDecision {
    private final long vmId;
    private final long selectedHostId;
    private final double placementScore;
    private final double decisionTime;
    private final String decisionReason;
    private final double cpuScore;
    private final double ramScore;
    private final double bandwidthScore;
    private final double storageScore;
    private final double energyScore;

    /**
     * Creates an immutable VM placement decision.
     *
     * @param vmId VM identifier
     * @param selectedHostId selected host identifier
     * @param placementScore weighted placement score
     * @param decisionTime CloudSim decision time in seconds
     * @param decisionReason explanation of the host selection
     * @param cpuScore remaining CPU capacity score
     * @param ramScore remaining RAM capacity score
     * @param bandwidthScore remaining bandwidth capacity score
     * @param storageScore remaining storage capacity score
     * @param energyScore energy-efficiency score
     */
    public PlacementDecision(long vmId, long selectedHostId, double placementScore, double decisionTime,
                             String decisionReason, double cpuScore, double ramScore, double bandwidthScore,
                             double storageScore, double energyScore) {
        this.vmId = vmId;
        this.selectedHostId = selectedHostId;
        this.placementScore = placementScore;
        this.decisionTime = decisionTime;
        this.decisionReason = decisionReason;
        this.cpuScore = cpuScore;
        this.ramScore = ramScore;
        this.bandwidthScore = bandwidthScore;
        this.storageScore = storageScore;
        this.energyScore = energyScore;
    }

    /** @return VM identifier */ public long getVmId() { return vmId; }
    /** @return selected host identifier */ public long getSelectedHostId() { return selectedHostId; }
    /** @return weighted placement score */ public double getPlacementScore() { return placementScore; }
    /** @return CloudSim decision time in seconds */ public double getDecisionTime() { return decisionTime; }
    /** @return selection rationale */ public String getDecisionReason() { return decisionReason; }
    /** @return CPU score */ public double getCpuScore() { return cpuScore; }
    /** @return RAM score */ public double getRamScore() { return ramScore; }
    /** @return bandwidth score */ public double getBandwidthScore() { return bandwidthScore; }
    /** @return storage score */ public double getStorageScore() { return storageScore; }
    /** @return energy-efficiency score */ public double getEnergyScore() { return energyScore; }
}
