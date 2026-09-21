package com.deepak.greencloud.cloudlet;

import com.deepak.greencloud.config.SimulationConfig;
import java.util.ArrayList;
import java.util.List;
import org.cloudsimplus.cloudlets.Cloudlet;
import org.cloudsimplus.cloudlets.CloudletSimple;

/**
 * Creates cloudlets for the simulation workload.
 */
public class CloudletManager {

    /**
     * Creates the configured cloudlet list.
     *
     * @return list of configured cloudlets
     */
    public List<Cloudlet> createCloudlets() {
        List<Cloudlet> cloudlets = new ArrayList<>(SimulationConfig.CLOUDLET_COUNT);
        for (int cloudletIndex = 0; cloudletIndex < SimulationConfig.CLOUDLET_COUNT; cloudletIndex++) {
            cloudlets.add(createCloudlet());
        }
        return cloudlets;
    }

    private Cloudlet createCloudlet() {
        return new CloudletSimple(
                SimulationConfig.CLOUDLET_LENGTH,
                SimulationConfig.CLOUDLET_PES)
                .setFileSize(SimulationConfig.CLOUDLET_FILE_SIZE)
                .setOutputSize(SimulationConfig.CLOUDLET_OUTPUT_SIZE);
    }
}
