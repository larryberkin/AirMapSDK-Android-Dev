package com.airmap.airmapsdk.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.mapbox.services.android.telemetry.location.AndroidLocationEngine;
import com.mapbox.services.android.telemetry.location.LocationEngine;
import com.mapbox.services.android.telemetry.location.LocationEngineListener;
import com.mapbox.services.android.telemetry.location.LocationEnginePriority;
import com.mapbox.services.android.telemetry.permissions.PermissionsManager;

import java.lang.ref.WeakReference;

import timber.log.Timber;

import static android.location.LocationManager.GPS_PROVIDER;
import static android.location.LocationManager.NETWORK_PROVIDER;

public class BetterAndroidLocationEngine extends LocationEngine implements LocationListener {

    private static final String DEFAULT_PROVIDER = LocationManager.GPS_PROVIDER;
    private static final long DEFAULT_MIN_TIME = 0;
    private static final float DEFAULT_MIN_DISTANCE = 0;

    private static BetterAndroidLocationEngine instance;

    private WeakReference<Context> context;
    private LocationManager locationManager;
    private String currentProvider;

    @SuppressLint("MissingPermission")
    public BetterAndroidLocationEngine(Context context) {
        super();

        Timber.v("Initializing.");
        this.context = new WeakReference<>(context);
        locationManager = (LocationManager) this.context.get().getSystemService(Context.LOCATION_SERVICE);

        for (final String provider : locationManager.getProviders(true)) {
            locationManager.requestSingleUpdate(provider, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    Timber.v("onLocationChanged: " + provider);
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                    Timber.v("onStatusChanged: " + provider);
                }

                @Override
                public void onProviderEnabled(String provider) {
                    Timber.v("onProviderEnabled: " + provider);
                }

                @Override
                public void onProviderDisabled(String provider) {
                    Timber.v("onProviderDisabled: " + provider);
                }
            }, null);
        }

        currentProvider = DEFAULT_PROVIDER;

    }

    public static synchronized LocationEngine getLocationEngine(Context context) {
        if (instance == null) {
            instance = new BetterAndroidLocationEngine(context.getApplicationContext());
        }

        return instance;
    }

    @Override
    public void activate() {
        // "Connection" is immediate
        Timber.v( "Activating.");
        for (LocationEngineListener listener : locationListeners) {
            listener.onConnected();
        }
    }

    @Override
    public void deactivate() {
        // No op
        Timber.v( "Deactivating.");
    }

    @Override
    public boolean isConnected() {
        return true;
    }

    @Override
    public Location getLastLocation() {
        Location location = null;
        if (!TextUtils.isEmpty(currentProvider)) {
            //noinspection MissingPermission
            location = locationManager.getLastKnownLocation(currentProvider);
        }

        if (location == null) {
            location = locationManager.getLastKnownLocation(NETWORK_PROVIDER);
        }

        return location;
    }

    @Override
    public void requestLocationUpdates() {
        if (!TextUtils.isEmpty(currentProvider)) {
            //noinspection MissingPermission
            Timber.v("requestLocationUpdates");
            locationManager.requestLocationUpdates(currentProvider, DEFAULT_MIN_TIME, DEFAULT_MIN_DISTANCE, this);
        }
    }

    @Override
    public void setPriority(int priority) {
        super.setPriority(priority);
        updateCurrentProvider();
    }

    @Override
    public void removeLocationUpdates() {
        if (PermissionsManager.areLocationPermissionsGranted(context.get())) {
            //noinspection MissingPermission
            locationManager.removeUpdates(this);
        }
    }

    @Override
    public Type obtainType() {
        return Type.ANDROID;
    }

    /**
     * Called when the location has changed.
     */
    @Override
    public void onLocationChanged(Location location) {
        Timber.v( "New location received.");
        for (LocationEngineListener listener : locationListeners) {
            listener.onLocationChanged(location);
        }
    }

    /**
     * Called when the provider status changes.
     */
    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Timber.v( String.format("Provider %s status changed to %d (current provider is %s).",
                provider, status, currentProvider));
    }

    /**
     * Called when the provider is enabled by the user.
     */
    @Override
    public void onProviderEnabled(String provider) {
        Timber.v( String.format("Provider %s was enabled (current provider is %s).", provider, currentProvider));
    }

    /**
     * Called when the provider is disabled by the user.
     */
    @Override
    public void onProviderDisabled(String provider) {
        Timber.v( String.format("Provider %s was disabled (current provider is %s).", provider, currentProvider));
    }

    private void updateCurrentProvider() {
        // We might want to explore android.location.Criteria here.
        if (priority == LocationEnginePriority.NO_POWER) {
            currentProvider = LocationManager.PASSIVE_PROVIDER;
        } else if (priority == LocationEnginePriority.LOW_POWER) {
            currentProvider = NETWORK_PROVIDER;
        } else if (priority == LocationEnginePriority.BALANCED_POWER_ACCURACY) {
            currentProvider = NETWORK_PROVIDER;
        } else if (priority == LocationEnginePriority.HIGH_ACCURACY) {
            currentProvider = LocationManager.GPS_PROVIDER;
        }

        Timber.v( String.format("Priority set to %d (current provider is %s).", priority, currentProvider));
    }
}
