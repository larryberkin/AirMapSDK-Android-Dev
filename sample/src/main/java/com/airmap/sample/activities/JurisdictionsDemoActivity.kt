package com.airmap.sample.activities

import android.os.Bundle
import android.support.v7.widget.Toolbar

import com.airmap.airmapsdk.AirMapException
import com.airmap.airmapsdk.models.AirMapWeather
import com.airmap.airmapsdk.models.Coordinate
import com.airmap.airmapsdk.models.flight.AirMapFlight
import com.airmap.airmapsdk.models.flight.AirMapFlightBriefing
import com.airmap.airmapsdk.models.flight.AirMapFlightPlan
import com.airmap.airmapsdk.models.rules.AirMapJurisdiction
import com.airmap.airmapsdk.models.shapes.AirMapPolygon
import com.airmap.airmapsdk.models.status.AirMapAirspaceStatus
import com.airmap.airmapsdk.models.traffic.AirMapTraffic
import com.airmap.airmapsdk.networking.callbacks.AirMapCallback
import com.airmap.airmapsdk.networking.callbacks.AirMapTrafficListener
import com.airmap.airmapsdk.networking.services.AirMap
import com.airmap.sample.R
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback

import java.util.Arrays
import java.util.Date

import timber.log.Timber

class JurisdictionsDemoActivity : BaseActivity(), OnMapReadyCallback {

    private val toolbar by lazy { findViewById<Toolbar>(R.id.toolbar) }
    private val mapView by lazy { findViewById<MapView>(R.id.map_view) }

