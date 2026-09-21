package com.deepak.greencloud.ai;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.cloudsimplus.hosts.Host;

/** Collects actual CloudSim host utilization and converts it to reusable energy metrics. */
public final class EnergyCalculator {
    private final EnergyModel energyModel;
    private final List<EnergySnapshot> energyHistory = new ArrayList<>();
    private final Map<Long, EnergySnapshot> latestSnapshots = new HashMap<>();
    private final Map<Long, Double> lastSampleTimes = new HashMap<>();

    /** Creates a calculator using the default linear energy model. */
    public EnergyCalculator() { this(new EnergyModel()); }

    /**
     * Creates a calculator using the supplied energy model.
     *
     * @param energyModel model used for power and energy calculations
     */
    public EnergyCalculator(EnergyModel energyModel) { this.energyModel = energyModel; }

    /**
     * Captures energy readings for every supplied host using actual CloudSim utilization.
     *
     * @param hosts hosts to sample
     * @param simulationTime current CloudSim time in seconds
     * @return snapshots captured during this call
     */
    public synchronized List<EnergySnapshot> capture(List<Host> hosts, double simulationTime) {
        List<EnergySnapshot> captured = new ArrayList<>(hosts.size());
        for (Host host : hosts) {
            captured.add(capture(host, simulationTime));
        }
        return List.copyOf(captured);
    }

    /**
     * Captures one host's actual CloudSim utilization and energy state.
     *
     * @param host host to sample
     * @param simulationTime current CloudSim time in seconds
     * @return captured energy snapshot
     */
    public synchronized EnergySnapshot capture(Host host, double simulationTime) {
        double cpuUtilization = clamp(host.getCpuPercentUtilization());
        double ramUtilization = clamp(host.getRam().getPercentUtilization());
        double power = energyModel.calculatePower(cpuUtilization);
        EnergySnapshot previous = latestSnapshots.get(host.getId());
        double previousTime = lastSampleTimes.getOrDefault(host.getId(), simulationTime);
        double accumulated = previous == null ? 0 : previous.getAccumulatedEnergyWattHours();
        accumulated += energyModel.calculateEnergy(power, Math.max(0, simulationTime - previousTime));
        EnergySnapshot snapshot = new EnergySnapshot(simulationTime, host.getId(), cpuUtilization, ramUtilization,
                power, accumulated, host.isActive() ? "ACTIVE" : "INACTIVE", Instant.now());
        latestSnapshots.put(host.getId(), snapshot);
        lastSampleTimes.put(host.getId(), simulationTime);
        energyHistory.add(snapshot);
        return snapshot;
    }

    /** @return total latest host power in watts */
    public synchronized double getCurrentTotalPower() { return latestSnapshots.values().stream().mapToDouble(EnergySnapshot::getCurrentPowerWatts).sum(); }
    /** @return total accumulated host energy in watt-hours */
    public synchronized double getTotalEnergy() { return latestSnapshots.values().stream().mapToDouble(EnergySnapshot::getAccumulatedEnergyWattHours).sum(); }
    /** @return average accumulated energy per sampled host in watt-hours */
    public synchronized double getAverageEnergy() { return latestSnapshots.isEmpty() ? 0 : getTotalEnergy() / latestSnapshots.size(); }
    /** @return average current host power in watts */
    public synchronized double getAverageHostPower() { return latestSnapshots.isEmpty() ? 0 : getCurrentTotalPower() / latestSnapshots.size(); }
    /** @return highest accumulated host energy in watt-hours */
    public synchronized double getPeakEnergy() { return latestSnapshots.values().stream().mapToDouble(EnergySnapshot::getAccumulatedEnergyWattHours).max().orElse(0); }
    /** @return immutable energy history */
    public synchronized List<EnergySnapshot> getEnergyHistory() { return List.copyOf(energyHistory); }
    /**
     * Gets the latest reading for a host.
     *
     * @param hostId host identifier
     * @return latest snapshot when the host has been sampled
     */
    public synchronized Optional<EnergySnapshot> getLatestSnapshot(long hostId) { return Optional.ofNullable(latestSnapshots.get(hostId)); }
    /** @return host with the lowest accumulated energy, when readings exist */
    public synchronized Optional<EnergySnapshot> getLowestEnergyHost() { return latestSnapshots.values().stream().min(Comparator.comparingDouble(EnergySnapshot::getAccumulatedEnergyWattHours)); }
    /** @return host with the highest accumulated energy, when readings exist */
    public synchronized Optional<EnergySnapshot> getHighestEnergyHost() { return latestSnapshots.values().stream().max(Comparator.comparingDouble(EnergySnapshot::getAccumulatedEnergyWattHours)); }
    /** @return energy model used by this calculator */
    public EnergyModel getEnergyModel() { return energyModel; }
    private double clamp(double value) { return Math.max(0, Math.min(1, value)); }
}
