package com.airmap.airmapsdk.networking.callbacks;

import android.os.Handler;
import android.os.Looper;

import com.airmap.airmapsdk.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Response;

public class PhoneVerificationCallback extends GenericBaseOkHttpCallback {
    public PhoneVerificationCallback(AirMapCallback<Boolean> listener) {
        super(listener, null);
    }

    @Override
    public void onResponse(Call call, final Response response) {
        if (listener == null) {
            return; //Don't need to do anything if no listener was provided
        }
        String jsonString;
        try {
            jsonString = response.body().string();
        } catch (final IOException e) {
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Utils.error(listener, e);
                }
            });
            return;
        }
        response.body().close();
        JSONObject result = null;
        try {
            result = new JSONObject(jsonString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        if (!response.isSuccessful() || !Utils.statusSuccessful(result)) {
            final JSONObject resultJSON = result;
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    Utils.error(listener, response.code(), resultJSON);
                }
            });
        } else {
            JSONObject data = null;
            try {
                data = result.getJSONObject("data");
            } catch (JSONException e) {
                e.printStackTrace();
            }

            final boolean verified = data != null && data.optBoolean("verified", false);

            //noinspection unchecked
            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    listener.onSuccess(verified);
                }
            });
        }
    }
}