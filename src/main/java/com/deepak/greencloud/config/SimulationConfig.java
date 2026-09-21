package com.deepak.greencloud.config;

/**
 * Stores configurable simulation parameters and resource sizing helpers for Module 1.
 */
public final class SimulationConfig {

    public static final String HOST_COUNT_PROPERTY = "greencloud.hostCount";
    public static final String VM_COUNT_PROPERTY = "greencloud.vmCount";
    public static final String CLOUDLET_COUNT_PROPERTY = "greencloud.cloudletCount";
    public static final String BASE_HOST_PES_PROPERTY = "greencloud.baseHostPes";
    public static final String BASE_HOST_RAM_PROPERTY = "greencloud.baseHostRam";
    public static final String BASE_HOST_BW_PROPERTY = "greencloud.baseHostBw";
    public static final String BASE_HOST_STORAGE_PROPERTY = "greencloud.baseHostStorage";
    public static final String BASE_HOST_PE_MIPS_PROPERTY = "greencloud.baseHostPeMips";
    public static final String VM_PES_PROPERTY = "greencloud.vmPes";
    public static final String VM_RAM_PROPERTY = "greencloud.vmRam";
    public static final String VM_BW_PROPERTY = "greencloud.vmBw";
    public static final String VM_SIZE_PROPERTY = "greencloud.vmSize";
    public static final String VM_MIPS_PROPERTY = "greencloud.vmMips";
    public static final String CLOUDLET_PES_PROPERTY = "greencloud.cloudletPes";
    public static final String CLOUDLET_LENGTH_PROPERTY = "greencloud.cloudletLength";
    public static final String CLOUDLET_FILE_SIZE_PROPERTY = "greencloud.cloudletFileSize";
    public static final String CLOUDLET_OUTPUT_SIZE_PROPERTY = "greencloud.cloudletOutputSize";
    public static final String AUTO_SCALE_HOST_RESOURCES_PROPERTY = "greencloud.autoScaleHostResources";
    public static final String AUTO_CREATE_REQUIRED_HOSTS_PROPERTY = "greencloud.autoCreateRequiredHosts";
    public static final String TARGET_VMS_PER_HOST_PROPERTY = "greencloud.targetVmsPerHost";
    public static final String RESOURCE_SAFETY_MARGIN_PERCENT_PROPERTY = "greencloud.resourceSafetyMarginPercent";
    public static final String REPORTS_DIRECTORY_PROPERTY = "greencloud.reportsDirectory";
    public static final String MONITORING_ENABLED_PROPERTY = "greencloud.monitoringEnabled";
    public static final String MONITORING_INTERVAL_PROPERTY = "greencloud.monitoringInterval";
    public static final String MONITORING_SNAPSHOT_INTERVAL_PROPERTY = "greencloud.monitoringSnapshotInterval";
    public static final String SHOW_VM_DETAILS_PROPERTY = "greencloud.showVmDetails";
    public static final String SHOW_HOST_DETAILS_PROPERTY = "greencloud.showHostDetails";
    public static final String SHOW_CLOUDLET_DETAILS_PROPERTY = "greencloud.showCloudletDetails";
    public static final String CLEAR_CONSOLE_PROPERTY = "greencloud.clearConsole";
    public static final String MAX_VM_DISPLAY_PROPERTY = "greencloud.maxVmDisplay";

    public static final int DEFAULT_HOST_COUNT = 10;
    public static final int DEFAULT_BASE_HOST_PES = 4;
    public static final long DEFAULT_BASE_HOST_RAM = 16_384;
    public static final long DEFAULT_BASE_HOST_BW = 10_000;
    public static final long DEFAULT_BASE_HOST_STORAGE = 1_000_000;
    public static final double DEFAULT_BASE_HOST_PE_MIPS = 10_000;
    public static final int DEFAULT_VM_COUNT = 20;
    public static final int DEFAULT_VM_PES = 2;
    public static final long DEFAULT_VM_RAM = 4_096;
    public static final long DEFAULT_VM_BW = 1_000;
    public static final long DEFAULT_VM_SIZE = 10_000;
    public static final double DEFAULT_VM_MIPS = 2_500;
    public static final int DEFAULT_CLOUDLET_COUNT = 50;
    public static final int DEFAULT_CLOUDLET_PES = 1;
    public static final long DEFAULT_CLOUDLET_LENGTH = 10_000;
    public static final long DEFAULT_CLOUDLET_FILE_SIZE = 300;
    public static final long DEFAULT_CLOUDLET_OUTPUT_SIZE = 300;
    public static final boolean DEFAULT_AUTO_SCALE_HOST_RESOURCES = true;
    public static final boolean DEFAULT_AUTO_CREATE_REQUIRED_HOSTS = true;
    public static final int DEFAULT_TARGET_VMS_PER_HOST = 5;
    public static final int DEFAULT_RESOURCE_SAFETY_MARGIN_PERCENT = 20;
    public static final String DEFAULT_REPORTS_DIRECTORY = "reports";
    public static final boolean DEFAULT_MONITORING_ENABLED = true;
    public static final double DEFAULT_MONITORING_INTERVAL = 1.0;
    public static final boolean DEFAULT_SHOW_VM_DETAILS = true;
    public static final boolean DEFAULT_SHOW_HOST_DETAILS = true;
    public static final boolean DEFAULT_SHOW_CLOUDLET_DETAILS = false;
    public static final boolean DEFAULT_CLEAR_CONSOLE = false;
    public static final int DEFAULT_MAX_VM_DISPLAY = 20;

