package com.airmap.airmapsdk.networking.callbacks;

import com.airmap.airmapsdk.models.AirMapBaseModel;
import com.airmap.airmapsdk.util.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Response;

public class ListFlightsCallback extends GenericListOkHttpCallback {
    public ListFlightsCallback(AirMapCallback listener, Class<? extends AirMapBaseModel> classToInstantiate) {
        super(listener, classToInstantiate);
    }

    @Override
    public void onResponse(Call call, Response response) {
        if (listener == null) {
            return; //Don't need to do anything if no listener was provided
        }
        String jsonString;
        try {
            jsonString = response.body().string();
        } catch (IOException e) {
            Utils.error(listener, e);
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
            Utils.error(listener, response.code(), result);
            return;
        }
        JSONObject dataObject = result.optJSONObject("data");
        if (dataObject == null) {
            Utils.error(listener, response.code(), result);
            return;
        }
        JSONArray jsonArray = dataObject.optJSONArray("results");
        if (jsonArray == null && result.isNull("data")) {
            listener.onSuccess(new ArrayList<>()); //Pretend that there was an array with no items
        } else if (jsonArray == null) {
            Utils.error(listener, response.code(), result); //There was a parsing exception most likely
        } else {
            List<AirMapBaseModel> models = new ArrayList<>(jsonArray.length());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.optJSONObject(i);
                try {
                    AirMapBaseModel model = classToInstantiate.newInstance().constructFromJson(jsonObject);
                    models.add(model);
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }
            listener.onSuccess(models);
        }
    }
}
