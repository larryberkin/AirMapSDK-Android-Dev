package com.airmap.airmapsdk.controllers;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.airmap.airmapsdk.AirMapException;
import com.airmap.airmapsdk.Analytics;
import com.airmap.airmapsdk.models.map.AirMapFillLayerStyle;
import com.airmap.airmapsdk.models.map.AirMapLayerStyle;
import com.airmap.airmapsdk.models.map.AirMapLineLayerStyle;
import com.airmap.airmapsdk.models.map.AirMapSymbolLayerStyle;
import com.airmap.airmapsdk.models.map.MapStyle;
import com.airmap.airmapsdk.models.status.AirMapAdvisory;
import com.airmap.airmapsdk.networking.callbacks.AirMapCallback;
import com.airmap.airmapsdk.networking.services.AirMap;
import com.airmap.airmapsdk.networking.services.MappingService;
import com.airmap.airmapsdk.ui.views.AirMapMapView;
import com.airmap.airmapsdk.util.AirMapConstants;
import com.mapbox.geojson.Feature;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.style.expressions.Expression;
import com.mapbox.mapboxsdk.style.layers.BackgroundLayer;
import com.mapbox.mapboxsdk.style.layers.FillLayer;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.TileSet;
import com.mapbox.mapboxsdk.style.sources.VectorSource;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.Locale;

import timber.log.Timber;

import static com.airmap.airmapsdk.networking.services.MappingService.AirMapMapTheme.Dark;
import static com.airmap.airmapsdk.networking.services.MappingService.AirMapMapTheme.Light;
import static com.airmap.airmapsdk.networking.services.MappingService.AirMapMapTheme.Satellite;
import static com.airmap.airmapsdk.networking.services.MappingService.AirMapMapTheme.Standard;
import static com.mapbox.mapboxsdk.style.expressions.Expression.*;
import static com.mapbox.mapboxsdk.style.expressions.Expression.all;
import static com.mapbox.mapboxsdk.style.expressions.Expression.any;
import static com.mapbox.mapboxsdk.style.expressions.Expression.eq;
import static com.mapbox.mapboxsdk.style.expressions.Expression.get;
import static com.mapbox.mapboxsdk.style.expressions.Expression.gt;
import static com.mapbox.mapboxsdk.style.expressions.Expression.lt;
import static com.mapbox.mapboxsdk.style.expressions.Expression.not;
import static com.mapbox.mapboxsdk.style.expressions.Expression.number;

public class MapStyleController implements MapView.OnMapChangedListener {

    private AirMapMapView map;

    private MappingService.AirMapMapTheme currentTheme;
    private MapStyle mapStyle;
    private Callback callback;
    private LineLayer highlightLayer;

    public MapStyleController(AirMapMapView map, @Nullable MappingService.AirMapMapTheme mapTheme, Callback callback) {
        this.map = map;
        this.callback = callback;

        if (mapTheme != null) {
            currentTheme = mapTheme;
        } else {
            // use last used theme or Standard if none has be saved
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(map.getContext());
            String savedTheme = prefs.getString(AirMapConstants.MAP_STYLE, MappingService.AirMapMapTheme.Standard.toString());
            currentTheme = MappingService.AirMapMapTheme.fromString(savedTheme);
        }
    }

    public void onMapReady() {
        loadStyleJSON();

        map.addOnMapChangedListener(this);
    }

    @Override
    public void onMapChanged(int change) {
        switch (change) {
            case MapView.DID_FINISH_LOADING_STYLE: {
                // Adjust the background overlay opacity to improve map visually on Android
                BackgroundLayer backgroundLayer = map.getMap().getLayerAs("background-overlay");
                if (backgroundLayer != null) {
                    if (currentTheme == MappingService.AirMapMapTheme.Light || currentTheme == MappingService.AirMapMapTheme.Standard) {
                        backgroundLayer.setProperties(PropertyFactory.backgroundOpacity(0.95f));
                    } else if (currentTheme == MappingService.AirMapMapTheme.Dark) {
                        backgroundLayer.setProperties(PropertyFactory.backgroundOpacity(0.9f));
                    }
                }

                try {
                    mapStyle = new MapStyle(map.getMap().getStyleJson());
                } catch (JSONException e) {
                    Timber.e(e, "Failed to parse style json");
                }

                // change labels to local if device is not in english
                if (!Locale.ENGLISH.getLanguage().equals(Locale.getDefault().getLanguage())) {
                    for (Layer layer : map.getMap().getLayers()) {
                        if (layer instanceof SymbolLayer && (layer.getId().contains("label") || layer.getId().contains("place") || layer.getId().contains("poi"))) {
                            layer.setProperties(PropertyFactory.textField("{name}"));
                        }
                    }
                }

                callback.onMapStyleLoaded();
                break;
            }
        }
    }

