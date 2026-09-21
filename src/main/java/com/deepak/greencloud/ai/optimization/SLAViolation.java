package com.deepak.greencloud.ai.optimization;

/**
 * Represents a single SLA violation occurrence with timing and resource details.
 */
public final class SLAViolation {
    /**
     * Severity levels for SLA violations.
     */
    public enum Severity { LOW, MEDIUM, HIGH, CRITICAL }

    private final double violationTime;
    private final long hostId;
    private final Long vmId;
    private final String violationType;
    private final double violationValue;
    private final double threshold;
    private final Severity severity;

    /**
     * Creates an immutable SLA violation record.
     *
     * @param violationTime simulation time when the violation occurred
     * @param hostId host experiencing the violation
     * @param vmId VM involved (nullable for host-level violations)
     * @param violationType type of violation (e.g., "CPU_OVERLOAD", "RAM_OVERLOAD")
     * @param violationValue actual measured value
     * @param threshold configured threshold that was exceeded
     * @param severity violation severity level
     */
    public SLAViolation(double violationTime, long hostId, Long vmId, String violationType,
                        double violationValue, double threshold, Severity severity) {
        this.violationTime = violationTime;
        this.hostId = hostId;
        this.vmId = vmId;
        this.violationType = violationType;
        this.violationValue = violationValue;
        this.threshold = threshold;
        this.severity = severity;
    }

    public double getViolationTime() { return violationTime; }
    public long getHostId() { return hostId; }
    public Long getVmId() { return vmId; }
    public String getViolationType() { return violationType; }
    public double getViolationValue() { return violationValue; }
    public double getThreshold() { return threshold; }
    public Severity getSeverity() { return severity; }
}
