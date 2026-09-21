package com.deepak.greencloud.monitoring;

/** Receives throttled, authoritative monitoring updates from a running simulation. */
@FunctionalInterface
public interface MonitoringListener {
    void onMonitoringUpdate(LiveMonitoringData data);
}
