package com.airmap.airmapsdk.models.aircraft;

import com.airmap.airmapsdk.models.AirMapBaseModel;

import org.json.JSONObject;

import java.io.Serializable;

import static com.airmap.airmapsdk.util.Utils.optString;

@SuppressWarnings("unused")
public class AirMapAircraftModel implements Serializable, AirMapBaseModel {

    private String modelId;
    private String name;
    private AirMapAircraftManufacturer manufacturer;

    public AirMapAircraftModel(JSONObject modelJson) {
        constructFromJson(modelJson);
    }

    public AirMapAircraftModel() {

    }

    @Override
    public AirMapAircraftModel constructFromJson(JSONObject json) {
        if (json != null) {
            setModelId(optString(json, "id"));
            setName(optString(json, "name"));
            setManufacturer(new AirMapAircraftManufacturer(json.optJSONObject("manufacturer")));
        }
        return this;
    }

    public String getModelId() {
        return modelId;
    }

    public AirMapAircraftModel setModelId(String modelId) {
        this.modelId = modelId;
        return this;
    }

    public String getName() {
        return name;
    }

    public AirMapAircraftModel setName(String name) {
        this.name = name;
        return this;
    }

    public AirMapAircraftManufacturer getManufacturer() {
        return manufacturer;
    }

    public AirMapAircraftModel setManufacturer(AirMapAircraftManufacturer manufacturer) {
        this.manufacturer = manufacturer;
        return this;
    }

    /**
     * Comparison based on ID
     */
    @Override
    public boolean equals(Object o) {
        return o instanceof AirMapAircraftModel && ((AirMapAircraftModel) o).getModelId().equals(getModelId());
    }

    @Override
    public String toString() {
        return manufacturer.getName() + " " + getName();
    }
}
