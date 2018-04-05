package com.airmap.airmapsdk.models;

import org.json.JSONObject;

public interface AirMapBaseModel {
    AirMapBaseModel constructFromJson(JSONObject json);
}