    // Updates the map to use a custom style based on theme and selected layers
    public void rotateMapTheme() {
        MappingService.AirMapMapTheme theme = Standard;
        switch (currentTheme) {
            case Standard: {
                Analytics.logEvent(Analytics.Event.map, Analytics.Action.select, "Dark");
                theme = Dark;
                break;
            }
            case Dark: {
                Analytics.logEvent(Analytics.Event.map, Analytics.Action.select, "Light");
                theme = Light;
                break;
            }
            case Light: {
                Analytics.logEvent(Analytics.Event.map, Analytics.Action.select, "Satellite");
                theme = Satellite;
                break;
            }
            case Satellite: {
                Analytics.logEvent(Analytics.Event.map, Analytics.Action.select, "Standard");
                theme = Standard;
                break;
            }
        }

        updateMapTheme(theme);
    }

    public void updateMapTheme(MappingService.AirMapMapTheme theme) {
        callback.onMapStyleReset();

        currentTheme = theme;
        loadStyleJSON();

        PreferenceManager.getDefaultSharedPreferences(map.getContext()).edit().putString(AirMapConstants.MAP_STYLE, currentTheme.toString()).apply();
    }

    public void addMapLayers(String sourceId, List<String> layers) {
        if (map.getMap().getSource(sourceId) != null) {
            Timber.d("Source already added for: %s", sourceId);
        } else {
            String urlTemplates = AirMap.getRulesetTileUrlTemplate(sourceId, layers);
            TileSet tileSet = new TileSet("2.2.0", urlTemplates);
            tileSet.setMaxZoom(15f);
            tileSet.setMinZoom(7f);
            VectorSource tileSource = new VectorSource(sourceId, tileSet);
            map.getMap().addSource(tileSource);
        }

        for (String sourceLayer : layers) {
            if (TextUtils.isEmpty(sourceLayer)) {
                continue;
            }

            for (AirMapLayerStyle layerStyle : mapStyle.getLayerStyles()) {
                if (layerStyle == null || !layerStyle.sourceLayer.equals(sourceLayer) || map.getMap().getLayer(layerStyle.id + "|" + sourceId + "|new") != null) {
                    continue;
                }

                Layer layer = map.getMap().getLayerAs(layerStyle.id);
                if (layerStyle instanceof AirMapFillLayerStyle) {
                    FillLayer newLayer = (FillLayer) layerStyle.toMapboxLayer(layer, sourceId);
                    if (newLayer.getId().contains("airmap|tfr") || newLayer.getId().contains("notam")) {
                        addTemporalFilter(newLayer, 4);
                    }
                    map.getMap().addLayerAbove(newLayer, layerStyle.id);
                } else if (layerStyle instanceof AirMapLineLayerStyle) {
                    LineLayer newLayer = (LineLayer) layerStyle.toMapboxLayer(layer, sourceId);
                    if (newLayer.getId().contains("airmap|tfr") || newLayer.getId().contains("notam")) {
                        addTemporalFilter(newLayer, 4);
                    }
                    map.getMap().addLayerAbove(newLayer, layerStyle.id);
                } else if (layerStyle instanceof AirMapSymbolLayerStyle) {
                    map.getMap().addLayerAbove(layerStyle.toMapboxLayer(layer, sourceId), layerStyle.id);
                }
            }
        }

        // add highlight layer
        if (map.getMap().getLayer("airmap|highlight|line|" + sourceId) == null) {
            LineLayer highlightLayer = new LineLayer("airmap|highlight|line|" + sourceId, sourceId);
            highlightLayer.setProperties(PropertyFactory.lineColor("#f9e547"));
            highlightLayer.setProperties(PropertyFactory.lineWidth(4f));
            highlightLayer.setProperties(PropertyFactory.lineOpacity(0.9f));
            Expression filter = all(eq(get("id"), "x"));

            try {
                highlightLayer.setFilter(filter);
                map.getMap().addLayer(highlightLayer);
            } catch (Throwable t) {
                // https://github.com/mapbox/mapbox-gl-native/issues/10947
                // https://github.com/mapbox/mapbox-gl-native/issues/11264
                // A layer is associated with a style, not the mapView/mapbox
                Analytics.report(new Exception(t));
                t.printStackTrace();
            }
        }
    }

