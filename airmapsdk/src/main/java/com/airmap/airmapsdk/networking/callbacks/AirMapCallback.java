package com.airmap.airmapsdk.networking.callbacks;

import android.os.Handler;
import android.os.Looper;

import com.airmap.airmapsdk.AirMapException;

public abstract class AirMapCallback<T> {

    public final void success(final T response) {
        new Handler(Looper.getMainLooper()).post(() -> onSuccess(response));
    }

    public final void error(final AirMapException e) {
        new Handler(Looper.getMainLooper()).post(() -> onError(e));
    }

    /**
     * Called when the request was successful
     * @param response The object response
     */
    protected abstract void onSuccess(T response);

    /**
     * Called when the request was unsuccessful or there was an error making the request
     * @param e Specifics of the error
     */
    protected abstract void onError(AirMapException e);
}