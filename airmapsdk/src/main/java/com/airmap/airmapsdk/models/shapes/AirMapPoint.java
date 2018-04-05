package com.airmap.airmapsdk.models.shapes;

import com.airmap.airmapsdk.models.Coordinate;

import java.io.Serializable;
import java.util.Scanner;

@SuppressWarnings("unused")
public class AirMapPoint extends AirMapGeometry implements Serializable {

    private Coordinate coordinate;

    public AirMapPoint(String pointString) {
        int startIndex = pointString.indexOf('('); //This is where the coordinates will start
        int endIndex = pointString.indexOf(')');
        Scanner in = new Scanner(pointString.substring(startIndex, endIndex));
        if (in.hasNextDouble()) {
            double lat = in.nextDouble();
            if (in.hasNextDouble()) {
                double lng = in.nextDouble();
                setCoordinate(new Coordinate(lat, lng));
            }
        }
        in.close();
    }

    public AirMapPoint(Coordinate coordinate) {
        this.coordinate = coordinate;
    }

    public AirMapPoint() {

    }

    public Coordinate getCoordinate() {
        return coordinate;
    }

    public AirMapPoint setCoordinate(Coordinate coordinate) {
        this.coordinate = coordinate;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof AirMapPoint && getCoordinate().equals(((AirMapPoint) o).getCoordinate());
    }

    @Override
    public String toString() {
        return "POINT" +
                "(" +
                coordinate.toString() +
                ")";
    }
}
