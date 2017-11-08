package com.airmap.airmapsdk.models.flight;

import com.airmap.airmapsdk.models.AirMapBaseModel;
import com.airmap.airmapsdk.models.aircraft.AirMapAircraft;
import com.airmap.airmapsdk.models.Coordinate;
import com.airmap.airmapsdk.models.pilot.AirMapPilot;
import com.airmap.airmapsdk.models.shapes.AirMapGeometry;
import com.airmap.airmapsdk.models.shapes.AirMapPath;
import com.airmap.airmapsdk.models.shapes.AirMapPoint;
import com.airmap.airmapsdk.models.shapes.AirMapPolygon;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static com.airmap.airmapsdk.util.Utils.getDateFromIso8601String;
import static com.airmap.airmapsdk.util.Utils.getIso8601StringFromDate;

/**
 * Created by Vansh Gandhi on 6/15/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */
@SuppressWarnings("unused")
public class AirMapFlight implements Serializable, AirMapBaseModel {

    public enum AirMapFlightGeometryType {
        Point("point"), Path("path"), Polygon("polygon");

        AirMapFlightGeometryType(String text) {
            this.text = text;
        }

        private String text;

        private static AirMapFlightGeometryType fromString(String text) {
            switch (text) {
                case "path":
                    return Path;
                case "polygon":
                    return Polygon;
                case "point":
                default:
                    return Point;
            }
        }

        @Override
        public String toString() {
            return text;
        }
    }

    private String flightId;
    private String flightPlanId;
    private String pilotId;
    private AirMapPilot pilot;
    private Coordinate coordinate;
    private double maxAltitude;
    private AirMapAircraft aircraft;
    private String aircraftId;
    private Date createdAt;
    private Date startsAt;
    private Date endsAt;
    private String country;
    private String state;
    private String city;
    private double buffer;
    private boolean isPublic;
    private boolean notify;
    private List<String> permitIds;
    private List<AirMapFlightStatus> statuses;
    private AirMapGeometry geometry;

    public AirMapFlight(JSONObject flightJson) {
        constructFromJson(flightJson);
    }

    public AirMapFlight() {
        statuses = new ArrayList<>();
        permitIds = new ArrayList<>();
    }

    @Override
    public AirMapFlight constructFromJson(JSONObject json) {
        if (json != null) {
            setFlightId(json.optString("id"));
            setFlightPlanId(json.optString("flight_plan_id"));
            setCoordinate(new Coordinate(json.optDouble("latitude", 0), json.optDouble("longitude", 0)));
            setMaxAltitude(json.optDouble("max_altitude"));
            setCity(json.optString("city"));
            setState(json.optString("state"));
            setCountry(json.optString("country"));
            setNotify(json.optBoolean("notify"));
            setPilotId(json.optString("pilot_id"));
            setPilot(new AirMapPilot(json.optJSONObject("pilot")));
            setAircraftId(json.optString("aircraft_id", null));
            setAircraft(new AirMapAircraft(json.optJSONObject("aircraft")));
            setPublic(json.optBoolean("public"));
            setBuffer(json.optDouble("buffer"));
            setGeometry(AirMapGeometry.getGeometryFromGeoJSON(json.optJSONObject("geometry")));
            statuses = new ArrayList<>();
            permitIds = new ArrayList<>();
            JSONArray statusesJson = json.optJSONArray("statuses");
            for (int i = 0; statusesJson != null && i < statusesJson.length(); i++) {
                statuses.add(new AirMapFlightStatus(statusesJson.optJSONObject(i)));
            }

            JSONArray permitIdsJson = json.optJSONArray("permits");
            for (int i = 0; permitIdsJson != null && i < permitIdsJson.length(); i++) {
                addPermitId(permitIdsJson.optString(i));
            }

            //Created at
            if (json.has("created_at")) {
                setCreatedAt(getDateFromIso8601String(json.optString("created_at")));
            } else if (json.has("creation_date")) {
                setCreatedAt(getDateFromIso8601String(json.optString("creation_date")));
            }
            setStartsAt(getDateFromIso8601String(json.optString("start_time")));
            setEndsAt(getDateFromIso8601String(json.optString("end_time")));
        }
        return this;
    }

