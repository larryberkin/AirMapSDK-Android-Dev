package com.airmap.airmapsdk.networking.services;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.airmap.airmapsdk.models.permits.AirMapAvailablePermit;
import com.airmap.airmapsdk.models.permits.AirMapPilotPermit;
import com.airmap.airmapsdk.networking.callbacks.AirMapCallback;
import com.airmap.airmapsdk.networking.callbacks.GenericListOkHttpCallback;
import com.airmap.airmapsdk.networking.callbacks.GenericOkHttpCallback;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

/**
 * Created by Vansh Gandhi on 7/21/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */
@SuppressWarnings("unused")
class PermitService extends BaseService {
    /**
     * Get permits by permit ID and/or organization ID. At least one of these parameters must be
     * non-null
     *
     * @param permitIds      The IDs of the permit to get
     * @param organizationId The organization of the permit
     * @param listener       The callback that is invoked on success or error
     */
    static Call getPermits(@Nullable List<String> permitIds, @Nullable String organizationId,
                           AirMapCallback<List<AirMapAvailablePermit>> listener) {
        Map<String, String> params = new HashMap<>();
        if (permitIds != null && !permitIds.isEmpty()) {
            params.put("ids", TextUtils.join(",", permitIds));
        }
        params.put("organization_id", organizationId);
        return AirMap.getClient().get(permitBaseUrl, params, new GenericListOkHttpCallback(listener, AirMapAvailablePermit.class));
    }

    /**
     * Apply for a permit
     *
     * @param permit   The permit to apply for
     * @param listener The callback that is invoked on success or error
     */
    static Call applyForPermit(AirMapAvailablePermit permit, AirMapCallback<AirMapPilotPermit> listener) {
        String url = String.format(permitApplyUrl, permit.getId());
        return AirMap.getClient().postWithJsonBody(url, permit.getAsParams(), new GenericOkHttpCallback(listener, AirMapPilotPermit.class));
    }
}
