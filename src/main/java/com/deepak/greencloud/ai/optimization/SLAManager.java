package com.deepak.greencloud.ai.optimization;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.cloudsimplus.cloudlets.Cloudlet;
import org.cloudsimplus.hosts.Host;
import org.cloudsimplus.vms.Vm;

/**
 * Detects and tracks SLA violations including CPU/RAM overload,
 * long waiting cloudlets, and overloaded hosts.
 */
public final class SLAManager {
    private static final double CPU_SLA_THRESHOLD = 0.90;
    private static final double RAM_SLA_THRESHOLD = 0.90;
    private static final double CPU_CRITICAL_THRESHOLD = 0.95;
    private static final double RAM_CRITICAL_THRESHOLD = 0.95;
    private static final double WAITING_TIME_THRESHOLD = 30.0; // seconds
    private static final double OVERLOAD_THRESHOLD = 0.80; // for host consolidation

    private final List<SLAViolation> violations = new ArrayList<>();

    /**
     * Evaluates all hosts and VMs for SLA violations.
     *
     * @param hosts hosts to evaluate
     * @param cloudlets cloudlets to check for excessive waiting
     * @param simulationTime current simulation time
     * @return list of violations detected in this evaluation
     */
    public synchronized List<SLAViolation> evaluate(List<Host> hosts, List<Cloudlet> cloudlets, double simulationTime) {
        List<SLAViolation> detected = new ArrayList<>();

        // Check host CPU and RAM utilization
        for (Host host : hosts) {
            detected.addAll(detectHostUtilizationViolations(host, simulationTime));
        }

        // Check for long waiting cloudlets
        detected.addAll(detectWaitingCloudletViolations(cloudlets, simulationTime));

        // Record all violations
        violations.addAll(detected);
        return List.copyOf(detected);
    }

    /**
     * Gets CPU utilization violations for a specific host.
     */
    private List<SLAViolation> detectHostUtilizationViolations(Host host, double simulationTime) {
        List<SLAViolation> hostViolations = new ArrayList<>();

        double cpuUtil = clamp(host.getCpuPercentUtilization());
        double ramUtil = clamp(host.getRam().getPercentUtilization());

        // Check CPU violation
        if (cpuUtil > CPU_CRITICAL_THRESHOLD) {
            hostViolations.add(new SLAViolation(
                    simulationTime, host.getId(), null,
                    "CPU_CRITICAL", cpuUtil * 100, CPU_CRITICAL_THRESHOLD * 100,
                    SLAViolation.Severity.CRITICAL));
        } else if (cpuUtil > CPU_SLA_THRESHOLD) {
            hostViolations.add(new SLAViolation(
                    simulationTime, host.getId(), null,
                    "CPU_OVERLOAD", cpuUtil * 100, CPU_SLA_THRESHOLD * 100,
                    SLAViolation.Severity.HIGH));
        }

        // Check RAM violation
        if (ramUtil > RAM_CRITICAL_THRESHOLD) {
            hostViolations.add(new SLAViolation(
                    simulationTime, host.getId(), null,
                    "RAM_CRITICAL", ramUtil * 100, RAM_CRITICAL_THRESHOLD * 100,
                    SLAViolation.Severity.CRITICAL));
        } else if (ramUtil > RAM_SLA_THRESHOLD) {
            hostViolations.add(new SLAViolation(
                    simulationTime, host.getId(), null,
                    "RAM_OVERLOAD", ramUtil * 100, RAM_SLA_THRESHOLD * 100,
                    SLAViolation.Severity.HIGH));
        }

        // Check for overloaded host (for consolidation purposes)
        if (cpuUtil > OVERLOAD_THRESHOLD || ramUtil > OVERLOAD_THRESHOLD) {
            hostViolations.add(new SLAViolation(
                    simulationTime, host.getId(), null,
                    "HOST_OVERLOADED", Math.max(cpuUtil, ramUtil) * 100, OVERLOAD_THRESHOLD * 100,
                    SLAViolation.Severity.MEDIUM));
        }

        return hostViolations;
    }

