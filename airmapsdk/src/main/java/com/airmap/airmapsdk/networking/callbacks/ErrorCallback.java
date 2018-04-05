package com.airmap.airmapsdk.networking.callbacks;

import com.airmap.airmapsdk.AirMapException;

/**
 * Created by collin@airmap.com on 2/14/18.
 */

public abstract class ErrorCallback {

    protected abstract void onError(AirMapException e);
}
