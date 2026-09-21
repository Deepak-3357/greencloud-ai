package com.deepak.greencloud.simulation;

import com.deepak.greencloud.ai.placement.AIPlacementManager;
import com.deepak.greencloud.broker.BrokerManager;
import com.deepak.greencloud.cloudlet.CloudletManager;
import com.deepak.greencloud.config.SimulationConfig;
import com.deepak.greencloud.config.SimulationConfig.ResourcePlan;
import com.deepak.greencloud.constants.Constants;
import com.deepak.greencloud.datacenter.DatacenterManager;
import com.deepak.greencloud.exceptions.SimulationException;
import com.deepak.greencloud.host.HostManager;
import com.deepak.greencloud.monitoring.MonitoringManager;
import com.deepak.greencloud.monitoring.MonitoringListener;
import com.deepak.greencloud.utils.LoggerUtil;
import com.deepak.greencloud.vm.VMManager;
import java.util.List;
import org.cloudsimplus.brokers.DatacenterBroker;
import org.cloudsimplus.cloudlets.Cloudlet;
import org.cloudsimplus.core.CloudSimPlus;
import org.cloudsimplus.datacenters.Datacenter;
import org.cloudsimplus.hosts.Host;
import org.cloudsimplus.vms.Vm;

/**
 * Coordinates the CloudSim Plus workflow for Module 1.
 */
public class CloudSimulation {

    private CloudSimPlus simulation;
    private Datacenter datacenter;
    private DatacenterBroker broker;
    private List<Host> hosts;
    private List<Vm> vms;
    private List<Cloudlet> cloudlets;
    private ResourcePlan resourcePlan;
    private MonitoringManager monitoringManager;
    private AIPlacementManager aiPlacementManager;
    private double executionTime;
    private final MonitoringListener monitoringListener;

    public CloudSimulation() {
        this(null);
    }

    public CloudSimulation(MonitoringListener monitoringListener) {
        this.monitoringListener = monitoringListener;
    }

    /**
     * Initializes CloudSim Plus and creates the simulation entities.
     */
    public void initialize() {
        LoggerUtil.printBanner();
        LoggerUtil.info(Constants.SIMULATION_STARTED_MESSAGE);

        simulation = new CloudSimPlus();

        LoggerUtil.info(Constants.CREATING_DATACENTER_MESSAGE);
        HostManager hostManager = new HostManager();
        hostManager.validateResourceCapacity();
        resourcePlan = hostManager.getResourcePlan();
        if (!resourcePlan.hasSufficientCapacity()) {
            throw new SimulationException(Constants.CAPACITY_INSUFFICIENT_MESSAGE);
        }

        aiPlacementManager = new AIPlacementManager();
        DatacenterManager datacenterManager = new DatacenterManager(simulation, hostManager, aiPlacementManager);
        datacenter = datacenterManager.createDatacenter();
        hosts = datacenter.getHostList();

        LoggerUtil.info(Constants.CREATING_BROKER_MESSAGE);
        BrokerManager brokerManager = new BrokerManager(simulation);
        broker = brokerManager.createBroker();

        LoggerUtil.info(Constants.CREATING_VMS_MESSAGE);
        VMManager vmManager = new VMManager();
        vms = vmManager.createVirtualMachines();
        broker.submitVmList(vms);

        LoggerUtil.info(Constants.CREATING_CLOUDLETS_MESSAGE);
        CloudletManager cloudletManager = new CloudletManager();
        cloudlets = cloudletManager.createCloudlets();
        broker.submitCloudletList(cloudlets);

        monitoringManager = new MonitoringManager();
        monitoringManager.initialize(simulation, datacenter, broker, hosts, vms, cloudlets, aiPlacementManager);
        monitoringManager.addMonitoringListener(monitoringListener);
    }

    /**
     * Starts the simulation.
     */
    public void run() {
        if (simulation == null) {
            throw new SimulationException(Constants.SIMULATION_NOT_INITIALIZED_MESSAGE);
        }

        LoggerUtil.info(Constants.SIMULATION_RUNNING_MESSAGE);
        monitoringManager.startMonitoring();
        double startTime = System.nanoTime();
        runSimulation();
        executionTime = (System.nanoTime() - startTime) / Constants.NANOSECONDS_PER_SECOND;
        monitoringManager.stopMonitoring();
        LoggerUtil.info(Constants.SIMULATION_COMPLETED_MESSAGE);
    }

    private void runSimulation() {
        if (!SimulationConfig.MONITORING_ENABLED) {
            simulation.start();
            return;
        }

        simulation.startSync();
        while (simulation.isRunning()) {
            simulation.runFor(SimulationConfig.MONITORING_INTERVAL);
            monitoringManager.displayLiveMonitoring(simulation.clock());
        }
    }

