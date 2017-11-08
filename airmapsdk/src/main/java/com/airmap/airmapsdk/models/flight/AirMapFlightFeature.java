package com.airmap.airmapsdk.models.flight;

import android.support.annotation.StringRes;

import com.airmap.airmapsdk.R;
import com.airmap.airmapsdk.models.AirMapBaseModel;
import com.airmap.airmapsdk.models.rules.AirMapRule;

import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by collin@airmap.com on 5/22/17.
 */

public class AirMapFlightFeature implements Serializable, AirMapBaseModel {

    public InputType getInputType() {
        return inputType;
    }

    public void setInputType(InputType inputType) {
        this.inputType = inputType;
    }

    public enum InputType {
        Double,Boolean, String, Info, Unknown;

        private static InputType fromText(String text) {
            switch (text.toLowerCase()) {
                case "float":
                    return Double;
                case "bool":
                    return Boolean;
                case "string":
                    return String;
                case "info":
                    return Info;
                default:
                    return Unknown;
            }
        }

        public int value() {
            switch (this) {
                case Info:
                    return 0;
                case String:
                    return 1;
                case Double:
                    return 2;
                case Boolean:
                    return 3;
                default:
                    return 4;
            }
        }
    }

    public enum MeasurementType {
        Speed, Weight, Distance, Unknown;

        private static MeasurementType fromText(String text) {
            switch (text.toLowerCase()) {
                case "speed":
                    return Speed;
                case "weight":
                    return Weight;
                case "distance":
                    return Distance;
                default:
                    return Unknown;
            }
        }

        @StringRes
        public int getStringRes() {
            switch (this) {
                case Speed:
                    return R.string.flight_feature_speed;
                case Weight:
                    return R.string.flight_feature_weight;
                case Distance:
                    return R.string.flight_feature_distance;
                case Unknown:
                default:
                    return -1;
            }
        }
    }

    public enum MeasurementUnit {
        Kilograms, Meters, MetersPerSecond, Unknown;

        private static MeasurementUnit fromText(String text) {
            switch (text.toLowerCase()) {
                case "kilograms":
                    return Kilograms;
                case "meters":
                    return Meters;
                case "meters_per_sec":
                    return MetersPerSecond;
                default:
                    return Unknown;
            }
        }
    }

    private int id;
    private String flightFeature;
    private String description;
    private InputType inputType;
    private MeasurementType measurementType;
    private MeasurementUnit measurementUnit;
    private boolean isCalculated;

    public AirMapFlightFeature(String flightFeature) {
        setFlightFeature(flightFeature);
    }

    public AirMapFlightFeature(JSONObject jsonObject) {
        constructFromJson(jsonObject);
    }

    @Override
    public AirMapBaseModel constructFromJson(JSONObject json) {
        setFlightFeature(json.optString("flight_feature"));
        setInputType(InputType.fromText(json.optString("input_type")));
        setDescription(json.optString("description"));
        setMeasurementType(MeasurementType.fromText(json.optString("measurement_type")));
        setMeasurementUnit(MeasurementUnit.fromText(json.optString("measurement_unit")));
        setCalculated(json.optBoolean("is_calculated"));
        return this;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFlightFeature() {
        return flightFeature;
    }

    public void setFlightFeature(String flightFeature) {
        this.flightFeature = flightFeature;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public MeasurementType getMeasurementType() {
        return measurementType;
    }

    public void setMeasurementType(MeasurementType measurementType) {
        this.measurementType = measurementType;
    }

    public MeasurementUnit getMeasurementUnit() {
        return measurementUnit;
    }

    public void setMeasurementUnit(MeasurementUnit measurementUnit) {
        this.measurementUnit = measurementUnit;
    }

    public boolean isCalculated() {
        return isCalculated;
    }

    public void setCalculated(boolean calculated) {
        isCalculated = calculated;
    }

    @Override
    public String toString() {
        return flightFeature;
    }

    @Override
    public boolean equals(Object o) {
        return o != null && o instanceof AirMapFlightFeature && ((AirMapFlightFeature) o).getFlightFeature().equals(this.flightFeature);
    }

    @Override
    public int hashCode() {
        return flightFeature.hashCode();
    }

    public boolean isAltitudeFeature() {
        return (flightFeature.contains("agl") || flightFeature.contains("altitude")) && getInputType() == InputType.Double;
    }
}
