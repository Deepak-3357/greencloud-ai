package com.deepak.greencloud.broker;

import org.cloudsimplus.brokers.DatacenterBroker;
import org.cloudsimplus.brokers.DatacenterBrokerSimple;
import org.cloudsimplus.core.CloudSimPlus;

/**
 * Creates the simulation broker.
 */
public class BrokerManager {

    private final CloudSimPlus simulation;

    public BrokerManager(CloudSimPlus simulation) {
        this.simulation = simulation;
    }

    /**
     * Creates one datacenter broker.
     *
     * @return configured datacenter broker
     */
    public DatacenterBroker createBroker() {
        return new DatacenterBrokerSimple(simulation);
    }
}
