package com.airmap.sample.ui

import android.graphics.Bitmap
import android.os.Parcel
import android.os.Parcelable

import com.airmap.airmapsdk.models.traffic.AirMapTraffic
import com.mapbox.mapboxsdk.annotations.BaseMarkerOptions
import com.mapbox.mapboxsdk.annotations.IconFactory
import com.mapbox.mapboxsdk.geometry.LatLng

class TrafficMarkerOptions : BaseMarkerOptions<TrafficMarker, TrafficMarkerOptions> {

    private val traffic: AirMapTraffic

    /**
     * Will update the traffic property AND the position on the map
     */
    constructor(traffic: AirMapTraffic) {
        this.traffic = traffic
        val latLng = if (traffic.coordinate != null) traffic.coordinate.toMapboxLatLng() else null
        position(latLng)
        title("traffic")
    }

    private constructor(parcel: Parcel) {
        position(parcel.readParcelable<Parcelable>(LatLng::class.java.classLoader) as LatLng)
        snippet(parcel.readString())
        val iconId = parcel.readString()
        val iconBitmap = parcel.readParcelable<Bitmap>(Bitmap::class.java.classLoader)
        val icon = IconFactory.recreate(iconId, iconBitmap)
        icon(icon)
        title(parcel.readString())
    }

    override fun getThis() = this

    override fun getMarker() = TrafficMarker(this, traffic)

    override fun describeContents() = 0

    override fun writeToParcel(out: Parcel, flags: Int) = with(out) {
        writeParcelable(position, flags)
        writeString(snippet)
        writeString(icon.id)
        writeParcelable(icon.bitmap, flags)
        writeString(title)
    }

    companion object {
        @JvmStatic
        val CREATOR: Parcelable.Creator<TrafficMarkerOptions> = object : Parcelable.Creator<TrafficMarkerOptions> {
            override fun createFromParcel(parcel: Parcel) = TrafficMarkerOptions(parcel)
            override fun newArray(size: Int): Array<TrafficMarkerOptions?> = arrayOfNulls(size)
        }
    }
}