    public static final int HOST_COUNT = getInt(HOST_COUNT_PROPERTY, DEFAULT_HOST_COUNT);
    public static final int BASE_HOST_PES = getInt(BASE_HOST_PES_PROPERTY, DEFAULT_BASE_HOST_PES);
    public static final long BASE_HOST_RAM = getLong(BASE_HOST_RAM_PROPERTY, DEFAULT_BASE_HOST_RAM);
    public static final long BASE_HOST_BW = getLong(BASE_HOST_BW_PROPERTY, DEFAULT_BASE_HOST_BW);
    public static final long BASE_HOST_STORAGE = getLong(BASE_HOST_STORAGE_PROPERTY, DEFAULT_BASE_HOST_STORAGE);
    public static final double BASE_HOST_PE_MIPS = getDouble(BASE_HOST_PE_MIPS_PROPERTY, DEFAULT_BASE_HOST_PE_MIPS);
    public static final int HOST_PES = BASE_HOST_PES;
    public static final long HOST_RAM = BASE_HOST_RAM;
    public static final long HOST_BW = BASE_HOST_BW;
    public static final long HOST_STORAGE = BASE_HOST_STORAGE;
    public static final double HOST_PE_MIPS = BASE_HOST_PE_MIPS;

    public static final int VM_COUNT = getInt(VM_COUNT_PROPERTY, DEFAULT_VM_COUNT);
    public static final int VM_PES = getInt(VM_PES_PROPERTY, DEFAULT_VM_PES);
    public static final long VM_RAM = getLong(VM_RAM_PROPERTY, DEFAULT_VM_RAM);
    public static final long VM_BW = getLong(VM_BW_PROPERTY, DEFAULT_VM_BW);
    public static final long VM_SIZE = getLong(VM_SIZE_PROPERTY, DEFAULT_VM_SIZE);
    public static final double VM_MIPS = getDouble(VM_MIPS_PROPERTY, DEFAULT_VM_MIPS);

    public static final int CLOUDLET_COUNT = getInt(CLOUDLET_COUNT_PROPERTY, DEFAULT_CLOUDLET_COUNT);
    public static final int CLOUDLET_PES = getInt(CLOUDLET_PES_PROPERTY, DEFAULT_CLOUDLET_PES);
    public static final long CLOUDLET_LENGTH = getLong(CLOUDLET_LENGTH_PROPERTY, DEFAULT_CLOUDLET_LENGTH);
    public static final long CLOUDLET_FILE_SIZE = getLong(CLOUDLET_FILE_SIZE_PROPERTY, DEFAULT_CLOUDLET_FILE_SIZE);
    public static final long CLOUDLET_OUTPUT_SIZE = getLong(CLOUDLET_OUTPUT_SIZE_PROPERTY, DEFAULT_CLOUDLET_OUTPUT_SIZE);

