package com.airmap.airmapsdk.ui.views;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.PointF;
import android.graphics.RectF;
import android.provider.Settings;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.Gravity;

import com.airmap.airmapsdk.AirMapException;
import com.airmap.airmapsdk.Analytics;
import com.airmap.airmapsdk.R;
import com.airmap.airmapsdk.controllers.MapDataController;
import com.airmap.airmapsdk.controllers.MapStyleController;
import com.airmap.airmapsdk.models.rules.AirMapRuleset;
import com.airmap.airmapsdk.models.status.AirMapAdvisory;
import com.airmap.airmapsdk.models.status.AirMapAirspaceStatus;
import com.airmap.airmapsdk.networking.callbacks.AirMapCallback;
import com.airmap.airmapsdk.networking.services.MappingService;
import com.airmap.airmapsdk.util.Utils;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.style.layers.Filter;
import com.mapbox.services.commons.geojson.Feature;
import com.mapbox.services.commons.models.Position;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import timber.log.Timber;

public class AirMapMapView extends MapView implements MapView.OnMapChangedListener, MapboxMap.OnMapClickListener, MapDataController.Callback {

    private MapboxMap map;

    private MapStyleController mapStyleController;
    private MapDataController mapDataController;

    // optional callbacks
    private List<OnMapLoadListener> mapLoadListeners;
    private List<OnMapDataChangeListener> mapDataChangeListeners;
    private List<OnAdvisoryClickListener> advisoryClickListeners;

    public AirMapMapView(@NonNull Context context, Configuration configuration, @Nullable MappingService.AirMapMapTheme mapTheme) {
        super(context);

        init(configuration, mapTheme);
    }

    public AirMapMapView(@NonNull Context context) {
        this(context, null, 0);
    }

