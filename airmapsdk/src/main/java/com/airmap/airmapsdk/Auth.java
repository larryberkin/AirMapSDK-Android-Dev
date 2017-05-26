package com.airmap.airmapsdk;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.airmap.airmapsdk.networking.callbacks.AirMapCallback;
import com.airmap.airmapsdk.networking.services.AirMap;
import com.airmap.airmapsdk.networking.services.AuthService;
import com.airmap.airmapsdk.util.AirMapAuthenticationCallback;
import com.airmap.airmapsdk.util.PreferenceUtils;
import com.airmap.airmapsdk.util.SecuredPreferenceException;
import com.airmap.airmapsdk.util.Utils;
import com.auth0.android.Auth0;
import com.auth0.android.lock.Lock;

import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.NumericDate;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;

/**
 * Created by Vansh Gandhi on 8/10/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */
public class Auth {

    public enum ErrorType {
        DomainBlackList,
        EmailVerification,
        Unknown,
    }

    public static void loginOrSignup(Activity activity, AirMapAuthenticationCallback callback) {
        Auth0 auth0 = new Auth0(Utils.getClientId(), "sso.airmap.io");

        Lock lock = Lock.newBuilder(auth0, callback)
                .hideMainScreenTitle(true)
                .setTermsURL("https://www.airmap.com/terms")
                .setPrivacyURL("https://www.airmap.com/privacy")
                .withScope("openid offline_access")
                .withScheme("airmap")
                .closable(true)
                .build(activity);

        callback.setLock(lock);

        activity.startActivity(lock.newIntent(activity));
    }

    /**
     * Refreshes the saved access token. Non-blocking
     */
    public static void refreshAccessToken(final Context context, final AirMapCallback<Void> callback) {
        AirMapLog.i("AuthServices", "Trying to refresh token");

        String refreshToken = null;
        try {
            SharedPreferences preferences = PreferenceUtils.getPreferences(context);
            refreshToken = preferences.getString(Utils.REFRESH_TOKEN_KEY, "");
        } catch (SecuredPreferenceException e) {
            AirMapLog.e("Auth", "Unable to get refresh token from secure prefs", e);
        }

        // return if refresh token is empty
        if (TextUtils.isEmpty(refreshToken)) {
            if (callback != null) {
                callback.onError(new AirMapException("Invalid Refresh Token"));
            }
            return;
        }

        AuthService.refreshAccessToken(refreshToken, callback);
    }

    /**
     * Refresh Access Token. Blocking
     */
    public static void refreshAccessToken(final Context context) {
        AirMapLog.i("AuthServices", "Trying to refresh token");

        String refreshToken = null;
        try {
            SharedPreferences preferences = PreferenceUtils.getPreferences(context);
            refreshToken = preferences.getString(Utils.REFRESH_TOKEN_KEY, "");
        } catch (SecuredPreferenceException e) {
            AirMapLog.e("Auth", "Unable to get refresh token from secure prefs", e);
        }

        // return if refresh token is empty
        if (TextUtils.isEmpty(refreshToken)) {
            // throw ?
            return;
        }

        AuthService.refreshAccessToken(refreshToken);
    }

    public static boolean isTokenExpired() {
        try {
            String token = AirMap.getAuthToken();
            JwtConsumer consumer = new JwtConsumerBuilder()
                    .setSkipAllValidators()
                    .setDisableRequireSignature()
                    .setSkipSignatureVerification()
                    .build();
            JwtClaims claims = consumer.processToClaims(token);
            return claims.getExpirationTime().isBefore(NumericDate.now());
        } catch (InvalidJwtException | MalformedClaimException e) {
            e.printStackTrace();
            return true;
        }
    }
}
