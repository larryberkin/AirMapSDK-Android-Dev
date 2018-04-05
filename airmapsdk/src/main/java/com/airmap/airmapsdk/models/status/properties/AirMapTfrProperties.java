package com.airmap.airmapsdk.models.status.properties;

import com.airmap.airmapsdk.models.AirMapBaseModel;
import com.airmap.airmapsdk.util.Utils;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.Date;

import static com.airmap.airmapsdk.util.Utils.optString;

@SuppressWarnings("unused")
public class AirMapTfrProperties implements Serializable, AirMapBaseModel {
    private String url;
    private Date startTime;
    private Date endTime;

    /**
     * Initialize an AirMapTfrProperties from JSON
     *
     * @param propertiesJson The JSON representation
     */
    public AirMapTfrProperties(JSONObject propertiesJson) {
        constructFromJson(propertiesJson);
    }

    public AirMapTfrProperties() {

    }

    @Override
    public AirMapTfrProperties constructFromJson(JSONObject json) {
        if (json != null) {
            setUrl(optString(json, "url"));
            setStartTime(Utils.getDateFromIso8601String(optString(json, "effective_start", null)));
            setEndTime(Utils.getDateFromIso8601String(optString(json, "effective_end", null)));
        }
        return this;
    }

    public String getUrl() {
        return url;
    }

    public AirMapTfrProperties setUrl(String url) {
        this.url = url;
        return this;
    }

    public Date getStartTime() {
        return startTime;
    }

    public AirMapTfrProperties setStartTime(Date startTime) {
        this.startTime = startTime;
        return this;
    }

    public Date getEndTime() {
        return endTime;
    }

    public AirMapTfrProperties setEndTime(Date endTime) {
        this.endTime = endTime;
        return this;
    }
}
