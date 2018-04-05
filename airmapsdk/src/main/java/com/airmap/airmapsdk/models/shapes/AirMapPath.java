package com.airmap.airmapsdk.models.shapes;

import android.text.TextUtils;

import com.airmap.airmapsdk.models.Coordinate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

@SuppressWarnings("unused")
public class AirMapPath extends AirMapGeometry implements Serializable {

    private List<Coordinate> coordinates;

    public AirMapPath(String lineString) {
        if (!lineString.toUpperCase().startsWith("LINESTRING")) {
            throw new IllegalArgumentException("Illegal linestring");
        }
        List<Coordinate> coordinates = new ArrayList<>();
        int startIndex = lineString.indexOf('('); //This is where the coordinates will start
        int endIndex = lineString.indexOf(')');
        Scanner in = new Scanner(lineString.substring(startIndex, endIndex));
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

    public AirMapPath(List<Coordinate> coordinates) {
        this.coordinates = coordinates;
    }

    public AirMapPath() {

    }

    public List<Coordinate> getCoordinates() {
        return coordinates;
    }

    public AirMapPath setCoordinates(List<Coordinate> coordinates) {
        this.coordinates = coordinates;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof AirMapPath && getCoordinates().equals(((AirMapPath) o).getCoordinates());
    }

    @Override
    public String toString() {
        return "LINESTRING" +
                "(" +
                TextUtils.join(", ", coordinates) +
                ")";
    }
}