    /**
     * Turn the AirMapFlight into a Map of it's fields and values for easy use with web calls
     *
     * @return The AirMapFlight encoded as a Map
     */
    public JSONObject getAsParams() {
        Map<String, Object> params = new HashMap<>();

        if (getGeometry() instanceof AirMapPolygon) {
            params.put("geometry", getGeometry().toString());
            params.put("latitude", ((AirMapPolygon) getGeometry()).getCoordinates().get(0).getLatitude());
            params.put("longitude", ((AirMapPolygon) getGeometry()).getCoordinates().get(0).getLongitude());
        } else if (getGeometry() instanceof AirMapPath) {
            params.put("geometry", getGeometry().toString());
            params.put("latitude", ((AirMapPath) getGeometry()).getCoordinates().get(0).getLatitude());
            params.put("longitude", ((AirMapPath) getGeometry()).getCoordinates().get(0).getLongitude());
            params.put("buffer", getBuffer());
        } else {
            params.put("latitude", coordinate.getLatitude());
            params.put("longitude", coordinate.getLongitude());
            params.put("buffer", getBuffer());
        }

        params.put("max_altitude", getMaxAltitude());
        if (getAircraftId() != null && !getAircraftId().isEmpty()) {
            params.put("aircraft_id", getAircraftId());
        } else if (getAircraft() != null && getAircraft().getAircraftId() != null) {
            params.put("aircraft_id", getAircraft().getAircraftId());
        }
        if (getStartsAt() != null && getStartsAt().after(new Date()) && getIso8601StringFromDate(getStartsAt()) != null) {
            params.put("start_time", getIso8601StringFromDate(getStartsAt()));
        } else {
            params.put("start_time", "now");
        }
        params.put("end_time", getIso8601StringFromDate(getEndsAt()));
        params.put("public", isPublic());
        params.put("notify", shouldNotify());
        Iterator<Map.Entry<String, Object>> iterator = params.entrySet().iterator();
        while (iterator.hasNext()) { //Remove any null values
            Map.Entry<String, Object> entry = iterator.next();
            if (entry.getValue() == null || entry.getValue().equals("null")) {
                iterator.remove();
            }
        }
        JSONObject object = new JSONObject(params);
        if (getPermitIds() != null && !getPermitIds().isEmpty()) {
            JSONArray array = new JSONArray(getPermitIds());
            try {
                object.put("permits", array);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return object;
    }

    public AirMapFlightGeometryType getGeometryType() {
        if (geometry instanceof AirMapPath) {
            return AirMapFlightGeometryType.Path;
        } else if (geometry instanceof AirMapPolygon) {
            return AirMapFlightGeometryType.Polygon;
        } else {
            return AirMapFlightGeometryType.Point;
        }
    }

    /**
     * @param coordinates The coordinates to make a polygon from
     * @return A GeoJSON formatted String
     */
    public static String getPolygonString(List<Coordinate> coordinates) {
        return new AirMapPolygon(coordinates).toString();
    }

    /**
     * @param coordinates The coordinates to make a line of
     * @return a GeoJSON formatted String
     */
    public static String getLineString(List<Coordinate> coordinates) {
        return new AirMapPath(coordinates).toString();
    }

    /**
     * @param coordinate The coordinate to make the WKT string for
     * @return a GeoJSON formatted String
     */
    public static String getPointString(Coordinate coordinate) {
        return new AirMapPoint(coordinate).toString();
    }

    /**
     * Determines whether the current AirMapFlight is valid or not
     *
     * @return The validity of the flight
     */
    public boolean isValid() {
        return getFlightId() != null;
    }

    /**
     * Determines whether the AirMapFlight is a currently active flight
     *
     * @return Whether the flight is current or not
     */
    public boolean isActive() {
        Date now = new Date();
        return startsAt != null && endsAt != null && now.after(startsAt) && now.before(endsAt);
    }

    public String getFlightId() {
        return flightId;
    }

    public AirMapFlight setFlightId(String flightId) {
        this.flightId = flightId;
        return this;
    }

    public String getFlightPlanId() {
        return flightPlanId;
    }

    public void setFlightPlanId(String flightPlanId) {
        this.flightPlanId = flightPlanId;
    }

    public String getPilotId() {
        return pilotId;
    }

    public AirMapFlight setPilotId(String pilotId) {
        this.pilotId = pilotId;
        return this;
    }

    public AirMapPilot getPilot() {
        return pilot;
    }

    public AirMapFlight setPilot(AirMapPilot pilot) {
        this.pilot = pilot;
        return this;
    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public AirMapFlight setCoordinate(Coordinate coordinate) {
        this.coordinate = coordinate;
        return this;
    }

    /**
     * @return maxAltitude of the flight in meters
     */
    public double getMaxAltitude() {
        return maxAltitude;
    }

    /**
     * @param maxAltitude Altitude of the flight in meters
     */
    public AirMapFlight setMaxAltitude(double maxAltitude) {
        this.maxAltitude = maxAltitude;
        return this;
    }

    /**
     * @return buffer, in meters
     */
    public double getBuffer() {
        return buffer;
    }

    /**
     * @param buffer buffer, in meters
     */
    public AirMapFlight setBuffer(double buffer) {
        this.buffer = buffer;
        return this;
    }

    public String getAircraftId() {
        return aircraftId;
    }

    public AirMapFlight setAircraftId(String aircraftId) {
        this.aircraftId = aircraftId;
        return this;
    }

    public AirMapAircraft getAircraft() {
        return aircraft;
    }

    public AirMapFlight setAircraft(AirMapAircraft aircraft) {
        this.aircraft = aircraft;
        return this;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public AirMapFlight setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public Date getStartsAt() {
        return startsAt;
    }

    public AirMapFlight setStartsAt(Date startsAt) {
        this.startsAt = startsAt;
        return this;
    }

    public Date getEndsAt() {
        return endsAt;
    }

    public AirMapFlight setEndsAt(Date endsAt) {
        this.endsAt = endsAt;
        return this;
    }

    public String getCountry() {
        return country;
    }

    public AirMapFlight setCountry(String country) {
        this.country = country;
        return this;
    }

    public String getState() {
        return state;
    }

    public AirMapFlight setState(String state) {
        this.state = state;
        return this;
    }

    public String getCity() {
        return city;
    }

    public AirMapFlight setCity(String city) {
        this.city = city;
        return this;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public AirMapFlight setPublic(boolean aPublic) {
        isPublic = aPublic;
        return this;
    }

    public boolean shouldNotify() {
        return notify;
    }

    public AirMapFlight setNotify(boolean notify) {
        this.notify = notify;
        return this;
    }

    public List<String> getPermitIds() {
        return permitIds;
    }

    public AirMapFlight setPermitIds(ArrayList<String> permitIds) {
        this.permitIds = permitIds;
        return this;
    }

    /**
     * @param id A PilotPermit Id (should start with "permit_application")
     */
    public AirMapFlight addPermitId(String id) {
        permitIds.add(id);
        return this;
    }

    public List<AirMapFlightStatus> getStatuses() {
        return statuses;
    }

    public AirMapFlight setStatuses(ArrayList<AirMapFlightStatus> statuses) {
        this.statuses = statuses;
        return this;
    }

    public AirMapGeometry getGeometry() {
        return geometry;
    }

    public AirMapFlight setGeometry(AirMapGeometry geometry) {
        this.geometry = geometry;
        return this;
    }

    /**
     * Comparison based on ID
     */
    @Override
    public boolean equals(Object o) {
        return o instanceof AirMapFlight && getFlightId().equals(((AirMapFlight) o).getFlightId());
    }
}