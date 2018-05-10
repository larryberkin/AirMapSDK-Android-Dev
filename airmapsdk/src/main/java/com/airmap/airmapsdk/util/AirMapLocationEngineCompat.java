package com.airmap.airmapsdk.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Location;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.mapbox.services.android.telemetry.location.AndroidLocationEngine;
import com.mapbox.services.android.telemetry.location.LocationEngine;
import com.mapbox.services.android.telemetry.location.LocationEngineListener;

import timber.log.Timber;

public class AirMapLocationEngineCompat extends LocationEngine {

    private LocationEngine locationEngine;

    public AirMapLocationEngineCompat(Context context) {
        boolean hasGooglePlayServices = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS;
        locationEngine = hasGooglePlayServices ? AirMapLocationEngine.getLocationEngine(context) : AndroidLocationEngine.getLocationEngine(context);
    }

    public void setupLocationEngine() {
        locationEngine.activate();
    }

    public LocationEngine getLocationEngine() {
        return locationEngine;
    }

    @SuppressLint("MissingPermission")
    public Location getLastLocation() {
        return locationEngine.getLastLocation();
    }

    @SuppressLint("MissingPermission")
    public void getLastKnownLocation() {
        if (locationEngine instanceof AirMapLocationEngine) {
            ((AirMapLocationEngine) locationEngine).getLastKnownLocation();
        } else {
            locationEngine.getLastLocation();
        }
    }

    @SuppressLint("MissingPermission")
    public void requestLocationUpdates() {
        locationEngine.requestLocationUpdates();
    }

    public void removeLocationUpdates() {
        locationEngine.removeLocationUpdates();
    }

    @Override
    public Type obtainType() {
        return locationEngine.obtainType();
    }

    public void addLocationEngineListener(LocationEngineListener listener) {
        locationEngine.addLocationEngineListener(listener);
    }

    @Override
    public void activate() {
        locationEngine.activate();
    }

    @Override
    public void deactivate() {
        locationEngine.deactivate();
    }

    @Override
    public boolean isConnected() {
        return locationEngine.isConnected();
    }
}
