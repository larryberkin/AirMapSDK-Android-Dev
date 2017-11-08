package com.airmap.airmapsdk.networking.services;

import com.airmap.airmapsdk.models.aircraft.AirMapAircraft;
import com.airmap.airmapsdk.models.permits.AirMapPilotPermit;
import com.airmap.airmapsdk.models.pilot.AirMapPilot;
import com.airmap.airmapsdk.networking.callbacks.AirMapCallback;
import com.airmap.airmapsdk.networking.callbacks.GenericListOkHttpCallback;
import com.airmap.airmapsdk.networking.callbacks.GenericOkHttpCallback;
import com.airmap.airmapsdk.networking.callbacks.VoidCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

/**
 * Created by Vansh Gandhi on 6/23/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */
@SuppressWarnings("unused")
class PilotService extends BaseService {

    /**
     * Get a user's profile
     *
     * @param listener The callback that is invoked on success or error
     */
    static Call getPilot(String pilotId, AirMapCallback<AirMapPilot> listener) {
        String url = String.format(pilotByIdUrl, pilotId);
        return AirMap.getClient().get(url, new GenericOkHttpCallback(listener, AirMapPilot.class));
    }

    /**
     * Get the logged in user's profile
     *
     * @param listener The callback that is invoked on success or error
     */
    static Call getAuthenticatedPilot(AirMapCallback<AirMapPilot> listener) {
        return getPilot(AirMap.getUserId(), listener);
    }

    /**
     * Update the user's profile
     *
     * @param pilot    The updated version of the pilot
     * @param listener The callback that is invoked on success or error
     */
    static Call updatePilot(AirMapPilot pilot, AirMapCallback<AirMapPilot> listener) {
        String url = String.format(pilotByIdUrl, pilot.getPilotId());
        return AirMap.getClient().patch(url, pilot.getAsParams(), new GenericOkHttpCallback(listener, AirMapPilot.class));
    }

    /**
     * Update the user's phone number. The updated number will need to be verified
     * {@link #sendVerificationToken(AirMapCallback)}.
     *
     * @param phone    The new phone number
     * @param listener The callback that is invoked on success or error
     */
    static Call updatePhoneNumber(String phone, AirMapCallback<Void> listener) {
        String url = String.format(pilotByIdUrl, AirMap.getUserId());
        Map<String, String> params = new HashMap<>();
        params.put("phone", phone);
        return AirMap.getClient().patch(url, params, new VoidCallback(listener));
    }

    /**
     * Verify the user's phone number
     *
     * @param listener The callback that is invoked on success or error
     */
    static Call sendVerificationToken(AirMapCallback<Void> listener) {
        String url = String.format(pilotSendVerifyUrl, AirMap.getUserId());
        return AirMap.getClient().post(url, new VoidCallback(listener));
    }

    /**
     * Verify that the text message the user received was the correct one
     *
     * @param token    The token that the user received in the text
     * @param listener The callback that is invoked on success or error
     */
    static Call verifyToken(String token, AirMapCallback<Void> listener) {
        String url = String.format(pilotVerifyUrl, AirMap.getUserId());
        JSONObject params = new JSONObject();
        try {
            params.put("token", Integer.valueOf(token));
        } catch (JSONException | NumberFormatException e) {
            e.printStackTrace();
        }
        return AirMap.getClient().postWithJsonBody(url, params, new VoidCallback(listener));
    }

    //Aircraft related requests

    /**
     * Get the authenticated user's aircraft
     *
     * @param listener The callback that is invoked on success or error
     */
    static Call getAircraft(AirMapCallback<List<AirMapAircraft>> listener) {
        String url = String.format(pilotAircraftUrl, AirMap.getUserId());
        return AirMap.getClient().get(url, new GenericListOkHttpCallback(listener, AirMapAircraft.class));
    }

    /**
     * Get a specific aircraft by it's ID
     *
     * @param aircraftId The ID of the aircraft to get
     * @param listener   The callback that is invoked on success or error
     */
    static Call getAircraft(String aircraftId, AirMapCallback<AirMapAircraft> listener) {
        String url = String.format(pilotAircraftByIdUrl, AirMap.getUserId(), aircraftId);
        return AirMap.getClient().get(url, new GenericOkHttpCallback(listener, AirMapAircraft.class));
    }

    /**
     * Create an aircraft for the authenticated user
     *
     * @param aircraft The aircraft to add to the user's profile
     * @param listener The callback that is invoked on success or error
     */
    static Call createAircraft(AirMapAircraft aircraft, AirMapCallback<AirMapAircraft> listener) {
        String url = String.format(pilotAircraftUrl, AirMap.getUserId());
        return AirMap.getClient().post(url, aircraft.getAsParamsPost(), new GenericOkHttpCallback(listener, AirMapAircraft.class));
    }

    /**
     * Update an aircraft for the authenticated user
     *
     * @param aircraft The aircraft with the updated values (The ID must be valid and non-null)
     * @param listener The callback that is invoked on success or error
     */
    static Call updateAircraft(AirMapAircraft aircraft, AirMapCallback<AirMapAircraft> listener) {
        String url = String.format(pilotAircraftByIdUrl, AirMap.getUserId(), aircraft.getAircraftId());
        return AirMap.getClient().patch(url, aircraft.getAsParamsPatch(), new GenericOkHttpCallback(listener, AirMapAircraft.class));
    }

    /**
     * Delete an aircraft
     *
     * @param aircraft The aircraft to delete (The ID must be valid and non-null)
     * @param listener The callback that is invoked on success or error
     */
    static Call deleteAircraft(AirMapAircraft aircraft, AirMapCallback<Void> listener) {
        String url = String.format(pilotAircraftByIdUrl, AirMap.getUserId(), aircraft.getAircraftId());
        return AirMap.getClient().delete(url, new VoidCallback(listener));
    }

    //Permit related requests
    static Call getPermits(AirMapCallback<List<AirMapPilotPermit>> listener) {
        String url = String.format(pilotGetPermitsUrl, AirMap.getUserId());
        return AirMap.getClient().get(url, new GenericListOkHttpCallback(listener, AirMapPilotPermit.class));
    }

    static Call deletePermit(String permitId, AirMapCallback<Void> listener) {
        String url = String.format(pilotDeletePermitUrl, AirMap.getUserId(), permitId);
        return AirMap.getClient().delete(url, new VoidCallback(listener));
    }
}
