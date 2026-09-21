package com.deepak.greencloud.monitoring;

import com.deepak.greencloud.config.SimulationConfig;
import com.deepak.greencloud.constants.Constants;
import com.deepak.greencloud.utils.LoggerUtil;
import java.util.List;
import org.cloudsimplus.brokers.DatacenterBroker;
import org.cloudsimplus.cloudlets.Cloudlet;
import org.cloudsimplus.hosts.Host;
import org.cloudsimplus.vms.Vm;

/**
 * Displays current cloud resource state while the simulation is running.
 */
public class LiveMonitor {

    private final ResourceMonitor resourceMonitor;
    private double lastDisplayTime;
    private int lastPrintedEventIndex;
    private List<Host> hosts;
    private List<Vm> vms;
    private List<Cloudlet> cloudlets;
    private DatacenterBroker broker;
    private EventLogger eventLogger;

    public LiveMonitor(ResourceMonitor resourceMonitor) {
        this.resourceMonitor = resourceMonitor;
        this.lastDisplayTime = -SimulationConfig.MONITORING_INTERVAL;
        this.lastPrintedEventIndex = 0;
        this.hosts = List.of();
        this.vms = List.of();
        this.cloudlets = List.of();
    }

    /**
     * Initializes the live monitor with simulation entities.
     *
     * @param hosts monitored hosts
     * @param vms monitored VMs
     * @param cloudlets monitored cloudlets
     * @param broker datacenter broker
     * @param eventLogger event logger for recent events
     */
    public void initialize(
            List<Host> hosts,
            List<Vm> vms,
            List<Cloudlet> cloudlets,
            DatacenterBroker broker,
            EventLogger eventLogger) {
        this.hosts = List.copyOf(hosts);
        this.vms = List.copyOf(vms);
        this.cloudlets = List.copyOf(cloudlets);
        this.broker = broker;
        this.eventLogger = eventLogger;
    }

    /**
     * Displays live monitoring output if the configured interval has elapsed.
     *
     * @param simulationTime current simulation time
     */
    public boolean displayIfInterval(double simulationTime) {
        if (!SimulationConfig.MONITORING_ENABLED) {
            return false;
        }

        if (simulationTime - lastDisplayTime + Constants.MONITORING_TIME_EPSILON
                < SimulationConfig.MONITORING_INTERVAL) {
            return false;
        }

        if (SimulationConfig.CLEAR_CONSOLE) {
            clearConsole();
        }

        displayLiveMonitoringConsole(simulationTime);
        lastDisplayTime = simulationTime;
        return true;
    }

    private void displayLiveMonitoringConsole(double simulationTime) {
        List<HostMetrics> hostMetrics = resourceMonitor.collectHostMetrics(hosts, simulationTime);
        List<VmMetrics> vmMetrics = resourceMonitor.collectVmMetrics(vms, simulationTime);
        DatacenterMetrics datacenterMetrics = resourceMonitor.collectDatacenterMetrics(
                hosts,
                vms,
                cloudlets,
                broker,
                simulationTime);

        LoggerUtil.info(Constants.LIVE_SEPARATOR);
        LoggerUtil.info(Constants.LIVE_MONITOR_TITLE);
        LoggerUtil.info(String.format(Constants.LIVE_TIME_FORMAT, simulationTime));
        LoggerUtil.info(Constants.LIVE_SEPARATOR);
        displayDatacenterSummary(datacenterMetrics, hostMetrics, vmMetrics);

        if (SimulationConfig.SHOW_HOST_DETAILS) {
            displayHostDetails(hostMetrics);
        }
        if (SimulationConfig.SHOW_VM_DETAILS) {
            displayVmSummary(vmMetrics);
        }

        displayCloudletSummary(vmMetrics, simulationTime);
        if (SimulationConfig.SHOW_CLOUDLET_DETAILS) {
            displayCloudletDetails();
        }
        displayRecentEvents();
        LoggerUtil.info(Constants.LIVE_SEPARATOR);
    }

    private void displayDatacenterSummary(
            DatacenterMetrics datacenterMetrics,
            List<HostMetrics> hostMetrics,
            List<VmMetrics> vmMetrics) {
        LoggerUtil.info(Constants.DATACENTER_LIVE_TITLE);
        LoggerUtil.summary("Hosts", datacenterMetrics.getTotalHosts());
        LoggerUtil.summary("VMs", datacenterMetrics.getAllocatedVms());
        LoggerUtil.summary("Running Hosts", countRunningHosts(hostMetrics));
        LoggerUtil.summary("Running VMs", datacenterMetrics.getRunningVms());
        LoggerUtil.summary("Running Cloudlets", countRunningCloudlets(vmMetrics));
        LoggerUtil.summary("Completed Cloudlets", datacenterMetrics.getCompletedCloudlets());
        LoggerUtil.summary("Waiting Cloudlets", countWaitingCloudlets(vmMetrics, datacenterMetrics));
        LoggerUtil.summary("Average CPU", formatPercent(datacenterMetrics.getAverageCpuUtilization()));
        LoggerUtil.summary("Average RAM", formatPercent(datacenterMetrics.getAverageRamUtilization()));
        LoggerUtil.info(Constants.LIVE_SECTION_SEPARATOR);
    }

