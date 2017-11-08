package com.airmap.airmapsdk.models.rules;

import com.airmap.airmapsdk.models.AirMapBaseModel;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by collin@airmap.com on 8/11/17.
 */

public class AirMapAuthorization implements AirMapBaseModel, Serializable{

    public enum Status {
        NOT_REQUESTED, REJECTED_UPON_SUBMISSION, AUTHORIZED_UPON_SUBMISSION, MANUAL_AUTHORIZATION, ACCEPTED, REJECTED, PENDING, CANCELLED;

        public static Status fromText(String text) {
            switch (text) {
                case "not_requested":
                    return NOT_REQUESTED;
                case "rejected_upon_submission":
                    return REJECTED_UPON_SUBMISSION;
                case "authorized_upon_submission":
                    return AUTHORIZED_UPON_SUBMISSION;
                case "manual_authorization":
                    return MANUAL_AUTHORIZATION;
                case "pending":
                    return PENDING;
                case "accepted":
                    return ACCEPTED;
                case "rejected":
                    return REJECTED;
                case "cancelled":
                    return CANCELLED;
            }

            return REJECTED;
        }
    }

    private Status status;
    private AirMapAuthority authority;
    private String description;
    private String message;

    public AirMapAuthorization(JSONObject jsonObject) {
        constructFromJson(jsonObject);
    }

    @Override
    public AirMapBaseModel constructFromJson(JSONObject json) {
        if (json.has("authority")) {
            setAuthority(new AirMapAuthority(json.optJSONObject("authority")));
        }
        setStatus(Status.fromText(json.optString("status")));
        setMessage(json.optString("message"));
        setDescription(json.optString("description"));
        return this;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public AirMapAuthority getAuthority() {
        return authority;
    }

    public void setAuthority(AirMapAuthority authority) {
        this.authority = authority;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
