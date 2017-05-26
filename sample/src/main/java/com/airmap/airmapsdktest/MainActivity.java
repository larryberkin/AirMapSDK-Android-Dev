package com.airmap.airmapsdktest;

import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.airmap.airmapsdk.AirMapException;
import com.airmap.airmapsdk.models.Coordinate;
import com.airmap.airmapsdk.models.flight.AirMapFlight;
import com.airmap.airmapsdk.models.pilot.AirMapPilot;
import com.airmap.airmapsdk.models.traffic.AirMapTraffic;
import com.airmap.airmapsdk.networking.callbacks.AirMapCallback;
import com.airmap.airmapsdk.networking.callbacks.AirMapTrafficListener;
import com.airmap.airmapsdk.networking.callbacks.LoginCallback;
import com.airmap.airmapsdk.networking.services.AirMap;
import com.airmap.airmapsdk.networking.services.MappingService;
import com.airmap.airmapsdk.ui.activities.CreateFlightActivity;
import com.mapbox.mapboxsdk.annotations.Icon;
import com.mapbox.mapboxsdk.annotations.IconFactory;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerOptions;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback,
        View.OnClickListener, AirMapTrafficListener, MapboxMap.OnMapLongClickListener {
    private final int REQUEST_LOGIN = 1; //The request code for the LoginActivity
    private final int REQUEST_FLIGHT = 2; //The request code for creating a flight
    private final String[] compassDirections = new String[]{"n", "nne", "ne", "ene", "e", "ese",
            "se", "sse", "s", "ssw", "sw", "wsw", "w", "wnw", "nw", "nnw"}; //Compass directions

    private MapView mapView; //A MapBox MapView
    private MapboxMap map; //A MapboxMap is used to interact with the MapBox SDK

    private FloatingActionButton trafficFab;

    private List<MarkerOptions> markers; //List to keep track of traffic annotations on the map

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        trafficFab = (FloatingActionButton) findViewById(R.id.traffic_fab);
//        trafficFab.hide(); //Wait until user is logged in to show button
        trafficFab.setOnClickListener(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //AirMap.init is called in the onCreate of MyApplication

        //Show the login screen and have the user log in
        AirMap.showLogin(this, new LoginCallback() {
            @Override
            public void onSuccess(AirMapPilot pilot) {
                showToast("Hi, " + pilot.getFirstName() + "!");
                trafficFab.show(); //Show the button now that the user is logged in
                AirMap.getPublicFlights(null, null, null, new AirMapCallback<List<AirMapFlight>>() { //Get all public flights and display them on the map
                    @Override
                    public void onSuccess(List<AirMapFlight> response) {
                        if (map != null) { //Make sure the map has been initialized already
                            for (AirMapFlight publicFlight : response) {
                                //Add a map annotation with the location of the flight
                                map.addMarker(new MarkerOptions()
                                        .icon(IconFactory.getInstance(MainActivity.this).fromResource(R.drawable.airmap_flight_marker))
                                        .position(getLatLngFromCoordinate(publicFlight.getCoordinate())));
                            }
                        }
                    }

                    @Override
                    public void onError(AirMapException e) {
                        Log.e("MainActivity", "Error getting flights");
                    }
                });
            }

            @Override
            public void onError(AirMapException e) {
                Toast.makeText(MainActivity.this, "Login Failed :(", Toast.LENGTH_SHORT).show();
            }
        });

//        anonymousLogin();

        mapView = (MapView) findViewById(R.id.map);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        markers = new ArrayList<>();
        showToast("Tap and hold to create a flight");
    }

    /**
     * When the user finishes logging in, this method is called with the requestCode that was
     * originally passed in, whether the login was successful or not, and an AirMapPilot if the
     * Login was successful
     *
     * @param requestCode The requestCode originally passed in. In this case, it is REQUEST_LOGIN
     * @param resultCode  Either Activity.RESULT_OK or Activity.RESULT_CANCELLED
     * @param data        If resultCode == Activity.RESULT_OK, then data will contain an
     *                    AirMapPilot
     *                    with the key LoginActivity.PILOT
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_FLIGHT && resultCode == RESULT_OK) { //Check if flight creation was successful
            AirMapFlight flight = (AirMapFlight) data.getSerializableExtra(CreateFlightActivity.FLIGHT); //Get the flight from the data
            map.addMarker(new MarkerOptions() //Add the flight to the map
                    .position(getLatLngFromCoordinate(flight.getCoordinate()))
                    .icon(IconFactory.getInstance(this).fromResource(R.drawable.airmap_flight_marker)));
        }
    }

    /**
     * MapBox method that is called when the map is ready
     */
    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        map = mapboxMap;
        map.setOnMapLongClickListener(this);
    }

    /**
     * Called when the Traffic FAB is clicked. The purpose is to enable traffic alerts if the pilot
     * has an active flight
     */
    @Override
    public void onClick(final View view) {
        if (!AirMap.getAirMapTrafficService().isConnected()) {
            AirMap.enableTrafficAlerts(this);
            showToast("Now receiving traffic alerts");
        } else {
            AirMap.disableTrafficAlerts();
            showToast("Turning off traffic alerts");
        }

        //This part is not required to start traffic alerts
        AirMap.getCurrentFlight(new AirMapCallback<AirMapFlight>() { //Check if user has an active flight
            @Override
            public void onSuccess(AirMapFlight response) {
                if (response == null) { //If response is null, then there is no active flight
                    showToast("You do not have an active flight");
                }
            }

            @Override
            public void onError(AirMapException e) {
            }
        });
    }

    /**
     * Called when traffic needs to be added
     *
     * @param added A list of traffic that needs to be added to the map
     */
    @Override
    public void onAddTraffic(List<AirMapTraffic> added) {
        for (final AirMapTraffic traffic : added) {
            final MarkerOptions marker = new MarkerOptions()
                    .position(getLatLngFromCoordinate(traffic.getCoordinate())) //Place an annotation at the traffic's location
                    .title(traffic.getId()) //Set the title of the popup window
                    .icon(getIcon(traffic)); //Set the icon
            markers.add(marker);
            runOnUiThread(new Runnable() {
                public void run() {
                    map.addMarker(marker); //Add it to the map
                }
            });
        }
    }

    /**
     * Called when traffic needs to be updated
     *
     * @param updated A list of traffic that is already on the map whose location/speed/etc need to
     *                be updated
     */
    @Override
    public void onUpdateTraffic(List<AirMapTraffic> updated) {
        for (AirMapTraffic traffic : updated) {
            final MarkerOptions options = searchForId(traffic.getId()); //Find the annotation that needs to be updated
            if (options == null) {
                return; //If no traffic with that Id, don't do anything
            }
            final LatLng old = options.getPosition();
            markers.remove(options); //Remove old annotation from list
            options.position(getLatLngFromCoordinate(traffic.getCoordinate())); //Update the annotation's location
            options.icon(getIcon(traffic)); //The icon for the traffic may have changed (it could have changed directions)
            markers.add(options); //Add new annotation to list
            runOnUiThread(new Runnable() {
                public void run() {
                    Marker marker = options.getMarker();
                    ValueAnimator markerAnimator = ObjectAnimator.ofObject(marker, "position", new LatLngEvaluator(), old, marker.getPosition());
                    markerAnimator.setDuration(1100);
                    markerAnimator.start(); //Animate the traffic's location from old position to new position
                }
            });
        }
    }

    /**
     * Called when traffic needs to be removed
     *
     * @param removed A list of traffic that should no longer be on the map
     */
    @Override
    public void onRemoveTraffic(List<AirMapTraffic> removed) {
        for (AirMapTraffic traffic : removed) {
            final MarkerOptions options = searchForId(traffic.getId()); //Find the traffic that needs to be removed
            if (options == null) {
                return;
            }
            markers.remove(options);
            runOnUiThread(new Runnable() {
                public void run() {
                    map.removeMarker(options.getMarker()); //Remove the traffic
                }
            });
        }
    }

    /**
     * Called when the map is long clicked
     * @param point The location of the click
     */
    @Override
    public void onMapLongClick(@NonNull LatLng point) {
        //The created flight will be returned in onActivityResult
        AirMap.createFlight(this, REQUEST_FLIGHT, getCoordinateFromLatLng(point), null, null, MappingService.AirMapMapTheme.Standard);
    }


    /**
     * Dynamically provides an icon based on which direction the traffic is traveling
     *
     * @param traffic The traffic to get an icon for
     * @return An icon
     */
    private Icon getIcon(AirMapTraffic traffic) {
        //Generate the icon dynamically based on which direction the traffic is pointing/traveling
        IconFactory factory = IconFactory.getInstance(this);
        int id = 0;
        if (traffic == null) {
            id = R.drawable.airmap_flight_marker;
        } else if (traffic.getTrafficType() == AirMapTraffic.TrafficType.Alert) {
            id = getResources().getIdentifier("traffic_marker_icon_" + directionFromBearing(traffic.getTrueHeading()), "drawable", "com.airmap.airmapsdktest");
        } else if (traffic.getTrafficType() == AirMapTraffic.TrafficType.SituationalAwareness) {
            id = getResources().getIdentifier("sa_traffic_marker_icon_" + directionFromBearing(traffic.getTrueHeading()), "drawable", "com.airmap.airmapsdktest");
        }
        return factory.fromDrawable(ContextCompat.getDrawable(this, id));
    }

    /**
     * Search for an annotation
     *
     * @param id The id to search for
     * @return The found annotation
     */
    private MarkerOptions searchForId(String id) {
        for (MarkerOptions options : markers) {
            if (options.getTitle().equals(id)) {
                return options;
            }
        }
        return null;
    }

    /**
     * Used to animate traffic markers
     */
    private static class LatLngEvaluator implements TypeEvaluator<LatLng> {
        // Method is used to interpolate the marker animation.
        private LatLng latLng = new LatLng();
        @Override
        public LatLng evaluate(float fraction, LatLng startValue, LatLng endValue) {
            latLng.setLatitude(startValue.getLatitude() + ((endValue.getLatitude() - startValue.getLatitude()) * fraction));
            latLng.setLongitude(startValue.getLongitude() + ((endValue.getLongitude() - startValue.getLongitude()) * fraction));
            return latLng;
        }
    }

    /**
     * Converts a bearing into a compass direction
     *
     * @param bearing The bearing to convert
     * @return A compass direction
     */
    public String directionFromBearing(double bearing) {
        int index = (int) ((bearing / 22.5) + 0.5) % 16;
        return compassDirections[index];

    }

    /**
     * Utility method to convert from an AirMap Coordinate to a MapBox LatLng
     *
     * @param coordinate An AirMap Coordinate
     * @return A MapBox LatLng
     */
    public static LatLng getLatLngFromCoordinate(Coordinate coordinate) {
        if (coordinate != null) {
            return new LatLng(coordinate.getLatitude(), coordinate.getLongitude());
        }
        return null;
    }

    /**
     * Utility method to convert from a MapBox LatLng to an AirMap Coordinate
     * @param point A MapBox LatLng
     * @return An AirMap Coordinate
     */
    public static Coordinate getCoordinateFromLatLng(LatLng point) {
        if (point != null) {
            return new Coordinate(point.getLatitude(), point.getLongitude());
        }
        return null;
    }

    public void showToast(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, text, Toast.LENGTH_SHORT).show();
            }
        });
    }

    //MapBox required Override methods
    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    private void anonymousLogin() {
        AirMap.performAnonymousLogin("your_user_id", new AirMapCallback<Void>() {
            @Override
            public void onSuccess(Void response) {
                Log.v("Anon Login", "Token is: " + AirMap.getAuthToken());
            }

            @Override
            public void onError(AirMapException e) {
                Log.e("Anon Login", e.getDetailedMessage(), e);
            }
        });
    }
}