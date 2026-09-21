package com.deepak.greencloud.ai;

/** Represents a host's resource and energy suitability for future VM placement. */
public final class HostScore implements Comparable<HostScore> {
    private final long hostId;
    private final double cpuScore;
    private final double ramScore;
    private final double bandwidthScore;
    private final double storageScore;
    private final double energyScore;
    private final double finalScore;
    private final String reason;

    /** Creates an immutable host evaluation. */
    public HostScore(long hostId, double cpuScore, double ramScore, double bandwidthScore, double storageScore,
                     double energyScore, double finalScore, String reason) {
        this.hostId = hostId; this.cpuScore = cpuScore; this.ramScore = ramScore;
        this.bandwidthScore = bandwidthScore; this.storageScore = storageScore;
        this.energyScore = energyScore; this.finalScore = finalScore; this.reason = reason;
    }
    /** @return host identifier */ public long getHostId() { return hostId; }
    /** @return remaining CPU capacity score from 0 to 100 */ public double getCpuScore() { return cpuScore; }
    /** @return remaining RAM capacity score from 0 to 100 */ public double getRamScore() { return ramScore; }
    /** @return remaining bandwidth capacity score from 0 to 100 */ public double getBandwidthScore() { return bandwidthScore; }
    /** @return remaining storage capacity score from 0 to 100 */ public double getStorageScore() { return storageScore; }
    /** @return energy-efficiency score from 0 to 100 */ public double getEnergyScore() { return energyScore; }
    /** @return weighted final score from 0 to 100 */ public double getFinalScore() { return finalScore; }
    /** @return human-readable scoring rationale */ public String getReason() { return reason; }
    /** Compares scores in descending order so the best host sorts first. */
    @Override public int compareTo(HostScore other) { return Double.compare(other.finalScore, finalScore); }
}
