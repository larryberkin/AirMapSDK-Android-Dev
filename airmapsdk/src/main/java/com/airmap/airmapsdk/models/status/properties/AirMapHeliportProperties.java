package com.airmap.airmapsdk.models.status.properties;

import com.airmap.airmapsdk.models.AirMapBaseModel;

import org.json.JSONObject;

import java.io.Serializable;

import static com.airmap.airmapsdk.util.Utils.optString;

@SuppressWarnings("unused")
public class AirMapHeliportProperties implements Serializable, AirMapBaseModel {
    private boolean publicUse;
    private String phoneNumber;

    public AirMapHeliportProperties(JSONObject heliportJson) {
        constructFromJson(heliportJson);
    }

    public AirMapHeliportProperties() {
    }

    @Override
    public AirMapHeliportProperties constructFromJson(JSONObject json) {
        if (json != null) {
            String use = optString(json, "use");
            setPublicUse("public".equals(use));

            setPhoneNumber(optString(json, "phone"));
        }
        return this;
    }

    public boolean isPublicUse() {
        return publicUse;
    }

    public AirMapHeliportProperties setPublicUse(boolean publicUse) {
        this.publicUse = publicUse;
        return this;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }
}
