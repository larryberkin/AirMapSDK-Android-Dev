package com.airmap.airmapsdk.models.map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.airmap.airmapsdk.util.Utils.optString;

public class MapStyle {

    private List<AirMapLayerStyle> layerStyles;

    public MapStyle(String json) throws JSONException {
        JSONObject jsonObject = new JSONObject(json);

        JSONArray layersArray = jsonObject.optJSONArray("layers");

        layerStyles = new ArrayList<>();
        for (int i = 0; layersArray != null && i < layersArray.length(); i++) {
            JSONObject layerJson = layersArray.optJSONObject(i);
            if (optString(layerJson, "id").contains("airmap")) {
                layerStyles.add(AirMapLayerStyle.fromJson(layerJson));
            }
        }
    }

    public List<AirMapLayerStyle> getLayerStyles() {
        return layerStyles;
    }
}
