package com.airmap.airmapsdk.models.status.properties;

import com.airmap.airmapsdk.models.AirMapBaseModel;

import org.json.JSONObject;

import java.io.Serializable;

import static com.airmap.airmapsdk.util.Utils.optString;

@SuppressWarnings("unused")
public class AirMapOptionalProperties implements AirMapBaseModel, Serializable {

    private String url;
    private String description;

    public AirMapOptionalProperties(JSONObject propertiesJson) {
        constructFromJson(propertiesJson);
    }

    public AirMapOptionalProperties() {
    }

    @Override
    public AirMapOptionalProperties constructFromJson(JSONObject json) {
        if (json != null) {
            setUrl(optString(json, "url"));
            setDescription(optString(json, "description"));
        }
        return this;
    }

    public String getUrl() {
        return url;
    }

    public AirMapOptionalProperties setUrl(String url) {
        this.url = url;
        return this;
    }

    public String getDescription() {
        return description;
    }

    public AirMapOptionalProperties setDescription(String description) {
        this.description = description;
        return this;
    }
}
