package com.airmap.airmapsdk.models.pilot;

import com.airmap.airmapsdk.models.AirMapBaseModel;

import org.json.JSONObject;

import java.io.Serializable;

@SuppressWarnings("unused")
public class AirMapPilotStatsAircraft implements Serializable, AirMapBaseModel {

    private int total;

    public AirMapPilotStatsAircraft(JSONObject statsJson) {
        constructFromJson(statsJson);
    }

    public AirMapPilotStatsAircraft() {

    }

    @Override
    public AirMapPilotStatsAircraft constructFromJson(JSONObject json) {
        if (json != null) {
            setTotal(json.optInt("total"));
        }
        return this;
    }

    public int getTotal() {
        return total;
    }

    public AirMapPilotStatsAircraft setTotal(int total) {
        this.total = total;
        return this;
    }
}
