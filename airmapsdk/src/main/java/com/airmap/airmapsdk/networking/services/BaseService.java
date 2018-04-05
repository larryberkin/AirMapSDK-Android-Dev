package com.airmap.airmapsdk.networking.services;

import com.airmap.airmapsdk.util.AirMapConfig;

import static com.airmap.airmapsdk.util.AirMapConfig.getApiOverride;
import static com.airmap.airmapsdk.util.AirMapConfig.getAuth0Host;
import static com.airmap.airmapsdk.util.AirMapConfig.getDomain;
import static com.airmap.airmapsdk.util.Utils.getMqttDebugUrl;
import static com.airmap.airmapsdk.util.Utils.getStagingUrl;
import static com.airmap.airmapsdk.util.Utils.getTelemetryDebugUrl;

@SuppressWarnings({"ConstantConditions", "WeakerAccess"})
public class BaseService {

    //URLs should end with a /
    protected static final boolean OVERRIDE_TO_STAGING = false;
    public static final boolean STAGING = AirMapConfig.isStage() || OVERRIDE_TO_STAGING;

    //Base Urls
    protected static final String baseUrl = "https://api." + getDomain() + "/";
    protected static final String mapTilesVersion = STAGING ? getStagingUrl() : "v1/";
    protected static final String mapTilesBaseUrl = baseUrl + "maps/" + "v4/" + "tilejson/";
    protected static final String mapTilesRulesUrl = baseUrl + "tiledata/" + mapTilesVersion;

    //Aircraft
    protected static final String aircraftVersion = STAGING ? getStagingUrl() : "v2/";
    protected static final String aircraftBaseUrl = baseUrl + "aircraft/" + aircraftVersion;
    protected static final String aircraftManufacturersUrl = aircraftBaseUrl + "manufacturer/";
    protected static final String aircraftModelsUrl = aircraftBaseUrl + "model/";
    protected static final String aircraftModelUrl = aircraftModelsUrl + "%s/"; //Replace %s with id using String.format

    //Flight
    protected static final String flightVersion = STAGING ? getStagingUrl() : "v2/";
    protected static final String flightBaseUrl = baseUrl + "flight/" + flightVersion;
    protected static final String flightGetAllUrl = flightBaseUrl;
    protected static final String flightByIdUrl = flightBaseUrl + "%s/"; //Replace %s with id using String.format
    protected static final String flightDeleteUrl = flightByIdUrl + "delete/"; //Replace %s with id using String.format
    protected static final String flightEndUrl = flightByIdUrl + "end/"; //Replace %s with id using String.format
    protected static final String flightStartCommUrl = flightByIdUrl + "start-comm/"; //Replace %s with id using String.format
    protected static final String flightEndCommUrl = flightByIdUrl + "end-comm/"; //Replace %s with id using String.format
    protected static final String flightPlanUrl = getApiOverride("flightplan", flightBaseUrl + "plan/");
    protected static final String flightPlanByFlightIdUrl = flightBaseUrl + "%s/" + "plan/";
    protected static final String flightPlanPatchUrl = flightPlanUrl + "%s/";
    protected static final String flightPlanBriefingUrl = flightPlanPatchUrl + "briefing";
    protected static final String flightPlanSubmitUrl = flightPlanPatchUrl + "submit";
    protected static final String flightFeaturesByPlanIdUrl = flightPlanPatchUrl + "features";

    //Weather
    protected static final String weatherVersion = STAGING ? getStagingUrl() : "v1/";
    protected static final String weatherUrl = baseUrl + "advisory/" + weatherVersion + "weather";

    //Permits
    protected static final String permitVersion = STAGING ? getStagingUrl() : "v2/";
    protected static final String permitBaseUrl = baseUrl + "permit/" + permitVersion;
    protected static final String permitApplyUrl = permitBaseUrl + "%s/apply/"; //Replace %s with permitId using String.format

