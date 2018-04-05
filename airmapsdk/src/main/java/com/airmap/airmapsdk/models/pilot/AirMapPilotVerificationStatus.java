package com.airmap.airmapsdk.models.pilot;

import com.airmap.airmapsdk.models.AirMapBaseModel;

import org.json.JSONObject;

import java.io.Serializable;

@SuppressWarnings("unused")
public class AirMapPilotVerificationStatus implements Serializable, AirMapBaseModel{

    private boolean phone;
    private boolean email;

    public AirMapPilotVerificationStatus(JSONObject verificationJson) {
        constructFromJson(verificationJson);
    }

    public AirMapPilotVerificationStatus() {
    }

    @Override
    public AirMapPilotVerificationStatus constructFromJson(JSONObject json) {
        if (json != null) {
            setPhone(json.optBoolean("phone"));
            setEmail(json.optBoolean("email"));
        }
        return this;
    }

    public boolean isPhone() {
        return phone;
    }

    public AirMapPilotVerificationStatus setPhone(boolean phone) {
        this.phone = phone;
        return this;
    }

    public boolean isEmail() {
        return email;
    }

    public AirMapPilotVerificationStatus setEmail(boolean email) {
        this.email = email;
        return this;
    }
}
