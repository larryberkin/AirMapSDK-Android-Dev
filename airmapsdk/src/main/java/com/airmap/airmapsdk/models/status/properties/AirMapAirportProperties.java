package com.airmap.airmapsdk.models.status.properties;

import com.airmap.airmapsdk.models.AirMapBaseModel;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.airmap.airmapsdk.util.Utils.optString;

@SuppressWarnings("unused")
public class AirMapAirportProperties implements Serializable, AirMapBaseModel {
    private String icao;
    private String phone;
    private String ownership;
    private boolean ifr;
    private String iata;
    private boolean paved;
    private boolean tower;
    private List<AirMapAirportRunway> runways;
    private int elevation;
    private String icaoCountry;
    private int longestRunway;


    /**
     * Initialize an AirMapAirportProperties from JSON
     *
     * @param propertiesJson The JSON representation
     */
    public AirMapAirportProperties(JSONObject propertiesJson) {
        constructFromJson(propertiesJson);
    }

    public AirMapAirportProperties() {

    }

    @Override
    public AirMapAirportProperties constructFromJson(JSONObject json) {
        if (json != null) {
            setIcao(optString(json, "icao"));
            setPhone(optString(json, "phone"));
            setOwnership(optString(json, "ownership"));
            setIfr(json.optBoolean("ifr"));
            setIata(optString(json, "iata"));
            setPaved(json.optBoolean("paved"));
            setTower(json.optBoolean("tower"));
            setElevation(json.optInt("elevation"));
            setIcaoCountry(optString(json, "icao_country"));
            setLongestRunway(json.optInt("longestRunway"));
            List<AirMapAirportRunway> runways = new ArrayList<>();
            JSONArray runwaysArray = json.optJSONArray("runways");
            for (int i = 0; runwaysArray != null && i < runwaysArray.length(); i++) {
                AirMapAirportRunway runway = new AirMapAirportRunway(runwaysArray.optJSONObject(i));
                runways.add(runway);
            }
            setRunways(runways);
        }
        return this;
    }

    public String getIcao() {
        return icao;
    }

    public AirMapAirportProperties setIcao(String icao) {
        this.icao = icao;
        return this;
    }

    public String getPhone() {
        return phone;
    }

    public AirMapAirportProperties setPhone(String phone) {
        this.phone = phone;
        return this;
    }

    public String getOwnership() {
        return ownership;
    }

    public AirMapAirportProperties setOwnership(String ownership) {
        this.ownership = ownership;
        return this;
    }

    public boolean isIfr() {
        return ifr;
    }

    public AirMapAirportProperties setIfr(boolean ifr) {
        this.ifr = ifr;
        return this;
    }

    public String getIata() {
        return iata;
    }

    public AirMapAirportProperties setIata(String iata) {
        this.iata = iata;
        return this;
    }

    public boolean isPaved() {
        return paved;
    }

    public AirMapAirportProperties setPaved(boolean paved) {
        this.paved = paved;
        return this;
    }

    public boolean isTower() {
        return tower;
    }

    public AirMapAirportProperties setTower(boolean tower) {
        this.tower = tower;
        return this;
    }

    public List<AirMapAirportRunway> getRunways() {
        return runways;
    }

    public AirMapAirportProperties setRunways(List<AirMapAirportRunway> runways) {
        this.runways = runways;
        return this;
    }

    public int getElevation() {
        return elevation;
    }

    public AirMapAirportProperties setElevation(int elevation) {
        this.elevation = elevation;
        return this;
    }

    public String getIcaoCountry() {
        return icaoCountry;
    }

    public AirMapAirportProperties setIcaoCountry(String icaoCountry) {
        this.icaoCountry = icaoCountry;
        return this;
    }

    public int getLongestRunway() {
        return longestRunway;
    }

    public AirMapAirportProperties setLongestRunway(int longestRunway) {
        this.longestRunway = longestRunway;
        return this;
    }

    /**
     * Comparison based on ICAO
     */
    @Override
    public boolean equals(Object o) {
        return o instanceof AirMapAirportProperties && getIcao().equals(((AirMapAirportProperties) o).getIcao());
    }

    @Override
    public String toString() {
        return getIcao();
    }
}
