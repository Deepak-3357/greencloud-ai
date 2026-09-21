package com.deepak.greencloud.host;

import com.deepak.greencloud.config.SimulationConfig;
import com.deepak.greencloud.config.SimulationConfig.HostResources;
import com.deepak.greencloud.config.SimulationConfig.ResourcePlan;
import com.deepak.greencloud.constants.Constants;
import com.deepak.greencloud.utils.LoggerUtil;
import java.util.ArrayList;
import java.util.List;
import org.cloudsimplus.hosts.Host;
import org.cloudsimplus.hosts.HostSimple;
import org.cloudsimplus.resources.Pe;
import org.cloudsimplus.resources.PeSimple;
import org.cloudsimplus.schedulers.vm.VmSchedulerTimeShared;

/**
 * Creates hosts for the simulation datacenter.
 */
public class HostManager {

    private final ResourcePlan resourcePlan;

    public HostManager() {
        this.resourcePlan = SimulationConfig.calculateResourcePlan();
    }

    /**
     * Creates the configured host list.
     *
     * @return list of configured hosts
     */
    public List<Host> createHosts() {
        LoggerUtil.info(Constants.CREATING_HOSTS_MESSAGE);
        List<Host> hosts = new ArrayList<>(resourcePlan.hostsCreated());
        for (int hostIndex = 0; hostIndex < resourcePlan.hostsCreated(); hostIndex++) {
            hosts.add(createHost());
        }
        return hosts;
    }

    /**
     * Prints resource capacity before hosts are created.
     */
    public void validateResourceCapacity() {
        HostResources baseResources = resourcePlan.baseHostResources();
        HostResources scaledResources = resourcePlan.hostResources();

        LoggerUtil.printResourceValidationHeader();
        LoggerUtil.summary(Constants.VMS_REQUESTED_LABEL, resourcePlan.vmsRequested());
        LoggerUtil.summary(Constants.AVAILABLE_CAPACITY_LABEL, resourcePlan.capacityBeforeScaling());

        if (scaledResources.ram() > baseResources.ram()) {
            LoggerUtil.info(Constants.SCALING_HOST_RAM_MESSAGE);
        }
        if (scaledResources.pes() > baseResources.pes() || scaledResources.peMips() > baseResources.peMips()) {
            LoggerUtil.info(Constants.SCALING_HOST_CPU_MESSAGE);
        }
        if (scaledResources.bandwidth() > baseResources.bandwidth()) {
            LoggerUtil.info(Constants.SCALING_HOST_BANDWIDTH_MESSAGE);
        }
        if (scaledResources.storage() > baseResources.storage()) {
            LoggerUtil.info(Constants.SCALING_HOST_STORAGE_MESSAGE);
        }

        LoggerUtil.summary(Constants.NEW_CAPACITY_LABEL, resourcePlan.capacityAfterScaling());
        LoggerUtil.summary(Constants.PLANNED_VMS_PER_HOST_LABEL, resourcePlan.plannedVmsPerHost());
        LoggerUtil.summary(Constants.HOST_RAM_LABEL, scaledResources.ram());
        LoggerUtil.summary(Constants.HOST_BW_LABEL, scaledResources.bandwidth());
        LoggerUtil.summary(Constants.HOST_STORAGE_LABEL, scaledResources.storage());
        LoggerUtil.summary(Constants.HOST_PES_LABEL, scaledResources.pes());
        LoggerUtil.summary(Constants.HOST_MIPS_LABEL, scaledResources.peMips());
        LoggerUtil.info(resourcePlan.hasSufficientCapacity()
                ? Constants.CAPACITY_SUFFICIENT_MESSAGE
                : Constants.CAPACITY_INSUFFICIENT_MESSAGE);
        LoggerUtil.info(Constants.SEPARATOR);
    }

    /**
     * Returns the calculated resource plan.
     *
     * @return resource plan
     */
    public ResourcePlan getResourcePlan() {
        return resourcePlan;
    }

    private Host createHost() {
        HostResources hostResources = resourcePlan.hostResources();
        List<Pe> peList = new ArrayList<>(hostResources.pes());
        for (int peIndex = 0; peIndex < hostResources.pes(); peIndex++) {
            peList.add(new PeSimple(hostResources.peMips()));
        }

        return new HostSimple(
                hostResources.ram(),
                hostResources.bandwidth(),
                hostResources.storage(),
                peList)
                .setVmScheduler(new VmSchedulerTimeShared());
    }
}
