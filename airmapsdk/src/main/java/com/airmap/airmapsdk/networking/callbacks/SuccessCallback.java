package com.airmap.airmapsdk.networking.callbacks;

import com.airmap.airmapsdk.AirMapException;

import timber.log.Timber;

/**
 * Created by collin@airmap.com on 2/14/18.
 */

public abstract class SuccessCallback<T> extends AirMapCallback<T> {

    private ErrorCallback errorCallback;

    public SuccessCallback() {

    }

    public SuccessCallback(ErrorCallback callback) {
        this.errorCallback = callback;
    }

    /**
     * Called when the request was unsuccessful or there was an error making the request
     * @param e Specifics of the error
     */
    protected void onError(AirMapException e) {
        Timber.e(e,"Unsuccessful request not handled");

        if (errorCallback != null) {
            errorCallback.onError(e);
        }
    }

}