    private void displayHostDetails(List<HostMetrics> hostMetrics) {
        hostMetrics.forEach(metric -> LoggerUtil.info(String.format(
                Constants.LIVE_HOST_FORMAT,
                metric.getHostId(),
                toPercent(metric.getCpuUtilization()),
                toPercent(metric.getRamUtilization()),
                toPercent(metric.getBandwidthUtilization()),
                toPercent(metric.getStorageUtilization()),
                metric.getRunningVmCount(),
                metric.getAvailableRam(),
                metric.getAvailablePes(),
                metric.getStatus())));
    }

    private void displayVmSummary(List<VmMetrics> vmMetrics) {
        LoggerUtil.info(Constants.VM_SUMMARY_TITLE);
        int max = SimulationConfig.MAX_VM_DISPLAY;
        int displayed = 0;
        for (VmMetrics metric : vmMetrics) {
            if (metric.getHostId() < 0) continue;
            if (displayed >= max) break;
            LoggerUtil.info(String.format(
                    Constants.LIVE_VM_FORMAT,
                    metric.getVmId(),
                    metric.getHostId(),
                    toPercent(metric.getCpuUsage()),
                    toPercent(metric.getRamUsage()),
                    metric.getCurrentCloudlets(),
                    metric.getFinishedCloudlets(),
                    metric.getStatus()));
            displayed++;
        }
        int remaining = (int) vmMetrics.stream().filter(m -> m.getHostId() >= 0).count() - displayed;
        if (remaining > 0) {
            LoggerUtil.info(String.format("... %d more VMs omitted ...", remaining));
        }
        LoggerUtil.info(Constants.LIVE_SECTION_SEPARATOR);
    }

    private void displayCloudletSummary(List<VmMetrics> vmMetrics, double simulationTime) {
        LoggerUtil.info(Constants.CLOUDLET_SUMMARY_TITLE);
        LoggerUtil.summary("Running Cloudlets", countRunningCloudlets(vmMetrics));
        LoggerUtil.summary("Completed Cloudlets", broker.getCloudletFinishedList().size());
        LoggerUtil.summary("Waiting Cloudlets", countWaitingCloudlets(vmMetrics,
                resourceMonitor.collectDatacenterMetrics(hosts, vms, cloudlets, broker, simulationTime)));
        LoggerUtil.summary("Failed Cloudlets", countFailedCloudlets());
        LoggerUtil.summary("Average Execution Time", String.format(Constants.SECONDS_FORMAT, calculateAverageExecutionTime()));
        LoggerUtil.info(Constants.LIVE_SECTION_SEPARATOR);
    }

    private void displayCloudletDetails() {
        resourceMonitor.collectCloudletMetrics(cloudlets).forEach(metric -> LoggerUtil.info(String.format(
                Constants.LIVE_CLOUDLET_FORMAT,
                metric.getCloudletId(),
                metric.getVmId(),
                metric.getHostId(),
                metric.getStatus(),
                metric.getStartTime(),
                metric.getFinishTime())));
        LoggerUtil.info(Constants.LIVE_SECTION_SEPARATOR);
    }

    private void displayRecentEvents() {
        LoggerUtil.info(Constants.RECENT_EVENTS_TITLE);
        eventLogger.printEventsSince(lastPrintedEventIndex);
        lastPrintedEventIndex = eventLogger.size();
    }

    private long countRunningHosts(List<HostMetrics> hostMetrics) {
        return hostMetrics.stream()
                .filter(metric -> Constants.ACTIVE_STATUS.equals(metric.getStatus()))
                .count();
    }

    private int countRunningCloudlets(List<VmMetrics> vmMetrics) {
        return vmMetrics.stream()
                .mapToInt(VmMetrics::getCurrentCloudlets)
                .sum();
    }

    private int countWaitingCloudlets(List<VmMetrics> vmMetrics, DatacenterMetrics datacenterMetrics) {
        return Math.max(0, cloudlets.size()
                - datacenterMetrics.getCompletedCloudlets()
                - datacenterMetrics.getFailedCloudlets()
                - countRunningCloudlets(vmMetrics));
    }

    private long countFailedCloudlets() {
        return cloudlets.stream()
                .filter(cloudlet -> cloudlet.getStatus().name().startsWith("FAILED"))
                .count();
    }

    private double calculateAverageExecutionTime() {
        return broker.getCloudletFinishedList().stream()
                .mapToDouble(cloudlet -> Math.max(0, cloudlet.getFinishTime() - cloudlet.getStartTime()))
                .average()
                .orElse(0);
    }

    private String formatPercent(double value) {
        return String.format(Constants.PERCENT_FORMAT, toPercent(value));
    }

    private double toPercent(double value) {
        return Double.isFinite(value) ? value * Constants.PERCENT_SCALE : 0;
    }

    private void clearConsole() {
        System.out.print(Constants.CLEAR_CONSOLE_SEQUENCE);
        System.out.flush();
    }
}