    /**
     * Prints an aggregate simulation summary.
     */
    public void printResults() {
        int vmAllocated = broker.getVmCreatedList().size();
        int vmFailed = Math.max(0, resourcePlan.vmsRequested() - vmAllocated);
        int cloudletsCompleted = broker.getCloudletFinishedList().size();
        int cloudletsFailed = Math.max(0, resourcePlan.cloudletsRequested() - cloudletsCompleted);

        LoggerUtil.printSummaryHeader();
        LoggerUtil.summary(Constants.HOSTS_REQUESTED_LABEL, resourcePlan.hostsRequested());
        LoggerUtil.summary(Constants.HOSTS_CREATED_LABEL, hosts.size());
        LoggerUtil.summary(Constants.REQUIRED_HOSTS_LABEL, resourcePlan.requiredHosts());
        LoggerUtil.summary(Constants.VMS_REQUESTED_LABEL, resourcePlan.vmsRequested());
        LoggerUtil.summary(Constants.VMS_ALLOCATED_LABEL, vmAllocated);
        LoggerUtil.summary(Constants.VMS_FAILED_LABEL, vmFailed);
        LoggerUtil.summary(Constants.CLOUDLETS_REQUESTED_LABEL, resourcePlan.cloudletsRequested());
        LoggerUtil.summary(Constants.CLOUDLETS_COMPLETED_LABEL, cloudletsCompleted);
        LoggerUtil.summary(Constants.CLOUDLETS_FAILED_LABEL, cloudletsFailed);
        LoggerUtil.summary(Constants.AVERAGE_CPU_UTILIZATION_LABEL, formatPercent(calculateCpuUtilizationPercent()));
        LoggerUtil.summary(Constants.AVERAGE_HOST_UTILIZATION_LABEL, formatPercent(calculateAverageHostUtilizationPercent()));
        LoggerUtil.summary(Constants.SIMULATION_TIME_LABEL, String.format(Constants.SECONDS_FORMAT, executionTime));
        LoggerUtil.summary(Constants.RESOURCE_UTILIZATION_LABEL, formatResourceUtilization());
        LoggerUtil.summary(Constants.ALLOCATION_SUCCESS_LABEL, formatPercent(calculateAllocationSuccessPercent(vmAllocated)));
        monitoringManager.generateReports();
    }

    private double calculateCpuUtilizationPercent() {
        double totalHostMips = resourcePlan.totalHostMips();
        if (totalHostMips == 0) {
            return 0;
        }
        return resourcePlan.requestedVmMips() * SimulationConfig.PERCENT_MULTIPLIER / totalHostMips;
    }

    private double calculateAverageHostUtilizationPercent() {
        return (calculateRamUtilizationPercent()
                + calculateBandwidthUtilizationPercent()
                + calculateStorageUtilizationPercent()
                + calculatePeUtilizationPercent()
                + calculateCpuUtilizationPercent()) / Constants.RESOURCE_UTILIZATION_DIMENSIONS;
    }

    private double calculateRamUtilizationPercent() {
        long totalRam = resourcePlan.hostsCreated() * resourcePlan.hostResources().ram();
        long requestedRam = resourcePlan.vmsRequested() * resourcePlan.vmResources().ram();
        return calculatePercentage(requestedRam, totalRam);
    }

    private double calculateBandwidthUtilizationPercent() {
        long totalBandwidth = resourcePlan.hostsCreated() * resourcePlan.hostResources().bandwidth();
        long requestedBandwidth = resourcePlan.vmsRequested() * resourcePlan.vmResources().bandwidth();
        return calculatePercentage(requestedBandwidth, totalBandwidth);
    }

    private double calculateStorageUtilizationPercent() {
        long totalStorage = resourcePlan.hostsCreated() * resourcePlan.hostResources().storage();
        long requestedStorage = resourcePlan.vmsRequested() * resourcePlan.vmResources().size();
        return calculatePercentage(requestedStorage, totalStorage);
    }

    private double calculatePeUtilizationPercent() {
        long totalPes = (long) resourcePlan.hostsCreated() * resourcePlan.hostResources().pes();
        long requestedPes = (long) resourcePlan.vmsRequested() * resourcePlan.vmResources().pes();
        return calculatePercentage(requestedPes, totalPes);
    }

    private double calculateAllocationSuccessPercent(int vmAllocated) {
        return calculatePercentage(vmAllocated, resourcePlan.vmsRequested());
    }

    private double calculatePercentage(double value, double total) {
        if (total == 0) {
            return 0;
        }
        return value * SimulationConfig.PERCENT_MULTIPLIER / total;
    }

    private String formatResourceUtilization() {
        return String.format(
                "RAM %s | CPU %s | PEs %s | BW %s | Storage %s",
                formatPercent(calculateRamUtilizationPercent()),
                formatPercent(calculateCpuUtilizationPercent()),
                formatPercent(calculatePeUtilizationPercent()),
                formatPercent(calculateBandwidthUtilizationPercent()),
                formatPercent(calculateStorageUtilizationPercent()));
    }

    private String formatPercent(double value) {
        return String.format(Constants.PERCENT_FORMAT, value);
    }
}
