package com.deepak.greencloud.monitoring;

/**
 * Represents a simulation event captured by the monitoring subsystem.
 */
public class EventRecord {

    private final double simulationTime;
    private final String eventType;
    private final long resourceId;
    private final String relatedResource;
    private final String description;

    public EventRecord(double simulationTime, String eventType, long resourceId, String relatedResource, String description) {
        this.simulationTime = simulationTime;
        this.eventType = eventType;
        this.resourceId = resourceId;
        this.relatedResource = relatedResource;
        this.description = description;
    }

    public double getSimulationTime() {
        return simulationTime;
    }

    public String getEventType() {
        return eventType;
    }

    public long getResourceId() {
        return resourceId;
    }

    public String getRelatedResource() {
        return relatedResource;
    }

    public String getDescription() {
        return description;
    }
}
