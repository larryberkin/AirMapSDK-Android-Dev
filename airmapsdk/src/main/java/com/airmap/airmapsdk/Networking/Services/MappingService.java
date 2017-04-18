package com.airmap.airmapsdk.networking.services;

import android.support.annotation.Nullable;
import android.support.annotation.StringRes;

import com.airmap.airmapsdk.R;

import java.io.Serializable;
import java.util.List;

/**
 * Created by Vansh Gandhi on 7/7/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */
@SuppressWarnings("unused")
public class MappingService extends BaseService {

    public enum AirMapLayerType implements Serializable {
        TFRS("tfrs", R.string.tile_layer_tfr_faa),
        Wildfires("wildfires", R.string.tile_layer_wildfires),
        Fires("fires", R.string.tile_layer_fires),
        Emergencies("emergencies", R.string.tile_layer_emergencies),
        Prohibited("sua_prohibited", R.string.tile_layer_prohibited),
        Restricted("sua_restricted", R.string.tile_layer_restricted_airspace),
        NationalParks("national_parks", R.string.tile_layer_national_parks),
        NOAA("noaa", R.string.tile_layer_noaa),
        Schools("schools", R.string.tile_layer_schools),
        Hospitals("hospitals", R.string.tile_layer_hospitals),
        Heliports("heliports", R.string.tile_layer_heliports),
        PowerPlants("power_plants", R.string.tile_layer_power_plants),
        AirportsCommercial("airports_commercial,airports_commercial_private", R.string.tile_layer_airports),
        AirportsRecreational("airports_recreational,airports_recreational_private", R.string.tile_layer_airports),
        AirportsCommercialPrivate("airports_commercial_private", R.string.tile_layer_private_airports),
        AirportsRecreationalPrivate("airports_recreational_private", R.string.tile_layer_private_airports),
        ClassB("class_b", R.string.tile_layer_class_b),
        ClassC("class_c", R.string.tile_layer_class_c),
        ClassD("class_d", R.string.tile_layer_class_d),
        ClassE("class_e0", R.string.tile_layer_class_e),
        HazardAreas("hazard_areas", R.string.tile_layer_hazard_areas),
        AerialRecreationalAreas("aerial_recreational_areas", R.string.tile_layer_aerial_rec_areas),
        Cities("cities", R.string.tile_layer_cities),
        Custom("custom", R.string.tile_layer_custom),
        Prisons("prisons", R.string.tile_layer_prisons),
        Universities("universities", R.string.tile_layer_universities),
        Other("aerial_recreational_areas,custom,hazard_areas,hospitals,power_plants,prisons,schools,universities,cities", R.string.tile_layer_other_cautionary_areas);

        private final String text;
        private final int title;

        AirMapLayerType(String text, @StringRes int title) {
            this.text = text;
            this.title = title;
        }

        @Override
        public String toString() {
            return text;
        }

        public int getTitle() {
            return title;
        }

        public static AirMapLayerType fromString(String text) {
            switch (text) {
                case "tfrs":
                    return TFRS;
                case "wildfires":
                    return Wildfires;
                case "fires":
                    return Fires;
                case "emergencies":
                    return Emergencies;
                case "sua_prohibited":
                    return Prohibited;
                case "sua_restricted":
                    return Restricted;
                case "national_parks":
                    return NationalParks;
                case "noaa":
                    return NOAA;
                case "schools":
                    return Schools;
                case "hospitals":
                    return Hospitals;
                case "heliports":
                    return Heliports;
                case "power_plants":
                    return PowerPlants;
                case "airports_commercial,airports_commercial_private":
                case "airports_commercial":
                    return AirportsCommercial;
                case "airports_recreational,airports_recreational_private":
                    return AirportsRecreational;
                case "airports_commercial_private":
                    return AirportsCommercialPrivate;
                case "airports_recreational_private":
                    return AirportsRecreationalPrivate;
                case "class_b":
                    return ClassB;
                case "class_c":
                    return ClassC;
                case "class_d":
                    return ClassD;
                case "class_e0":
                    return ClassE;
                case "hazard_areas":
                    return HazardAreas;
                case "aerial_recreational_areas":
                    return AerialRecreationalAreas;
                case "cities":
                    return Cities;
                case "custom":
                    return Custom;
                case "prisons":
                    return Prisons;
                case "universities":
                    return Universities;
                case "aerial_recreational_areas,custom,hazard_areas,hospitals,power_plants,prisons,schools,universities,cities":
                    return Other;
                default:
                    return null;
            }
        }

