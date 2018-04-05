package com.airmap.airmapsdk.models.comm;

import android.util.Base64;

import com.airmap.airmapsdk.models.AirMapBaseModel;

import org.json.JSONObject;

import java.io.Serializable;

import static com.airmap.airmapsdk.util.Utils.optString;

@SuppressWarnings("unused")
public class AirMapComm implements Serializable, AirMapBaseModel {
    private byte[] key;

    public AirMapComm(JSONObject commJson) {
        constructFromJson(commJson);
    }

    public AirMapComm() {
    }

    @Override
    public AirMapComm constructFromJson(JSONObject json) {
        if (json != null) {
            setKey(Base64.decode(optString(json, "key"), Base64.DEFAULT));
        }

        return this;
    }

    public byte[] getKey() {
        return key;
    }

    public AirMapComm setKey(byte[] key) {
        this.key = key;
        return this;
    }

}