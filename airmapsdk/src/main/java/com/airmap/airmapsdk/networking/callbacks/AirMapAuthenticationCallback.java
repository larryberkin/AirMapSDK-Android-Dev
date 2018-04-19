package com.airmap.airmapsdk.networking.callbacks;

import android.app.Activity;

import com.airmap.airmapsdk.AirMapException;
import com.airmap.airmapsdk.models.pilot.AirMapPilot;
import com.airmap.airmapsdk.networking.services.AirMap;
import com.airmap.airmapsdk.util.PreferenceUtils;
import com.airmap.airmapsdk.util.SecuredPreferenceException;
import com.airmap.airmapsdk.util.Utils;
import com.auth0.android.lock.AuthenticationCallback;
import com.auth0.android.lock.Lock;
import com.auth0.android.lock.utils.LockException;
import com.auth0.android.result.Credentials;

import timber.log.Timber;

public class AirMapAuthenticationCallback extends AuthenticationCallback {

    private Activity activity;
    private LoginCallback callback;
    private Lock lock;

    public AirMapAuthenticationCallback(Activity activity, LoginCallback callback) {
        this.activity = activity;
        this.callback = callback;
    }

    @Override
    public void onAuthentication(Credentials credentials) {
        try {
            PreferenceUtils.getPreferences(activity).edit()
                    .putString(Utils.REFRESH_TOKEN_KEY, credentials.getRefreshToken())
                    .apply();
        } catch (SecuredPreferenceException e) {
            Timber.e(e, "Secured Preferences Failed");
        }

        AirMap.setAuthToken(credentials.getIdToken());
        AirMap.getPilot(new AirMapCallback<AirMapPilot>() {
            @Override
            public void onSuccess(final AirMapPilot response) {
                Timber.d("get pilot succeeded");

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onSuccess(response);
                    }
                });
            }

            @Override
            public void onError(final AirMapException e) {
                Timber.e(e, "get pilot failed");

                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        callback.onError(e);
                    }
                });
            }
        });

        if (lock != null) {
            lock.onDestroy(activity);
            lock = null;
        }
    }

    public void setLock(Lock lock) {
        this.lock = lock;
    }

    @Override
    public void onCanceled() {
        if (lock != null) {
            lock.onDestroy(activity);
            lock = null;
        }
    }

    @Override
    public void onError(LockException error) {
        Timber.e(error, "Error authenticating with auth0");
        callback.onError(new AirMapException(error.getMessage()));

        if (lock != null) {
            lock.onDestroy(activity);
            lock = null;
        }
    }
}
