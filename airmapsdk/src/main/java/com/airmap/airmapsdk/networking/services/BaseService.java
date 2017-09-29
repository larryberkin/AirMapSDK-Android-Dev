package com.airmap.airmapsdk.networking.services;

import com.airmap.airmapsdk.BuildConfig;
import com.airmap.airmapsdk.util.AirMapConfig;
import com.airmap.airmapsdk.util.Utils;

/**
 * Created by Vansh Gandhi on 6/23/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */
@SuppressWarnings({"ConstantConditions", "WeakerAccess"})
class BaseService {

    //URLs should end with a /
    protected static final boolean OVERRIDE = false;
    protected static final boolean STAGING = AirMapConfig.isStage() || OVERRIDE;

    //Base Urls
    protected static final String baseUrl = "https://api." + AirMapConfig.getDomain() + "/";
    protected static final String mapTilesVersion = STAGING ? Utils.getStagingUrl() : "v1/";
    protected static final String mapTilesBaseUrl = baseUrl + "maps/" + mapTilesVersion + "tilejson/";
    protected static final String mapTilesRulesUrl = baseUrl + "tiledata/" + mapTilesVersion;

    //Aircraft
    protected static final String aircraftVersion = STAGING ? Utils.getStagingUrl() : "v2/";
    protected static final String aircraftBaseUrl = baseUrl + "aircraft/" + aircraftVersion;
    protected static final String aircraftManufacturersUrl = aircraftBaseUrl + "manufacturer/";
    protected static final String aircraftModelsUrl = aircraftBaseUrl + "model/";
    protected static final String aircraftModelUrl = aircraftModelsUrl + "%s/"; //Replace %s with id using String.format

    //Flight
    protected static final String flightVersion = STAGING ? Utils.getStagingUrl() : "v2/";
    protected static final String flightBaseUrl = baseUrl + "flight/" + flightVersion;
    protected static final String flightGetAllUrl = flightBaseUrl;
    protected static final String flightByIdUrl = flightBaseUrl + "%s/"; //Replace %s with id using String.format
    protected static final String flightDeleteUrl = flightByIdUrl + "delete/"; //Replace %s with id using String.format
    protected static final String flightEndUrl = flightByIdUrl + "end/"; //Replace %s with id using String.format
    protected static final String flightStartCommUrl = flightByIdUrl + "start-comm/"; //Replace %s with id using String.format
    protected static final String flightEndCommUrl = flightByIdUrl + "end-comm/"; //Replace %s with id using String.format
    protected static final String flightPlanUrl = flightBaseUrl + "plan/";
    protected static final String flightPlanByFlightIdUrl = flightBaseUrl + "%s/" + "plan/";
    protected static final String flightPlanPatchUrl = flightPlanUrl + "%s/";
    protected static final String flightPlanBriefingUrl = flightPlanPatchUrl + "briefing";
    protected static final String flightPlanSubmitUrl = flightPlanPatchUrl + "submit";
    protected static final String flightFeaturesByPlanIdUrl = flightPlanPatchUrl + "features";

    //Weather
    protected static final String weatherVersion = STAGING ? Utils.getStagingUrl() : "v1/";
    protected static final String weatherUrl = baseUrl + "advisory/" + weatherVersion + "weather";

    //Permits
    protected static final String permitVersion = STAGING ? Utils.getStagingUrl() : "v2/";
    protected static final String permitBaseUrl = baseUrl + "permit/" + permitVersion;
    protected static final String permitApplyUrl = permitBaseUrl + "%s/apply/"; //Replace %s with permitId using String.format

    //Pilot
    protected static final String pilotVersion = STAGING ? Utils.getStagingUrl() : "v2/";
    protected static final String pilotBaseUrl = baseUrl + "pilot/" + pilotVersion;
    protected static final String pilotByIdUrl = pilotBaseUrl + "%s/"; //Replace %s with id using String.format
    protected static final String pilotGetPermitsUrl = pilotByIdUrl + "permit/"; //Replace %s with id using String.format
    protected static final String pilotDeletePermitUrl = pilotGetPermitsUrl + "%s/"; //Replace BOTH occurrences of %s with user id and permit id, using String.format
    protected static final String pilotAircraftUrl = pilotByIdUrl + "aircraft/"; //Replace %s with id using String.format
    protected static final String pilotAircraftByIdUrl = pilotAircraftUrl + "%s/"; //Replace BOTH occurrences of %s with user id and aircraft id, using String.format
    protected static final String pilotSendVerifyUrl = pilotByIdUrl + "phone/send_token/"; //Replace %s with id using String.format
    protected static final String pilotVerifyUrl = pilotByIdUrl + "phone/verify_token/"; //Replace %s with id using String.format

    //Status
    protected static final String statusVersion = STAGING ? Utils.getStagingUrl() : "v2/";
    protected static final String statusBaseUrl = baseUrl + "status/" + statusVersion;
    protected static final String statusPointUrl = statusBaseUrl + "point/";
    protected static final String statusPathUrl = statusBaseUrl + "path/";
    protected static final String statusPolygonUrl = statusBaseUrl + "polygon/";

    //Airspace
    protected static final String airspaceVersion = STAGING ? Utils.getStagingUrl() : "v2/";
    protected static final String airspaceBaseUrl = baseUrl + "airspace/" + airspaceVersion;
    protected static final String airspaceByIdUrl = airspaceBaseUrl + "%s/"; //Replace %s with id using String.format
    protected static final String airspaceByIdsUrl = airspaceBaseUrl + "list/";

    //Traffic Alerts
    protected static final String mqttBaseUrl = STAGING ? Utils.getMqttDebugUrl() : "ssl://mqtt-prod." + AirMapConfig.getMqttDomain() + ":8883";
    protected static final String trafficAlertChannel = "uav/traffic/alert/%s"; //Replace %s with id using String.format. *Don't* end this url with a /
    protected static final String situationalAwarenessChannel = "uav/traffic/sa/%s"; //Replace %s with id using String.format

    //Telemetry
    protected static final String telemetryBaseUrl = STAGING ? Utils.getTelemetryDebugUrl() : "api-udp-telemetry.prod." + AirMapConfig.getDomain();
    protected static final int telemetryPort = 16060;

    //Auth
    protected static final String loginUrl = "https://" + AirMapConfig.getAuth0Host() + "/delegation";
    protected static final String authVersion = STAGING ? Utils.getStagingUrl() : "v1/";
    protected static final String authBaseUrl = baseUrl + "auth/" + authVersion;
    protected static final String anonymousLoginUrl = authBaseUrl + "anonymous/token";
    protected static final String delegationUrl = loginUrl;
    protected static final String auth0Domain = AirMapConfig.getAuth0Host();

    //Rules
    protected static final String rulesetsVersion = STAGING ? Utils.getStagingUrl() : "v1/";
    protected static final String rulesetsBaseUrl = baseUrl + "rules/" + rulesetsVersion;
    protected static final String welcomeBaseUrl = STAGING ? baseUrl + "rules/stage/locale/" : baseUrl + "rules/v1/locale";
    protected static final String rulesByIdUrl = rulesetsBaseUrl + "%s/"; //Replace %s with id using String.format
    protected static final String rulesetsUrl = rulesetsBaseUrl + "rule/";
    protected static final String advisoriesUrl = baseUrl + "advisory/" + rulesetsVersion + "airspace";
}