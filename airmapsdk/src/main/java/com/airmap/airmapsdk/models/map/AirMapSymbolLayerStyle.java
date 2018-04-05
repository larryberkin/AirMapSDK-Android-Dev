package com.airmap.airmapsdk.models.map;

import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;

import org.json.JSONObject;

import static com.airmap.airmapsdk.util.Utils.optString;

public class AirMapSymbolLayerStyle extends AirMapLayerStyle {

    public final String iconImage;
    public final double iconPadding;
    public final boolean iconAllowOverlap;
    public final boolean iconKeepUpright;

    protected AirMapSymbolLayerStyle(JSONObject json) {
        super(json);

        JSONObject layoutJson = json.optJSONObject("layout");
        if (layoutJson != null) {
            iconImage = optString(layoutJson, "icon-image");
            iconPadding = layoutJson.optDouble("icon-padding");
            iconAllowOverlap = layoutJson.optBoolean("icon-allow-overlap");
            iconKeepUpright = layoutJson.optBoolean("icon-keep-upright");
        } else {
            iconImage = null;
            iconPadding = 0;
            iconAllowOverlap = false;
            iconKeepUpright = false;
        }
    }

    public boolean isBackgroundStyle() {
        return id.contains("background");
    }

    @Override
    public SymbolLayer toMapboxLayer(Layer layerToClone, String sourceId) {
        SymbolLayer layer = new SymbolLayer(id + "|" + sourceId + "|new", sourceId);
        layer.setSourceLayer(sourceId + "_" + sourceLayer);
        layer.setProperties(PropertyFactory.iconImage(iconImage));
        layer.setProperties(PropertyFactory.iconAllowOverlap(iconAllowOverlap));
        layer.setProperties(PropertyFactory.iconKeepUpright(iconKeepUpright));
        layer.setProperties(PropertyFactory.iconPadding((float) iconPadding));

        if (minZoom != 0) {
            layer.setMinZoom(minZoom);
        }

        if (filter != null) {
            layer.setFilter(filter);
        }

        return layer;
    }
}
