package com.airmap.airmapsdk.models.rules;

import com.airmap.airmapsdk.models.AirMapBaseModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.airmap.airmapsdk.util.Utils.optString;

public class AirMapJurisdiction implements Serializable, AirMapBaseModel {

    public enum RegionCategory {
        National("national"), State("state"), County("county"), City("city"), Local("local");

        private String text;

        RegionCategory(String text) {
            this.text = text;
        }

        public static RegionCategory fromString(String text) {
            switch (text.toLowerCase()) {
                case "federal":
                case "national":
                    return National;
                case "state":
                    return State;
                case "county":
                    return County;
                case "city":
                    return City;
                case "local":
                    return Local;
            }

            return null;
        }

        public String toString() {
            return text;
        }

        public int intValue() {
            switch (text.toLowerCase()) {
                case "federal":
                case "national":
                    return 5;
                case "state":
                    return 4;
                case "county":
                    return 3;
                case "city":
                    return 2;
                case "local":
                    return 1;
                default:
                    return 0;
            }
        }
    }

    private int id;
    private String name;
    private AirMapJurisdiction.RegionCategory region;
    private Set<AirMapRuleset> rulesets;

    public AirMapJurisdiction(JSONObject resultJson) {
        constructFromJson(resultJson);
    }

    @Override
    public AirMapBaseModel constructFromJson(JSONObject json) {
        setId(json.optInt("id"));
        setName(optString(json, "name"));
        setRegion(RegionCategory.fromString(optString(json, "region")));

        JSONArray rulesetsJSON = json.optJSONArray("rulesets");
        rulesets = new HashSet<>();

        if (rulesetsJSON != null) {
            for (int i = 0; i < rulesetsJSON.length(); i++) {
                AirMapRuleset ruleset = new AirMapRuleset(rulesetsJSON.optJSONObject(i));
                ruleset.setJurisdiction(this);
                rulesets.add(ruleset);
            }
        }

        return this;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<AirMapRuleset> getRulesets() {
        return rulesets;
    }

    public void setRulesets(Set<AirMapRuleset> rulesets) {
        this.rulesets = rulesets;
    }

    public RegionCategory getRegion() {
        return region;
    }

    public void setRegion(RegionCategory region) {
        this.region = region;
    }

    public void addRuleset(AirMapRuleset ruleset) {
        rulesets.add(ruleset);
    }

    public List<AirMapRuleset> getPickOneRulesets() {
        List<AirMapRuleset> pickOneRulesets = new ArrayList<>();

        for (AirMapRuleset ruleset : rulesets) {
            if (ruleset.getType() == AirMapRuleset.Type.PickOne) {
                pickOneRulesets.add(ruleset);
            }
        }

        return pickOneRulesets;
    }

    public List<AirMapRuleset> getOptionalRulesets() {
        List<AirMapRuleset> optionalRulesets = new ArrayList<>();

        for (AirMapRuleset ruleset : rulesets) {
            if (ruleset.getType() == AirMapRuleset.Type.Optional) {
                optionalRulesets.add(ruleset);
            }
        }

        return optionalRulesets;
    }

    public List<AirMapRuleset> getRequiredRulesets() {
        List<AirMapRuleset> requiredRulesets = new ArrayList<>();

        for (AirMapRuleset ruleset : rulesets) {
            if (ruleset.getType() == AirMapRuleset.Type.Required) {
                requiredRulesets.add(ruleset);
            }
        }

        return requiredRulesets;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof AirMapJurisdiction && ((AirMapJurisdiction) o).id == this.id;
    }
}
