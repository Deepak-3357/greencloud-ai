package com.deepak.greencloud.ai.placement;

import com.deepak.greencloud.ai.HostScore;
import com.deepak.greencloud.ai.HostScoringEngine;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.cloudsimplus.hosts.Host;
import org.cloudsimplus.vms.Vm;

/** Selects the best suitable host for a VM by reusing the AI host-scoring engine. */
public final class AIPlacementManager {
    private final HostScoringEngine hostScoringEngine;
    private final PlacementHistory placementHistory;

    /** Creates a manager with independent scoring and decision-history services. */
    public AIPlacementManager() { this(new HostScoringEngine(), new PlacementHistory()); }

    /**
     * Creates a manager with supplied reusable collaborators.
     *
     * @param hostScoringEngine engine that evaluates hosts
     * @param placementHistory decision history store
     */
    public AIPlacementManager(HostScoringEngine hostScoringEngine, PlacementHistory placementHistory) {
        this.hostScoringEngine = hostScoringEngine;
        this.placementHistory = placementHistory;
    }

    /**
     * Selects and records the highest-scoring host that is suitable for the VM.
     *
     * @param vm VM requiring placement
     * @param hosts all datacenter hosts to evaluate
     * @return selected suitable host, or empty when no host can accommodate the VM
     */
    public Optional<Host> selectHost(Vm vm, List<Host> hosts) {
        return makePlacementDecision(vm, hosts).flatMap(decision -> hosts.stream()
                .filter(host -> host.getId() == decision.getSelectedHostId()).findFirst());
    }

    /**
     * Scores all hosts, records the best suitable candidate, and returns its decision.
     *
     * @param vm VM requiring placement
     * @param hosts all datacenter hosts to evaluate
     * @return recorded placement decision, or empty when no host is suitable
     */
    public Optional<PlacementDecision> makePlacementDecision(Vm vm, List<Host> hosts) {
        List<HostScore> scores = hostScoringEngine.evaluate(hosts);
        Optional<HostScore> selectedScore = scores.stream().filter(score -> hosts.stream()
                .anyMatch(host -> host.getId() == score.getHostId() && host.isSuitableForVm(vm))).findFirst();
        if (selectedScore.isEmpty()) return Optional.empty();
        HostScore score = selectedScore.get();
        PlacementDecision decision = new PlacementDecision(vm.getId(), score.getHostId(), score.getFinalScore(),
                vm.getSimulation().clock(), buildReason(score), score.getCpuScore(), score.getRamScore(),
                score.getBandwidthScore(), score.getStorageScore(), score.getEnergyScore());
        placementHistory.add(decision);
        return Optional.of(decision);
    }

    /** @return independent decision history */
    public PlacementHistory getPlacementHistory() { return placementHistory; }

    /** @return host scoring engine used by this manager */
    public HostScoringEngine getHostScoringEngine() { return hostScoringEngine; }

    private String buildReason(HostScore score) {
        List<String> reasons = new ArrayList<>();
        if (score.getEnergyScore() >= 50) reasons.add("Lowest Energy");
        if (score.getCpuScore() >= 20) reasons.add("Balanced CPU");
        if (score.getRamScore() >= 20) reasons.add("Enough RAM");
        if (score.getBandwidthScore() >= 50) reasons.add("Low Network Usage");
        if (score.getCpuScore() >= 20 && score.getRamScore() >= 20) reasons.add("No Overload Risk");
        return String.join("; ", reasons.isEmpty() ? List.of(score.getReason()) : reasons);
    }
}
