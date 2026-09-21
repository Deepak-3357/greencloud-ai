package com.deepak.greencloud.monitoring;

import com.deepak.greencloud.ai.EnergySnapshot;
import com.deepak.greencloud.ai.migration.MigrationDecision;
import java.util.List;

/** Immutable live view produced from one MonitoringManager snapshot. */
public final class LiveMonitoringData {
    private final ResourceSnapshot snapshot;
    private final double totalEnergy;
    private final List<EnergySnapshot> energy;
    private final List<MigrationDecision> migrations;
    private final int slaViolations;

    public LiveMonitoringData(ResourceSnapshot snapshot, double totalEnergy,
                              List<EnergySnapshot> energy,
                              List<MigrationDecision> migrations,
                              int slaViolations) {
        this.snapshot = snapshot;
        this.totalEnergy = totalEnergy;
        this.energy = List.copyOf(energy);
        this.migrations = List.copyOf(migrations);
        this.slaViolations = slaViolations;
    }

    public ResourceSnapshot getSnapshot() { return snapshot; }
    public double getTotalEnergy() { return totalEnergy; }
    public List<EnergySnapshot> getEnergy() { return energy; }
    public List<MigrationDecision> getMigrations() { return migrations; }
    public int getSlaViolations() { return slaViolations; }
}
