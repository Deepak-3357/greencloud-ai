package com.deepak.greencloud.ai.migration;

import com.deepak.greencloud.ai.EnergyCalculator;
import com.deepak.greencloud.ai.HostScore;
import com.deepak.greencloud.ai.HostScoringEngine;
import com.deepak.greencloud.config.SimulationConfig;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.cloudsimplus.datacenters.Datacenter;
import org.cloudsimplus.hosts.Host;
import org.cloudsimplus.vms.Vm;

/** Evaluates host utilization and produces safe VM-migration decisions at monitoring intervals. */
public final class MigrationManager {
    private final HostScoringEngine hostScoringEngine;
    private final EnergyCalculator energyCalculator;
    private final MigrationPolicy migrationPolicy;
    private final MigrationHistory migrationHistory;
    private final Set<String> activeRecommendationKeys = new HashSet<>();
    private long mostLoadedHostId = -1;
    private long leastLoadedHostId = -1;
    private double averageHostUtilization;

    /** Creates a manager with reusable scoring, energy, policy, and history services. */
    public MigrationManager(HostScoringEngine hostScoringEngine, EnergyCalculator energyCalculator) {
        this(hostScoringEngine, energyCalculator, new MigrationPolicy(), new MigrationHistory());
    }

    /** Creates a manager with explicit collaborators for extension and testing. */
    public MigrationManager(HostScoringEngine hostScoringEngine, EnergyCalculator energyCalculator,
                            MigrationPolicy migrationPolicy, MigrationHistory migrationHistory) {
        this.hostScoringEngine = hostScoringEngine;
        this.energyCalculator = energyCalculator;
        this.migrationPolicy = migrationPolicy;
        this.migrationHistory = migrationHistory;
    }

    /**
     * Evaluates all hosts, detects overload and underload, and records migration actions or recommendations.
     *
     * @param hosts all hosts in the datacenter
     * @param datacenter datacenter used to request live migration when supported
     * @param simulationTime current CloudSim time in seconds
     * @return decisions created during this evaluation
     */
    public List<MigrationDecision> evaluate(List<Host> hosts, Datacenter datacenter, double simulationTime) {
        updateHostSummary(hosts);
        Set<Long> selectedVmIds = new HashSet<>();
        List<MigrationDecision> decisions = new ArrayList<>();
        for (Host host : hosts) {
            if (migrationPolicy.isOverloaded(host)) {
                selectVm(host, simulationTime).flatMap(vm -> createDecision(vm, host, hosts, datacenter,
                        simulationTime, "CPU/RAM Overload", selectedVmIds)).ifPresent(decisions::add);
            }
        }
        for (Host host : hosts) {
            if (migrationPolicy.isUnderloaded(host)) {
                List<Vm> vms = new ArrayList<>(host.getVmList());
                for (Vm vm : vms) {
                    createDecision(vm, host, hosts, datacenter, simulationTime, "Underloaded Host Consolidation", selectedVmIds)
                            .ifPresent(decisions::add);
                }
            }
        }
        return List.copyOf(decisions);
    }

    /** @return migration decision history */
    public MigrationHistory getMigrationHistory() { return migrationHistory; }
    /** @return most loaded host from the latest evaluation, or -1 when none exists */
    public long getMostLoadedHostId() { return mostLoadedHostId; }
    /** @return least loaded host from the latest evaluation, or -1 when none exists */
    public long getLeastLoadedHostId() { return leastLoadedHostId; }
    /** @return average host utilization from the latest evaluation */
    public double getAverageHostUtilization() { return averageHostUtilization; }

    private Optional<Vm> selectVm(Host host, double simulationTime) {
        return host.<Vm>getVmList().stream().max(Comparator
                .comparingDouble((Vm vm) -> finite(vm.getCpuPercentUtilization(simulationTime)))
                .thenComparingLong(Vm::getId));
    }

    private Optional<MigrationDecision> createDecision(Vm vm, Host source, List<Host> hosts, Datacenter datacenter,
                                                        double simulationTime, String reason, Set<Long> selectedVmIds) {
        if (!selectedVmIds.add(vm.getId())) return Optional.empty();
        Optional<HostScore> destinationScore = hostScoringEngine.evaluate(hosts).stream()
                .filter(score -> score.getHostId() != source.getId())
                .filter(score -> datacenter.getHostById(score.getHostId()).isSuitableForVm(vm))
                .findFirst();
        if (destinationScore.isEmpty()) {
            selectedVmIds.remove(vm.getId());
            return Optional.empty();
        }
        HostScore score = destinationScore.get();
        Host destination = datacenter.getHostById(score.getHostId());
        MigrationDecision.MigrationStatus status = requestOrRecommend(vm, destination, datacenter);
        String recommendationKey = vm.getId() + ":" + source.getId() + ":" + destination.getId() + ":" + reason;
        if (status == MigrationDecision.MigrationStatus.RECOMMENDED && !activeRecommendationKeys.add(recommendationKey)) {
            return Optional.empty();
        }
        if (status != MigrationDecision.MigrationStatus.RECOMMENDED) {
            activeRecommendationKeys.remove(recommendationKey);
        }
        double cpuBefore = finite(source.getCpuPercentUtilization());
        double cpuAfter = Math.max(0, cpuBefore - vm.getTotalMipsCapacity() / Math.max(1, source.getTotalMipsCapacity()));
        double energySaved = energyCalculator.getEnergyModel().calculateEnergy(
                Math.max(0, energyCalculator.getEnergyModel().calculatePower(cpuBefore)
                        - energyCalculator.getEnergyModel().calculatePower(cpuAfter)),
                SimulationConfig.MONITORING_INTERVAL);
        MigrationDecision decision = new MigrationDecision(simulationTime, vm.getId(), source.getId(), destination.getId(),
                reason, score.getFinalScore(), cpuBefore, cpuAfter, energySaved, status);
        migrationHistory.add(decision);
        return Optional.of(decision);
    }

    private MigrationDecision.MigrationStatus requestOrRecommend(Vm vm, Host destination, Datacenter datacenter) {
        if (!datacenter.isMigrationsEnabled() || !datacenter.getVmAllocationPolicy().isVmMigrationSupported()) {
            return MigrationDecision.MigrationStatus.RECOMMENDED;
        }
        try {
            datacenter.requestVmMigration(vm, destination);
            return MigrationDecision.MigrationStatus.SUCCESS;
        } catch (RuntimeException exception) {
            return MigrationDecision.MigrationStatus.FAILED;
        }
    }

    private void updateHostSummary(List<Host> hosts) {
        averageHostUtilization = hosts.stream().mapToDouble(migrationPolicy::getAverageUtilization).average().orElse(0);
        mostLoadedHostId = hosts.stream().max(Comparator.comparingDouble(migrationPolicy::getAverageUtilization))
                .map(Host::getId).orElse(-1L);
        leastLoadedHostId = hosts.stream().min(Comparator.comparingDouble(migrationPolicy::getAverageUtilization))
                .map(Host::getId).orElse(-1L);
    }

    private double finite(double value) { return Double.isFinite(value) ? Math.max(0, Math.min(1, value)) : 0; }
}
