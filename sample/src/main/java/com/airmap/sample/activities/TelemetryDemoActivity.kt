package com.airmap.sample.activities

import android.annotation.SuppressLint
import android.graphics.PointF
import android.graphics.RectF
import android.os.Bundle
import android.support.v7.widget.Toolbar
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import com.airmap.airmapsdk.AirMapException
import com.airmap.airmapsdk.models.flight.AirMapFlight
import com.airmap.airmapsdk.networking.callbacks.AirMapCallback
import com.airmap.airmapsdk.networking.services.AirMap
import com.airmap.airmapsdk.util.AnnotationsFactory
import com.airmap.sample.R
import com.airmap.sample.Utils.Companion.getBitmap
import com.mapbox.mapboxsdk.annotations.IconFactory
import com.mapbox.mapboxsdk.annotations.Marker
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.annotations.MarkerView
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapView
import com.mapbox.mapboxsdk.maps.MapboxMap
import timber.log.Timber

class TelemetryDemoActivity : BaseActivity() {

    private val toolbar by lazy { findViewById<Toolbar>(R.id.toolbar) }
    private val mapView by lazy { findViewById<MapView>(R.id.map_view) }

    private var currentFlight: AirMapFlight? = null
    private var myMarker: Marker? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_telemetry)

        toolbar.title = title
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync { map ->
            map.uiSettings.setAllGesturesEnabled(false)
            setupMapDragging(map)

            AirMap.getCurrentFlight(object : AirMapCallback<AirMapFlight>() {
                override fun onSuccess(flight: AirMapFlight?) = flight?.let {
                    // if user has flight, add traffic
                    currentFlight = flight

                    // add a marker for our flight
                    val markerOptions = MarkerOptions()
                            .position(currentFlight!!.coordinate.toMapboxLatLng())
                            .icon(IconFactory.getInstance(this@TelemetryDemoActivity).fromBitmap(getBitmap(this@TelemetryDemoActivity, R.drawable.current_flgiht_marker_icon)))

                    myMarker = map.addMarker(markerOptions)
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(flight.coordinate.toMapboxLatLng(), 12.5))

                    // Start listening for telemetry
                    Toast.makeText(this@TelemetryDemoActivity, "Enabling telemetry", Toast.LENGTH_SHORT).show()
                } ?: Toast.makeText(this@TelemetryDemoActivity, "No active flight. Please go to brief and create a flight first.", Toast.LENGTH_SHORT).show()

                override fun onError(e: AirMapException) = Timber.e(e, "Get current flight failed")
            })
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupMapDragging(map: MapboxMap) {
        val screenDensity = resources.displayMetrics.density
        mapView.setOnTouchListener(View.OnTouchListener { _, event ->
            event?.let { event ->
                if (event.pointerCount > 1) {
                    return@OnTouchListener false //Don't drag if there are multiple fingers on screen
                }

                if (myMarker == null) {
                    return@OnTouchListener false
                }

                val tapPoint = PointF(event.x, event.y)
                drag(map.projection.fromScreenLocation(tapPoint), false)

                val toleranceSides = 4 * screenDensity
                val toleranceTopBottom = 10 * screenDensity
                val averageIconWidth = 42 * screenDensity
                val averageIconHeight = 42 * screenDensity
                val tapRect = RectF(tapPoint.x - averageIconWidth / 2 - toleranceSides,
                        tapPoint.y - averageIconHeight / 2 - toleranceTopBottom,
                        tapPoint.x + averageIconWidth / 2 + toleranceSides,
                        tapPoint.y + averageIconHeight / 2 + toleranceTopBottom)

                var newSelectedMarker: Marker? = null
                val nearbyMarkers = map.getMarkerViewsInRect(tapRect)
                val selectedMarkers = map.selectedMarkers
                if (selectedMarkers.isEmpty() && !nearbyMarkers.isEmpty()) {
                    nearbyMarkers.sort()
                    for (marker in nearbyMarkers) {
                        if (marker is MarkerView && !marker.isVisible) {
                            continue //Don't let user click on hidden midpoints
                        }
                        if (marker.title != AnnotationsFactory.INTERSECTION_TAG) {
                            newSelectedMarker = marker
                            break
                        }
                    }
                } else if (selectedMarkers.isNotEmpty()) {
                    newSelectedMarker = selectedMarkers[0]
                }

                if (newSelectedMarker != null && newSelectedMarker is MarkerView) {
                    val doneDragging = event.action == MotionEvent.ACTION_UP
                    val deletePoint = false

                    //DRAG!
                    map.selectMarker(newSelectedMarker) //Use the marker selection state to prevent selecting another marker when dragging over it
                    newSelectedMarker.hideInfoWindow()
                    drag(map.projection.fromScreenLocation(tapPoint), doneDragging)
                    if (doneDragging) {
                        map.deselectMarker(newSelectedMarker)
                    }
                    return@OnTouchListener true
                }
            }
            false
        })
    }

    private fun drag(newLocation: LatLng, doneDragging: Boolean) {
        myMarker?.position = newLocation
        AirMap.getTelemetryService().sendPositionMessage(
                currentFlight!!.flightId,
                newLocation.latitude,
                newLocation.longitude,
                0f,
                newLocation.altitude.toFloat(),
                1f
        )
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
