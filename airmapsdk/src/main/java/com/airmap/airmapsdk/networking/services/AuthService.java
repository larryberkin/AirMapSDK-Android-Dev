package com.airmap.airmapsdk.networking.services;

import com.airmap.airmapsdk.AirMapException;
import com.airmap.airmapsdk.AirMapLog;
import com.airmap.airmapsdk.models.AirMapToken;
import com.airmap.airmapsdk.networking.callbacks.AirMapCallback;
import com.airmap.airmapsdk.networking.callbacks.GenericOkHttpCallback;
import com.airmap.airmapsdk.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.Response;

import static com.airmap.airmapsdk.networking.services.AirMap.getClient;

/**
 * Created by Vansh Gandhi on 4/6/17.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */

@SuppressWarnings("WeakerAccess")
public class AuthService extends BaseService {

    public static void performAnonymousLogin(String userId, final AirMapCallback<Void> callback) {
        Map<String, String> params = new HashMap<>();
        params.put("user_id", userId);
        getClient().post(anonymousLoginUrl, params, new GenericOkHttpCallback(new AirMapCallback<AirMapToken>() {
            @Override
            public void onSuccess(AirMapToken response) {
                AirMap.setAuthToken(response.getAuthToken());
                callback.onSuccess(null);
            }

            @Override
            public void onError(AirMapException e) {
                callback.onError(e);
            }
        }, AirMapToken.class));
    }

    public static void refreshAccessToken(String refreshToken) {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(BaseService.loginUrl).newBuilder();
        urlBuilder.addQueryParameter("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer");
        urlBuilder.addQueryParameter("api_type", "app");
        urlBuilder.addQueryParameter("client_id", Utils.getClientId());
        urlBuilder.addQueryParameter("refresh_token", refreshToken);
        String url = urlBuilder.build().toString();
        try {
            String json = AirMap.getClient().get(url);
            JSONObject jsonObject = new JSONObject(json);
            String idToken = jsonObject.getString("id_token");
            AirMap.setAuthToken(idToken);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void refreshAccessToken(String refreshToken, final AirMapCallback<Void> listener) {
        HttpUrl.Builder urlBuilder = HttpUrl.parse("https://sso.airmap.io/delegation").newBuilder();
        urlBuilder.addQueryParameter("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer");
        urlBuilder.addQueryParameter("api_type", "app");
        urlBuilder.addQueryParameter("client_id", Utils.getClientId());
        urlBuilder.addQueryParameter("refresh_token", refreshToken);
        String url = urlBuilder.build().toString();
        getClient().get(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                AirMapLog.e("AuthServices", e.getMessage());
                if (listener != null) {
                    listener.onError(new AirMapException(e.getMessage()));
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String json = response.body().string();
                    response.body().close();
                    JSONObject jsonObject = new JSONObject(json);
                    String idToken = jsonObject.getString("id_token");
                    AirMap.setAuthToken(idToken);
                    if (listener != null) {
                        listener.onSuccess(null);
                    }
                } catch (JSONException e) {
                    AirMapLog.e("AuthServices", e.getMessage());
                    if (listener != null) {
                        listener.onError(new AirMapException(response.code(), e.getMessage()));
                    }
                }
            }
        });
    }
}