    public static final boolean AUTO_SCALE_HOST_RESOURCES = getBoolean(
            AUTO_SCALE_HOST_RESOURCES_PROPERTY,
            DEFAULT_AUTO_SCALE_HOST_RESOURCES);
    public static final boolean AUTO_CREATE_REQUIRED_HOSTS = getBoolean(
            AUTO_CREATE_REQUIRED_HOSTS_PROPERTY,
            DEFAULT_AUTO_CREATE_REQUIRED_HOSTS);
    public static final int TARGET_VMS_PER_HOST = getInt(TARGET_VMS_PER_HOST_PROPERTY, DEFAULT_TARGET_VMS_PER_HOST);
    public static final int RESOURCE_SAFETY_MARGIN_PERCENT = getInt(
            RESOURCE_SAFETY_MARGIN_PERCENT_PROPERTY,
            DEFAULT_RESOURCE_SAFETY_MARGIN_PERCENT);
    public static final String REPORTS_DIRECTORY = System.getProperty(REPORTS_DIRECTORY_PROPERTY, DEFAULT_REPORTS_DIRECTORY);
    public static final boolean MONITORING_ENABLED = getBoolean(MONITORING_ENABLED_PROPERTY, DEFAULT_MONITORING_ENABLED);
    public static final double MONITORING_INTERVAL = getDouble(MONITORING_INTERVAL_PROPERTY, DEFAULT_MONITORING_INTERVAL);
    public static final double MONITORING_SNAPSHOT_INTERVAL = getDouble(
            MONITORING_SNAPSHOT_INTERVAL_PROPERTY,
            MONITORING_INTERVAL);
    public static final boolean SHOW_VM_DETAILS = getBoolean(SHOW_VM_DETAILS_PROPERTY, DEFAULT_SHOW_VM_DETAILS);
    public static final boolean SHOW_HOST_DETAILS = getBoolean(SHOW_HOST_DETAILS_PROPERTY, DEFAULT_SHOW_HOST_DETAILS);
    public static final boolean SHOW_CLOUDLET_DETAILS = getBoolean(
            SHOW_CLOUDLET_DETAILS_PROPERTY,
            DEFAULT_SHOW_CLOUDLET_DETAILS);
    public static final boolean CLEAR_CONSOLE = getBoolean(CLEAR_CONSOLE_PROPERTY, DEFAULT_CLEAR_CONSOLE);
    public static final int MAX_VM_DISPLAY = getInt(MAX_VM_DISPLAY_PROPERTY, DEFAULT_MAX_VM_DISPLAY);
    public static final int PERCENT_MULTIPLIER = 100;
    public static final int MINIMUM_RESOURCE = 1;

    private SimulationConfig() {
    }

    /**
     * Calculates a complete resource plan for the requested workload.
     *
     * @return resource plan used to create hosts and validate capacity
     */
    public static ResourcePlan calculateResourcePlan() {
        int requiredHosts = calculateRequiredHosts();
        int hostsCreated = AUTO_CREATE_REQUIRED_HOSTS ? Math.max(HOST_COUNT, requiredHosts) : HOST_COUNT;
        int plannedVmsPerHost = calculatePlannedVmsPerHost(hostsCreated);

        HostResources baseHostResources = new HostResources(
                BASE_HOST_RAM,
                BASE_HOST_BW,
                BASE_HOST_STORAGE,
                BASE_HOST_PES,
                BASE_HOST_PE_MIPS);

        HostResources scaledHostResources = new HostResources(
                calculateHostRam(plannedVmsPerHost),
                calculateHostBandwidth(plannedVmsPerHost),
                calculateHostStorage(plannedVmsPerHost),
                calculateHostPes(plannedVmsPerHost),
                calculateHostPeMips());

        HostResources selectedHostResources = AUTO_SCALE_HOST_RESOURCES ? scaledHostResources : baseHostResources;
        int capacityBeforeScaling = calculateVmCapacity(HOST_COUNT, baseHostResources, calculateVmResources());
        int capacityAfterScaling = calculateVmCapacity(hostsCreated, selectedHostResources, calculateVmResources());

        return new ResourcePlan(
                HOST_COUNT,
                hostsCreated,
                requiredHosts,
                VM_COUNT,
                CLOUDLET_COUNT,
                plannedVmsPerHost,
                capacityBeforeScaling,
                capacityAfterScaling,
                baseHostResources,
                selectedHostResources,
                calculateVmResources());
    }

    /**
     * Calculates the minimum hosts required using the configured target VM density.
     *
     * @return required host count
     */
    public static int calculateRequiredHosts() {
        return divideAndRoundUp(VM_COUNT, Math.max(MINIMUM_RESOURCE, TARGET_VMS_PER_HOST));
    }

    /**
     * Calculates the RAM each host needs for the planned VM density.
     *
     * @return RAM in MB
     */
    public static long calculateHostRam(int plannedVmsPerHost) {
        return Math.max(BASE_HOST_RAM, applySafetyMargin(VM_RAM * plannedVmsPerHost));
    }

    /**
     * Calculates the processing elements each host needs for the planned VM density.
     *
     * @return host PE count
     */
    public static int calculateHostPes(int plannedVmsPerHost) {
        return Math.max(BASE_HOST_PES, VM_PES * plannedVmsPerHost);
    }

    /**
     * Calculates the bandwidth each host needs for the planned VM density.
     *
     * @return bandwidth in Mbps
     */
    public static long calculateHostBandwidth(int plannedVmsPerHost) {
        return Math.max(BASE_HOST_BW, applySafetyMargin(VM_BW * plannedVmsPerHost));
    }

    /**
     * Calculates the storage each host needs for the planned VM density and cloudlet workload.
     *
     * @return storage in MB
     */
    public static long calculateHostStorage(int plannedVmsPerHost) {
        long vmStorage = VM_SIZE * plannedVmsPerHost;
        long cloudletStorageShare = divideAndRoundUp(CLOUDLET_COUNT * (CLOUDLET_FILE_SIZE + CLOUDLET_OUTPUT_SIZE),
                Math.max(MINIMUM_RESOURCE, HOST_COUNT));
        return Math.max(BASE_HOST_STORAGE, applySafetyMargin(vmStorage + cloudletStorageShare));
    }

