package com.airmap.airmapsdk.models.status.properties;

import com.airmap.airmapsdk.models.AirMapBaseModel;
import com.airmap.airmapsdk.util.Utils;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.Date;

import static com.airmap.airmapsdk.util.Utils.optString;

@SuppressWarnings("unused")
public class AirMapWildfireProperties implements AirMapBaseModel, Serializable {

    private int size; //In acres
    private Date effectiveDate;

    public AirMapWildfireProperties(JSONObject wildfireJson) {
        constructFromJson(wildfireJson);
    }

    public AirMapWildfireProperties() {

    }

    @Override
    public AirMapWildfireProperties constructFromJson(JSONObject json) {
        if (json != null) {
            setSize(json.optInt("size", -1));
            setEffectiveDate(Utils.getDateFromIso8601String(optString(json, "date_effective")));
        }
        return this;
    }

    public int getSize() {
        return size;
    }

    public AirMapWildfireProperties setSize(int size) {
        this.size = size;
        return this;
    }

    public Date getEffectiveDate() {
        return effectiveDate;
    }

    public AirMapWildfireProperties setEffectiveDate(Date effectiveDate) {
        this.effectiveDate = effectiveDate;
        return this;
    }
}
