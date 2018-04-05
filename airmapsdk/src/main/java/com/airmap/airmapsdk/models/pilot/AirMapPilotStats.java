package com.airmap.airmapsdk.models.pilot;

import com.airmap.airmapsdk.models.AirMapBaseModel;

import org.json.JSONObject;

import java.io.Serializable;

@SuppressWarnings("unused")
public class AirMapPilotStats implements Serializable, AirMapBaseModel {

    private AirMapPilotStatsFlight flightStats;
    private AirMapPilotStatsAircraft aircraftStats;

    public AirMapPilotStats(JSONObject json) {
        constructFromJson(json);
    }

    public AirMapPilotStats() {

    }

    @Override
    public AirMapPilotStats constructFromJson(JSONObject json) {
        if (json != null) {
            setAircraftStats(new AirMapPilotStatsAircraft(json.optJSONObject("aircraft")));
            setFlightStats(new AirMapPilotStatsFlight(json.optJSONObject("flight")));
        }
        return this;
    }

    public AirMapPilotStatsFlight getFlightStats() {
        return flightStats;
    }

    public AirMapPilotStats setFlightStats(AirMapPilotStatsFlight flightStats) {
        this.flightStats = flightStats;
        return this;
    }

    public AirMapPilotStatsAircraft getAircraftStats() {
        return aircraftStats;
    }

    public AirMapPilotStats setAircraftStats(AirMapPilotStatsAircraft aircraftStats) {
        this.aircraftStats = aircraftStats;
        return this;
    }
}