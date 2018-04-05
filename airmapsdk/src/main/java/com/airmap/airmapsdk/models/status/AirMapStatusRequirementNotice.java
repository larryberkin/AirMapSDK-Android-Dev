package com.airmap.airmapsdk.models.status;

import com.airmap.airmapsdk.models.AirMapBaseModel;

import org.json.JSONObject;

import java.io.Serializable;

import static com.airmap.airmapsdk.util.Utils.optString;

@SuppressWarnings("unused")
public class AirMapStatusRequirementNotice implements Serializable, AirMapBaseModel {
    private boolean digital;
    private String phoneNumber;

    /**
     * Initialize an AirMapStatusRequirementNotice from JSON
     *
     * @param noticeJson A JSON representation of an AirMapStatusRequirementNotice
     */
    public AirMapStatusRequirementNotice(JSONObject noticeJson) {
        constructFromJson(noticeJson);
    }

    /**
     * Initialize an AirMapStatusRequirementNotice with default values
     */
    public AirMapStatusRequirementNotice() {

    }

    @Override
    public AirMapStatusRequirementNotice constructFromJson(JSONObject json) {
        if (json != null) {
            setDigital(json.optBoolean("digital"));
            setPhoneNumber(optString(json, "phone"));
        }
        return this;
    }

    public boolean isDigital() {
        return digital;
    }

    /**
     * @return If submitting notice by phone number is required
     */
    public boolean isNoticeRequired() {
        return !isDigital();
    }

    public AirMapStatusRequirementNotice setDigital(boolean digital) {
        this.digital = digital;
        return this;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public AirMapStatusRequirementNotice setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
        return this;
    }
}
