package com.airmap.airmapsdk.models;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class AirMapWeather implements AirMapBaseModel, Serializable {

    private List<AirMapWeatherUpdate> updates;

    public AirMapWeather(JSONObject jsonObject) {
        constructFromJson(jsonObject);
    }

    public AirMapWeather() {}

    @Override
    public AirMapBaseModel constructFromJson(JSONObject json) {
        if (json != null) {
            updates = new ArrayList<>();

            JSONArray updatesArray = json.optJSONArray("weather");
            for (int i = 0; updatesArray != null && i < updatesArray.length(); i++) {
                updates.add(new AirMapWeatherUpdate(updatesArray.optJSONObject(i)));
            }
        }

        return this;
    }

    public List<AirMapWeatherUpdate> getUpdates() {
        return updates;
    }

    public void setUpdates(List<AirMapWeatherUpdate> updates) {
        this.updates = updates;
    }
}
