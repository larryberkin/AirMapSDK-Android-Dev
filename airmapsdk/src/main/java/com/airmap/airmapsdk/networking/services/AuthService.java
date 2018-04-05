package com.airmap.airmapsdk.networking.services;

import com.airmap.airmapsdk.AirMapException;
import com.airmap.airmapsdk.models.AirMapToken;
import com.airmap.airmapsdk.networking.callbacks.AirMapCallback;
import com.airmap.airmapsdk.networking.callbacks.GenericOkHttpCallback;
import com.airmap.airmapsdk.util.AirMapConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.Response;
import timber.log.Timber;

import static com.airmap.airmapsdk.networking.services.AirMap.getClient;

@SuppressWarnings("WeakerAccess")
public class AuthService extends BaseService {

    public static String getAuth0Domain() {
        return auth0Domain;
    }

    public static Call performAnonymousLogin(String userId, final AirMapCallback<Void> callback) {
        Map<String, String> params = new HashMap<>();
        params.put("user_id", userId);
        return getClient().post(anonymousLoginUrl, params, new GenericOkHttpCallback(new AirMapCallback<AirMapToken>() {
            @Override
            public void onSuccess(AirMapToken response) {
                AirMap.setAuthToken(response.getAuthToken());
                callback.success(null);
            }

            @Override
            public void onError(AirMapException e) {
                callback.error(e);
            }
        }, AirMapToken.class));
    }

    public static void refreshAccessToken(String refreshToken) {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(BaseService.loginUrl).newBuilder();
        urlBuilder.addQueryParameter("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer");
        urlBuilder.addQueryParameter("api_type", "app");
        urlBuilder.addQueryParameter("client_id", AirMapConfig.getAuth0ClientId());
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
        HttpUrl.Builder urlBuilder = HttpUrl.parse(delegationUrl).newBuilder();
        urlBuilder.addQueryParameter("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer");
        urlBuilder.addQueryParameter("api_type", "app");
        urlBuilder.addQueryParameter("client_id", AirMapConfig.getAuth0ClientId());
        urlBuilder.addQueryParameter("refresh_token", refreshToken);
        String url = urlBuilder.build().toString();
        getClient().get(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                Timber.e(e, "error refreshing access token");
                if (listener != null) {
                    listener.error(new AirMapException(e.getMessage()));
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
                        listener.success(null);
                    }
                } catch (JSONException e) {
                    Timber.e(e, "error parsing json");
                    if (listener != null) {
                        listener.error(new AirMapException(response.code(), e.getMessage()));
                    }
                }
            }
        });
    }

    public static void getFirebaseToken(final AirMapCallback<String> listener) {
        HttpUrl.Builder urlBuilder = HttpUrl.parse(delegationUrl).newBuilder();
        urlBuilder.addQueryParameter("grant_type", "urn:ietf:params:oauth:grant-type:jwt-bearer");
        urlBuilder.addQueryParameter("client_id", AirMapConfig.getAuth0ClientId());
        urlBuilder.addQueryParameter("id_token", AirMap.getAuthToken());
        urlBuilder.addQueryParameter("scope", "openid");
        urlBuilder.addQueryParameter("device", "android_app");
        String url = urlBuilder.build().toString();
        AirMap.getClient().get(url, new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                if (listener != null) {
                    listener.error(new AirMapException(e.getMessage()));
                }
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String json = response.body().string();
                    response.body().close();
                    JSONObject jsonObject = new JSONObject(json);
                    String customToken = jsonObject.getString("id_token");
                    if (listener != null) {
                        listener.success(customToken);
                    }
                } catch (JSONException e) {
                    Timber.e(e, "error parsing json");
                    if (listener != null) {
                        listener.error(new AirMapException(response.code(), e.getMessage()));
                    }
                }
            }
        });
    }
}
