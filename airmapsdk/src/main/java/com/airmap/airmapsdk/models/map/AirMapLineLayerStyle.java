package com.airmap.airmapsdk.models.map;

import com.mapbox.mapboxsdk.style.functions.Function;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;

import org.json.JSONArray;
import org.json.JSONObject;

import static com.airmap.airmapsdk.util.Utils.optString;

public class AirMapLineLayerStyle extends AirMapLayerStyle {

    private final float lineWidth;
    private final float lineOpacity;
    private final Function lineOpacityFunction;
    private final String lineColor;
    private final Function lineColorFunction;
    private final Float[] lineDashArray;

    protected AirMapLineLayerStyle(JSONObject json) {
        super(json);

        JSONObject paintJson = json.optJSONObject("paint");
        if (paintJson != null) {
            lineWidth = (float) paintJson.optDouble("line-width", 1.0f);

            JSONObject lineOpacityJson = paintJson.optJSONObject("line-opacity");
            if (lineOpacityJson != null) {
                lineOpacityFunction = getFillOpacityFunction(lineOpacityJson);
                lineOpacity = 0;
            } else {
                lineOpacity = (float) paintJson.optDouble("line-opacity", 1.0f);
                lineOpacityFunction = null;
            }

            JSONObject lineColorJson = paintJson.optJSONObject("line-color");
            if (lineColorJson != null) {
                lineColorFunction = getFillColorFunction(lineColorJson);
                lineColor = "#000000";
            } else {
                lineColor = optString(paintJson, "line-color");
                lineColorFunction = null;
            }

            JSONArray dashArray = paintJson.optJSONArray("line-dasharray");
            if (dashArray != null && dashArray.length() > 0) {
                lineDashArray = new Float[dashArray.length()];
                for (int i = 0; i < dashArray.length(); i++) {
                    lineDashArray[i] = (float) dashArray.optDouble(i);
                }
            } else {
                lineDashArray = null;
            }
        } else {
            lineOpacity = 1.0f;
            lineOpacityFunction = null;
            lineWidth = 1f;
            lineColor = "#000000";
            lineColorFunction = null;
            lineDashArray = null;
        }
    }

    @Override
    public LineLayer toMapboxLayer(Layer layerToClone, String sourceId) {
        LineLayer layer = new LineLayer(id + "|" + sourceId + "|new", sourceId);
        layer.setSourceLayer(sourceId + "_" + sourceLayer);
        layer.setProperties(PropertyFactory.lineWidth(lineWidth));

        if (lineOpacityFunction != null) {
            layer.setProperties(PropertyFactory.lineOpacity(lineOpacityFunction));
        } else {
            layer.setProperties(PropertyFactory.lineOpacity(lineOpacity));
        }

        if (lineColorFunction != null) {
            layer.setProperties(PropertyFactory.lineColor(lineColorFunction));
        } else {
            layer.setProperties(PropertyFactory.lineColor(lineColor));
        }

        if (lineDashArray != null) {
            layer.setProperties(PropertyFactory.lineDasharray(lineDashArray));
        }

        if (filter != null) {
            layer.setFilter(filter);
        }

        return layer;
    }
}
