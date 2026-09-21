package com.deepak.greencloud.constants;

/**
 * Stores fixed application labels and formatting constants.
 */
public final class Constants {

    public static final String SEPARATOR = "========================================";
    public static final String PROJECT_NAME = "Green Cloud Framework";
    public static final String SIMULATION_NAME = "CloudSim Plus Simulation";
    public static final String SUMMARY_TITLE = "Simulation Summary";

    public static final String SIMULATION_STARTED_MESSAGE = "Simulation Started";
    public static final String CREATING_DATACENTER_MESSAGE = "Creating Datacenter...";
    public static final String CREATING_HOSTS_MESSAGE = "Creating Hosts...";
    public static final String CREATING_BROKER_MESSAGE = "Creating Broker...";
    public static final String CREATING_VMS_MESSAGE = "Creating VMs...";
    public static final String CREATING_CLOUDLETS_MESSAGE = "Creating Cloudlets...";
    public static final String SIMULATION_RUNNING_MESSAGE = "Simulation Running...";
    public static final String SIMULATION_COMPLETED_MESSAGE = "Simulation Completed";
    public static final String SIMULATION_NOT_INITIALIZED_MESSAGE = "Simulation must be initialized before it can run.";

    public static final String RESOURCE_VALIDATION_TITLE = "Resource Validation";
    public static final String HOSTS_REQUESTED_LABEL = "Hosts Requested";
    public static final String HOSTS_CREATED_LABEL = "Hosts Created";
    public static final String REQUIRED_HOSTS_LABEL = "Required Hosts";
    public static final String VMS_REQUESTED_LABEL = "VM Requested";
    public static final String VMS_ALLOCATED_LABEL = "VM Allocated";
    public static final String VMS_FAILED_LABEL = "VM Failed";
    public static final String CLOUDLETS_REQUESTED_LABEL = "Cloudlets Requested";
    public static final String CLOUDLETS_COMPLETED_LABEL = "Cloudlets Completed";
    public static final String CLOUDLETS_FAILED_LABEL = "Cloudlets Failed";
    public static final String AVERAGE_CPU_UTILIZATION_LABEL = "Average CPU Utilization";
    public static final String AVERAGE_HOST_UTILIZATION_LABEL = "Average Host Utilization";
    public static final String SIMULATION_TIME_LABEL = "Simulation Time";
    public static final String RESOURCE_UTILIZATION_LABEL = "Resource Utilization";
    public static final String ALLOCATION_SUCCESS_LABEL = "Allocation Success %";
    public static final String AVAILABLE_CAPACITY_LABEL = "Available Capacity";
    public static final String NEW_CAPACITY_LABEL = "New Capacity";
    public static final String PLANNED_VMS_PER_HOST_LABEL = "Planned VMs Per Host";
    public static final String HOST_RAM_LABEL = "Host RAM";
    public static final String HOST_BW_LABEL = "Host Bandwidth";
    public static final String HOST_STORAGE_LABEL = "Host Storage";
    public static final String HOST_PES_LABEL = "Host PEs";
    public static final String HOST_MIPS_LABEL = "Host PE MIPS";
    public static final String SCALING_HOST_RAM_MESSAGE = "Scaling Host RAM...";
    public static final String SCALING_HOST_CPU_MESSAGE = "Scaling Host CPU...";
    public static final String SCALING_HOST_BANDWIDTH_MESSAGE = "Scaling Host Bandwidth...";
    public static final String SCALING_HOST_STORAGE_MESSAGE = "Scaling Host Storage...";
    public static final String CAPACITY_SUFFICIENT_MESSAGE = "Resource capacity is sufficient.";
    public static final String CAPACITY_INSUFFICIENT_MESSAGE = "Warning: Resource capacity is still insufficient.";
    public static final String PERCENT_FORMAT = "%.2f%%";
    public static final String SECONDS_FORMAT = "%.4f seconds";
    public static final String SIMULATION_EVENT_LOG_TITLE = "SIMULATION EVENT LOG";
    public static final String LIVE_MONITOR_TITLE = "LIVE CLOUD RESOURCE MONITOR";
    public static final String DATACENTER_LIVE_TITLE = "DATACENTER";
    public static final String VM_SUMMARY_TITLE = "VM SUMMARY";
    public static final String CLOUDLET_SUMMARY_TITLE = "CLOUDLET SUMMARY";
    public static final String RECENT_EVENTS_TITLE = "RECENT EVENTS";
    public static final String RESOURCE_UTILIZATION_TITLE = "RESOURCE UTILIZATION";
    public static final String PERFORMANCE_SUMMARY_TITLE = "PERFORMANCE SUMMARY";
    public static final String EVENT_TIMELINE_FORMAT = "[%.2f] %s";
    public static final String HOST_RESOURCE_BLOCK_FORMAT = "Host %d%nCPU Usage: %.2f%%%nRAM Usage: %.2f%%%nBandwidth Usage: %.2f%%%nStorage Usage: %.2f%%%nRunning VMs: %d%n----------------------------------------";
    public static final String LIVE_SEPARATOR = "====================================================";
    public static final String LIVE_SECTION_SEPARATOR = "----------------------------------------------------";
    public static final String LIVE_TIME_FORMAT = "Simulation Time : %.2f sec";
    public static final String LIVE_HOST_FORMAT = "Host %d%nCPU : %.2f%%%nRAM : %.2f%%%nBandwidth : %.2f%%%nStorage : %.2f%%%nRunning VMs : %d%nAvailable RAM : %d%nAvailable CPU : %d PEs%nStatus : %s%n----------------------------------------------------";
    public static final String LIVE_VM_FORMAT = "VM %d | Host %d | CPU %.2f%% | RAM %.2f%% | Running Cloudlets %d | Completed Cloudlets %d | Status %s";
    public static final String LIVE_CLOUDLET_FORMAT = "Cloudlet %d | VM %d | Host %d | Status %s | Start %.2f | Finish %.2f";
    public static final String CLEAR_CONSOLE_SEQUENCE = "\033[H\033[2J";
    public static final String ACTIVE_STATUS = "ACTIVE";
    public static final String INACTIVE_STATUS = "INACTIVE";
    public static final String EVENT_SIMULATION_STARTED = "Simulation Started";
    public static final String EVENT_SIMULATION_FINISHED = "Simulation Finished";
    public static final String EVENT_HOST_CREATED = "Host Created";
    public static final String EVENT_VM_CREATED = "VM Created";
    public static final String EVENT_VM_ALLOCATED = "VM Allocated";
    public static final String EVENT_VM_DESTROYED = "VM Destroyed";
    public static final String EVENT_VM_FAILED = "VM Failed";
    public static final String EVENT_CLOUDLET_SUBMITTED = "Cloudlet Submitted";
    public static final String EVENT_CLOUDLET_STARTED = "Cloudlet Started";
    public static final String EVENT_CLOUDLET_FINISHED = "Cloudlet Finished";
    public static final String EVENT_CLOUDLET_FAILED = "Cloudlet Failed";
    public static final String EVENT_DATACENTER_STARTED = "Datacenter Started";
    public static final String EVENT_DATACENTER_FINISHED = "Datacenter Finished";
    public static final String EVENT_LOG_FILE = "event_log.csv";
    public static final String HOST_METRICS_FILE = "host_metrics.csv";
    public static final String VM_METRICS_FILE = "vm_metrics.csv";
    public static final String CLOUDLET_METRICS_FILE = "cloudlet_metrics.csv";
    public static final String SIMULATION_SUMMARY_FILE = "simulation_summary.csv";

    public static final double NANOSECONDS_PER_SECOND = 1_000_000_000.0;
    public static final double RESOURCE_UTILIZATION_DIMENSIONS = 5.0;
    public static final double MONITORED_HOST_RESOURCE_DIMENSIONS = 4.0;
    public static final double PERCENT_SCALE = 100.0;
    public static final double MONITORING_TIME_EPSILON = 0.000001;

    private Constants() {
    }
}
