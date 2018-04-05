package com.airmap.airmapsdk.models.flight;

import com.airmap.airmapsdk.models.AirMapBaseModel;
import com.airmap.airmapsdk.models.rules.AirMapAuthorization;
import com.airmap.airmapsdk.models.rules.AirMapRuleset;
import com.airmap.airmapsdk.models.rules.AirMapValidation;
import com.airmap.airmapsdk.models.status.AirMapAirspaceStatus;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.airmap.airmapsdk.util.Utils.getDateFromIso8601String;
import static com.airmap.airmapsdk.util.Utils.optString;

public class AirMapFlightBriefing implements Serializable, AirMapBaseModel {

    private String color;
    private Date createdAt;
    private List<AirMapRuleset> rulesets;
    private AirMapAirspaceStatus airspace;
    private List<AirMapValidation> validations;
    private List<AirMapAuthorization> authorizations;

    public AirMapFlightBriefing() {

    }

    public AirMapFlightBriefing(JSONObject jsonObject) {
        constructFromJson(jsonObject);
    }

    @Override
    public AirMapBaseModel constructFromJson(JSONObject json) {
        if (json != null) {
            color = optString(json, "color");
            createdAt = getDateFromIso8601String(optString(json, "created_at"));

            rulesets = new ArrayList<>();
            if (json.has("rulesets")) {
                JSONArray rulesetsArray = json.optJSONArray("rulesets");
                for (int i = 0; i < rulesetsArray.length(); i++) {
                    rulesets.add(new AirMapRuleset(rulesetsArray.optJSONObject(i)));
                }
            }

            if (json.has("airspace")) {
                airspace = new AirMapAirspaceStatus(json.optJSONObject("airspace"));
            }

            validations = new ArrayList<>();
            if (json.has("validations")) {
                JSONArray validationsArray = json.optJSONArray("validations");
                for (int i = 0; validationsArray != null && i < validationsArray.length(); i++) {
                    validations.add(new AirMapValidation(validationsArray.optJSONObject(i)));
                }
            }

            authorizations = new ArrayList<>();
            if (json.has("authorizations")) {
                JSONArray authorizationsArray = json.optJSONArray("authorizations");
                for (int i = 0; authorizationsArray != null && i < authorizationsArray.length(); i++) {
                    authorizations.add(new AirMapAuthorization(authorizationsArray.optJSONObject(i)));
                }
            }
        }
        return this;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public List<AirMapRuleset> getRulesets() {
        return rulesets;
    }

    public void setRulesets(List<AirMapRuleset> rulesets) {
        this.rulesets = rulesets;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public AirMapAirspaceStatus getAirspace() {
        return airspace;
    }

    public void setAirspace(AirMapAirspaceStatus airspace) {
        this.airspace = airspace;
    }

    public List<AirMapValidation> getValidations() {
        return validations;
    }

    public void setValidations(List<AirMapValidation> validations) {
        this.validations = validations;
    }

    public List<AirMapAuthorization> getAuthorizations() {
        return authorizations;
    }

    public void setAuthorizations(List<AirMapAuthorization> authorizations) {
        this.authorizations = authorizations;
    }
}
