package com.deepak.greencloud.monitoring;

/**
 * POJO containing cloudlet lifecycle metrics.
 */
public class CloudletMetrics {

    private final long cloudletId;
    private final long vmId;
    private final long hostId;
    private final double submissionTime;
    private final double startTime;
    private final double finishTime;
    private final double waitingTime;
    private final double executionTime;
    private final double cpuTime;
    private final String status;

    public CloudletMetrics(
            long cloudletId,
            long vmId,
            long hostId,
            double submissionTime,
            double startTime,
            double finishTime,
            double waitingTime,
            double executionTime,
            double cpuTime,
            String status) {
        this.cloudletId = cloudletId;
        this.vmId = vmId;
        this.hostId = hostId;
        this.submissionTime = submissionTime;
        this.startTime = startTime;
        this.finishTime = finishTime;
        this.waitingTime = waitingTime;
        this.executionTime = executionTime;
        this.cpuTime = cpuTime;
        this.status = status;
    }

    public long getCloudletId() {
        return cloudletId;
    }

    public long getVmId() {
        return vmId;
    }

    public long getHostId() {
        return hostId;
    }

    public double getSubmissionTime() {
        return submissionTime;
    }

    public double getStartTime() {
        return startTime;
    }

    public double getFinishTime() {
        return finishTime;
    }

    public double getWaitingTime() {
        return waitingTime;
    }

    public double getExecutionTime() {
        return executionTime;
    }

    public double getCpuTime() {
        return cpuTime;
    }

    public String getStatus() {
        return status;
    }
}