    /**
     * Calculates the VM resource requirements.
     *
     * @return VM resource specification
     */
    public static VmResources calculateVmResources() {
        return new VmResources(VM_RAM, VM_BW, VM_SIZE, VM_PES, VM_MIPS);
    }

    /**
     * Calculates total VM capacity for a host collection.
     *
     * @param hostCount host count
     * @param hostResources resources available on each host
     * @param vmResources resources required by each VM
     * @return total VM capacity
     */
    public static int calculateVmCapacity(int hostCount, HostResources hostResources, VmResources vmResources) {
        if (hostCount <= 0) {
            return 0;
        }

        long ramCapacity = hostResources.ram() / vmResources.ram();
        long bwCapacity = hostResources.bandwidth() / vmResources.bandwidth();
        long storageCapacity = hostResources.storage() / vmResources.size();
        long peCapacity = hostResources.pes() / vmResources.pes();
        long cpuCapacity = (long) ((hostResources.pes() * hostResources.peMips())
                / (vmResources.pes() * vmResources.mips()));
        long hostCapacity = Math.min(Math.min(ramCapacity, bwCapacity), Math.min(storageCapacity, Math.min(peCapacity, cpuCapacity)));

        return Math.toIntExact(Math.max(0, hostCapacity * hostCount));
    }

    private static int calculatePlannedVmsPerHost(int hostsCreated) {
        return divideAndRoundUp(VM_COUNT, Math.max(MINIMUM_RESOURCE, hostsCreated));
    }

    private static double calculateHostPeMips() {
        return Math.max(BASE_HOST_PE_MIPS, VM_MIPS);
    }

    private static long applySafetyMargin(long value) {
        return divideAndRoundUp(value * (PERCENT_MULTIPLIER + RESOURCE_SAFETY_MARGIN_PERCENT), PERCENT_MULTIPLIER);
    }

    private static int divideAndRoundUp(int dividend, int divisor) {
        return Math.max(MINIMUM_RESOURCE, (dividend + divisor - MINIMUM_RESOURCE) / divisor);
    }

    private static long divideAndRoundUp(long dividend, long divisor) {
        return Math.max(MINIMUM_RESOURCE, (dividend + divisor - MINIMUM_RESOURCE) / divisor);
    }

    private static int getInt(String propertyName, int defaultValue) {
        return Integer.parseInt(System.getProperty(propertyName, String.valueOf(defaultValue)));
    }

    private static long getLong(String propertyName, long defaultValue) {
        return Long.parseLong(System.getProperty(propertyName, String.valueOf(defaultValue)));
    }

    private static double getDouble(String propertyName, double defaultValue) {
        return Double.parseDouble(System.getProperty(propertyName, String.valueOf(defaultValue)));
    }

    private static boolean getBoolean(String propertyName, boolean defaultValue) {
        return Boolean.parseBoolean(System.getProperty(propertyName, String.valueOf(defaultValue)));
    }

    /**
     * Host resource values used for capacity planning.
     */
    public record HostResources(long ram, long bandwidth, long storage, int pes, double peMips) {
    }

    /**
     * VM resource values used for capacity planning.
     */
    public record VmResources(long ram, long bandwidth, long size, int pes, double mips) {
    }

    /**
     * Full resource plan for the configured simulation workload.
     */
    public record ResourcePlan(
            int hostsRequested,
            int hostsCreated,
            int requiredHosts,
            int vmsRequested,
            int cloudletsRequested,
            int plannedVmsPerHost,
            int capacityBeforeScaling,
            int capacityAfterScaling,
            HostResources baseHostResources,
            HostResources hostResources,
            VmResources vmResources) {

        /**
         * Checks whether the final plan can allocate all requested VMs.
         *
         * @return true when capacity is sufficient
         */
        public boolean hasSufficientCapacity() {
            return capacityAfterScaling >= vmsRequested;
        }

        /**
         * Calculates the percentage of planned VM capacity that is requested.
         *
         * @return allocation pressure percentage
         */
        public double allocationPressurePercent() {
            if (capacityAfterScaling == 0) {
                return 0;
            }
            return (double) vmsRequested * PERCENT_MULTIPLIER / capacityAfterScaling;
        }

        /**
         * Calculates total host CPU capacity in MIPS.
         *
         * @return CPU capacity in MIPS
         */
        public double totalHostMips() {
            return hostsCreated * hostResources.pes() * hostResources.peMips();
        }

        /**
         * Calculates requested VM CPU capacity in MIPS.
         *
         * @return requested VM CPU in MIPS
         */
        public double requestedVmMips() {
            return vmsRequested * vmResources.pes() * vmResources.mips();
        }
    }
}
