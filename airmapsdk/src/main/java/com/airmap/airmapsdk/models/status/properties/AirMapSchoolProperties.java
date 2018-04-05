package com.airmap.airmapsdk.models.status.properties;

import com.airmap.airmapsdk.models.AirMapBaseModel;

import org.json.JSONObject;

import java.io.Serializable;

import static com.airmap.airmapsdk.util.Utils.optString;

@SuppressWarnings("unused")
public class AirMapSchoolProperties implements Serializable, AirMapBaseModel {
    private boolean building;
    private double wayArea;
    private String type; //TODO: turn this into an enum

    /**
     * Initialize an AirMapSchoolProperties from JSON
     *
     * @param propertiesJson The JSON representation
     */
    public AirMapSchoolProperties(JSONObject propertiesJson) {
        constructFromJson(propertiesJson);
    }

    public AirMapSchoolProperties() {

    }

    @Override
    public AirMapSchoolProperties constructFromJson(JSONObject json) {
        if (json != null) {
            setBuilding(json.optBoolean("building"));
            setWayArea(json.optDouble("way_area"));
            setType(optString(json, "type"));
        }
        return this;
    }

    public boolean isBuilding() {
        return building;
    }

    public AirMapSchoolProperties setBuilding(boolean building) {
        this.building = building;
        return this;
    }

    public double getWayArea() {
        return wayArea;
    }

    public AirMapSchoolProperties setWayArea(double wayArea) {
        this.wayArea = wayArea;
        return this;
    }

    public String getType() {
        return type;
    }

    public AirMapSchoolProperties setType(String type) {
        this.type = type;
        return this;
    }
}