    private var mapboxMap: MapboxMap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_traffic)

        toolbar.title = title
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)
    }

    override fun onMapReady(map: MapboxMap) {
        this.mapboxMap = map
        map.addOnCameraIdleListener { updateJurisdictions() }
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(34.0195, -118.4912), 13.0))
    }

    private fun updateJurisdictions() {
        val map = mapboxMap ?: return
        val bounds = map.projection.visibleRegion.latLngBounds
        val coordinates = listOf(
                Coordinate(bounds.latNorth, bounds.lonWest),
                Coordinate(bounds.latNorth, bounds.lonEast),
                Coordinate(bounds.latSouth, bounds.lonEast),
                Coordinate(bounds.latSouth, bounds.lonWest),
                Coordinate(bounds.latNorth, bounds.lonWest)
        )
        val polygon = AirMapPolygon(coordinates)
        AirMap.getJurisdictions(polygon, object : AirMapCallback<List<AirMapJurisdiction>>() {
            override fun onSuccess(jurisdictions: List<AirMapJurisdiction>) {
                Timber.v("Jurisdictions: %s", jurisdictions)
                // Available jurisdictions and their rulesets
                for (jurisdiction in jurisdictions) {
                    Timber.v("Jurisdiction: %s", jurisdiction)
                    Timber.v("Pick One rulesets: %s", jurisdiction.pickOneRulesets)
                    Timber.v("Optional rulesets: %s", jurisdiction.optionalRulesets)
                    Timber.v("Required rulesets: %s", jurisdiction.requiredRulesets)
                }
                // Display jurisdictions and their respective groups of rulesets
            }

            override fun onError(e: AirMapException) = Timber.e(e, "Unable to get jurisdictions")
        })

        val rulesetIds = Arrays.asList("usa_part_107")

        AirMap.getAirspaceStatus(polygon, rulesetIds, object : AirMapCallback<AirMapAirspaceStatus>() {
            override fun onSuccess(status: AirMapAirspaceStatus) = Timber.v("Status: %s", status) // Show status advisories
            override fun onError(e: AirMapException) = Timber.e(e, "Error getting AirspaceStatus")
        })

        val coordinate = Coordinate(map.cameraPosition.target)
        val startTime = Date()
        val endTime = Date(startTime.time + 4 * 60 * 60 * 1000)

        AirMap.getWeather(coordinate, startTime, endTime, object : AirMapCallback<AirMapWeather>() {
            override fun onSuccess(weather: AirMapWeather) = Timber.v("Weather: %s", weather.updates)
            override fun onError(e: AirMapException) = Timber.e(e, "Error getting weather")
        })

        val userId = "acme|123"

        AirMap.performAnonymousLogin(userId, object : AirMapCallback<Void>() {
            public override fun onSuccess(response: Void) {
                Timber.v("Token is: %s", AirMap.getAuthToken())

                // handle login
                createFlightPlan(polygon, coordinate, rulesetIds)
            }

            public override fun onError(e: AirMapException) = Timber.e(e, "Error performing anonymous login: %s", e.detailedMessage)
        })
    }

    private fun createFlightPlan(polygon: AirMapPolygon, takeoffCoordinate: Coordinate, rulesetIds: List<String>) {
        // sets required params
        val flightPlan = AirMapFlightPlan().apply {
            setGeometry(polygon)
            pilotId = AirMap.getUserId()
            buffer = 100f
            maxAltitude = 100f
            this.takeoffCoordinate = takeoffCoordinate
            this.rulesetIds = rulesetIds
            // default start & end time - now to 4 hours from now
            val duration = (4 * 60 * 60 * 1000).toLong()
            durationInMillis = duration
            startsAt = Date()
            endsAt = Date(System.currentTimeMillis() + duration)
        }

        AirMap.createFlightPlan(flightPlan, object : AirMapCallback<AirMapFlightPlan>() {
            override fun onSuccess(response: AirMapFlightPlan) {
                Timber.v("Flight plan created: %s", response.planId)
                // Handle success

                fly(response.planId)
            }

            override fun onError(e: AirMapException) = Timber.e(e, "Failed to create flight plan")
        })
    }

    private fun getFlightBriefing(flightPlanId: String) {
        AirMap.getFlightBrief(flightPlanId, object : AirMapCallback<AirMapFlightBriefing>() {
            override fun onSuccess(briefing: AirMapFlightBriefing) = Timber.v("Got flight briefing")
            override fun onError(e: AirMapException) = Timber.e(e, "Error getting flight briefing")
        })
    }

    private fun fly(flightPlanId: String) {
        AirMap.submitFlightPlan(flightPlanId, object : AirMapCallback<AirMapFlightPlan>() {
            override fun onSuccess(flightPlan: AirMapFlightPlan) = Timber.v("Flight id: %s", flightPlan.flightId)
            override fun onError(e: AirMapException) = Timber.e(e, "Error submitting flight plan")
        })
    }

    private fun sendTelemetry() {
        val flight: AirMapFlight? = null

        //        AirMap.getTelemetryService().sendAttitudeMessage(flight, yaw, pitch, roll);
        //
        //        AirMap.getTelemetryService().sendPositionMessage(flight, lat, longitude, altAGL, altMSL, horizontalAccuracy);
        //
        //        AirMap.getTelemetryService().sendSpeedMessage(flight, speedX, speedY, speedZ);
        //
        //        AirMap.getTelemetryService().setBarometerMessage(flight, pressure);
    }

    private fun getTraffic() {
        // To turn on traffic alerts
        AirMap.enableTrafficAlerts(object : AirMapTrafficListener {
            override fun onAddTraffic(added: List<AirMapTraffic>) {
                // Display traffic on map
            }

            override fun onUpdateTraffic(updated: List<AirMapTraffic>) {
                // Update traffic on map
            }

            override fun onRemoveTraffic(removed: List<AirMapTraffic>) {
                // Remove traffic from map
            }
        })

        // To turn off traffic alerts
        AirMap.disableTrafficAlerts()
    }

    private fun endFlight(flightId: String) {
        // only call end if the flight is active
        AirMap.endFlight(flightId, object : AirMapCallback<AirMapFlight>() {
            public override fun onSuccess(response: AirMapFlight) {
                // Handle success (remove from map, disable traffic alerts, etc)
            }

            public override fun onError(e: AirMapException) {
                // Handle error
            }
        })

    }

    // Mapbox requires lifecycle
    public override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    public override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    public override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    public override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    public override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    public override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

}
