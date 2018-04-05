package com.airmap.airmapsdk.models.status.properties;

import com.airmap.airmapsdk.models.AirMapBaseModel;
import com.airmap.airmapsdk.util.Utils;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.Date;

import static com.airmap.airmapsdk.util.Utils.optString;

@SuppressWarnings("unused")
public class AirMapEmergencyProperties implements AirMapBaseModel, Serializable {

    private String agencyId;
    private Date effectiveDate;

    public AirMapEmergencyProperties(JSONObject emergencyJson) {
        constructFromJson(emergencyJson);
    }

    public AirMapEmergencyProperties() {

    }

    @Override
    public AirMapEmergencyProperties constructFromJson(JSONObject json) {
        if (json != null) {
            setAgencyId(optString(json, "agency_id"));
            setEffectiveDate(Utils.getDateFromIso8601String(optString(json, "date_effective")));
        }
        return this;
    }

    public String getAgencyId() {
        return agencyId;
    }

    public AirMapEmergencyProperties setAgencyId(String agencyId) {
        this.agencyId = agencyId;
        return this;
    }

    public Date getEffectiveDate() {
        return effectiveDate;
    }

    public AirMapEmergencyProperties setEffectiveDate(Date effectiveDate) {
        this.effectiveDate = effectiveDate;
        return this;
    }
}
