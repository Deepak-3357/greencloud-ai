package com.deepak.greencloud.datacenter;

import com.deepak.greencloud.ai.placement.AIPlacementManager;
import com.deepak.greencloud.host.HostManager;
import org.cloudsimplus.allocationpolicies.VmAllocationPolicySimple;
import org.cloudsimplus.core.CloudSimPlus;
import org.cloudsimplus.datacenters.Datacenter;
import org.cloudsimplus.datacenters.DatacenterSimple;

/**
 * Creates the simulation datacenter.
 */
public class DatacenterManager {

    private final CloudSimPlus simulation;
    private final HostManager hostManager;
    private final AIPlacementManager aiPlacementManager;

    public DatacenterManager(CloudSimPlus simulation, HostManager hostManager, AIPlacementManager aiPlacementManager) {
        this.simulation = simulation;
        this.hostManager = hostManager;
        this.aiPlacementManager = aiPlacementManager;
    }

    /**
     * Creates one datacenter using the configured host list.
     *
     * @return configured datacenter
     */
    public Datacenter createDatacenter() {
        return new DatacenterSimple(
                simulation,
                hostManager.createHosts(),
                new VmAllocationPolicySimple((policy, vm) -> aiPlacementManager.selectHost(vm, policy.getHostList())));
    }
}
