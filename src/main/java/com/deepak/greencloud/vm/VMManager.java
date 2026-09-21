package com.deepak.greencloud.vm;

import com.deepak.greencloud.config.SimulationConfig;
import java.util.ArrayList;
import java.util.List;
import org.cloudsimplus.schedulers.cloudlet.CloudletSchedulerTimeShared;
import org.cloudsimplus.vms.Vm;
import org.cloudsimplus.vms.VmSimple;

/**
 * Creates virtual machines for the simulation.
 */
public class VMManager {

    /**
     * Creates the configured virtual machine list.
     *
     * @return list of configured virtual machines
     */
    public List<Vm> createVirtualMachines() {
        List<Vm> vms = new ArrayList<>(SimulationConfig.VM_COUNT);
        for (int vmIndex = 0; vmIndex < SimulationConfig.VM_COUNT; vmIndex++) {
            vms.add(createVirtualMachine());
        }
        return vms;
    }

    private Vm createVirtualMachine() {
        return new VmSimple(SimulationConfig.VM_MIPS, SimulationConfig.VM_PES)
                .setRam(SimulationConfig.VM_RAM)
                .setBw(SimulationConfig.VM_BW)
                .setSize(SimulationConfig.VM_SIZE)
                .setCloudletScheduler(new CloudletSchedulerTimeShared());
    }
}
