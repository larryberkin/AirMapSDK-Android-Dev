package com.airmap.airmapsdk.networking.services;

import com.airmap.airmapsdk.util.Utils;

/**
 * Created by Vansh Gandhi on 6/23/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */
@SuppressWarnings({"ConstantConditions", "WeakerAccess"})
class BaseService {

    //URLs should end with a /
    //TODO: Change all the debug servers to production upon release
    private static final boolean DEBUG = false;

    //Base Urls
    protected static final String baseUrl = "https://api.airmap.com/";
    protected static final String mapTilesVersion = DEBUG ? Utils.getDebugUrl() : "v4/";
    protected static final String mapTilesBaseUrl = baseUrl + "maps/" + mapTilesVersion + "tilejson/";

    //Aircraft
    protected static final String aircraftVersion = DEBUG ? Utils.getDebugUrl() : "v2/";
    protected static final String aircraftBaseUrl = baseUrl + "aircraft/" + aircraftVersion;
    protected static final String aircraftManufacturersUrl = aircraftBaseUrl + "manufacturer/";
    protected static final String aircraftModelsUrl = aircraftBaseUrl + "model/";
    protected static final String aircraftModelUrl = aircraftModelsUrl + "%s/"; //Replace %s with id using String.format

    //Flight
    protected static final String flightVersion = DEBUG ? Utils.getDebugUrl() : "v2/";
    protected static final String flightBaseUrl = baseUrl + "flight/" + flightVersion;
    protected static final String flightGetAllUrl = flightBaseUrl;
    protected static final String flightPointUrl = flightBaseUrl + "point/";
    protected static final String flightPathUrl = flightBaseUrl + "path/";
    protected static final String flightPolygonUrl = flightBaseUrl + "polygon/";
    protected static final String flightByIdUrl = flightBaseUrl + "%s/"; //Replace %s with id using String.format
    protected static final String flightDeleteUrl = flightByIdUrl + "delete/"; //Replace %s with id using String.format
    protected static final String flightEndUrl = flightByIdUrl + "end/"; //Replace %s with id using String.format
    protected static final String flightStartCommUrl = flightByIdUrl + "start-comm/"; //Replace %s with id using String.format
    protected static final String flightEndCommUrl = flightByIdUrl + "end-comm/"; //Replace %s with id using String.format

    //Permits
    protected static final String permitVersion = DEBUG ? Utils.getDebugUrl() : "v2/";
    protected static final String permitBaseUrl = baseUrl + "permit/" + permitVersion;
    protected static final String permitApplyUrl = permitBaseUrl + "%s/apply/"; //Replace %s with permitId using String.format


    //Pilot
    protected static final String pilotVersion = DEBUG ? Utils.getDebugUrl() : "v2/";
    protected static final String pilotBaseUrl = baseUrl + "pilot/" + pilotVersion;
    protected static final String pilotByIdUrl = pilotBaseUrl + "%s/"; //Replace %s with id using String.format
    protected static final String pilotGetPermitsUrl = pilotByIdUrl + "permit/"; //Replace %s with id using String.format
    protected static final String pilotDeletePermitUrl = pilotGetPermitsUrl + "%s/"; //Replace BOTH occurrences of %s with user id and permit id, using String.format
    protected static final String pilotAircraftUrl = pilotByIdUrl + "aircraft/"; //Replace %s with id using String.format
    protected static final String pilotAircraftByIdUrl = pilotAircraftUrl + "%s/"; //Replace BOTH occurrences of %s with user id and aircraft id, using String.format
    protected static final String pilotSendVerifyUrl = pilotByIdUrl + "phone/send_token/"; //Replace %s with id using String.format
    protected static final String pilotVerifyUrl = pilotByIdUrl + "phone/verify_token/"; //Replace %s with id using String.format

    //Status
    protected static final String statusVersion = DEBUG ? Utils.getDebugUrl() : "alpha/";
    protected static final String statusBaseUrl = baseUrl + "status/" + statusVersion;
    protected static final String statusPointUrl = statusBaseUrl + "point/";
    protected static final String statusPathUrl = statusBaseUrl + "path/";
    protected static final String statusPolygonUrl = statusBaseUrl + "polygon/";

    //Airspace
    protected static final String airspaceVersion = DEBUG ? Utils.getDebugUrl() : "v2/";
    protected static final String airspaceBaseUrl = baseUrl + "airspace/" + airspaceVersion;
    protected static final String airspaceByIdUrl = airspaceBaseUrl + "%s/"; //Replace %s with id using String.format
    protected static final String airspaceByIdsUrl = airspaceBaseUrl + "list/";

    //Traffic Alerts
    protected static final String mqttBaseUrl = DEBUG ? Utils.getMqttDebugUrl() : "ssl://mqtt-prod.airmap.io:8883";
    protected static final String trafficAlertChannel = "uav/traffic/alert/%s"; //Replace %s with id using String.format. *Don't* end this url with a /
    protected static final String situationalAwarenessChannel = "uav/traffic/sa/%s"; //Replace %s with id using String.format

    //Telemetry
    protected static final String telemetryBaseUrl = DEBUG ? Utils.getTelemetryDebugUrl() : "api-udp-telemetry.prod.airmap.com";
    protected static final int telemetryPort = 16060;

    //Welcome
    protected static final String welcomeVersion = DEBUG ? Utils.getDebugUrl() : "v1/";
    protected static final String welcomeBaseUrl = baseUrl + "rules/" + welcomeVersion;
    protected static final String welcomeUrl = welcomeBaseUrl + "locale/";

    //Auth
    protected static final String loginUrl = "https://sso.airmap.io/delegation";
    protected static final String authVersion = DEBUG ? Utils.getDebugUrl() : "v1/";
    protected static final String authBaseUrl = baseUrl + "auth/" + authVersion;
    protected static final String anonymousLoginUrl = authBaseUrl + "anonymous/token";
}