    //Pilot
    protected static final String pilotVersion = STAGING ? getStagingUrl() : "v2/";
    protected static final String pilotBaseUrl = baseUrl + "pilot/" + pilotVersion;
    protected static final String pilotByIdUrl = pilotBaseUrl + "%s/"; //Replace %s with id using String.format
    protected static final String pilotGetPermitsUrl = pilotByIdUrl + "permit/"; //Replace %s with id using String.format
    protected static final String pilotDeletePermitUrl = pilotGetPermitsUrl + "%s/"; //Replace BOTH occurrences of %s with user id and permit id, using String.format
    protected static final String pilotAircraftUrl = pilotByIdUrl + "aircraft/"; //Replace %s with id using String.format
    protected static final String pilotAircraftByIdUrl = pilotAircraftUrl + "%s/"; //Replace BOTH occurrences of %s with user id and aircraft id, using String.format
    protected static final String pilotSendVerifyUrl = pilotByIdUrl + "phone/send_token/"; //Replace %s with id using String.format
    protected static final String pilotVerifyUrl = pilotByIdUrl + "phone/verify_token/"; //Replace %s with id using String.format

    //Status
    protected static final String statusVersion = STAGING ? getStagingUrl() : "v2/";
    protected static final String statusBaseUrl = baseUrl + "status/" + statusVersion;
    protected static final String statusPointUrl = statusBaseUrl + "point/";
    protected static final String statusPathUrl = statusBaseUrl + "path/";
    protected static final String statusPolygonUrl = statusBaseUrl + "polygon/";

    //Airspace
    protected static final String airspaceVersion = STAGING ? getStagingUrl() : "v2/";
    protected static final String airspaceBaseUrl = baseUrl + "airspace/" + airspaceVersion;
    protected static final String airspaceByIdUrl = airspaceBaseUrl + "%s/"; //Replace %s with id using String.format
    protected static final String airspaceByIdsUrl = airspaceBaseUrl + "list/";

    //Traffic Alerts
    protected static final String mqttBaseUrl = STAGING ? getMqttDebugUrl() : "ssl://mqtt-prod." + AirMapConfig.getMqttDomain() + ":8883";
    protected static final String trafficAlertChannel = "uav/traffic/alert/%s"; //Replace %s with id using String.format. *Don't* end this url with a /
    protected static final String situationalAwarenessChannel = "uav/traffic/sa/%s"; //Replace %s with id using String.format

    //Telemetry
    protected static final String telemetryBaseUrl = STAGING ? ("api.k8s.stage.airmap.com") : ("api-udp-telemetry." + (STAGING ? "stage." : "") + getDomain());
    protected static final int telemetryPort = STAGING ? 32003 : 16060;

    //Auth
    protected static final String loginUrl = "https://" + getAuth0Host() + "/delegation";
    protected static final String authVersion = STAGING ? getStagingUrl() : "v1/";
    protected static final String authBaseUrl = baseUrl + "auth/" + authVersion;
    protected static final String anonymousLoginUrl = authBaseUrl + "anonymous/token";
    protected static final String delegationUrl = loginUrl;
    protected static final String auth0Domain = getAuth0Host();

    //Rules
    protected static final String rulesetsVersion = STAGING ? getStagingUrl() : "v1/";
    protected static final String rulesetBaseUrl = getApiOverride("rules", baseUrl + "rules/" + rulesetsVersion);
    protected static final String rulesetsByIdUrl = rulesetBaseUrl + "rule/"; //Replace %s with id using String.format
    protected static final String rulesetByIdUrl = rulesetBaseUrl + "%s/"; //Replace %s with id using String.format
    protected static final String rulesByIdUrl = rulesetBaseUrl + "%s/"; //Replace %s with id using String.format
    protected static final String advisoriesUrl = baseUrl + "advisory/" + rulesetsVersion + "airspace";
    protected static final String evaluationUrl = rulesetBaseUrl + "evaluation";
}
