package com.airmap.airmapsdk.networking.services;

import android.text.TextUtils;

import com.airmap.airmapsdk.AirMapException;
import com.airmap.airmapsdk.Auth;
import com.airmap.airmapsdk.models.AirMapBaseModel;
import com.airmap.airmapsdk.util.Utils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.CertificatePinner;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.RequestBody;
import okhttp3.Response;
import rx.Observable;
import rx.functions.Func0;

/**
 * Created by Vansh Gandhi on 6/16/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */
@SuppressWarnings("unused")
public class AirMapClient {

    private static final String TAG = "AirMapClient";

    private OkHttpClient client;

    /**
     * Initialize the client
     */
    public AirMapClient() {
        resetClient(); //Will initialize OkHttpClient client, add cert pinning, and interceptors
    }

    /**
     * Make a GET call with params
     *
     * @param url      The full url to GET
     * @param params   The params to add to the request
     * @param callback An OkHttp Callback
     */
    public Call get(String url, Map<String, String> params, Callback callback) {
        Request request = new Builder().url(urlBodyFromMap(url, params)).get().tag(url).build();
        Call call = client.newCall(request);
        call.enqueue(callback);
        return call;
    }

    /**
     * Make a GET call
     *
     * @param url      The full url to GET
     * @param callback An OkHttp Callback
     */
    public Call get(String url, Callback callback) {
        Request request = new Builder().url(url).get().tag(url).build();
        Call call = client.newCall(request);
        call.enqueue(callback);
        return call;
    }

    /**
     * Make a blocking GET call
     *
     * @param url The full url to GET
     * @return the string contents of the response body
     */
    public String get(String url) throws IOException {
        Request request = new Builder().url(url).get().tag(url).build();
        Call call = client.newCall(request);
        Response response = call.execute();
        String responseString = response.body().string();
        response.body().close();
        return responseString;
    }

    /**
     * Make a POST call with params
     *
     * @param url      The full url to POST
     * @param params   The params to add to the request
     * @param callback An OkHttp Callback
     */
    public Call post(String url, Map<String, String> params, Callback callback) {
        Request request = new Builder().url(url).post(bodyFromMap(params)).tag(url).build();
        Call call = client.newCall(request);
        call.enqueue(callback);
        return call;
    }

    /**
     * Make a POST call with params
     *
     * @param url    The full url to POST
     * @param params The params to add to the request
     */
    public Observable post(String url, Map<String, String> params) {
        try {
            Request request = new Builder().url(url).post(bodyFromMap(params)).tag(url).build();
            Response response = client.newCall(request).execute();
            return Observable.just(response);
        } catch (IOException e) {
            return Observable.error(e);
        }
    }

    /**
     * Make a POST call without params
     *
     * @param url The full url to POST
     */
    public <T extends AirMapBaseModel> Observable<T> post(final String url, final Class<T> classToInstantiate) {
        return Observable.defer(new Func0<Observable<T>>() {
            @Override
            public Observable<T> call() {
                try {
                    Request request = new Builder().url(url).post(bodyFromMap(null)).tag(url).build();
                    Response response = client.newCall(request).execute();
                    T model = parseResponse(response, classToInstantiate);

                    return Observable.just(model);
                } catch (IOException | IllegalAccessException | InstantiationException | JSONException | AirMapException e) {
                    return Observable.error(e);
                }
            }
        });
    }

    private <T extends AirMapBaseModel> T parseResponse(Response response, Class<T> classToInstantiate) throws IOException, JSONException,
            IllegalAccessException, InstantiationException, AirMapException {

        String jsonString = response.body().string();
        response.body().close();
        JSONObject result = new JSONObject(jsonString);

        if (!response.isSuccessful() || !Utils.statusSuccessful(result)) {
            throw new AirMapException(response.code(), result);
        }

        JSONObject jsonObject = result.optJSONObject("data");
        T model = classToInstantiate.newInstance();
        model.constructFromJson(jsonObject);
        return model;
    }

