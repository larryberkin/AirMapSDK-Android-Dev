package com.airmap.airmapsdk.networking.callbacks;

import com.airmap.airmapsdk.AirMapException;
import com.airmap.airmapsdk.models.pilot.AirMapPilot;

/**
 * Internal use
 * Created by Vansh Gandhi on 8/10/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */
public interface LoginCallback {
    void onSuccess(AirMapPilot pilot);
    void onError(AirMapException e);
}
