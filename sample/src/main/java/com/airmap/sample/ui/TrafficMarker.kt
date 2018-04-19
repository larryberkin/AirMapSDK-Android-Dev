package com.airmap.sample.ui

import com.airmap.airmapsdk.models.traffic.AirMapTraffic
import com.mapbox.mapboxsdk.annotations.Marker

class TrafficMarker(trafficMarkerOptions: TrafficMarkerOptions, var traffic: AirMapTraffic) : Marker(trafficMarkerOptions)
