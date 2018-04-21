package com.airmap.sample.activities

import android.animation.ObjectAnimator
import android.animation.TypeEvaluator
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.support.v7.widget.Toolbar
import android.view.animation.LinearInterpolator

import com.airmap.airmapsdk.AirMapException
import com.airmap.airmapsdk.models.flight.AirMapFlight
import com.airmap.airmapsdk.models.traffic.AirMapTraffic
import com.airmap.airmapsdk.networking.callbacks.AirMapCallback
import com.airmap.airmapsdk.networking.callbacks.AirMapTrafficListener
import com.airmap.airmapsdk.networking.services.AirMap
import com.airmap.airmapsdk.ui.views.AirMapMapView
import com.airmap.sample.R
import com.airmap.sample.Utils
import com.airmap.sample.ui.TrafficMarker
import com.airmap.sample.ui.TrafficMarkerOptions
import com.mapbox.mapboxsdk.annotations.Icon
import com.mapbox.mapboxsdk.annotations.IconFactory
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng

import java.util.Locale

import timber.log.Timber

import com.airmap.airmapsdk.util.Utils.metersToMiles
import com.airmap.sample.Utils.Companion.directionFromBearing
import com.airmap.sample.Utils.Companion.getBitmap
import com.airmap.sample.Utils.Companion.ktsToMph
import com.airmap.sample.Utils.Companion.minutesToMinSec

class TrafficDemoActivity : BaseActivity(), AirMapMapView.OnMapLoadListener, AirMapTrafficListener {

    companion object {
        fun Context.TrafficDemoIntent() = Intent(this, TrafficDemoActivity::class.java)
    }

    private val toolbar by lazy { findViewById<Toolbar>(R.id.toolbar) }
    private val mapView by lazy { findViewById<AirMapMapView>(R.id.map_view) }

