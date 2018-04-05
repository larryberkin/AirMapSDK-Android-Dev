package com.airmap.airmapsdk.models.rules;

import com.airmap.airmapsdk.models.AirMapBaseModel;

import org.json.JSONObject;

import java.io.Serializable;

import static com.airmap.airmapsdk.util.Utils.optString;

public class AirMapValidation implements AirMapBaseModel, Serializable {

    public enum Status {
        VALID, INVALID, UNKNOWN;

        public static Status fromText(String text) {
            switch (text) {
                case "valid":
                    return VALID;
                case "invalid":
                    return INVALID;
                case "unknown":
                    return UNKNOWN;
            }

            return null;
        }
    }

    public class Feature {

        private String code;
        private String description;

        public Feature(JSONObject json) {
            setCode(optString(json, "code"));
            setDescription(optString(json, "description"));
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }
    }

    private String data;
    private Status status;
    private String message;
    private Feature feature;
    private AirMapAuthority authority;

    public AirMapValidation(JSONObject jsonObject) {
        constructFromJson(jsonObject);
    }

    @Override
    public AirMapBaseModel constructFromJson(JSONObject json) {
        setData(optString(json, "data"));
        setStatus(Status.fromText(optString(json, "status")));
        setMessage(optString(json, "message"));

        if (json.has("feature")) {
            setFeature(new Feature(json.optJSONObject("feature")));
        }

        if (json.has("authority")) {
            setAuthority(new AirMapAuthority(json.optJSONObject("authority")));
        }

        return this;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    public Feature getFeature() {
        return feature;
    }

    public void setFeature(Feature feature) {
        this.feature = feature;
    }

    public AirMapAuthority getAuthority() {
        return authority;
    }

    public void setAuthority(AirMapAuthority authority) {
        this.authority = authority;
    }
}