        public AirMapAirspaceType getAirspaceType() {
            switch (this) {
                case TFRS:
                    return AirMapAirspaceType.TFR;
                case Wildfires:
                    return AirMapAirspaceType.Wildfires;
                case Fires:
                    return AirMapAirspaceType.Fires;
                case Emergencies:
                    return AirMapAirspaceType.Emergencies;
                case Prohibited:
                case Restricted:
                    return AirMapAirspaceType.SpecialUse;
                case NationalParks:
                case NOAA:
                    return AirMapAirspaceType.Park;
                case Schools:
                    return AirMapAirspaceType.School;
                case Hospitals:
                    return AirMapAirspaceType.Hospitals;
                case PowerPlants:
                    return AirMapAirspaceType.PowerPlant;
                case AirportsCommercial:
                case AirportsRecreational:
                case AirportsCommercialPrivate:
                case AirportsRecreationalPrivate:
                    return AirMapAirspaceType.Airport;
                case Heliports:
                case ClassB:
                case ClassC:
                case ClassD:
                case ClassE:
                    return AirMapAirspaceType.ControlledAirspace;
                case HazardAreas:
                    return AirMapAirspaceType.HazardArea;
                case AerialRecreationalAreas:
                    return AirMapAirspaceType.RecreationalArea;
                case Cities:
                    return AirMapAirspaceType.City;
                case Custom:
                    return AirMapAirspaceType.Custom;
                case Prisons:
                    return AirMapAirspaceType.Prison;
                case Universities:
                    return AirMapAirspaceType.University;
            }
            return null;
        }
    }

    public enum AirMapAirspaceType {
        Airport("airport", R.string.airspace_type_airport),
        Heliport("heliport", R.string.airspace_type_heliport),
        Park("park", R.string.airspace_type_national_park),
        PowerPlant("power_plant", R.string.airspace_type_power_plant),
        ControlledAirspace("controlled_airspace", R.string.airspace_type_controlled),
        School("school", R.string.airspace_type_school),
        SpecialUse("special_use_airspace", R.string.airspace_type_special_use),
        TFR("tfr", R.string.airspace_type_tfr_faa),
        Wildfires("wildfire", R.string.airspace_type_wildfire),
        Fires("fire", R.string.airspace_type_fire),
        Emergencies("emergency", R.string.airspace_type_emergency),
        Hospitals("hospital", R.string.airspace_type_hospital),
        HazardArea("hazard_area", R.string.airspace_type_hazard_area),
        RecreationalArea("recreational_area", R.string.airspace_type_aerial_rec_area),
        City("city", R.string.airspace_type_city),
        Custom("custom", R.string.airspace_type_custom),
        Prison("prison", R.string.airspace_type_prison),
        University("university", R.string.airspace_type_university);

        private final String text;
        private final int title;

        AirMapAirspaceType(String text, @StringRes int title) {
            this.text = text;
            this.title = title;
        }

        @Override
        public String toString() {
            return text;
        }

        public int getTitle() {
            return title;
        }

        public static AirMapAirspaceType fromString(String text) {
            switch (text) {
                case "airport":
                    return Airport;
                case "heliport":
                    return Heliport;
                case "park":
                    return Park;
                case "power_plant":
                    return PowerPlant;
                case "controlled_airspace":
                    return ControlledAirspace;
                case "school":
                    return School;
                case "special_use_airspace":
                    return SpecialUse;
                case "tfr":
                    return TFR;
                case "wildfire":
                    return Wildfires;
                case "fire":
                    return Fires;
                case "emergency":
                    return Emergencies;
                case "hospital":
                    return Hospitals;
                case "hazard_area":
                    return HazardArea;
                case "recreational_area":
                    return RecreationalArea;
                case "city":
                    return City;
                case "custom":
                    return Custom;
                case "prison":
                    return Prison;
                case "university":
                    return University;
            }
            return null;
        }
    }

    public enum AirMapMapTheme {
        Standard("standard"),
        Light("light"),
        Dark("dark"),
        Satellite("satellite");

        private final String text;

        AirMapMapTheme(String text) {
            this.text = text;
        }

        @Override
        public String toString() {
            return text;
        }

        public static AirMapMapTheme fromString(String text) {
            switch (text) {
                case "standard":
                    return Standard;
                case "light":
                    return Light;
                case "dark":
                    return Dark;
                case "satellite":
                    return Satellite;
            }
            return null;
        }
    }

    protected String getTileSourceUrl(@Nullable List<AirMapLayerType> layers, AirMapMapTheme theme) {
        String tiles = (layers == null || layers.size() == 0) ? "_-_" : android.text.TextUtils.join(",", layers);
        return mapTilesBaseUrl + tiles + "?&theme=" + theme.toString() + "&apikey=" + AirMap.getInstance().getApiKey() + "&token=" + AirMap.getInstance().getApiKey();
    }
}
