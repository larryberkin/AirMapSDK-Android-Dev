package com.airmap.airmapsdk;

import org.json.JSONArray;
import org.json.JSONObject;

import static com.airmap.airmapsdk.util.Utils.optString;

/**
 * An Exception wrapper class
 */
@SuppressWarnings("unused")
public class AirMapException extends Exception {

    private static final String UNKNOWN_ERROR_MESSAGE = "Unknown error";

    private int errorCode;
    private String errorMessage = "";
    private String detailedMessage = "";

    public AirMapException(String message) {
        errorMessage = message;
    }

    public AirMapException(int code, String message) {
        errorCode = code;
        errorMessage = message;
    }

    public AirMapException(int code, JSONObject json) {
        errorCode = code;

        if (code == 400) {
            detailedMessage = getBadRequestErrorMessage(json); //errorMessage is set in the function
        } else if (code > 400 && code < 500) {
            errorMessage = get400sErrorMessage(json);
            detailedMessage = errorMessage;
        } else if (code >= 500 && code < 600) {
            errorMessage = get500sErrorMessage(json);
            detailedMessage = errorMessage;
        } else {
            errorMessage = UNKNOWN_ERROR_MESSAGE;
        }
    }

    public int getErrorCode() {
        return errorCode;
    }

    @Override
    public String getMessage() {
        return errorMessage;
    }

    public String getDetailedMessage() {
        return detailedMessage;
    }

    @Override
    public String getLocalizedMessage() {
        return getMessage();
    }

    private String getBadRequestErrorMessage(JSONObject json) {
        if (json == null) {
            return UNKNOWN_ERROR_MESSAGE;
        }
        JSONObject data = json.optJSONObject("data");
        if (data == null) {
            return UNKNOWN_ERROR_MESSAGE;
        }
        errorMessage = optString(data, "message");
        JSONArray errors = data.optJSONArray("errors");
        if (errors == null) {
            return UNKNOWN_ERROR_MESSAGE;
        }
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < errors.length(); i++) {
            JSONObject error = errors.optJSONObject(i);
            builder.append(optString(error, "name"));
            builder.append(": ");
            builder.append(optString(error, "message"));
            builder.append(", ");
        }
        if (builder.length() > 2) { //In case no errors were listed in the array
            builder.deleteCharAt(builder.length() - 1); //Get rid of trailing space
            builder.deleteCharAt(builder.length() - 1); //Get rid of trailing comma
        }

        return builder.toString();
    }

    private String get400sErrorMessage(JSONObject json) {
        if (json == null) {
            return UNKNOWN_ERROR_MESSAGE;
        }
        JSONObject data = json.optJSONObject("data");
        if (data == null) {
            if (json.has("message")) {
                return optString(json, "message");
            }
            return UNKNOWN_ERROR_MESSAGE;
        }
        return optString(data, "message");
    }

    private String get500sErrorMessage(JSONObject json) {
        if (json == null) {
            return UNKNOWN_ERROR_MESSAGE;
        }
        return optString(json, "message");
    }
}
