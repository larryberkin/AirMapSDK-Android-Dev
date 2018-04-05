package com.airmap.airmapsdk.models.status.properties;

import com.airmap.airmapsdk.models.AirMapBaseModel;

import org.json.JSONObject;

import java.io.Serializable;

import static com.airmap.airmapsdk.util.Utils.optString;

@SuppressWarnings("unused")
public class AirMapControlledAirspaceProperties implements Serializable, AirMapBaseModel {

    private String classAirspace;
    private String airportIdentifier;
    private boolean laanc;
    private boolean authorization;

    /**
     * Initialize an AirMapControlledAirspaceProperties from JSON
     *
     * @param propertiesJson The JSON representation
     */
    public AirMapControlledAirspaceProperties(JSONObject propertiesJson) {
        constructFromJson(propertiesJson);
    }

    @Override
    public AirMapControlledAirspaceProperties constructFromJson(JSONObject json) {
        if (json != null) {
            setClassAirspace(optString(json, "classAirspace"));
            setAirportIdentifier(optString(json, "airportIdentifier"));
            setLaanc(json.optBoolean("laanc"));
            setAuthorization(json.optBoolean("authorization"));
        }
        return this;
    }

    public String getClassAirspace() {
        return classAirspace;
    }

    public AirMapControlledAirspaceProperties setClassAirspace(String classAirspace) {
        this.classAirspace = classAirspace;
        return this;
    }

    public String getAirportIdentifier() {
        return airportIdentifier;
    }

    public AirMapControlledAirspaceProperties setAirportIdentifier(String airportIdentifier) {
        this.airportIdentifier = airportIdentifier;
        return this;
    }

    public boolean isLaanc() {
        return laanc;
    }

    public void setLaanc(boolean laanc) {
        this.laanc = laanc;
    }

    public boolean isAuthorization() {
        return authorization;
    }

    public void setAuthorization(boolean authorization) {
        this.authorization = authorization;
    }
}
