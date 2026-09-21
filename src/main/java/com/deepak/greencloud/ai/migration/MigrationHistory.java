package com.deepak.greencloud.ai.migration;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/** Independent, thread-safe store for AI migration decisions. */
public final class MigrationHistory {
    private final List<MigrationDecision> decisions = new ArrayList<>();

    /** @param decision decision to add to the history */
    public synchronized void add(MigrationDecision decision) { decisions.add(decision); }
    /** @return immutable migration history in decision order */
    public synchronized List<MigrationDecision> getAll() { return List.copyOf(decisions); }
    /** @return latest migration decision, when one exists */
    public synchronized Optional<MigrationDecision> getLatest() {
        return decisions.isEmpty() ? Optional.empty() : Optional.of(decisions.get(decisions.size() - 1));
    }
    /** @return aggregate migration statistics */
    public synchronized MigrationStatistics getStatistics() {
        long successful = decisions.stream().filter(decision -> decision.getStatus() == MigrationDecision.MigrationStatus.SUCCESS).count();
        long recommended = decisions.stream().filter(decision -> decision.getStatus() == MigrationDecision.MigrationStatus.RECOMMENDED).count();
        long failed = decisions.stream().filter(decision -> decision.getStatus() == MigrationDecision.MigrationStatus.FAILED).count();
        double energySaved = decisions.stream().mapToDouble(MigrationDecision::getEnergySavedEstimateWattHours).sum();
        return new MigrationStatistics(decisions.size(), successful, recommended, failed, energySaved);
    }

    /** Immutable aggregate values derived from a migration history. */
    public static final class MigrationStatistics {
        private final int totalDecisions;
        private final long successfulMigrations;
        private final long recommendedMigrations;
        private final long failedMigrations;
        private final double estimatedEnergySavedWattHours;

        private MigrationStatistics(int totalDecisions, long successfulMigrations, long recommendedMigrations,
                                    long failedMigrations, double estimatedEnergySavedWattHours) {
            this.totalDecisions = totalDecisions;
            this.successfulMigrations = successfulMigrations;
            this.recommendedMigrations = recommendedMigrations;
            this.failedMigrations = failedMigrations;
            this.estimatedEnergySavedWattHours = estimatedEnergySavedWattHours;
        }
        /** @return total recorded decisions */ public int getTotalDecisions() { return totalDecisions; }
        /** @return number of migration requests accepted by CloudSim */ public long getSuccessfulMigrations() { return successfulMigrations; }
        /** @return number of recommendations produced when live migration is unavailable */ public long getRecommendedMigrations() { return recommendedMigrations; }
        /** @return number of failed migration attempts */ public long getFailedMigrations() { return failedMigrations; }
        /** @return total estimated energy saved in watt-hours */ public double getEstimatedEnergySavedWattHours() { return estimatedEnergySavedWattHours; }
    }
}
