package com.airmap.airmapsdk.networking.callbacks;

import com.airmap.airmapsdk.AirMapException;
import com.airmap.airmapsdk.models.pilot.AirMapPilot;

public interface LoginCallback {
    void onSuccess(AirMapPilot pilot);
    void onError(AirMapException e);
}
