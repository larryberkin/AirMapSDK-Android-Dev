package com.airmap.airmapsdk.models.status.properties;

import com.airmap.airmapsdk.models.AirMapBaseModel;

import org.json.JSONObject;

import java.io.Serializable;

import static com.airmap.airmapsdk.util.Utils.optString;

@SuppressWarnings("unused")
public class AirMapParkProperties implements Serializable, AirMapBaseModel {
    private int size;
    private String type; //TODO: turn into an enum

    public AirMapParkProperties(JSONObject propertiesJson) {
        constructFromJson(propertiesJson);
    }

    public AirMapParkProperties() {

    }

    @Override
    public AirMapParkProperties constructFromJson(JSONObject json) {
        if (json != null) {
            setSize(json.optInt("size"));
            setType(optString(json, "type"));
        }
        return this;
    }

    public int getSize() {
        return size;
    }

    public AirMapParkProperties setSize(int size) {
        this.size = size;
        return this;
    }

    public String getType() {
        return type;
    }

    public AirMapParkProperties setType(String type) {
        this.type = type;
        return this;
    }
}
