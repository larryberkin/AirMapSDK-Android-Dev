package com.airmap.airmapsdktest.ui;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import com.airmap.airmapsdk.models.traffic.AirMapTraffic;
import com.mapbox.mapboxsdk.annotations.BaseMarkerOptions;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;

public class TrafficMarkerOptions extends BaseMarkerOptions<TrafficMarker, TrafficMarkerOptions> {

    private AirMapTraffic traffic;

    /**
     * Will update the traffic property AND the position on the map
     */
    public TrafficMarkerOptions setTraffic(AirMapTraffic traffic) {
        this.traffic = traffic;

        LatLng latLng = traffic.getCoordinate() != null ? traffic.getCoordinate().toMapboxLatLng() : null;
        return this.position(latLng).title("traffic");
    }

    public TrafficMarkerOptions() {
    }

    private TrafficMarkerOptions(Parcel in) {
        position((LatLng) in.readParcelable(LatLng.class.getClassLoader()));
        snippet(in.readString());
        String iconId = in.readString();
        Bitmap iconBitmap = in.readParcelable(Bitmap.class.getClassLoader());
        Icon icon = IconFactory.recreate(iconId, iconBitmap);
        icon(icon);
        title(in.readString());
    }

    @Override
    public TrafficMarkerOptions getThis() {
        return this;
    }

    @Override
    public TrafficMarker getMarker() {
        return new TrafficMarker(this, traffic);
    }

    public static final Parcelable.Creator<TrafficMarkerOptions> CREATOR = new Parcelable.Creator<TrafficMarkerOptions>() {
        public TrafficMarkerOptions createFromParcel(Parcel in) {
            return new TrafficMarkerOptions(in);
        }

        public TrafficMarkerOptions[] newArray(int size) {
            return new TrafficMarkerOptions[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeParcelable(position, flags);
        out.writeString(snippet);
        out.writeString(icon.getId());
        out.writeParcelable(icon.getBitmap(), flags);
        out.writeString(title);
    }
}
