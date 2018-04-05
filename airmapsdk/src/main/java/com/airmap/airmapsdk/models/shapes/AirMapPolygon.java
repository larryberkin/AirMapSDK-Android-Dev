package com.airmap.airmapsdk.models.shapes;

import android.text.TextUtils;

import com.airmap.airmapsdk.models.Coordinate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@SuppressWarnings("unused")
public class AirMapPolygon extends AirMapGeometry implements Serializable {

    private List<Coordinate> coordinates;

    public AirMapPolygon(String polygonGeometry) {
        if (!polygonGeometry.toUpperCase().startsWith("POLYGON")) {
            throw new IllegalArgumentException("Illegal linestring");
        }
        List<Coordinate> coordinates = new ArrayList<>();
        int startIndex = polygonGeometry.indexOf('('); //This is where the coordinates will start
        int endIndex = polygonGeometry.indexOf(')');
        Scanner in = new Scanner(polygonGeometry.substring(startIndex, endIndex));
        while (in.hasNextDouble()) {
            double lat = in.nextDouble();
            if (in.hasNextDouble()) {
                double lng = in.nextDouble();
                coordinates.add(new Coordinate(lat, lng));
            }
        }
        in.close();
        setCoordinates(coordinates);
    }

    public AirMapPolygon(List<Coordinate> coordinates) {
        this.coordinates = coordinates;
    }

    public AirMapPolygon() {

    }

    public List<Coordinate> getCoordinates() {
        return coordinates;
    }

    public AirMapPolygon setCoordinates(List<Coordinate> coordinates) {
        this.coordinates = coordinates;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof AirMapPolygon && getCoordinates().equals(((AirMapPolygon) o).getCoordinates());
    }

    @Override
    public String toString() {
        return "POLYGON" +
                "(" +
                TextUtils.join(",", coordinates) +
                ")";
    }
}