    /**
     * Detects cloudlets waiting too long in the queue.
     */
    private List<SLAViolation> detectWaitingCloudletViolations(List<Cloudlet> cloudlets, double simulationTime) {
        List<SLAViolation> waitingViolations = new ArrayList<>();

        for (Cloudlet cloudlet : cloudlets) {
            if (cloudlet.getStatus() == Cloudlet.Status.QUEUED || cloudlet.getStatus() == Cloudlet.Status.INEXEC) {
                double waitingTime = simulationTime - cloudlet.getSubmissionDelay();
                if (waitingTime > WAITING_TIME_THRESHOLD) {
                    // Find which host this cloudlet is on (if any) via its VM
                    long hostId = -1;
                    Vm vm = cloudlet.getVm();
                    if (vm != null && vm.getHost() != null) {
                        hostId = vm.getHost().getId();
                    }
                    waitingViolations.add(new SLAViolation(
                            simulationTime, hostId, cloudlet.getVm().getId(),
                            "LONG_WAITING_CLOUDLET", waitingTime, WAITING_TIME_THRESHOLD,
                            SLAViolation.Severity.MEDIUM));
                }
            }
        }

        return waitingViolations;
    }

    /**
     * Gets all recorded SLA violations.
     *
     * @return immutable list of violations
     */
    public synchronized List<SLAViolation> getAllViolations() {
        return List.copyOf(violations);
    }

    /**
     * Gets violations of a specific type.
     *
     * @param violationType type to filter (e.g., "CPU_OVERLOAD")
     * @return violations matching the type
     */
    public synchronized List<SLAViolation> getViolationsByType(String violationType) {
        return violations.stream()
                .filter(v -> v.getViolationType().equals(violationType))
                .toList();
    }

    /**
     * Gets violations for a specific host.
     *
     * @param hostId host to filter
     * @return violations for that host
     */
    public synchronized List<SLAViolation> getViolationsByHost(long hostId) {
        return violations.stream()
                .filter(v -> v.getHostId() == hostId)
                .toList();
    }

    /**
     * Counts SLA violations by severity.
     */
    public synchronized SLAComplianceMetrics getComplianceMetrics() {
        long critical = violations.stream()
                .filter(v -> v.getSeverity() == SLAViolation.Severity.CRITICAL).count();
        long high = violations.stream()
                .filter(v -> v.getSeverity() == SLAViolation.Severity.HIGH).count();
        long medium = violations.stream()
                .filter(v -> v.getSeverity() == SLAViolation.Severity.MEDIUM).count();
        long low = violations.stream()
                .filter(v -> v.getSeverity() == SLAViolation.Severity.LOW).count();

        return new SLAComplianceMetrics(violations.size(), critical, high, medium, low);
    }

    private double clamp(double value) {
        return Math.max(0, Math.min(1, value));
    }

    /**
     * Immutable metrics representing SLA compliance status.
     */
    public static final class SLAComplianceMetrics {
        private final int totalViolations;
        private final long criticalViolations;
        private final long highViolations;
        private final long mediumViolations;
        private final long lowViolations;

        public SLAComplianceMetrics(int totalViolations, long criticalViolations, long highViolations,
                                    long mediumViolations, long lowViolations) {
            this.totalViolations = totalViolations;
            this.criticalViolations = criticalViolations;
            this.highViolations = highViolations;
            this.mediumViolations = mediumViolations;
            this.lowViolations = lowViolations;
        }

        public int getTotalViolations() { return totalViolations; }
        public long getCriticalViolations() { return criticalViolations; }
        public long getHighViolations() { return highViolations; }
        public long getMediumViolations() { return mediumViolations; }
        public long getLowViolations() { return lowViolations; }

        public double getComplianceScore() {
            if (totalViolations == 0) return 100.0;
            double penalty = (criticalViolations * 10.0 + highViolations * 5.0 + mediumViolations * 2.0 + lowViolations * 0.5);
            return Math.max(0, 100.0 - penalty);
        }
    }
}
