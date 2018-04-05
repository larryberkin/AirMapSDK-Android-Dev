package com.airmap.airmapsdk.models.airspace;

import com.airmap.airmapsdk.models.AirMapBaseModel;
import com.airmap.airmapsdk.models.shapes.AirMapGeometry;
import com.airmap.airmapsdk.networking.services.MappingService;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.List;

import static com.airmap.airmapsdk.util.Utils.optString;

public class AirMapAirspace implements Serializable, AirMapBaseModel {

    private String airspaceId;
    private String name;
    private MappingService.AirMapAirspaceType airspaceType;
    private String country;
    private String state;
    private String city;
    private AirMapGeometry geometry;
    private AirMapGeometry propertyBoundary;

    public AirMapAirspace(JSONObject airspaceJson) {
        constructFromJson(airspaceJson);
    }

    public AirMapAirspace() {

    }

    @Override
    public AirMapAirspace constructFromJson(JSONObject json) {
        if (json != null) {
            setAirspaceId(optString(json, "id"));
            setName(optString(json, "name"));
            setCountry(optString(json, "country"));
            setState(optString(json, "state"));
            setCity(optString(json, "city"));
            setAirspaceType(MappingService.AirMapAirspaceType.fromString(optString(json, "type")));
            setGeometry(AirMapGeometry.getGeometryFromGeoJSON(json.optJSONObject("geometry")));
            try {
                JSONObject propertyBoundaryJson = json.optJSONObject("related_geometry").optJSONObject("property_boundary").optJSONObject("geometry");
                setPropertyBoundary(AirMapGeometry.getGeometryFromGeoJSON(propertyBoundaryJson));
            } catch (Exception e) { //There would be a lot of null checks in that optJSONObject chain, so just catching NPEs instead
                setPropertyBoundary(null);
                e.printStackTrace();
            }
        }
        return this;
    }

    public String getAirspaceId() {
        return airspaceId;
    }

    public void setAirspaceId(String airspaceId) {
        this.airspaceId = airspaceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public MappingService.AirMapAirspaceType getAirspaceType() {
        return airspaceType;
    }

    public void setAirspaceType(MappingService.AirMapAirspaceType airspaceType) {
        this.airspaceType = airspaceType;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public AirMapGeometry getGeometry() {
        return geometry;
    }

    public void setGeometry(AirMapGeometry geometry) {
        this.geometry = geometry;
    }

    public AirMapGeometry getPropertyBoundary() {
        return propertyBoundary;
    }

    public void setPropertyBoundary(AirMapGeometry propertyBoundary) {
        this.propertyBoundary = propertyBoundary;
    }
}
