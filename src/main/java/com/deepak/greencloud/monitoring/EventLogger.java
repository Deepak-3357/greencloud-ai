package com.deepak.greencloud.monitoring;

import com.deepak.greencloud.constants.Constants;
import com.deepak.greencloud.utils.LoggerUtil;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Captures and prints important simulation events as a readable timeline.
 */
public class EventLogger {

    private final List<EventRecord> events = new ArrayList<>();

    /**
     * Adds an event record to the timeline.
     *
     * @param simulationTime current simulation time
     * @param eventType event category
     * @param resourceId resource identifier
     * @param relatedResource related resource text
     * @param description event description
     */
    public void log(double simulationTime, String eventType, long resourceId, String relatedResource, String description) {
        events.add(new EventRecord(simulationTime, eventType, resourceId, relatedResource, description));
    }

    /**
     * Returns all captured events.
     *
     * @return immutable event list
     */
    public List<EventRecord> getEvents() {
        return Collections.unmodifiableList(events);
    }

    /**
     * Returns events recorded after the given index.
     *
     * @param startIndex first event index to include
     * @return immutable list of recent events
     */
    public List<EventRecord> getEventsSince(int startIndex) {
        if (startIndex >= events.size()) {
            return List.of();
        }
        return Collections.unmodifiableList(events.subList(Math.max(0, startIndex), events.size()));
    }

    /**
     * Returns the total number of events captured so far.
     *
     * @return event count
     */
    public int size() {
        return events.size();
    }

    /**
     * Prints the captured event timeline.
     */
    public void printTimeline() {
        LoggerUtil.info(Constants.SEPARATOR);
        LoggerUtil.info(Constants.SIMULATION_EVENT_LOG_TITLE);
        LoggerUtil.info(Constants.SEPARATOR);
        printEvents(events);
    }

    /**
     * Prints new timeline events as concise, grouped entries.
     *
     * @param startIndex first event index to include
     */
    public void printEventsSince(int startIndex) {
        printEvents(getEventsSince(startIndex));
    }

    private void printEvents(List<EventRecord> timelineEvents) {
        aggregate(timelineEvents).forEach(entry -> LoggerUtil.info(String.format(
                Constants.EVENT_TIMELINE_FORMAT,
                entry.simulationTime(),
                entry.description())));
    }

    private List<TimelineEntry> aggregate(List<EventRecord> timelineEvents) {
        Map<EventGroup, List<EventRecord>> groupedEvents = new LinkedHashMap<>();
        timelineEvents.forEach(event -> groupedEvents
                .computeIfAbsent(new EventGroup(event.getSimulationTime(), event.getEventType()), ignored -> new ArrayList<>())
                .add(event));
        List<TimelineEntry> entries = new ArrayList<>();
        groupedEvents.forEach((group, groupEvents) -> entries.add(new TimelineEntry(
                group.simulationTime(),
                describe(group.eventType(), groupEvents))));
        return entries;
    }

    private String describe(String eventType, List<EventRecord> groupEvents) {
        int count = groupEvents.size();
        if (count > 1 && Constants.EVENT_VM_CREATED.equals(eventType)) {
            return count + " VMs created";
        }
        if (count > 1 && Constants.EVENT_CLOUDLET_SUBMITTED.equals(eventType)) {
            return count + " Cloudlets submitted";
        }
        if (count > 1 && Constants.EVENT_HOST_CREATED.equals(eventType)) {
            return count + " Hosts created";
        }
        return groupEvents.get(0).getDescription();
    }

    private record EventGroup(double simulationTime, String eventType) { }

    private record TimelineEntry(double simulationTime, String description) { }
}
