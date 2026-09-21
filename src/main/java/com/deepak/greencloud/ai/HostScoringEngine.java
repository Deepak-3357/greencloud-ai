package com.deepak.greencloud.ai;

import java.util.ArrayList;
import java.util.List;
import org.cloudsimplus.hosts.Host;

/** Evaluates current host resources and energy efficiency without allocating or migrating VMs. */
public final class HostScoringEngine {
    private static final double CPU_WEIGHT = 0.40;
    private static final double RAM_WEIGHT = 0.25;
    private static final double ENERGY_WEIGHT = 0.15;
    private static final double BANDWIDTH_WEIGHT = 0.10;
    private static final double STORAGE_WEIGHT = 0.10;
    private final EnergyModel energyModel;

    /** Creates an engine using the default server power model. */
    public HostScoringEngine() { this(new EnergyModel()); }
    /** @param energyModel energy model used to derive the energy score */
    public HostScoringEngine(EnergyModel energyModel) { this.energyModel = energyModel; }

    /**
     * Evaluates all hosts from actual CloudSim utilization and returns best-to-worst rankings.
     *
     * @param hosts hosts to evaluate
     * @return sorted host scores
     */
    public List<HostScore> evaluate(List<Host> hosts) {
        double lowestPower = hosts.stream().mapToDouble(host -> energyModel.calculatePower(host.getCpuPercentUtilization())).min().orElse(0);
        List<HostScore> scores = new ArrayList<>(hosts.size());
        for (Host host : hosts) scores.add(evaluate(host, lowestPower));
        scores.sort(null);
        return List.copyOf(scores);
    }

    /**
     * Evaluates one host from actual CloudSim utilization.
     *
     * @param host host to evaluate
     * @return host score
     */
    public HostScore evaluate(Host host) { return evaluate(host, energyModel.calculatePower(host.getCpuPercentUtilization())); }

    private HostScore evaluate(Host host, double lowestPower) {
        double cpu = remaining(host.getCpuPercentUtilization());
        double ram = remaining(host.getRam().getPercentUtilization());
        double bandwidth = remaining(host.getBw().getPercentUtilization());
        double storage = remaining(host.getStorage().getPercentUtilization());
        double power = energyModel.calculatePower(host.getCpuPercentUtilization());
        double energy = (1 - power / energyModel.getMaxPowerWatts()) * 100;
        double finalScore = cpu * CPU_WEIGHT + ram * RAM_WEIGHT + energy * ENERGY_WEIGHT
                + bandwidth * BANDWIDTH_WEIGHT + storage * STORAGE_WEIGHT;
        List<String> reasons = new ArrayList<>();
        if (host.getCpuPercentUtilization() <= 0.80 && host.getRam().getPercentUtilization() <= 0.80) reasons.add("Balanced utilization");
        if (Math.abs(power - lowestPower) < 0.000001) reasons.add("Lowest energy");
        if (cpu >= 20 && ram >= 20 && bandwidth >= 20 && storage >= 20) reasons.add("Enough resources");
        if (reasons.isEmpty()) reasons.add("Limited available resources");
        return new HostScore(host.getId(), cpu, ram, bandwidth, storage, Math.max(0, energy), finalScore, String.join("; ", reasons));
    }
    private double remaining(double utilization) { return (1 - Math.max(0, Math.min(1, utilization))) * 100; }
}
