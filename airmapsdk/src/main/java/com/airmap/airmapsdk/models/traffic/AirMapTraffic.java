package com.airmap.airmapsdk.models.traffic;

import com.airmap.airmapsdk.models.AirMapBaseModel;
import com.airmap.airmapsdk.models.Coordinate;

import org.json.JSONObject;

import java.io.Serializable;
import java.util.Date;

/**
 * Created by Vansh Gandhi on 6/15/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */
@SuppressWarnings("unused")
public class AirMapTraffic implements Serializable, AirMapBaseModel {
    /**
     * SituationalAwareness indicates traffic that is within 10 miles
     * Alert indicates traffic that is headed towards the tracked aircraft will be within 1000
     * meters in the next 30 seconds
     */
    public enum TrafficType {
        Alert,   //Traffic that is headed towards the tracked aircraft and will be within 1000 meters in 30 seconds
        SituationalAwareness //Traffic that is within 10 miles
    }

    private String id;
    private double direction;
    private double altitude;
    private int groundSpeedKt;
    private int trueHeading;
    private Coordinate coordinate;
    private Coordinate initialCoordinate;
    private boolean showAlert;
    private AirMapTrafficProperties properties;
    private TrafficType trafficType;
    private Date timestamp = new Date();
    private Date recordedTime = new Date();
    private Date createdAt = new Date();

    private Date incomingTime; //Set manually

    public AirMapTraffic(JSONObject trafficJson) {
        constructFromJson(trafficJson);
    }

    public Date getIncomingTime() {
        return incomingTime;
    }

    public AirMapTraffic() {

    }

    @Override
    public AirMapTraffic constructFromJson(JSONObject json) {
        if (json != null) {
            setId(json.optString("id"));
            setDirection(json.optDouble("direction", -1));
            setAltitude(json.optDouble("altitude"));
            setGroundSpeedKt(json.optInt("ground_speed_kts", -1));
            setTrueHeading(json.optInt("true_heading", -1));
            setProperties(new AirMapTrafficProperties(json.optJSONObject("properties")));
            setTimestamp(new Date(json.optLong("timestamp")));
            setRecordedTime(new Date(json.optLong("recorded_time") * 1000)); //recorded time comes back in seconds not millis
            setCoordinate(new Coordinate(json.optDouble("latitude", 0), json.optDouble("longitude", 0)));
            setInitialCoordinate(getCoordinate());
            incomingTime = new Date();
        }
        return this;
    }

    public String getId() {
        return id;
    }

    public AirMapTraffic setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * @return The angle in degrees of the location of the traffic in relation to the current flight
     */
    public double getDirection() {
        return direction;
    }

    public AirMapTraffic setDirection(double direction) {
        this.direction = direction;
        return this;
    }

    public double getAltitude() {
        return altitude;
    }

    public AirMapTraffic setAltitude(double altitude) {
        this.altitude = altitude;
        return this;
    }

    /**
     * @return Ground speed in knots
     */
    public int getGroundSpeedKt() {
        return groundSpeedKt;
    }

    public AirMapTraffic setGroundSpeedKt(int groundSpeedKt) {
        this.groundSpeedKt = groundSpeedKt;
        return this;
    }

    /**
     * @return The heading of the traffic (the direction in which it is travelling)
     */
    public int getTrueHeading() {
        return trueHeading;
    }

    public AirMapTraffic setTrueHeading(int trueHeading) {
        this.trueHeading = trueHeading;
        return this;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public AirMapTraffic setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
        return this;
    }

    public Date getRecordedTime() {
        return recordedTime;
    }

    public AirMapTraffic setRecordedTime(Date recordedTime) {
        this.recordedTime = recordedTime;
        return this;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public AirMapTraffic setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public AirMapTraffic setCoordinate(Coordinate coordinate) {
        this.coordinate = coordinate;
        return this;
    }

    public Coordinate getInitialCoordinate() {
        return initialCoordinate;
    }

    public AirMapTraffic setInitialCoordinate(Coordinate initialCoordinate) {
        this.initialCoordinate = initialCoordinate;
        return this;
    }

    public boolean shouldShowAlert() {
        return showAlert;
    }

    public AirMapTraffic setShowAlert(boolean showAlert) {
        this.showAlert = showAlert;
        return this;
    }

    public AirMapTrafficProperties getProperties() {
        return properties;
    }

    public AirMapTraffic setProperties(AirMapTrafficProperties properties) {
        this.properties = properties;
        return this;
    }

    public TrafficType getTrafficType() {
        return trafficType;
    }

    public AirMapTraffic setTrafficType(TrafficType trafficType) {
        this.trafficType = trafficType;
        return this;
    }

    /**
     * Comparison based on ID
     */
    @Override
    public boolean equals(Object o) {
        return o instanceof AirMapTraffic && ((AirMapTraffic) o).getId().equals(getId());
    }
}
