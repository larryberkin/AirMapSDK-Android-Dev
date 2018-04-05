package com.airmap.airmapsdk.models.map;

import android.text.TextUtils;

import com.mapbox.mapboxsdk.style.functions.Function;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;

import org.json.JSONObject;

import static com.airmap.airmapsdk.util.Utils.optString;

public class AirMapFillLayerStyle extends AirMapLayerStyle {

    public final float fillOpacity;
    public final Function fillOpacityFunction;
    public final String fillColor;
    public final Function fillColorFunction;
    public final String fillOutlineColor;

    protected AirMapFillLayerStyle(JSONObject json) {
        super(json);

        JSONObject paintJson = json.optJSONObject("paint");
        if (paintJson != null) {

            JSONObject fillOpacityJson = paintJson.optJSONObject("fill-opacity");
            if (fillOpacityJson != null) {
                fillOpacityFunction = getFillOpacityFunction(fillOpacityJson);
                fillOpacity = 0;
            } else {
                fillOpacity = (float) paintJson.optDouble("fill-opacity", 1);
                fillOpacityFunction = null;
            }

            JSONObject fillColorJson = paintJson.optJSONObject("fill-color");
            if (fillColorJson != null) {
                fillColorFunction = getFillColorFunction(fillColorJson);
                fillColor = "#000000";
            } else {
                fillColor = optString(paintJson, "fill-color");
                fillColorFunction = null;
            }

            fillOutlineColor = optString(paintJson, "fill-outline-color");
        } else {
            fillOpacity = 0;
            fillOpacityFunction = null;
            fillColor = "#000000";
            fillColorFunction = null;
            fillOutlineColor = "#000000";
        }
    }

    public boolean isBackgroundStyle() {
        return id.contains("background");
    }

    @Override
    public FillLayer toMapboxLayer(Layer layerToClone, String sourceId) {
        FillLayer fillLayer = new FillLayer(id + "|" + sourceId + "|new", sourceId);
        fillLayer.setSourceLayer(sourceId + "_" + sourceLayer);

        if (!TextUtils.isEmpty(fillOutlineColor)) {
            fillLayer.setProperties(PropertyFactory.fillOutlineColor(fillOutlineColor));
        }

        if (fillOpacityFunction != null) {
            fillLayer.setProperties(PropertyFactory.fillOpacity(fillOpacityFunction));
        } else {
            fillLayer.setProperties(PropertyFactory.fillOpacity(fillOpacity));
        }

        if (fillColorFunction != null) {
            fillLayer.setProperties(PropertyFactory.fillColor(fillColorFunction));
        } else {
            fillLayer.setProperties(PropertyFactory.fillColor(fillColor));
        }

        if (filter != null) {
            fillLayer.setFilter(filter);
        }

        return fillLayer;
    }
}
