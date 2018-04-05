package com.airmap.airmapsdk.models.status.properties;

import com.airmap.airmapsdk.models.AirMapBaseModel;

import org.json.JSONObject;

import java.io.Serializable;

import static com.airmap.airmapsdk.util.Utils.optString;

@SuppressWarnings("unused")
public class AirMapPowerPlantProperties implements Serializable, AirMapBaseModel {
    private String tech;
    private int plantCode;

    /**
     * Initialize an AirMapPowerPlantProperties from JSON
     *
     * @param propertiesJson The JSON representation
     */
    public AirMapPowerPlantProperties(JSONObject propertiesJson) {
        constructFromJson(propertiesJson);
    }

    public AirMapPowerPlantProperties() {

    }

    @Override
    public AirMapPowerPlantProperties constructFromJson(JSONObject json) {
        if (json != null) {
            setTech(optString(json, "tech"));
            setPlantCode(json.optInt("plant_code"));
        }
        return this;
    }

    public String getTech() {
        return tech;
    }

    public AirMapPowerPlantProperties setTech(String tech) {
        this.tech = tech;
        return this;
    }

    public int getPlantCode() {
        return plantCode;
    }

    public AirMapPowerPlantProperties setPlantCode(int plantCode) {
        this.plantCode = plantCode;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof AirMapPowerPlantProperties && getPlantCode() == ((AirMapPowerPlantProperties) o).getPlantCode();
    }
}