    public AirMapMapView(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public AirMapMapView(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray a = context.getTheme().obtainStyledAttributes(
                attrs,
                R.styleable.AirMapMapView,
                0, 0);

        int mapThemeValue;
        try {
            mapThemeValue = a.getInteger(R.styleable.AirMapMapView_mapTheme, -1);
        } finally {
            a.recycle();
        }

        MappingService.AirMapMapTheme mapTheme = null;
        switch (mapThemeValue) {
            case 0:
                mapTheme = MappingService.AirMapMapTheme.Standard;
                break;
            case 1:
                mapTheme = MappingService.AirMapMapTheme.Dark;
                break;
            case 2:
                mapTheme = MappingService.AirMapMapTheme.Light;
                break;
            case 3:
                mapTheme = MappingService.AirMapMapTheme.Satellite;
                break;
        }

        Configuration defaultConfig = new AutomaticConfiguration();
        init(defaultConfig, mapTheme);
    }

    public void init(Configuration configuration, @Nullable MappingService.AirMapMapTheme mapTheme) {
        mapLoadListeners = new ArrayList<>();
        mapDataChangeListeners = new ArrayList<>();
        advisoryClickListeners = new ArrayList<>();

        // default data controller
        mapDataController = new MapDataController(this, configuration);

        mapStyleController = new MapStyleController(this, mapTheme, new MapStyleController.Callback() {
            @Override
            public void onMapStyleReset() {
                mapDataController.onMapReset();
            }

            @Override
            public void onMapStyleLoaded() {
                Timber.v("onMapStyleLoaded: %s", getMap().getCameraPosition());
                mapDataController.onMapLoaded();

                for (OnMapLoadListener mapLoadListener : mapLoadListeners) {
                    mapLoadListener.onMapLoaded();
                }
            }
        });

        addOnMapChangedListener(this);
    }


    public void configure(Configuration configuration) {
        mapDataController.configure(configuration);
    }

    /**
     * Override data controller
     *
     * @param controller
     */
    public void setMapDataController(MapDataController controller) {
        // destroy old data controller (unsubscribe from rx)
        mapDataController.onDestroy();

        this.mapDataController = controller;
    }

    /**
     * Go to next theme (Standard to Dark to Light to Satellite to Standard to Dark...)
     */
    public void rotateMapTheme() {
        // check if map is ready yet
        if (map == null) {
            return;
        }
        mapStyleController.rotateMapTheme();
    }

    /**
     * Explicitly set theme (Standard, Dark, Light, Satellite
     */
    public void setMapTheme(MappingService.AirMapMapTheme theme) {
        // check if map is ready yet
        if (map == null) {
            return;
        }
        mapStyleController.updateMapTheme(theme);
    }

    @Override
    public void getMapAsync(final OnMapReadyCallback callback) {
        super.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(MapboxMap mapboxMap) {
                map = mapboxMap;
                map.addOnMapClickListener(AirMapMapView.this);
                map.getUiSettings().setLogoGravity(Gravity.BOTTOM | Gravity.END); // Move to bottom right
                map.getUiSettings().setAttributionGravity(Gravity.BOTTOM | Gravity.END); // Move to bottom right
                map.setPrefetchesTiles(true);
                mapStyleController.onMapReady();
                if (callback != null) {
                    callback.onMapReady(mapboxMap);
                }

            }
        });
    }

    @Override
    public void onMapChanged(int change) {
        switch (change) {
            // check if map failed
            case MapView.DID_FAIL_LOADING_MAP: {
                // Devices without internet connection will not be able to load the mapbox map
                //TODO: add more sophisticated check (like actually check style url for 200)
                if (!Utils.isNetworkConnected(getContext())) {
                    for (OnMapLoadListener mapLoadListener : mapLoadListeners) {
                        mapLoadListener.onMapFailed(MapFailure.NETWORK_CONNECTION_FAILURE);
                    }

                // Devices with an inaccurate date/time will not be able to load the mapbox map
                // If the "automatic date/time" is disabled on the device and the map fails to load, recommend the user enable it
                } else if (Settings.Global.getInt(getContext().getContentResolver(), Settings.Global.AUTO_TIME, 0) == 0) {
                    for (OnMapLoadListener mapLoadListener : mapLoadListeners) {
                        mapLoadListener.onMapFailed(MapFailure.INACCURATE_DATE_TIME_FAILURE);
                    }

                // check connection by requesting the styles json directly (async)
                } else {
                    mapStyleController.checkConnection(new AirMapCallback<Void>() {
                        @Override
                        protected void onSuccess(Void response) {
                            for (OnMapLoadListener mapLoadListener : mapLoadListeners) {
                                mapLoadListener.onMapFailed(MapFailure.UNKNOWN_FAILURE);
                            }

                            String logs = Utils.getMapboxLogs();
                            Analytics.report(new Exception("Mapbox map failed to load due to no network connection but able to access styles directly: " + logs));
                        }

                        @Override
                        protected void onError(AirMapException e) {
                            for (OnMapLoadListener mapLoadListener : mapLoadListeners) {
                                mapLoadListener.onMapFailed(MapFailure.NETWORK_CONNECTION_FAILURE);
                            }
                        }
                    });
                    return;
                }
                break;
            }
            case MapView.REGION_DID_CHANGE:
            case MapView.REGION_DID_CHANGE_ANIMATED: {
                if (mapDataController != null) {
                    mapDataController.onMapRegionChanged();
                }
                break;
            }
            case MapView.DID_FINISH_RENDERING_MAP_FULLY_RENDERED: {
                if (mapDataController != null) {
                    mapDataController.onMapFinishedRendering();
                }
                break;
            }
            default: {
                break;
            }
        }
    }

    @Override
    public void onMapClick(@NonNull LatLng point) {
        if (advisoryClickListeners == null || advisoryClickListeners.isEmpty() || map.getCameraPosition().zoom < 11) {
            return;
        }

        PointF clickPoint = map.getProjection().toScreenLocation(point);
        int slop = Utils.dpToPixels(getContext(), 10).intValue();
        RectF clickRect = new RectF(clickPoint.x - slop, clickPoint.y - slop, clickPoint.x + slop, clickPoint.y + slop);
        Filter.Statement filter = Filter.has("id");

        final List<Feature> selectedFeatures = map.queryRenderedFeatures(clickRect, filter);
        if (selectedFeatures.isEmpty()) {
            return;
        }

        List<AirMapAdvisory> allAdvisories = mapDataController.getCurrentAdvisories();

        Feature featureClicked = null;
        AirMapAdvisory advisoryClicked = null;
        Set<AirMapAdvisory> filteredAdvisories = new HashSet<>();

        for (Feature feature : selectedFeatures) {
            if (allAdvisories == null) {
                break;
            }

            for (AirMapAdvisory advisory : allAdvisories) {
                if (advisory.getId().equals(feature.getStringProperty("id"))) {
                    // set as the clicked advisory based on size/importance
                    if (advisoryClicked == null || hasHigherPriority(advisory, advisoryClicked)) {
                        featureClicked = feature;
                        advisoryClicked = advisory;

                    }
                    filteredAdvisories.add(advisory);
                }
            }
        }

        // if feature has matching advisory
        if (featureClicked != null) {
            // draw yellow outline & zoom
            mapStyleController.highlight(featureClicked, advisoryClicked);
            zoomToFeatureIfNecessary(featureClicked);

            for (AirMapMapView.OnAdvisoryClickListener advisoryClickListener : advisoryClickListeners) {
                advisoryClickListener.onAdvisoryClicked(advisoryClicked, new ArrayList<>(filteredAdvisories));
            }

        // if feature doesn't have matching advisory, wait
        } else {
            // highlight the feature
            for (Feature feature : selectedFeatures) {
                // set as the clicked advisory based on size/importance
                if (featureClicked == null || hasHigherPriority(feature, featureClicked)) {
                    featureClicked = feature;
                }
            }
            mapStyleController.highlight(featureClicked);

            Timber.w("Feature clicked doesn't have matching advisory yet");

            // if advisory is missing, show loading until advisories loaded
            for (AirMapMapView.OnAdvisoryClickListener advisoryClickListener : advisoryClickListeners) {
                advisoryClickListener.onAdvisoryClicked(null, null);
            }

            // callback for when advisories are loaded
            addOnMapDataChangedListener(new OnMapDataChangeListener() {
                int count = 0;
                @Override
                public void onRulesetsChanged(List<AirMapRuleset> availableRulesets, List<AirMapRuleset> selectedRulesets) {}

                @Override
                public void onAdvisoryStatusChanged(AirMapAirspaceStatus status) {
                    Feature featureClicked = null;
                    AirMapAdvisory advisoryClicked = null;
                    Set<AirMapAdvisory> filteredAdvisories = new HashSet<>();

                    for (Feature feature : selectedFeatures) {
                        if (status == null || status.getAdvisories() == null || status.getAdvisories().isEmpty()) {
                            break;
                        }

                        for (AirMapAdvisory advisory : status.getAdvisories()) {
                            if (advisory.getId().equals(feature.getStringProperty("id"))) {
                                // set as the clicked advisory based on size/importance
                                if (advisoryClicked == null || hasHigherPriority(advisory, advisoryClicked)) {
                                    featureClicked = feature;
                                    advisoryClicked = advisory;
                                }

                                filteredAdvisories.add(advisory);
                            }
                        }
                    }

                    if (featureClicked != null) {
                        Timber.w("Matching advisory found for feature");
                        mapStyleController.highlight(featureClicked, advisoryClicked);
                        zoomToFeatureIfNecessary(featureClicked);

                        for (AirMapMapView.OnAdvisoryClickListener advisoryClickListener : advisoryClickListeners) {
                            advisoryClickListener.onAdvisoryClicked(advisoryClicked, new ArrayList<>(filteredAdvisories));
                        }

                        removeOnMapDataChangedListener(this);
                    } else {
                        if (count > 3) {
                            removeOnMapDataChangedListener(this);
                        }
                    }

                    count++;
                }

                @Override
                public void onAdvisoryStatusLoading() {}
            });
        }
    }

    private boolean hasHigherPriority(AirMapAdvisory advisory, AirMapAdvisory selectedAdvisory) {
        switch (selectedAdvisory.getType()) {
            case Emergencies:
            case School:
            case Fires:
            case Prison:
            case Wildfires:
            case PowerPlant:
                return false;
            default:
                return true;
        }
    }

    private boolean hasHigherPriority(Feature feature, Feature selectedFeature) {
        String type = selectedFeature.getStringProperty("category");
        switch (type) {
            case "emergency":
            case "school":
            case "fire":
            case "prison":
            case "wildfire":
            case "power_plant":
                return false;
            default:
                return true;
        }
    }

    private void zoomToFeatureIfNecessary(Feature featureClicked) {
        try {
            LatLngBounds cameraBounds = map.getProjection().getVisibleRegion().latLngBounds;
            LatLngBounds.Builder advisoryLatLngsBuilder = new LatLngBounds.Builder();
            boolean zoom = false;

            if (featureClicked.getGeometry().getCoordinates() instanceof ArrayList) {
                List<Position> positions = Utils.getPositionsFromFeature((ArrayList) featureClicked.getGeometry().getCoordinates());
                for (Position position : positions) {
                    LatLng latLng = new LatLng(position.getLatitude(), position.getLongitude());
                    advisoryLatLngsBuilder.include(latLng);
                    if (!cameraBounds.contains(latLng)) {
                        Timber.d("Camera position doesn't contain point");
                        zoom = true;
                    }
                }
            } else if (featureClicked.getGeometry().getCoordinates() instanceof Position) {
                Position position = (Position) featureClicked.getGeometry().getCoordinates();
                LatLng latLng = new LatLng(position.getLatitude(), position.getLongitude());
                advisoryLatLngsBuilder.include(latLng);
                if (!cameraBounds.contains(latLng)) {
                    Timber.d("Camera position doesn't contain point");
                    zoom = true;
                }
            }

            if (zoom) {
                int padding = Utils.dpToPixels(getContext(), 72).intValue();
                map.moveCamera(CameraUpdateFactory.newLatLngBounds(advisoryLatLngsBuilder.build(), padding));
            }
        } catch (ClassCastException e) {
            Timber.e(e,"Unable to get feature geometry");
            Analytics.report(e);
        }
    }

    public void highlight(AirMapAdvisory advisory) {
        RectF mapRectF = new RectF(getLeft(), getTop(), getRight(), getBottom());
        Filter.Statement filter = Filter.has("id");
        List<Feature> selectedFeatures = map.queryRenderedFeatures(mapRectF, filter);

        for (Feature feature : selectedFeatures) {
            if (advisory.getId().equals(feature.getStringProperty("id"))) {
                mapStyleController.highlight(feature, advisory);
                zoomToFeatureIfNecessary(feature);
                break;
            }
        }
    }

    public void unhighlight() {
        mapStyleController.unhighlight();
    }

    @Override
    public void onRulesetsUpdated(List<AirMapRuleset> availableRulesets, List<AirMapRuleset> selectedRulesets, List<AirMapRuleset> previouslySelectedRulesetsSelectedRulesets) {
        Timber.i("onRulesetsUpdated to: %s from: %s", selectedRulesets, previouslySelectedRulesetsSelectedRulesets);

        setLayers(selectedRulesets, previouslySelectedRulesetsSelectedRulesets);

        for (OnMapDataChangeListener mapDataChangeListener : mapDataChangeListeners) {
            mapDataChangeListener.onRulesetsChanged(availableRulesets, selectedRulesets);
        }
    }

    @Override
    public void onAdvisoryStatusUpdated(AirMapAirspaceStatus advisoryStatus) {
        for (OnMapDataChangeListener mapDataChangeListener : new ArrayList<>(mapDataChangeListeners)) {
            mapDataChangeListener.onAdvisoryStatusChanged(advisoryStatus);
        }
    }

    @Override
    public void onAdvisoryStatusLoading() {
        for (OnMapDataChangeListener mapDataChangeListener : mapDataChangeListeners) {
            mapDataChangeListener.onAdvisoryStatusLoading();
        }
    }

    private void setLayers(List<AirMapRuleset> newRulesets, List<AirMapRuleset> oldRulesets) {
        if (oldRulesets != null) {
            for (AirMapRuleset oldRuleset : oldRulesets) {
                if (!newRulesets.contains(oldRuleset)) {
                    mapStyleController.removeMapLayers(oldRuleset.getId(), oldRuleset.getLayers());
                }
            }
        }


        for (AirMapRuleset newRuleset : newRulesets) {
            if (oldRulesets == null || !oldRulesets.contains(newRuleset)) {
                mapStyleController.addMapLayers(newRuleset.getId(), newRuleset.getLayers());
            }
        }
    }

    public MapboxMap getMap() {
        return map;
    }

    public List<AirMapRuleset> getSelectedRulesets() {
        return mapDataController.getSelectedRulesets();
    }

    public void disableAdvisories() {
        mapDataController.disableAdvisories();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mapDataController.onDestroy();
    }

    // callbacks
    public void addOnMapLoadListener(OnMapLoadListener listener) {
        mapLoadListeners.add(listener);

        if (getMap() != null) {
            listener.onMapLoaded();
        }
    }

    public void removeOnMapLoadListener(OnMapLoadListener listener) {
        mapLoadListeners.remove(listener);
    }

    public void addOnMapDataChangedListener(OnMapDataChangeListener listener) {
        mapDataChangeListeners.add(listener);

        if (getMap() != null) {
            listener.onRulesetsChanged(mapDataController.getAvailableRulesets(), mapDataController.getSelectedRulesets());

            listener.onAdvisoryStatusChanged(mapDataController.getAirspaceStatus());
        }
    }

    public void removeOnMapDataChangedListener(OnMapDataChangeListener listener) {
        mapDataChangeListeners.remove(listener);
    }

    public void addOnAdvisoryClickListener(OnAdvisoryClickListener listener) {
        advisoryClickListeners.add(listener);
    }

    public void removeOnAdvisoryClickListener(OnAdvisoryClickListener listener) {
        advisoryClickListeners.remove(listener);
    }

    public interface OnMapLoadListener {
        void onMapLoaded();

        void onMapFailed(MapFailure reason);
    }

    public interface OnMapDataChangeListener {
        void onRulesetsChanged(List<AirMapRuleset> availableRulesets, List<AirMapRuleset> selectedRulesets);

        void onAdvisoryStatusChanged(AirMapAirspaceStatus status);

        void onAdvisoryStatusLoading();
    }

    public interface OnAdvisoryClickListener {
        void onAdvisoryClicked(@Nullable AirMapAdvisory advisoryClicked, @Nullable List<AirMapAdvisory> advisoriesFiltered);
    }

    public enum MapFailure {
        INACCURATE_DATE_TIME_FAILURE, NETWORK_CONNECTION_FAILURE, UNKNOWN_FAILURE
    }

    public abstract static class Configuration {
        public enum Type {
            AUTOMATIC,
            DYNAMIC,
            MANUAL
        }

        public final Type type;

        public Configuration(Type type) {
            this.type = type;
        }
    }

    public static class AutomaticConfiguration extends Configuration {

        public AutomaticConfiguration() {
            super(Type.AUTOMATIC);
        }
    }

    public static class DynamicConfiguration extends Configuration {

        public final Set<String> preferredRulesetIds;
        public final Set<String> unpreferredRulesetIds;
        public final boolean enableRecommendedRulesets;

        public DynamicConfiguration(@Nullable Set<String> preferredRulesetIds, @Nullable Set<String> unpreferredRulesetIds, boolean enableRecommendedRulesets) {
            super(Type.DYNAMIC);

            this.preferredRulesetIds = preferredRulesetIds != null ? preferredRulesetIds : new HashSet<String>();
            this.unpreferredRulesetIds = unpreferredRulesetIds != null ? unpreferredRulesetIds : new HashSet<String>();
            this.enableRecommendedRulesets = enableRecommendedRulesets;
        }
    }

    public static class ManualConfiguration extends Configuration {

        public final List<AirMapRuleset> selectedRulesets;

        public ManualConfiguration(List<AirMapRuleset> selectedRulesets) {
            super(Type.MANUAL);

            this.selectedRulesets = selectedRulesets;
        }
    }
}
