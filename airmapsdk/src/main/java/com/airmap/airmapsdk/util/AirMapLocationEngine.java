package com.airmap.airmapsdk.util;

import android.content.Context;
import android.location.Location;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.mapbox.services.android.telemetry.location.LocationEngine;
import com.mapbox.services.android.telemetry.location.LocationEngineListener;
import com.mapbox.services.android.telemetry.location.LocationEnginePriority;

import timber.log.Timber;

public class AirMapLocationEngine extends LocationEngine {

    private static final String TAG = "AirMapLocationEngine";

    private static AirMapLocationEngine instance;

    private LocationCallback locationCallback;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;

    private AirMapLocationEngine(Context context) {
        super();

        locationCallback = new LocationCallback() {
            @Override
            public void onLocationAvailability(LocationAvailability locationAvailability) {
                super.onLocationAvailability(locationAvailability);
                Timber.e("onLocationAvailability: " + locationAvailability);

                if (locationAvailability.isLocationAvailable()) {
//                    getLastKnownLocation();
//
//                    requestLocationUpdates();
                }
            }

            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                Timber.e("onLocationResult: " + locationResult);

                if (locationResult != null && !locationResult.getLocations().isEmpty()) {
                    Location location = locationResult.getLocations().get(0);
                    for (LocationEngineListener listener : locationListeners) {
                        listener.onLocationChanged(location);
                    }

                    removeLocationUpdates();
                }
            }
        };

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
    }

    public static synchronized AirMapLocationEngine getLocationEngine(Context context) {
        if (instance == null) {
            instance = new AirMapLocationEngine(context.getApplicationContext());
        }
        return instance;
    }

    public void setLocationRequest(LocationRequest locationRequest) {
        this.locationRequest = locationRequest;
    }

    @Override
    public void activate() {
        Timber.e("activate");
        requestLocationUpdates();
    }

    @Override
    public void deactivate() {
        Timber.e("deactivate");
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @Override
    public boolean isConnected() {
        return true;
    }

    public void flush() {
        Task flushTask = fusedLocationClient.flushLocations();
        flushTask.addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                Timber.e("flush successful: " + task.isSuccessful());
            }
        });
    }

    public void getLastKnownLocation() {
        Task<Location> locationTask = fusedLocationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        Timber.e("getLastLocationTask success: " + location);
                        if (location != null) {
                            for (LocationEngineListener listener : locationListeners) {
                                listener.onLocationChanged(location);
                            }
                        }
                    }
                });

        if (locationTask.isComplete()) {
            if (locationTask.getResult() != null) {
                Timber.e("already has result: " + locationTask.getResult());
                for (LocationEngineListener listener : locationListeners) {
                    listener.onLocationChanged(locationTask.getResult());
                }
            } else if (locationTask.getException() != null) {
                Timber.e("already has exception", locationTask.getException());
            }
        }
    }

    @Override
    public Location getLastLocation() {
        Task<Location> task = fusedLocationClient.getLastLocation();
        if (task.isComplete()) {
            return task.getResult();
        }

        return null;
    }

    @Override
    public void requestLocationUpdates() {
        Timber.e("locationRequestLocationUpdates w/ priority: " + locationRequest.getPriority());
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.myLooper());
    }

    @Override
    public void removeLocationUpdates() {
        Timber.e("removeLocationUpdates");
        fusedLocationClient.removeLocationUpdates(locationCallback);
    }

    @Override
    public Type obtainType() {
        return Type.GOOGLE_PLAY_SERVICES;
    }
}
