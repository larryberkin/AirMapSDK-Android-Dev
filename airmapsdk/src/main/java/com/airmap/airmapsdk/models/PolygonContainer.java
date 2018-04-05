package com.airmap.airmapsdk.models;

import com.mapbox.mapboxsdk.annotations.Polygon;
import com.mapbox.mapboxsdk.annotations.PolygonOptions;
import com.mapbox.mapboxsdk.annotations.Polyline;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;

public class PolygonContainer implements Container {
    public Polygon polygon;
    public Polyline outline;

    @Override
    public void clear() {
        polygon = new PolygonOptions().getPolygon();
        outline = new PolylineOptions().getPolyline();
    }

    @Override
    public boolean isValid() {
        return polygon != null && outline != null;
    }
}
