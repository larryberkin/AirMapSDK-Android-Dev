package com.airmap.airmapsdk.models.pilot;

import android.text.TextUtils;

import com.airmap.airmapsdk.models.AirMapBaseModel;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import static com.airmap.airmapsdk.util.Utils.optString;

@SuppressWarnings("unused")
public class AirMapPilot implements Serializable, AirMapBaseModel {
    private String pilotId;
    private String email;
    private String firstName;
    private String lastName;
    private String username;
    private String pictureUrl;
    private String phone;
    private AirMapPilotVerificationStatus verificationStatus;
    private AirMapPilotMetaData userMetaData;
    private AirMapPilotMetaData appMetaData;
    private AirMapPilotStats stats;

    public AirMapPilot(JSONObject profileJson) {
        constructFromJson(profileJson);
    }

    public AirMapPilot() {
    }

    @Override
    public AirMapPilot constructFromJson(JSONObject json) {
        if (json != null) {
            setPilotId(optString(json, "id"));
            setEmail(optString(json, "email"));
            setFirstName(optString(json, "first_name"));
            setLastName(optString(json, "last_name"));
            setPhone(optString(json, "phone"));
            setPictureUrl(optString(json, "picture_url"));
            setUsername(optString(json, "username"));
            setVerificationStatus(new AirMapPilotVerificationStatus(json.optJSONObject("verification_status")));
            setUserMetaData(new AirMapPilotMetaData(json.optJSONObject("user_metadata")));
            setAppMetaData(new AirMapPilotMetaData(json.optJSONObject("app_metadata")));
            setStats(new AirMapPilotStats(json.optJSONObject("statistics")));
        }
        return this;
    }

    //Does not submit phone number
    public JSONObject getAsParams() {
        Map<String, Object> params = new HashMap<>();
        put(params, "first_name", getFirstName());
        put(params, "last_name", getLastName());
        put(params, "username", getUsername());
        if (getUserMetaData() != null) {
            put(params, "user_metadata", getUserMetaData().getAsParams());
        }
        if (getAppMetaData() != null) {
            put(params, "app_metadata", getAppMetaData().getAsParams());
        }
        return new JSONObject(params);
    }

    /**
     * Only adds to the map if object to be added is not null
     */
    private static void put(Map<String, Object> map, String key, Object value) {
        if (value != null) {
            map.put(key, value);
        }
    }

    public String getEmail() {
        return email;
    }

    public AirMapPilot setEmail(String email) {
        this.email = email;
        return this;
    }

    public String getFirstName() {
        return firstName;
    }

    public AirMapPilot setFirstName(String firstName) {
        this.firstName = firstName;
        return this;
    }

    public String getLastName() {
        return lastName;
    }

    public AirMapPilot setLastName(String lastName) {
        this.lastName = lastName;
        return this;
    }

    public String getFullName() {
        return getFirstName() + " " + getLastName();
    }

    public String getPhone() {
        return phone;
    }

    public AirMapPilot setPhone(String phone) {
        this.phone = phone;
        return this;
    }

    public String getPictureUrl() {
        return pictureUrl;
    }

    public AirMapPilot setPictureUrl(String pictureUrl) {
        this.pictureUrl = pictureUrl;
        return this;
    }

    public String getPilotId() {
        return pilotId;
    }

    public AirMapPilot setPilotId(String pilotId) {
        this.pilotId = pilotId;
        return this;
    }

    public String getUsername() {
        return username;
    }

    public AirMapPilot setUsername(String username) {
        this.username = username;
        return this;
    }

    public AirMapPilotStats getStats() {
        return stats;
    }

    public AirMapPilot setStats(AirMapPilotStats stats) {
        this.stats = stats;
        return this;
    }

    public AirMapPilotVerificationStatus getVerificationStatus() {
        return verificationStatus;
    }

    public AirMapPilot setVerificationStatus(AirMapPilotVerificationStatus verificationStatus) {
        this.verificationStatus = verificationStatus;
        return this;
    }

    public AirMapPilotMetaData getUserMetaData() {
        return userMetaData;
    }

    public AirMapPilot setUserMetaData(AirMapPilotMetaData userMetaData) {
        this.userMetaData = userMetaData;
        return this;
    }

    public AirMapPilotMetaData getAppMetaData() {
        return appMetaData;
    }

    public AirMapPilot setAppMetaData(AirMapPilotMetaData appMetaData) {
        this.appMetaData = appMetaData;
        return this;
    }

    public boolean isPhoneVerified() {
        return !TextUtils.isEmpty(phone) && verificationStatus != null && verificationStatus.isPhone();
    }

    /**
     * Comparison based on ID
     */
    @Override
    public boolean equals(Object o) {
        return o instanceof AirMapPilot && getPilotId().equals(((AirMapPilot) o).getPilotId());
    }

    @Override
    public String toString() {
        return getFullName();
    }
}
