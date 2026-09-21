package com.deepak.greencloud.ai.placement;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Stores placement decisions independently from the placement algorithm and simulation lifecycle. */
public final class PlacementHistory {
    private final List<PlacementDecision> decisions = new ArrayList<>();

    /**
     * Adds a completed placement decision to the history.
     *
     * @param decision placement decision to store
     */
    public synchronized void add(PlacementDecision decision) { decisions.add(decision); }

    /** @return immutable placement decision history in decision order */
    public synchronized List<PlacementDecision> getAll() { return List.copyOf(decisions); }

    /** @return latest placement decision, if one exists */
    public synchronized Optional<PlacementDecision> getLatest() {
        return decisions.isEmpty() ? Optional.empty() : Optional.of(decisions.get(decisions.size() - 1));
    }

    /** @return aggregate placement statistics */
    public synchronized PlacementStatistics getStatistics() {
        if (decisions.isEmpty()) return PlacementStatistics.empty();
        double average = decisions.stream().mapToDouble(PlacementDecision::getPlacementScore).average().orElse(0);
        double best = decisions.stream().mapToDouble(PlacementDecision::getPlacementScore).max().orElse(0);
        double worst = decisions.stream().mapToDouble(PlacementDecision::getPlacementScore).min().orElse(0);
        Map<Long, Long> counts = new LinkedHashMap<>();
        decisions.forEach(decision -> counts.merge(decision.getSelectedHostId(), 1L, Long::sum));
        long mostSelected = counts.entrySet().stream().max(Map.Entry.<Long, Long>comparingByValue()
                .thenComparing(Map.Entry.comparingByKey())).map(Map.Entry::getKey).orElse(-1L);
        long leastSelected = counts.entrySet().stream().min(Map.Entry.<Long, Long>comparingByValue()
                .thenComparing(Map.Entry.comparingByKey())).map(Map.Entry::getKey).orElse(-1L);
        return new PlacementStatistics(decisions.size(), average, best, worst, mostSelected, leastSelected);
    }

    /** Immutable aggregate values derived from a placement history. */
    public static final class PlacementStatistics {
        private final int totalDecisions;
        private final double averagePlacementScore;
        private final double bestPlacementScore;
        private final double worstPlacementScore;
        private final long mostSelectedHostId;
        private final long leastSelectedHostId;

        private PlacementStatistics(int totalDecisions, double averagePlacementScore, double bestPlacementScore,
                                    double worstPlacementScore, long mostSelectedHostId, long leastSelectedHostId) {
            this.totalDecisions = totalDecisions;
            this.averagePlacementScore = averagePlacementScore;
            this.bestPlacementScore = bestPlacementScore;
            this.worstPlacementScore = worstPlacementScore;
            this.mostSelectedHostId = mostSelectedHostId;
            this.leastSelectedHostId = leastSelectedHostId;
        }

        /** @return zero-value statistics for an empty history */
        public static PlacementStatistics empty() { return new PlacementStatistics(0, 0, 0, 0, -1, -1); }
        /** @return number of decisions */ public int getTotalDecisions() { return totalDecisions; }
        /** @return mean placement score */ public double getAveragePlacementScore() { return averagePlacementScore; }
        /** @return best placement score */ public double getBestPlacementScore() { return bestPlacementScore; }
        /** @return worst placement score */ public double getWorstPlacementScore() { return worstPlacementScore; }
        /** @return most frequently selected host, or -1 when empty */ public long getMostSelectedHostId() { return mostSelectedHostId; }
        /** @return least frequently selected host, or -1 when empty */ public long getLeastSelectedHostId() { return leastSelectedHostId; }
    }
}
