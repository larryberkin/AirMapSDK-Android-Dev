package com.airmap.airmapsdk.controllers;

import com.airmap.airmapsdk.AirMapException;
import com.airmap.airmapsdk.models.Coordinate;
import com.airmap.airmapsdk.models.rules.AirMapJurisdiction;
import com.airmap.airmapsdk.models.shapes.AirMapPolygon;
import com.airmap.airmapsdk.networking.callbacks.AirMapCallback;
import com.airmap.airmapsdk.networking.services.AirMap;
import com.airmap.airmapsdk.ui.views.AirMapMapView;

import java.util.List;

import okhttp3.Call;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.functions.Func1;
import rx.subscriptions.Subscriptions;

public class FlightPlanDataController extends MapDataController {

    private AirMapPolygon polygon;
    private Coordinate takeoffCoordinate;
    private float buffer;
    private float altitude;

    public FlightPlanDataController(AirMapMapView map, AirMapMapView.Configuration configuration) {
        super(map, configuration);
    }

    @Override
    protected Func1<AirMapPolygon, Observable<List<AirMapJurisdiction>>> getJurisdictions() {
        return polygon -> Observable.create((Observable.OnSubscribe<List<AirMapJurisdiction>>) subscriber -> {
            final Call statusCall = AirMap.getJurisdictions(polygon, new AirMapCallback<List<AirMapJurisdiction>>() {
                @Override
                public void onSuccess(final List<AirMapJurisdiction> response) {
                    subscriber.onNext(response);
                    subscriber.onCompleted();
                }

                @Override
                public void onError(AirMapException e) {
                    if (polygon == null) {
                        subscriber.onNext(null);
                        subscriber.onCompleted();
                    } else {
                        subscriber.onError(e);
                    }
                }
            });

            subscriber.add(Subscriptions.create(statusCall::cancel));
        })
        .onErrorResumeNext(throwable -> Observable.just(null));
    }

    @Override
    public void onMapRegionChanged() {
        // map region does not affect jurisdictions, rulesets or advisories in this mode
    }

    public void onPolygonChanged(AirMapPolygon polygon, Coordinate takeoffCoordinate, float buffer) {
        this.polygon = polygon;
        this.takeoffCoordinate = takeoffCoordinate;
        this.buffer = buffer;

        jurisdictionsPublishSubject.onNext(polygon);
    }

    public void onAltitudeChanged(float altitude) {
        this.altitude = altitude;
    }

    public AirMapPolygon getPolygon() {
        return polygon;
    }

    public Coordinate getTakeoffCoordinate() {
        return takeoffCoordinate;
    }

    public float getBuffer() {
        return buffer;
    }

    public float getAltitude() {
        return altitude;
    }
}