    private void addTemporalFilter(Layer layer, int hours) {
        Long now = System.currentTimeMillis() / 1000;
        Long in4Hrs = now + (hours * 60 * 60);
        Expression validNowFilter = all(
                gt(number(get("start")), now),
                lt(number(get("end")), now)
        );
        Expression startsSoonFilter = all(
                gt(number(get("start")), now),
                lt(number(get("start")), in4Hrs)
        );
        Expression permanent = eq(
                get("permanent"),
                true
        );
        Expression hasNoEnd = all(
                not(has("end")),
                not(has("base"))
        );
        Expression filter = any(validNowFilter, startsSoonFilter, permanent, hasNoEnd);

        if (layer instanceof FillLayer) {
            ((FillLayer) layer).setFilter(filter);
        } else if (layer instanceof LineLayer) {
            ((LineLayer) layer).setFilter(filter);
        }
    }

    public void removeMapLayers(String sourceId, List<String> sourceLayers) {
        if (sourceLayers == null || sourceLayers.isEmpty()) {
            return;
        }

        Timber.v("remove source: %s layers: %s", sourceId, TextUtils.join(",", sourceLayers));

        for (String sourceLayer : sourceLayers) {
            for (AirMapLayerStyle layerStyle : mapStyle.getLayerStyles()) {
                if (layerStyle != null && layerStyle.sourceLayer.equals(sourceLayer)) {
                    map.getMap().removeLayer(layerStyle.id + "|" + sourceId + "|new");
                }
            }
        }

        // remove highlight
        map.getMap().removeLayer("airmap|highlight|line|" + sourceId);
        if (highlightLayer != null && highlightLayer.getId().equals("airmap|highlight|line|" + sourceId)) {
            highlightLayer = null;
        }

        map.getMap().removeSource(sourceId);
    }

    public void highlight(@NonNull Feature feature, AirMapAdvisory advisory) {
        // remove old highlight
        unhighlight();

        // add new highlight
        String sourceId = feature.getStringProperty("ruleset_id");
        highlightLayer = map.getMap().getLayerAs("airmap|highlight|line|" + sourceId);
        highlightLayer.setSourceLayer(sourceId + "_" + advisory.getType().toString());

        // feature's airspace_id can be an int or string (tile server bug), so match on either
        Expression filter;
        try {
            int airspaceId = Integer.parseInt(advisory.getId());
            filter = any(eq(get("id"), advisory.getId()), eq(get("id"), airspaceId));
        } catch (NumberFormatException e) {
            filter = any(eq(get("id"), advisory.getId()));
        }
        highlightLayer.setFilter(filter);
    }

    public void highlight(Feature feature) {
        String id = feature.getStringProperty("id");
        String type = feature.getStringProperty("category");

        // remove old highlight
        unhighlight();

        // add new highlight
        String sourceId = feature.getStringProperty("ruleset_id");
        highlightLayer = map.getMap().getLayerAs("airmap|highlight|line|" + sourceId);
        highlightLayer.setSourceLayer(sourceId + "_" + type);

        // feature's airspace_id can be an int or string (tile server bug), so match on either
        Expression filter;
        try {
            int airspaceId = Integer.parseInt(id);
            filter = any(eq(get("id"), id), eq(get("id"), airspaceId));
        } catch (NumberFormatException e) {
            filter = any(eq(get("id"), id));
        }
        highlightLayer.setFilter(filter);
    }

    public void unhighlight() {
        if (highlightLayer != null) {
            try {
                LineLayer oldHighlightLayer = map.getMap().getLayerAs(highlightLayer.getId());
                if (oldHighlightLayer != null) {
                    Expression filter = all(eq(get("id"), "x"));
                    highlightLayer.setFilter(filter);
                }
            } catch (RuntimeException e) {
                for (Layer l : map.getMap().getLayers()) {
                    if (l instanceof LineLayer) {
                        Expression filter = all(eq(get("id"), "x"));
                        ((LineLayer) l).setFilter(filter);
                    }
                }
                Analytics.report(e);
            }
        }
    }

    private void loadStyleJSON() {
        map.getMap().setStyleUrl(AirMap.getMapStylesUrl(currentTheme));
    }

    public void checkConnection(final AirMapCallback<Void> callback) {
        AirMap.getMapStylesJson(MappingService.AirMapMapTheme.Standard, new AirMapCallback<JSONObject>() {
            @Override
            protected void onSuccess(JSONObject response) {
                callback.success(null);
            }

            @Override
            protected void onError(AirMapException e) {
                callback.error(e);
            }
        });
    }

    public interface Callback {
        void onMapStyleLoaded();

        void onMapStyleReset();
    }
}
