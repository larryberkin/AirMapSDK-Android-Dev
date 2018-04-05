package com.airmap.airmapsdk.models.aircraft;

import com.airmap.airmapsdk.models.AirMapBaseModel;

import org.json.JSONObject;

import java.io.Serializable;

import static com.airmap.airmapsdk.util.Utils.optString;

@SuppressWarnings("unused")
public class AirMapAircraftManufacturer implements Serializable, AirMapBaseModel {

    private String id;
    private String name;
    private String url;

    public AirMapAircraftManufacturer(JSONObject manufacturerJson) {
        constructFromJson(manufacturerJson);
    }

    public AirMapAircraftManufacturer() {
    }

    @Override
    public AirMapAircraftManufacturer constructFromJson(JSONObject json) {
        if (json != null) {
            setId(optString(json, "id"));
            setName(optString(json, "name"));
            setUrl(optString(json, "url"));
        }
        return this;
    }

    public String getId() {
        return id;
    }

    public AirMapAircraftManufacturer setId(String id) {
        this.id = id;
        return this;
    }

    public String getName() {
        return name;
    }

    public AirMapAircraftManufacturer setName(String name) {
        this.name = name;
        return this;
    }

    public String getUrl() {
        return url;
    }

    public AirMapAircraftManufacturer setUrl(String url) {
        this.url = url;
        return this;
    }

    /**
     * Comparison based on ID
     */
    @Override
    public boolean equals(Object o) {
        return o instanceof AirMapAircraftManufacturer && ((AirMapAircraftManufacturer) o).getId().equals(getId());
    }

    @Override
    public String toString() {
        return getName();
    }
}