    /**
     * Make a POST call with a JSON body
     *
     * @param url      The full url to POST
     * @param params   The JSON params to add to the request
     * @param callback An OkHttp Callback
     */
    public Call postWithJsonBody(String url, JSONObject params, Callback callback) {
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, params.toString());
        Request request = new Builder().url(url).post(body).build();
        Call call = client.newCall(request);
        call.enqueue(callback);
        return call;
    }

    /**
     * Make a POST call with no params
     *
     * @param url      The full url to POST
     * @param callback An OkHttp Callback
     */
    public Call post(String url, Callback callback) {
        return post(url, null, callback);
    }

    /**
     * Make a PATCH call with params
     *
     * @param url      The full url to PATCH
     * @param params   The params to add to the request
     * @param callback An OkHttp Callback
     */
    public Call patch(String url, Map<String, String> params, Callback callback) {
        Request request = new Builder().url(url).patch(bodyFromMap(params)).build();
        Call call = client.newCall(request);
        call.enqueue(callback);
        return call;
    }

    /**
     * Make a PATCH call with JSON body
     *
     * @param url      The full url to PATCH
     * @param params   The params to add to the request
     * @param callback An OkHttp Callback
     */
    public Call patch(String url, JSONObject params, Callback callback) {
        MediaType JSON = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(JSON, params.toString());
        Request request = new Builder().url(url).patch(body).build();
        Call call = client.newCall(request);
        call.enqueue(callback);
        return call;
    }

    /**
     * Make a PUT call with params
     *
     * @param url      The full url to PUT
     * @param params   The params to add to the request
     * @param callback An OkHttp Callback
     */
    public Call put(String url, Map<String, String> params, Callback callback) {
        Request request = new Builder().url(url).put(bodyFromMap(params)).build();
        Call call = client.newCall(request);
        call.enqueue(callback);
        return call;
    }

    /**
     * Make a PUT call
     *
     * @param url      The full url to PUT
     * @param callback An OkHttp Callback
     */
    public Call put(String url, Callback callback) {
        return put(url, null, callback);
    }

    /**
     * Make a DELETE call
     *
     * @param url      The full url to DELETE
     * @param callback An OkHttp Callback
     */
    public Call delete(String url, Callback callback) {
        Request request = new Builder().url(url).delete().build();
        Call call = client.newCall(request);
        call.enqueue(callback);
        return call;
    }

    /**
     * Clears Interceptor Headers and adds Authorization + x-Api-Key
     */
    public void resetClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        if (AirMap.isCertificatePinningEnabled()) {
            builder.certificatePinner(getCertificatePinner());
        }

        // This interceptor refreshes the access token if needed
        builder.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                // Don't intercept if refresh token not yet expired or for actual refresh token request
                if (!Auth.isTokenExpired() || chain.request().url().toString().startsWith(BaseService.loginUrl)) {
                    return chain.proceed(chain.request());
                }
                AirMap.refreshAccessToken();
                Builder newRequest = chain.request().newBuilder();
                String authToken = AirMap.getAuthToken();
                if (!TextUtils.isEmpty(authToken)) {
                    newRequest.header("Authorization", "Bearer " + authToken);
                }
                return chain.proceed(newRequest.build());
            }
        });

        // This interceptor adds the api and auth headers
        builder.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                // Only attach our API Key and Auth token if we're going to AirMap
                if (chain.request().url().host().equals("api.airmap.com")) {
                    Builder newRequest = chain.request().newBuilder();
                    String authToken = AirMap.getAuthToken();
                    String xApiKey = AirMap.getApiKey();
                    if (!TextUtils.isEmpty(xApiKey)) {
                        newRequest.header("x-Api-Key", xApiKey);
                    }
                    if (!TextUtils.isEmpty(authToken)) {
                        newRequest.header("Authorization", "Bearer " + authToken);
                    }
                    return chain.proceed(newRequest.build());
                }
                return chain.proceed(chain.request());
            }
        });

        // This interceptor will show the login screen if we get an unauthorized
        builder.addInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Response response = chain.proceed(chain.request());
                if (response.code() == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    AirMap.showLogin();
                }
                return response;
            }
        });
        //TODO: Check for active connections before reassigning client
        client = builder.connectTimeout(15, TimeUnit.SECONDS).readTimeout(15, TimeUnit.SECONDS).writeTimeout(15, TimeUnit.SECONDS).build();
    }

    /**
     * Builds and Returns a CertificatePinner for AirMap API calls
     *
     * @return CertificatePinner
     */
    private CertificatePinner getCertificatePinner() {
        String host = "api.airmap.com";
        return new CertificatePinner.Builder()
                .add(host, "sha256/47DEQpj8HBSa+/TImW+5JCeuQeRkm5NMpJWZG3hSuFU=")
                .add(host, "sha256/CJlvFGiErgX6zPm0H+oO/TRbKOERdQOAYOs2nUlvIQ0=")
                .add(host, "sha256/8Rw90Ej3Ttt8RRkrg+WYDS9n7IS03bk5bjP/UXPtaY8=")
                .add(host, "sha256/Ko8tivDrEjiY90yGasP6ZpBU4jwXvHqVvQI0GS3GNdA=")
                .build();
    }

    /**
     * Creates url based on map of params
     *
     * @param base The base url
     * @param map  The parameters to add to the url
     * @return The url with parameters embedded
     */
    private HttpUrl urlBodyFromMap(String base, Map<String, String> map) {
        HttpUrl.Builder builder = HttpUrl.parse(base).newBuilder(base);
        for (Entry<String, String> entry : map.entrySet()) {
            if (entry.getValue() != null) {
                builder.addEncodedQueryParameter(entry.getKey(), entry.getValue());
            }
        }
        return builder.build();
    }

    /**
     * Creates a Request Body from a map of params
     *
     * @param map The parameters to add to the body
     * @return The request body
     */
    private FormBody bodyFromMap(Map<String, String> map) {
        FormBody.Builder formBody = new FormBody.Builder();
        if (map != null) {
            for (final Map.Entry<String, String> entrySet : map.entrySet()) {
                if (entrySet.getValue() != null) {
                    formBody.add(entrySet.getKey(), entrySet.getValue());
                }
            }
        }
        return formBody.build();
    }

    /**
     * Cancels requests with the given tag
     *
     * @param tag The tag of the call to cancel
     */
    public void cancelCallWithTag(String tag) {
        for (Call call : client.dispatcher().queuedCalls()) {
            if (call.request().tag().equals(tag)) {
                call.cancel();
            }
        }

        for (Call call : client.dispatcher().runningCalls()) {
            if (call.request().tag().equals(tag)) {
                call.cancel();
            }
        }
    }
}