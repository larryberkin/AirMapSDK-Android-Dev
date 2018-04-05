package com.airmap.airmapsdk.networking.callbacks;

import com.airmap.airmapsdk.models.traffic.AirMapTraffic;

import java.util.List;

public interface AirMapTrafficListener {
    void onAddTraffic(List<AirMapTraffic> added);
    void onUpdateTraffic(List<AirMapTraffic> updated);
    void onRemoveTraffic(List<AirMapTraffic> removed);
}