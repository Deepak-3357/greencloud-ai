package com.deepak.greencloud.monitoring;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.cloudsimplus.brokers.DatacenterBroker;
import org.cloudsimplus.cloudlets.Cloudlet;
import org.cloudsimplus.hosts.Host;
import org.cloudsimplus.vms.Vm;

/**
 * Collects resource metrics from hosts, VMs, cloudlets and the datacenter.
 */
public class ResourceMonitor {

    private final Map<Long, Long> vmHostAllocations = new ConcurrentHashMap<>();

    /**
     * Records a VM-to-host mapping as soon as allocation happens.
     *
     * @param vmId VM identifier
     * @param hostId host identifier
     */
    public void recordVmHostAllocation(long vmId, long hostId) {
        vmHostAllocations.put(vmId, hostId);
    }

    /**
     * Captures host metrics for all hosts.
     *
     * @param hosts monitored hosts
     * @param timestamp simulation time
     * @return host metric list
     */
    public List<HostMetrics> collectHostMetrics(List<Host> hosts, double timestamp) {
        return hosts.stream()
                .map(host -> collectHostMetrics(host, timestamp))
                .toList();
    }

    /**
     * Captures VM metrics for all VMs.
     *
     * @param vms monitored VMs
     * @param timestamp simulation time
     * @return VM metric list
     */
    public List<VmMetrics> collectVmMetrics(List<Vm> vms, double timestamp) {
        return vms.stream()
                .map(vm -> collectVmMetrics(vm, timestamp))
                .toList();
    }

    /**
     * Captures cloudlet metrics for all cloudlets.
     *
     * @param cloudlets monitored cloudlets
     * @return cloudlet metric list
     */
    public List<CloudletMetrics> collectCloudletMetrics(List<Cloudlet> cloudlets) {
        return cloudlets.stream()
                .map(this::collectCloudletMetrics)
                .toList();
    }

    /**
     * Captures aggregate datacenter metrics.
     *
     * @param hosts monitored hosts
     * @param vms monitored VMs
     * @param cloudlets monitored cloudlets
     * @param broker simulation broker
     * @param timestamp simulation time
     * @return datacenter metrics
     */
    public DatacenterMetrics collectDatacenterMetrics(
            List<Host> hosts,
            List<Vm> vms,
            List<Cloudlet> cloudlets,
            DatacenterBroker broker,
            double timestamp) {
        List<HostMetrics> hostMetrics = collectHostMetrics(hosts, timestamp);
        List<VmMetrics> vmMetrics = collectVmMetrics(vms, timestamp);
        int completedCloudlets = broker.getCloudletFinishedList().size();
        int failedCloudlets = countFailedCloudlets(cloudlets);

        return new DatacenterMetrics(
                hosts.size(),
                vms.size(),
                broker.getVmCreatedList().size(),
                (int) vmMetrics.stream().filter(vm -> "RUNNING".equals(vm.getStatus())).count(),
                (int) vmMetrics.stream().filter(vm -> "IDLE".equals(vm.getStatus())).count(),
                completedCloudlets,
                failedCloudlets,
                Math.max(0, cloudlets.size() - completedCloudlets - failedCloudlets),
                average(hostMetrics.stream().mapToDouble(HostMetrics::getCpuUtilization).toArray()),
                average(hostMetrics.stream().mapToDouble(HostMetrics::getRamUtilization).toArray()),
                average(hostMetrics.stream().mapToDouble(HostMetrics::getBandwidthUtilization).toArray()),
                timestamp);
    }

    private HostMetrics collectHostMetrics(Host host, double timestamp) {
        return new HostMetrics(
                host.getId(),
                host.getCpuPercentUtilization(),
                host.getRam().getPercentUtilization(),
                host.getBw().getPercentUtilization(),
                host.getStorage().getPercentUtilization(),
                host.getRam().getAvailableResource(),
                host.getFreePesNumber(),
                host.getBw().getAvailableResource(),
                host.getStorage().getAvailableResource(),
                host.getVmList().size(),
                host.isActive() ? "ACTIVE" : "INACTIVE",
                timestamp);
    }

    private VmMetrics collectVmMetrics(Vm vm, double timestamp) {
        long hostId = vm.isCreated() ? vm.getHost().getId() : -1;
        if (vm.isCreated()) {
            recordVmHostAllocation(vm.getId(), hostId);
        }
        int currentCloudlets = vm.getCloudletScheduler().getCloudletExecList().size();
        int finishedCloudlets = vm.getCloudletScheduler().getCloudletFinishedList().size();
        String status = !vm.isCreated() ? "NOT_ALLOCATED" : currentCloudlets > 0 ? "RUNNING" : "IDLE";

        return new VmMetrics(
                vm.getId(),
                hostId,
                vm.getCpuPercentUtilization(timestamp),
                vm.getHostRamUtilization(),
                vm.getHostBwUtilization(),
                currentCloudlets,
                finishedCloudlets,
                vm.getTotalExecutionTime(),
                status,
                timestamp);
    }

    private CloudletMetrics collectCloudletMetrics(Cloudlet cloudlet) {
        Vm vm = cloudlet.getVm();
        long vmId = vm == Vm.NULL ? -1 : vm.getId();
        long hostId = vm == Vm.NULL ? -1 : vmHostAllocations.getOrDefault(vmId, vm.isCreated() ? vm.getHost().getId() : -1);
        double startTime = cloudlet.getStartTime();
        double finishTime = cloudlet.getFinishTime();
        double submissionTime = cloudlet.getBrokerArrivalTime();
        double executionTime = Math.max(0, finishTime - startTime);
        double waitingTime = Math.max(0, cloudlet.getStartWaitTime());

        return new CloudletMetrics(
                cloudlet.getId(),
                vmId,
                hostId,
                submissionTime,
                startTime,
                finishTime,
                waitingTime,
                executionTime,
                executionTime,
                cloudlet.getStatus().name());
    }

    private int countFailedCloudlets(List<Cloudlet> cloudlets) {
        return (int) cloudlets.stream()
                .filter(cloudlet -> cloudlet.getStatus().name().startsWith("FAILED"))
                .count();
    }

    private double average(double[] values) {
        if (values.length == 0) {
            return 0;
        }

        double total = 0;
        for (double value : values) {
            total += value;
        }
        return total / values.length;
    }
}
