package com.airmap.airmapsdk.models.status.properties;

import com.airmap.airmapsdk.models.AirMapBaseModel;

import org.json.JSONObject;

import java.io.Serializable;

import static com.airmap.airmapsdk.util.Utils.optString;

@SuppressWarnings("unused")
public class AirMapAirportRunway implements AirMapBaseModel, Serializable {

    private String name;
    private int length;
    private int bearing;

    public AirMapAirportRunway(JSONObject runwayJson) {
        constructFromJson(runwayJson);
    }

    public AirMapAirportRunway() {

    }

    @Override
    public AirMapAirportRunway constructFromJson(JSONObject json) {
        if (json != null) {
            setName(optString(json, "name"));
            setLength(json.optInt("length"));
            setBearing(json.optInt("bearing"));
        }
        return this;
    }

    public String getName() {
        return name;
    }

    public AirMapAirportRunway setName(String name) {
        this.name = name;
        return this;
    }

    public int getLength() {
        return length;
    }

    public AirMapAirportRunway setLength(int length) {
        this.length = length;
        return this;
    }

    public int getBearing() {
        return bearing;
    }

    public AirMapAirportRunway setBearing(int bearing) {
        this.bearing = bearing;
        return this;
    }

    @Override
    public String toString() {
        return getName();
    }
}
