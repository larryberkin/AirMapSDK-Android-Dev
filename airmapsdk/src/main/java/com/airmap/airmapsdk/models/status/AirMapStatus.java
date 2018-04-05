package com.airmap.airmapsdk.models.status;

import android.text.TextUtils;

import com.airmap.airmapsdk.models.AirMapBaseModel;
import com.airmap.airmapsdk.models.Coordinate;
import com.airmap.airmapsdk.networking.services.MappingService;
import com.airmap.airmapsdk.util.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.airmap.airmapsdk.util.Utils.optString;

@Deprecated
public class AirMapStatus implements Serializable, AirMapBaseModel {

    private AirMapColor advisoryColor;
    private int maxSafeRadius;
    private List<AirMapStatusAdvisory> advisories;

    /**
     * Initialize an AirMapStatus from JSON
     *
     * @param statusJson A JSON representation of an AirMapStatus
     */
    public AirMapStatus(JSONObject statusJson) {
        constructFromJson(statusJson);
    }

    /**
     * Initialize an AirMapStatus with default values
     */
    public AirMapStatus() {

    }

    @Override
    public AirMapStatus constructFromJson(JSONObject json) {
        if (json != null) {
            List<AirMapStatusAdvisory> advisories = new ArrayList<>();
            JSONArray advisoriesJson = json.optJSONArray("advisories");
            for (int i = 0; advisoriesJson != null && i < advisoriesJson.length(); i++) {
                advisories.add(new AirMapStatusAdvisory(advisoriesJson.optJSONObject(i)));
            }
            setAdvisories(advisories);
            setMaxSafeRadius(json.optInt("max_safe_distance"));
            setAdvisoryColor(AirMapColor.fromString(optString(json, "advisory_color")));
        }
        return this;
    }

    /**
     * Serializes the necessary information for the Status API. Only serializes info which is
     * shared between point, path, and polygon. Other information must be added separately
     *
     * @param coordinate   A position with latitude and longitude
     * @param types        Airspace types to include in the calculation and response
     * @param ignoredTypes Airspace types to ignore in the calculation and repose
     * @param weather      If set to true, shows current weather conditions in the response
     * @param date         Date for planned flight
     * @return A map of all the required fields
     */
    public static Map<String, String> getAsParams(Coordinate coordinate,
                                                  List<MappingService.AirMapAirspaceType> types,
                                                  List<MappingService.AirMapAirspaceType> ignoredTypes,
                                                  boolean weather, Date date) {
        Map<String, String> params = new HashMap<>();
        params.put("latitude", String.valueOf(coordinate.getLatitude()));
        params.put("longitude", String.valueOf(coordinate.getLongitude()));
        if (types != null && !types.isEmpty()) {
            String typesString = TextUtils.join(",", types); //Will call toString on the AirMapAirspaceType, which will return the appropriate string
            params.put("types", typesString);
        }
        if (ignoredTypes != null && !ignoredTypes.isEmpty()) {
            String ignoredTypesString = TextUtils.join(",", types); //Will call toString on the AirMapAirspaceType, which will return the appropriate string
            params.put("ignored_types", ignoredTypesString);
        }
        params.put("weather", String.valueOf(weather));
        if (date != null) {
            params.put("datetime", Utils.getIso8601StringFromDate(date));
        }
        return params;
    }

    public int getMaxSafeRadius() {
        return maxSafeRadius;
    }

    public AirMapStatus setMaxSafeRadius(int maxSafeRadius) {
        this.maxSafeRadius = maxSafeRadius;
        return this;
    }

    public AirMapColor getAdvisoryColor() {
        return advisoryColor;
    }

    public AirMapStatus setAdvisoryColor(AirMapColor advisoryColor) {
        this.advisoryColor = advisoryColor;
        return this;
    }

    public List<AirMapStatusAdvisory> getAdvisories() {
        return advisories;
    }

    public AirMapStatus setAdvisories(List<AirMapStatusAdvisory> advisories) {
        this.advisories = advisories;
        return this;
    }
}