    private lateinit var textToSpeech: TextToSpeech
    private var trafficMarkers = mutableListOf<TrafficMarker>()
    private var currentFlight: AirMapFlight? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_traffic)

        textToSpeech = TextToSpeech(this, null)
        textToSpeech.language = Locale.getDefault()

        toolbar.title = title
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mapView.onCreate(savedInstanceState)
        mapView.addOnMapLoadListener(this)
    }

    override fun onMapLoaded() {
        mapView.map.animateCamera(CameraUpdateFactory.newLatLngZoom(LatLng(34.0195, -118.4912), 12.5))

        AirMap.getCurrentFlight(object : AirMapCallback<AirMapFlight>() {
            override fun onSuccess(response: AirMapFlight?) {
                // if user has flight, add traffic
                if (response != null) {
                    currentFlight = response

                    // add a marker for our flight
                    val markerOptions = MarkerOptions()
                            .position(currentFlight!!.coordinate.toMapboxLatLng())
                            .icon(IconFactory.getInstance(this@TrafficDemoActivity).fromBitmap(getBitmap(this@TrafficDemoActivity, R.drawable.current_flgiht_marker_icon)))

                    mapView?.map?.addMarker(markerOptions)

                    // Start listening for traffic
                    AirMap.enableTrafficAlerts(this@TrafficDemoActivity)
                    toast("Enabling traffic alerts")

                    // if not, user needs to create flight first
                } else {
                    toast("No active flight. Please create a flight first.")
                }
            }

            override fun onError(e: AirMapException) = Timber.e(e, "Get current flight failed")
        })
    }

    override fun onMapFailed(reason: AirMapMapView.MapFailure) = Unit

    override fun onAddTraffic(added: List<AirMapTraffic>) {
        var shouldSayTraffic = false
        for (traffic in added) {
            val trafficMarkerOptions = TrafficMarkerOptions(traffic).icon(getIcon(traffic))
            val trafficMarker = mapView?.map?.addMarker(trafficMarkerOptions) as TrafficMarker?
            trafficMarker?.let {
                trafficMarkers.add(trafficMarker)
            }
            showTrafficAlert(traffic)

            shouldSayTraffic = shouldSayTraffic || traffic.trafficType == AirMapTraffic.TrafficType.Alert
        }

        if (shouldSayTraffic) {
            sayTraffic()
        }
    }

    override fun onUpdateTraffic(updated: List<AirMapTraffic>) {
        var shouldSayTraffic = false
        for (traffic in updated) {
            searchById(traffic)?.let { marker ->
                if (traffic.trafficType == AirMapTraffic.TrafficType.Alert && marker.traffic.trafficType == AirMapTraffic.TrafficType.SituationalAwareness) { //If it changed from SitAwareness to Alert, notify
                    showTrafficAlert(traffic)
                    shouldSayTraffic = true
                    marker.icon = getIcon(traffic) //Change from Sit Awareness to Alert Icon
                }
                if (traffic.trueHeading != marker.traffic.trueHeading) {
                    marker.icon = getIcon(traffic)
                }
                marker.traffic = traffic
                val latLng = traffic.coordinate.toMapboxLatLng()
                val markerAnimator = ObjectAnimator.ofObject(marker, "position", LatLngEvaluator(), marker.position, latLng) //Animate the traffic's location from old position to new position
                markerAnimator.duration = 1000
                markerAnimator.interpolator = LinearInterpolator()
                runOnUiThread { markerAnimator.start() }
            }
        }
        if (shouldSayTraffic) {
            sayTraffic()
        }
    }

    override fun onRemoveTraffic(removed: List<AirMapTraffic>) {
        removed.mapNotNull { traffic -> searchById(traffic) }.forEach { marker ->
            marker.remove()
            trafficMarkers.remove(marker)
        }
    }

    /**
     * Audio alert
     */
    private fun sayTraffic() = textToSpeech.speak(getString(R.string.traffic), TextToSpeech.QUEUE_FLUSH, null, null)

    private fun showTrafficAlert(traffic: AirMapTraffic) {
        val activeFlightLocation = currentFlight!!.coordinate.toMapboxLatLng()
        val distanceInMeters = activeFlightLocation.distanceTo(traffic.coordinate.toMapboxLatLng())
        val distance = metersToMiles(distanceInMeters)
        val speed = ktsToMph(traffic.groundSpeedKt.toDouble())
        val timeInHours = distance / speed
        val trafficText = StringBuilder()
                .append(traffic.properties.aircraftId).append("\n")
                .append(getString(R.string.distance_in_miles, distance)).append(" ")
                .append(directionFromBearing(this, traffic.trueHeading.toDouble())).append("\n")
                .append(minutesToMinSec(this, timeInHours * 60))

        toast(trafficText.toString())
    }

    private fun searchById(traffic: AirMapTraffic) = trafficMarkers.firstOrNull { it.traffic == traffic }

    /**
     * Dynamically provides an icon based on which direction the traffic is traveling
     *
     * @param traffic The traffic to get an icon for
     * @return An icon
     */
    private fun getIcon(traffic: AirMapTraffic): Icon {
        //Generate the icon dynamically based on which direction the traffic is pointing/traveling
        val factory = IconFactory.getInstance(this)
        val idPrefix = if (traffic.trafficType == AirMapTraffic.TrafficType.SituationalAwareness) "sa_traffic_marker_icon_" else "traffic_marker_icon_"
        val id = resources.getIdentifier(idPrefix + directionFromBearing(this, traffic.trueHeading.toDouble()).toLowerCase(), "drawable", packageName)
        return factory.fromBitmap(Utils.getBitmap(this, id))
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
        mapView.removeOnMapLoadListener(this)

        AirMap.disableTrafficAlerts()
        textToSpeech.shutdown()
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

    private inner class LatLngEvaluator : TypeEvaluator<LatLng> {
        // Method is used to interpolate the marker animation.
        private val latLng = LatLng()

        override fun evaluate(fraction: Float, startValue: LatLng, endValue: LatLng): LatLng {
            latLng.latitude = startValue.latitude + (endValue.latitude - startValue.latitude) * fraction
            latLng.longitude = startValue.longitude + (endValue.longitude - startValue.longitude) * fraction
            return latLng
        }
    }
}
