package com.airmap.airmapsdk.networking.services;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.airmap.airmapsdk.models.Coordinate;
import com.airmap.airmapsdk.models.flight.AirMapFlightBriefing;
import com.airmap.airmapsdk.models.status.AirMapAirspaceStatus;
import com.airmap.airmapsdk.models.rules.AirMapJurisdiction;
import com.airmap.airmapsdk.models.rules.AirMapRuleset;
import com.airmap.airmapsdk.networking.callbacks.AirMapCallback;
import com.airmap.airmapsdk.networking.callbacks.GenericListOkHttpCallback;
import com.airmap.airmapsdk.networking.callbacks.GenericOkHttpCallback;
import com.airmap.airmapsdk.util.Utils;
import com.mapbox.mapboxsdk.geometry.LatLng;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

/**
 * Created by Vansh Gandhi on 1/12/17.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */

public class RulesetService extends BaseService {

    static Call getJurisdictions(LatLng southwest, LatLng northeast, AirMapCallback<List<AirMapJurisdiction>> callback) {
        Map<String, String> params = new HashMap<>();
        params.put("bounds", southwest.getLatitude() + "," + southwest.getLongitude() + "," + northeast.getLatitude() + "," + northeast.getLongitude());

        return AirMap.getClient().get(jurisdictionBaseUrl, params, new GenericListOkHttpCallback(callback, AirMapJurisdiction.class));
    }

    static Call getJurisdictions(JSONObject geometry, double buffer, AirMapCallback<List<AirMapJurisdiction>> callback) {
        Map<String, Object> params = new HashMap<>();
        params.put("geometry", geometry);
        params.put("buffer", buffer);

        return AirMap.getClient().postWithJsonBody(jurisdictionBaseUrl, params, new GenericListOkHttpCallback(callback, AirMapJurisdiction.class));
    }

    static Call getJurisdictions(List<String> jurisdictionIds, AirMapCallback<List<AirMapJurisdiction>> callback) {
        Map<String, String> params = new HashMap<>();
        params.put("ids", TextUtils.join(",", jurisdictionIds));

        return AirMap.getClient().get(jurisdictionBaseUrl, params, new GenericListOkHttpCallback(callback, AirMapJurisdiction.class));
    }

    static Call getJurisdiction(String jurisdictionId, AirMapCallback<AirMapJurisdiction> callback) {
        String url = String.format(jurisdictionByIdUrl, jurisdictionId);

        return AirMap.getClient().get(url, new GenericOkHttpCallback(callback, AirMapJurisdiction.class));
    }

    static Call getRulesets(Coordinate coordinate, AirMapCallback<List<AirMapRuleset>> listener) {
        Map<String, Object> params = new HashMap<>();
        params.put("latitude", String.valueOf(coordinate.getLatitude()));
        params.put("longitude", String.valueOf(coordinate.getLongitude()));
        return AirMap.getClient().postWithJsonBody(rulesetBaseUrl, params, new GenericListOkHttpCallback(listener, AirMapRuleset.class));
    }

    static Call getRulesets(JSONObject geometry, AirMapCallback<List<AirMapRuleset>> listener) {
        Map<String, Object> params = new HashMap<>();
        params.put("geometry", geometry);
        return AirMap.getClient().postWithJsonBody(rulesetBaseUrl, params, new GenericListOkHttpCallback(listener, AirMapRuleset.class));
    }

    static Call getRulesets(List<String> rulesetIds, AirMapCallback<List<AirMapRuleset>> listener) {
        Map<String, String> params = new HashMap<>();
        params.put("rulesets", TextUtils.join(",",rulesetIds));
        return AirMap.getClient().get(rulesetBaseUrl, params, new GenericListOkHttpCallback(listener, AirMapRuleset.class));
    }

    static Call getRuleset(String rulesetId, AirMapCallback<AirMapRuleset> callback) {
        String url = String.format(rulesetByIdUrl, rulesetId);

        return AirMap.getClient().get(url, new GenericOkHttpCallback(callback, AirMapRuleset.class));
    }

    static Call getRules(String rulesetId, AirMapCallback<AirMapRuleset> listener) {
        String url = String.format(rulesByIdUrl, rulesetId);
        return AirMap.getClient().get(url, new GenericOkHttpCallback(listener, AirMapRuleset.class));
    }

    static Call getAdvisories(List<String> rulesets, List<Coordinate> geometry, @Nullable Date start, @Nullable Date end, @Nullable Map<String,Object> flightFeatures, AirMapCallback<AirMapAirspaceStatus> listener) {
        Map<String, Object> params = new HashMap<>();
        params.put("rulesets", TextUtils.join(",", rulesets));
        params.put("geometry", "POLYGON(" + Utils.makeGeoString(geometry) + ")");

        if (start != null) {
            params.put("start", Utils.getIso8601StringFromDate(start));
        }

        if (end != null) {
            params.put("end", Utils.getIso8601StringFromDate(end));
        }

        if (flightFeatures != null && !flightFeatures.isEmpty()) {
            try {
                JSONObject jsonObject = new JSONObject();
                for (String key : flightFeatures.keySet()) {
                    jsonObject.put(key, flightFeatures.get(key));
                }
                params.put("flight_features", jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        Call call = AirMap.getClient().postWithJsonBody(advisoriesUrl, params, new GenericOkHttpCallback(listener, AirMapAirspaceStatus.class));
        return call;
    }

    static Call getAdvisories(List<String> rulesets, JSONObject geometry, @Nullable Date start, @Nullable Date end, @Nullable Map<String,Object> flightFeatures, AirMapCallback<AirMapAirspaceStatus> listener) {
        Map<String, Object> params = new HashMap<>();
        params.put("rulesets", TextUtils.join(",", rulesets));
        params.put("geometry", geometry);

        if (start != null) {
            params.put("start", Utils.getIso8601StringFromDate(start));
        }

        if (end != null) {
            params.put("end", Utils.getIso8601StringFromDate(end));
        }

        if (flightFeatures != null && !flightFeatures.isEmpty()) {
            try {
                JSONObject jsonObject = new JSONObject();
                for (String key : flightFeatures.keySet()) {
                    jsonObject.put(key, flightFeatures.get(key));
                }
                params.put("flight_features", jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        Call call = AirMap.getClient().postWithJsonBody(advisoriesUrl, params, new GenericOkHttpCallback(listener, AirMapAirspaceStatus.class));
        return call;
    }

    static Call getEvaluation(List<String> rulesets, JSONObject geometry, @Nullable Map<String,Object> flightFeatures, AirMapCallback<AirMapFlightBriefing> callback) {
        Map<String, Object> params = new HashMap<>();
        params.put("rulesets", TextUtils.join(",", rulesets));
        params.put("geometry", geometry);

        if (flightFeatures != null && !flightFeatures.isEmpty()) {
            try {
                JSONObject jsonObject = new JSONObject();
                for (String key : flightFeatures.keySet()) {
                    jsonObject.put(key, flightFeatures.get(key));
                }
                params.put("flight_features", jsonObject);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        return AirMap.getClient().postWithJsonBody(evaluationUrl, params, new GenericOkHttpCallback(callback, AirMapFlightBriefing.class));
    }
}
