package com.airmap.airmapsdk.models.pilot;

import com.airmap.airmapsdk.models.AirMapBaseModel;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@SuppressWarnings("unused")
public class AirMapPilotMetaData implements Serializable, AirMapBaseModel {

    private Map<String, Object> metaData;

    public AirMapPilotMetaData(JSONObject metaDataJson) {
        constructFromJson(metaDataJson);
    }

    public AirMapPilotMetaData() {

    }

    @Override
    public AirMapPilotMetaData constructFromJson(JSONObject json) {
        Map<String, Object> map = new HashMap<>();
        if (json != null) {
            Iterator<String> keys = json.keys();
            while (keys.hasNext()) {
                String key = keys.next();
                map.put(key, json.opt(key));
            }
        }
        setMetaData(map);
        return this;
    }

    public JSONObject getAsParams() {
        return new JSONObject(metaData);
    }

    public Map<String, Object> getMetaData() {
        return metaData;
    }

    public AirMapPilotMetaData setMetaData(Map<String, Object> metaData) {
        this.metaData = metaData;
        return this;
    }

    public AirMapPilotMetaData put(String key, Object value) {
        getMetaData().put(key, value);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof AirMapPilotMetaData && getMetaData().equals(((AirMapPilotMetaData) o).getMetaData());
    }
}
