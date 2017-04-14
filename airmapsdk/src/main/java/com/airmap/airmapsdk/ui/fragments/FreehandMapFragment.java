package com.airmap.airmapsdk.ui.fragments;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.airmap.airmapsdk.AdvisoriesBottomSheetAdapter;
import com.airmap.airmapsdk.AirMapException;
import com.airmap.airmapsdk.AirMapLog;
import com.airmap.airmapsdk.Analytics;
import com.airmap.airmapsdk.DrawingCallback;
import com.airmap.airmapsdk.R;
import com.airmap.airmapsdk.models.CircleContainer;
import com.airmap.airmapsdk.models.Coordinate;
import com.airmap.airmapsdk.models.LineContainer;
import com.airmap.airmapsdk.models.PolygonContainer;
import com.airmap.airmapsdk.models.airspace.AirMapAirspace;
import com.airmap.airmapsdk.models.flight.AirMapFlight;
import com.airmap.airmapsdk.models.permits.AirMapAvailablePermit;
import com.airmap.airmapsdk.models.permits.AirMapPermitIssuer;
import com.airmap.airmapsdk.models.permits.AirMapPilotPermit;
import com.airmap.airmapsdk.models.shapes.AirMapGeometry;
import com.airmap.airmapsdk.models.shapes.AirMapPath;
import com.airmap.airmapsdk.models.shapes.AirMapPoint;
import com.airmap.airmapsdk.models.shapes.AirMapPolygon;
import com.airmap.airmapsdk.models.status.AirMapStatus;
import com.airmap.airmapsdk.models.status.AirMapStatusAdvisory;
import com.airmap.airmapsdk.networking.callbacks.AirMapCallback;
import com.airmap.airmapsdk.networking.services.AirMap;
import com.airmap.airmapsdk.networking.services.AirspaceService;
import com.airmap.airmapsdk.networking.services.MappingService;
import com.airmap.airmapsdk.ui.activities.CreateFlightActivity;
import com.airmap.airmapsdk.ui.views.ClickableDrawableButton;
import com.airmap.airmapsdk.ui.views.DrawingBoard;
import com.airmap.airmapsdk.ui.views.ImageViewSwitch;
import com.airmap.airmapsdk.ui.views.Scratchpad;
import com.airmap.airmapsdk.util.AnnotationsFactory;
import com.airmap.airmapsdk.util.PointMath;
import com.airmap.airmapsdk.util.Utils;
import com.github.lzyzsd.jsbridge.BridgeWebView;
import com.github.lzyzsd.jsbridge.CallBackFunction;
import com.mapbox.mapboxsdk.annotations.Marker;
import com.mapbox.mapboxsdk.annotations.MarkerView;
import com.mapbox.mapboxsdk.annotations.Polygon;
import com.mapbox.mapboxsdk.annotations.PolygonOptions;
import com.mapbox.mapboxsdk.annotations.PolylineOptions;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.Call;

import static com.airmap.airmapsdk.models.status.AirMapStatus.StatusColor.Green;
import static com.airmap.airmapsdk.models.status.AirMapStatus.StatusColor.Yellow;
import static com.airmap.airmapsdk.util.PointMath.distanceBetween;
import static com.airmap.airmapsdk.util.Utils.getBufferPresets;
import static com.airmap.airmapsdk.util.Utils.getBufferPresetsMetric;

/**
 * Created by Vansh Gandhi on 11/13/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */

public class FreehandMapFragment extends Fragment implements OnMapReadyCallback,
        DrawingCallback, MapboxMap.OnCameraChangeListener, AirMapCallback<AirMapStatus> {

    private static final String TAG = "FreehandMapFragment";

    private static final String CIRCLE_TAG = "circle";
    private static final String PATH_TAG = "path";
    private static final String POLYGON_TAG = "polygon";

    private static final int INDEX_OF_CIRCLE_TAB = 0;
    private static final int INDEX_OF_PATH_TAB = 1;
    private static final int INDEX_OF_POLYGON_TAB = 2;

    //Main layout views
    private TabLayout tabLayout;
    private RelativeLayout seekBarContainer;
    private SeekBar seekBar;
    private TextView seekBarLabelTextView;
    private TextView seekBarValueTextView;
    private ImageViewSwitch enableDrawingSwitch;
    private ImageView deleteButton;
    private TextView tipTextView;
    private MapView mapView;
    private DrawingBoard drawingBoard;
    private Scratchpad scratchpad;
    private ClickableDrawableButton nextButton;
    private BridgeWebView webView; //For running javascript

    //Bottom sheet layout views
    private CoordinatorLayout bottomSheetLayout;
    private RecyclerView recyclerView;

    private BottomSheetBehavior bottomSheetBehavior;

    private MapboxMap map;

    private Map<String, AirMapStatusAdvisory> permitAdvisories;
    private Map<String, Polygon> polygonMap;
    private CircleContainer circleContainer;
    private PolygonContainer polygonContainer;
    private LineContainer lineContainer;
    private List<MarkerView> corners;
    private List<MarkerView> midpoints;
    private List<MarkerView> intersections; //Store this in the Polygon class? since this is only relevant for polygon
    private List<Polygon> redPolygons;
    private Rect deleteCoordinates = new Rect();
    private AirMapStatus latestStatus;
    private Call airspaceCall;
    private Call statusCall;
    private float screenDensity;

    private OnFragmentInteractionListener mListener;

    //Required empty constructor
    public FreehandMapFragment() {
    }

    /**
     * Create a new instance of the class
     *
     * @param coordinate The coordinate the center the map at
     * @return a new instance of the FreehandMapFragment
     */
    public static FreehandMapFragment newInstance(Coordinate coordinate) {
        Bundle args = new Bundle();
        args.putSerializable(CreateFlightActivity.COORDINATE, coordinate);
        FreehandMapFragment fragment = new FreehandMapFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.airmap_fragment_freehand, container, false);
        initializeViews(view); //Instantiate all the views
        setupBottomSheet();
        setupSwitch();
        setupMap(savedInstanceState);
        setupButtons();
        drawingBoard.setDoneDrawingCallback(this);

        permitAdvisories = new HashMap<>();
        polygonMap = new HashMap<>();

        circleContainer = new CircleContainer();
        lineContainer = new LineContainer();
        polygonContainer = new PolygonContainer();

        corners = new ArrayList<>();
        midpoints = new ArrayList<>();
        intersections = new ArrayList<>();
        redPolygons = new ArrayList<>();

        screenDensity = getResources().getDisplayMetrics().density;

        return view;
    }

    private void initializeViews(View view) {
        seekBarContainer = (RelativeLayout) view.findViewById(R.id.seekbar_container);
        seekBar = (SeekBar) view.findViewById(R.id.seekbar);
        seekBarLabelTextView = (TextView) view.findViewById(R.id.label);
        seekBarValueTextView = (TextView) view.findViewById(R.id.seekbar_value);
        enableDrawingSwitch = (ImageViewSwitch) view.findViewById(R.id.action_button);
        deleteButton = (ImageView) view.findViewById(R.id.delete_button);
        tipTextView = (TextView) view.findViewById(R.id.tip_text);
        mapView = (MapView) view.findViewById(R.id.map);
        drawingBoard = (DrawingBoard) view.findViewById(R.id.drawFrame);
        scratchpad = (Scratchpad) view.findViewById(R.id.scratchpad);
        nextButton = (ClickableDrawableButton) view.findViewById(R.id.next_button);

        //This hidden WebView does the turf line buffering
        webView = new BridgeWebView(getActivity());
        webView.setWillNotDraw(true);
        webView.loadUrl("file:///android_asset/turf.html");

        bottomSheetLayout = (CoordinatorLayout) view.findViewById(R.id.bottom_sheet);
        recyclerView = (RecyclerView) view.findViewById(R.id.advisories_list);
    }

    private void setupMap(Bundle savedInstanceState) {
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
    }

    private void setupTabs() {
        tabLayout = mListener.getTabLayout();
        tabLayout.clearOnTabSelectedListeners();
        tabLayout.removeAllTabs();
        tabLayout.setVisibility(View.VISIBLE);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            String lastTab;

            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                cancelNetworkCalls();
                clear();

                String analyticsPage = Analytics.Page.POINT_CREATE_FLIGHT;
                if (CIRCLE_TAG.equals(lastTab)) {
                    analyticsPage = Analytics.Page.POINT_CREATE_FLIGHT;
                } else if (PATH_TAG.equals(lastTab)) {
                    analyticsPage = Analytics.Page.PATH_CREATE_FLIGHT;
                } else if (POLYGON_TAG.equals(lastTab)) {
                    analyticsPage = Analytics.Page.POLYGON_CREATE_FLIGHT;
                }

                String tag = (String) tab.getTag();
                if (CIRCLE_TAG.equals(tag)) {
                    showSeekBarForCircle();
                    tipTextView.setVisibility(View.GONE);
                    drawingBoard.setPolygonMode(true); //Closes the drawn path
                    drawingBoard.setClickable(false);
                    drawingBoard.setVisibility(View.GONE);
                    enableDrawingSwitch.setVisibility(View.GONE);
                    deleteButton.setVisibility(View.GONE);

                    Analytics.logEvent(analyticsPage, Analytics.Action.tap, Analytics.Label.POINT);
                } else if (PATH_TAG.equals(tag)) {
                    showSeekBarForPath();
                    updateTip(R.string.airmap_freehand_tip_path);
                    drawingBoard.setPolygonMode(false); //Doesn't close the drawn path
                    drawingBoard.setVisibility(View.VISIBLE);
                    enableDrawingSwitch.setVisibility(View.VISIBLE);
                    enableDrawingSwitch.setChecked(true);
                    showDeleteButton(false);

                    Analytics.logEvent(analyticsPage, Analytics.Action.tap, Analytics.Label.PATH);
                } else if (POLYGON_TAG.equals(tag)) {
                    hideSeekBar();
                    updateTip(R.string.airmap_freehand_tip_area);
                    drawingBoard.setPolygonMode(true);
                    drawingBoard.setVisibility(View.VISIBLE);
                    enableDrawingSwitch.setVisibility(View.VISIBLE);
                    enableDrawingSwitch.setChecked(true);
                    showDeleteButton(false);

                    Analytics.logEvent(analyticsPage, Analytics.Action.tap, Analytics.Label.POLYGON);
                }

                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                lastTab = (String) tab.getTag();
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                String tag = (String) tab.getTag();
                if (CIRCLE_TAG.equals(tag)) {
                    Analytics.logEvent(Analytics.Page.POINT_CREATE_FLIGHT, Analytics.Action.tap, Analytics.Label.POINT);
                } else if (PATH_TAG.equals(tag)) {
                    Analytics.logEvent(Analytics.Page.PATH_CREATE_FLIGHT, Analytics.Action.tap, Analytics.Label.PATH);
                } else if (POLYGON_TAG.equals(tag)) {
                    Analytics.logEvent(Analytics.Page.POLYGON_CREATE_FLIGHT, Analytics.Action.tap, Analytics.Label.POLYGON);
                }
            }
        });

        //Add tabs after listener so that the callback is invoked
        tabLayout.addTab(getTab(tabLayout, R.string.airmap_circle, R.drawable.ic_point_tab, CIRCLE_TAG));
        tabLayout.addTab(getTab(tabLayout, R.string.airmap_path, R.drawable.ic_path_tab, PATH_TAG));
        tabLayout.addTab(getTab(tabLayout, R.string.airmap_polygon, R.drawable.ic_polygon_tab, POLYGON_TAG));
    }

    public TabLayout.Tab getTab(TabLayout tabLayout, @StringRes int textId, @DrawableRes int iconId, String id) {
        TabLayout.Tab tab = tabLayout.newTab();
        tab.setText(textId);
        tab.setIcon(iconId);
        tab.setTag(id);
        return tab;
    }

    private void setupSwitch() {
        enableDrawingSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                drawingBoard.setVisibility(isChecked ? View.VISIBLE : View.INVISIBLE);
                if (isChecked) {
                    updateTip(drawingBoard.isPolygonMode() ? R.string.airmap_draw_freehand_area : R.string.airmap_draw_freehand_path);
                } else {
                    updateTip(tabLayout.getSelectedTabPosition() == INDEX_OF_POLYGON_TAB ? R.string.airmap_freehand_tip_area : R.string.airmap_freehand_tip_path);
                }
            }
        });
    }

    private void setupButtons() {
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onDeleteClick();
            }
        });
        nextButton.setDrawableClickListener(new ClickableDrawableButton.DrawableClickListener() {
            @Override
            public void onDrawableClick() {
                onNextButtonDrawableClick();
            }
        });
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNextButtonClick();
            }
        });
    }

    private void onDeleteClick() {
        clear();
        showDeleteButton(false);
        if (tabLayout.getSelectedTabPosition() == INDEX_OF_POLYGON_TAB) {
            //Reset in case we displayed the intersection error message
            updateTip(R.string.airmap_freehand_tip_area);

            Analytics.logEvent(Analytics.Page.POLYGON_CREATE_FLIGHT, Analytics.Action.tap, Analytics.Label.TRASH_ICON);
        } else {
            Analytics.logEvent(Analytics.Page.PATH_CREATE_FLIGHT, Analytics.Action.tap, Analytics.Label.TRASH_ICON);
        }
    }

    private void onNextButtonClick() {
        if (mListener != null && tabLayout != null) {
            mListener.setPathBufferPoints(null); //reset buffer polygon
            if (tabLayout.getSelectedTabPosition() == INDEX_OF_CIRCLE_TAB && circleContainer.isValid()) { //Circle
                Coordinate center = new Coordinate(circleContainer.center);
                mListener.getFlight().setGeometry(new AirMapPoint(center));
                mListener.getFlight().setCoordinate(center);
                mListener.getFlight().setBuffer(circleContainer.radius);

                tabLayout.setVisibility(View.GONE);
                mListener.freehandNextClicked();

                Analytics.logEvent(Analytics.Page.POINT_CREATE_FLIGHT, Analytics.Action.tap, Analytics.Label.NEXT);
            } else if (tabLayout.getSelectedTabPosition() == INDEX_OF_PATH_TAB && lineContainer.isValid()) { //Path
                List<Coordinate> coordinates = new ArrayList<>();
                for (LatLng latLng : lineContainer.line.getPoints()) {
                    coordinates.add(new Coordinate(latLng));
                }
                mListener.getFlight().setGeometry(new AirMapPath(coordinates));
                mListener.getFlight().setBuffer(lineContainer.width);
                List<LatLng>[] bufferPoints = new ArrayList[lineContainer.buffers.size()];
                for (int i = 0; i < lineContainer.buffers.size(); i++) {
                    bufferPoints[i] = lineContainer.buffers.get(i).getPoints();
                }
                mListener.setPathBufferPoints(bufferPoints);

                tabLayout.setVisibility(View.GONE);
                mListener.freehandNextClicked();

                Analytics.logEvent(Analytics.Page.PATH_CREATE_FLIGHT, Analytics.Action.tap, Analytics.Label.NEXT);
            } else if (tabLayout.getSelectedTabPosition() == INDEX_OF_POLYGON_TAB && polygonContainer.isValid()) { //Polygon
                if (!PointMath.findIntersections(polygonContainer.polygon.getPoints()).isEmpty()) {
                    Toast.makeText(getActivity(), R.string.airmap_error_overlap, Toast.LENGTH_SHORT).show();
                    return;
                }
                List<Coordinate> coordinates = new ArrayList<>();
                for (LatLng latLng : polygonContainer.polygon.getPoints()) {
                    coordinates.add(new Coordinate(latLng));
                }
                mListener.getFlight().setGeometry(new AirMapPolygon(coordinates));

                tabLayout.setVisibility(View.GONE);
                mListener.freehandNextClicked();

                Analytics.logEvent(Analytics.Page.POLYGON_CREATE_FLIGHT, Analytics.Action.tap, Analytics.Label.NEXT);
            }
        }
    }

    private void onNextButtonDrawableClick() {
        if (latestStatus != null && !latestStatus.getAdvisories().isEmpty()) {
            Map<String, List<AirMapStatusAdvisory>> sections = new HashMap<>();
            for (AirMapStatusAdvisory advisory : latestStatus.getAdvisories()) {
                if (advisory.getType() != null) {
                    String key;
                    AirMapStatus.StatusColor statusColor = advisory.getColor();
                    switch (statusColor) {
                        case Red:
                            key = getResources().getString(R.string.flight_strictly_regulated);
                            break;
                        case Yellow:
                            key = getResources().getString(R.string.advisories);
                            break;
                        case Green:
                        default:
                            key = getResources().getString(R.string.informational);
                            break;
                    }
                    if (sections.get(key) == null) {
                        sections.put(key, new ArrayList<AirMapStatusAdvisory>());
                    }
                    sections.get(key).add(advisory);
                }
            }

            Map<String, String> organizationMap = new HashMap<>();
            if (latestStatus.getOrganizations() != null) {
                for (AirMapPermitIssuer issuer : latestStatus.getOrganizations()) {
                    organizationMap.put(issuer.getId(), issuer.getName());
                }
            }

            recyclerView.setAdapter(new AdvisoriesBottomSheetAdapter(getActivity(), sections, organizationMap));
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);


            if (tabLayout.getSelectedTabPosition() == INDEX_OF_CIRCLE_TAB) {
                Analytics.logEvent(Analytics.Page.POINT_CREATE_FLIGHT, Analytics.Action.tap, Analytics.Label.ADVISORY_ICON);
            } else if (tabLayout.getSelectedTabPosition() == INDEX_OF_PATH_TAB) {
                Analytics.logEvent(Analytics.Page.PATH_CREATE_FLIGHT, Analytics.Action.tap, Analytics.Label.ADVISORY_ICON);
            } else if (tabLayout.getSelectedTabPosition() == INDEX_OF_POLYGON_TAB) {
                Analytics.logEvent(Analytics.Page.POLYGON_CREATE_FLIGHT, Analytics.Action.tap, Analytics.Label.ADVISORY_ICON);
            }
        }
    }

    private void setupBottomSheet() {
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetLayout);
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (mListener != null) {
                    if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                        mListener.bottomSheetOpened();
                    } else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                        mListener.bottomSheetClosed();
                    } else if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                        mListener.bottomSheetClosed();
                    } else if (newState == BottomSheetBehavior.STATE_DRAGGING) {
                        mListener.bottomSheetClosed();
                    }
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        });
    }

    public void clear() {
        cancelNetworkCalls();
        map.clear();
        circleContainer.clear();
        lineContainer.clear();
        polygonContainer.clear();
        corners.clear();
        midpoints.clear();
        redPolygons.clear();
        permitAdvisories.clear();
        polygonMap.clear();
        updateButtonColor(null);
        latestStatus = null;
    }

    public void clearMidpoints() {
        if (midpoints != null) {
            map.removeAnnotations(midpoints);
            midpoints.clear();
        }
    }

    @Override
    public void doneDrawing(@NonNull List<PointF> points) {
        showDeleteButton(true);
        enableDrawingSwitch.setChecked(false);
        updateTip(R.string.airmap_done_drawing_tip);
        if (drawingBoard.isPolygonMode()) {
            drawPolygon(points);

            Analytics.logEvent(Analytics.Page.POLYGON_CREATE_FLIGHT, Analytics.Action.draw, Analytics.Label.DRAW_POLYGON);
        } else { //Path mode
            drawPath(points);

            Analytics.logEvent(Analytics.Page.PATH_CREATE_FLIGHT, Analytics.Action.draw, Analytics.Label.DRAW_PATH, points.size());
        }
    }

    private void drawCircle(LatLng center, double radius) {
        clear();
        List<LatLng> circlePoints = mListener.getAnnotationsFactory().polygonCircleForCoordinate(center, radius);
        PolylineOptions polylineOptions = mListener.getAnnotationsFactory().getDefaultPolylineOptions().addAll(circlePoints).add(circlePoints.get(0));
        circleContainer.circle = map.addPolygon(mListener.getAnnotationsFactory().getDefaultPolygonOptions().addAll(circlePoints));
        circleContainer.outline = map.addPolyline(polylineOptions);
        circleContainer.radius = radius;
        circleContainer.center = center;
        corners.add(map.addMarker(mListener.getAnnotationsFactory().getDefaultMarkerOptions(center))); //Treat the center of the circle as a "corner", cuz it's not a midpoint
    }

    private void zoomToCircle() {
        if (circleContainer.circle != null) {
            LatLngBounds bounds = new LatLngBounds.Builder().includes(circleContainer.circle.getPoints()).build();
            map.easeCamera(CameraUpdateFactory.newLatLngBounds(bounds, Utils.dpToPixels(getActivity(), 50).intValue()));
        }
    }

    public void drawPath(List<PointF> line) {
        clear();
        PolylineOptions thinLine = mListener.getAnnotationsFactory().getDefaultPolylineOptions();
        double width = getPathWidthFromSeekBar(getActivity(), seekBar.getProgress());
        List<LatLng> midPoints = PointMath.getMidpointsFromLatLngs(getLatLngsFromPointFs(line));
        List<LatLng> points = getLatLngsFromPointFs(line);
        for (int i = 0; i < points.size(); i++) {
            LatLng point = points.get(i);
            thinLine.add(point);
            corners.add(map.addMarker(mListener.getAnnotationsFactory().getDefaultMarkerOptions(point)));
            if (i < midPoints.size()) {
                LatLng midPoint = midPoints.get(i);
                midpoints.add(map.addMarker(mListener.getAnnotationsFactory().getDefaultMidpointMarker(midPoint)));
            }
        }

        lineContainer.width = width;
        calculatePathBufferAndDisplayLineAndBuffer(thinLine.getPoints(), lineContainer.width, false);

        LatLngBounds bounds = new LatLngBounds.Builder().includes(thinLine.getPoints()).build();
        map.easeCamera(CameraUpdateFactory.newLatLngBounds(bounds, Utils.dpToPixels(getActivity(), 80).intValue()));
    }

    public void drawPolygon(List<PointF> pointsDrawn) {
        clear();
        PointF first = new PointF(pointsDrawn.get(0).x, pointsDrawn.get(0).y); //Making copy of the object so MapBox doesn't mess up the point
        PointF last = new PointF(pointsDrawn.get(pointsDrawn.size() - 1).x, pointsDrawn.get(pointsDrawn.size() - 1).y); //Making copies so MapBox doesn't mess up the point
        if (distanceBetween(first, last) < 100) { //Check if the two points are close, based on screen distance between points, rather than LatLng distance
            pointsDrawn.set(pointsDrawn.size() - 1, first); //Ignore the last drawn point if it was close to the starting point
        } else {
            pointsDrawn.add(first);
        }
        PolygonOptions polygonOptions = mListener.getAnnotationsFactory().getDefaultPolygonOptions();
        PolylineOptions polylineOptions = mListener.getAnnotationsFactory().getDefaultPolylineOptions();
        List<LatLng> midPoints = PointMath.getMidpointsFromLatLngs(getLatLngsFromPointFs(pointsDrawn));
        List<LatLng> points = getLatLngsFromPointFs(pointsDrawn);
        //At this point, until MapBox fixes their fromScreenLocation bug, pointsDrawn has been tainted and is unusable
        for (int i = 0; i < points.size(); i++) {
            LatLng latLng = points.get(i);
            if (i < midPoints.size()) { //There is one less midpoint than total points
                LatLng midPoint = midPoints.get(i);
                midpoints.add(map.addMarker(mListener.getAnnotationsFactory().getDefaultMidpointMarker(midPoint)));
            }
            polygonOptions.add(latLng);
            polylineOptions.add(latLng);
            if (i != points.size() - 1) { //Don't add the last point because it's the same as the first point
                corners.add(map.addMarker(mListener.getAnnotationsFactory().getDefaultMarkerOptions(latLng)));
            }
        }
        polylineOptions.add(polylineOptions.getPoints().get(0)); //Close the polygon
        polygonContainer.polygon = map.addPolygon(polygonOptions);
        polygonContainer.outline = map.addPolyline(polylineOptions);

        LatLngBounds bounds = new LatLngBounds.Builder().includes(polylineOptions.getPoints()).build();
        map.easeCamera(CameraUpdateFactory.newLatLngBounds(bounds, Utils.dpToPixels(getActivity(), 80).intValue()));
        checkForIntersections(points); //Don't include the last point when checking for intersection, otherwise it will always say there is one
        checkPolygonStatus();
    }

    private void checkForIntersections(List<LatLng> pointsDrawn) {
        map.removeAnnotations(intersections);
        intersections.clear();
        List<LatLng> points = PointMath.findIntersections(pointsDrawn);
        if (!points.isEmpty()) {
            for (LatLng point : points) {
                intersections.add(map.addMarker(mListener.getAnnotationsFactory().getIntersectionMarker(point)));
            }
            updateTip(R.string.airmap_error_overlap, R.drawable.rounded_corners_red);
        } else {
            updateTip(R.string.airmap_done_drawing_tip); //If no intersections, then reset to freehand message
        }
    }

    private List<LatLng> getLatLngsFromPointFs(List<PointF> points) {
        List<LatLng> latLngs = new ArrayList<>();
        for (PointF pointF : points) {
            LatLng latLng = map.getProjection().fromScreenLocation(pointF);
            latLngs.add(latLng);
        }
        return latLngs;
    }

    private static double getPathWidthFromSeekBar(Context context, int progress) {
        return (Utils.useMetric(context) ? Utils.getBufferPresetsMetric()[progress] : Utils.getBufferPresets()[progress]);
    }

    @Override
    public void onMapReady(MapboxMap mapboxMap) {
        map = mapboxMap;
        map.setOnCameraChangeListener(this);
        map.setOnMarkerClickListener(new MapboxMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(@NonNull Marker marker) {
                return true; //This is simply to prevent opening the info window when selecting the marker from onTouch
            }
        });
        if (mListener != null) {
            String url = AirMap.getTileSourceUrl(mListener.getMapLayers(), mListener.getMapTheme());
            map.setStyleUrl(url);
        }
        setupTabs();
        mapView.setOnTouchListener(new View.OnTouchListener() {
            //This onTouch code is a copy of the MapView#onSingleTapConfirmed code, except
            //I'm dragging instead of clicking, and it's being called for every touch event rather than just a tap
            //It also simplifies some of the selection logic

            //If dragging ever stops working, this is the first place to look
            //The onTouch is based on MapView#onSingleTapConfirmed
            //Look for any changes in that function, and make those changes here too
            //Also need to look at MapView#getMarkersInRect, which is how I'm getting closeby markers right now
            //It might end up getting renamed, something about it may change, which won't be apparent since right now it uses reflection to be invoked

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event != null) {
                    if (event.getPointerCount() > 1) {
                        scratchpad.reset();
                        scratchpad.invalidate();
                        return false; //Don't drag if there are multiple fingers on screen
                    }
                    PointF tapPoint = new PointF(event.getX(), event.getY());
                    float toleranceSides = 4 * screenDensity;
                    float toleranceTopBottom = 10 * screenDensity;
                    float averageIconWidth = 42;
                    float averageIconHeight = 42;
                    RectF tapRect = new RectF(tapPoint.x - averageIconWidth / 2 - toleranceSides,
                            tapPoint.y - averageIconHeight / 2 - toleranceTopBottom,
                            tapPoint.x + averageIconWidth / 2 + toleranceSides,
                            tapPoint.y + averageIconHeight / 2 + toleranceTopBottom);
                    try {
                        Method method = mapView.getClass().getDeclaredMethod("getMarkersInRect", RectF.class); //Using reflection to access a Mapbox Package Private method
                        method.setAccessible(true);
                        Marker newSelectedMarker = null;
                        List<Marker> nearbyMarkers = (List<Marker>) method.invoke(mapView, tapRect);
                        List<Marker> selectedMarkers = map.getSelectedMarkers();
                        if (selectedMarkers.isEmpty() && nearbyMarkers != null && !nearbyMarkers.isEmpty()) {
                            Collections.sort(nearbyMarkers);
                            for (Marker marker : nearbyMarkers) {
                                if (marker instanceof MarkerView && !((MarkerView) marker).isVisible()) {
                                    continue; //Don't let user click on hidden midpoints
                                }
                                if (!marker.getTitle().equals(AnnotationsFactory.INTERSECTION_TAG)) {
                                    newSelectedMarker = marker;
                                    break;
                                }
                            }
                        } else if (!selectedMarkers.isEmpty()) {
                            newSelectedMarker = selectedMarkers.get(0);
                        }

                        if (newSelectedMarker != null && newSelectedMarker instanceof MarkerView) {
                            boolean doneDragging = event.getAction() == MotionEvent.ACTION_UP;
                            boolean deletePoint = false;
                            deleteButton.getDrawingRect(deleteCoordinates);
                            deleteCoordinates.left *= 2;
                            deleteCoordinates.right *= 2;
                            deleteCoordinates.top *= 2;
                            deleteCoordinates.bottom *= 2;
                            if (deleteCoordinates.contains((int) tapPoint.x, (int) tapPoint.y)) {
                                deleteButton.setSelected(true);
                                if (doneDragging) {
                                    deleteButton.setSelected(false);
                                    updateTip(tabLayout.getSelectedTabPosition() == INDEX_OF_POLYGON_TAB ? R.string.airmap_freehand_tip_area : R.string.airmap_freehand_tip_path);
                                    deletePoint = true;
                                }
                            } else {
                                deleteButton.setSelected(false);
                            }
                            if (tabLayout.getSelectedTabPosition() != INDEX_OF_CIRCLE_TAB) { //We're not showing a tip for circle
                                if (doneDragging) {
                                    updateTip(R.string.airmap_done_drawing_tip);
                                } else {
                                    updateTip(R.string.airmap_delete_tip);
                                }
                            }

                            //DRAG!
                            //Trying to put most logic in the drag() function, this is pretty messy already
                            boolean isMidpoint = midpoints.contains(newSelectedMarker);
                            map.selectMarker(newSelectedMarker); //Use the marker selection state to prevent selecting another marker when dragging over it
                            drag(isMidpoint ? midpoints.indexOf(newSelectedMarker) : corners.indexOf(newSelectedMarker), map.getProjection().fromScreenLocation(tapPoint), isMidpoint, doneDragging, deletePoint);
                            if (doneDragging) {
                                map.deselectMarker(newSelectedMarker);
                            }
                            return true;
                        }
                    } catch (Exception e) {
                        //Probably a reflection error
                        e.printStackTrace();
                    }
                }
                scratchpad.reset();
                scratchpad.invalidate();
                return false;
            }
        });

        if (getArguments() != null) {
            Coordinate coordinate = (Coordinate) getArguments().getSerializable(CreateFlightActivity.COORDINATE);
            if (coordinate != null) {
                map.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(coordinate.getLatitude(), coordinate.getLongitude())), new MapboxMap.CancelableCallback() {
                    @Override
                    public void onCancel() {

                    }

                    @Override
                    public void onFinish() {
                        showSeekBarForCircle(); //Now circle will show at right place initially
                    }
                });
            }
        }
    }

    private void drag(int indexOfAnnotationToDrag, LatLng newLocation, boolean isMidpoint, boolean doneDragging, boolean deletePoint) {
        //Don't show midpoints, corners, and intersections when dragging
        for (MarkerView midpoint : midpoints) {
            midpoint.setVisible(doneDragging);
        }
        for (MarkerView corner : corners) {
            corner.setVisible(doneDragging);
        }
        for (MarkerView intersection : intersections) {
            intersection.setVisible(false); //Never want to show intersections when dragging another marker
        }

        if (indexOfAnnotationToDrag == -1) {
            Log.e(TAG, "indexOfAnnotationToDrag was -1???");
            return;
        }

        if (tabLayout.getSelectedTabPosition() == INDEX_OF_CIRCLE_TAB) {
            dragCircle(indexOfAnnotationToDrag, newLocation, doneDragging);
        } else if (tabLayout.getSelectedTabPosition() == INDEX_OF_PATH_TAB) {
            dragPointOnLine(indexOfAnnotationToDrag, newLocation, isMidpoint, doneDragging, deletePoint);
        } else if (tabLayout.getSelectedTabPosition() == INDEX_OF_POLYGON_TAB) {
            dragPointOnPolygon(indexOfAnnotationToDrag, newLocation, isMidpoint, doneDragging, deletePoint);
        }
        if (doneDragging) {
            scratchpad.reset();
            scratchpad.invalidate();
            setMidpointVisibilities();
        }
    }

    private void dragCircle(int indexOfAnnotationToDrag, LatLng newLocation, boolean doneDragging) {
        map.removeAnnotation(circleContainer.circle);
        map.removeAnnotation(circleContainer.outline);
        corners.get(indexOfAnnotationToDrag).setPosition(newLocation); //Move the center point
        double radius = Utils.useMetric(getActivity()) ? getBufferPresetsMetric()[seekBar.getProgress()] : getBufferPresets()[seekBar.getProgress()]; //Move the circle polygon
        List<LatLng> circlePoints = mListener.getAnnotationsFactory().polygonCircleForCoordinate(newLocation, radius);
        PolylineOptions polylineOptions = mListener.getAnnotationsFactory().getDefaultPolylineOptions().addAll(circlePoints).add(circlePoints.get(0));
        circleContainer.circle = map.addPolygon(mListener.getAnnotationsFactory().getDefaultPolygonOptions().addAll(circlePoints));
        circleContainer.outline = map.addPolyline(polylineOptions);
        circleContainer.radius = radius;
        circleContainer.center = newLocation;
        if (doneDragging) {
            zoomToCircle();
            checkCircleStatus();

            Analytics.logEvent(Analytics.Page.POINT_CREATE_FLIGHT, Analytics.Action.drag, Analytics.Label.DRAG_POINT);
        }
    }

    private void dragPointOnLine(int indexOfAnnotationToDrag, LatLng newLocation, boolean isMidpoint, boolean doneDragging, boolean deletePoint) {
        (isMidpoint ? midpoints : corners).get(indexOfAnnotationToDrag).setPosition(newLocation);
        if (doneDragging) {
            List<LatLng> points = lineContainer.line.getPoints();
            if (deletePoint) {
                if (!isMidpoint) { //Only delete if it was not a midpoint
                    if (corners.size() > 2) { //Need at least 2 points to be a line
                        points.remove(indexOfAnnotationToDrag);
                        map.removeAnnotation(corners.remove(indexOfAnnotationToDrag));
                    } else {
                        corners.get(indexOfAnnotationToDrag).setPosition(points.get(indexOfAnnotationToDrag));
                    }
                }

                Analytics.logEvent(Analytics.Page.PATH_CREATE_FLIGHT, Analytics.Action.drop, Analytics.Label.DROP_POINT_TRASH_ICON);
            } else if (isMidpoint) {
                //Add the midpoint as a new normal point
                points.add((indexOfAnnotationToDrag + 1) % corners.size(), newLocation);
                corners.add((indexOfAnnotationToDrag + 1) % corners.size(), map.addMarker(mListener.getAnnotationsFactory().getDefaultMarkerOptions(newLocation)));
                //New midpoints will be added when recalculating all midpoints

                Analytics.logEvent(Analytics.Page.PATH_CREATE_FLIGHT, Analytics.Action.drag, Analytics.Label.DRAG_NEW_POINT);
            } else {
                points.set(indexOfAnnotationToDrag, newLocation); //If not midpoint, then the index of the point to change on the line is the same as the index of the corner annotation

                Analytics.logEvent(Analytics.Page.PATH_CREATE_FLIGHT, Analytics.Action.drag, Analytics.Label.DRAG_PATH_POINT);
            }
            //Update the polyline (both the line and widthPolyline)
            calculatePathBufferAndDisplayLineAndBuffer(points, lineContainer.width, false);
            //Update the Midpoints
            clearMidpoints();
            for (LatLng latLng : PointMath.getMidpointsFromLatLngs(points)) {
                midpoints.add(map.addMarker(mListener.getAnnotationsFactory().getDefaultMidpointMarker(latLng)));
            }
        } else {
            int indexOfPreviousAnnotation;
            int indexOfNextAnnotation;
            if (isMidpoint) {
                //If we look at midpoint 1, one corner will be at 1, the other will be at 2
                indexOfPreviousAnnotation = indexOfAnnotationToDrag;
                indexOfNextAnnotation = (indexOfAnnotationToDrag + 1) % corners.size();
                scratchpad.dragTo(map.getProjection().toScreenLocation(corners.get(indexOfPreviousAnnotation).getPosition()), map.getProjection().toScreenLocation(midpoints.get(indexOfAnnotationToDrag).getPosition()), map.getProjection().toScreenLocation(corners.get(indexOfNextAnnotation).getPosition()));
            } else {
                if (indexOfAnnotationToDrag == 0) { //If the annotation is the first, only 1 dotted line will be drawn
                    indexOfNextAnnotation = indexOfAnnotationToDrag + 1;
                    scratchpad.dragTo(map.getProjection().toScreenLocation(corners.get(indexOfNextAnnotation).getPosition()), map.getProjection().toScreenLocation(corners.get(indexOfAnnotationToDrag).getPosition()));
                } else if (indexOfAnnotationToDrag == corners.size() - 1) { //If the annotation is the last, only 1 dotted line will be drawn
                    indexOfPreviousAnnotation = indexOfAnnotationToDrag - 1;
                    scratchpad.dragTo(map.getProjection().toScreenLocation(corners.get(indexOfPreviousAnnotation).getPosition()), map.getProjection().toScreenLocation(corners.get(indexOfAnnotationToDrag).getPosition()));
                } else {
                    indexOfPreviousAnnotation = indexOfAnnotationToDrag - 1;
                    indexOfNextAnnotation = indexOfAnnotationToDrag + 1;
                    scratchpad.dragTo(map.getProjection().toScreenLocation(corners.get(indexOfPreviousAnnotation).getPosition()), map.getProjection().toScreenLocation(corners.get(indexOfAnnotationToDrag).getPosition()), map.getProjection().toScreenLocation(corners.get(indexOfNextAnnotation).getPosition()));
                }
            }
        }
    }

    private void dragPointOnPolygon(int indexOfAnnotationToDrag, LatLng newLocation, boolean isMidpoint, boolean doneDragging, boolean deletePoint) {
        (isMidpoint ? midpoints : corners).get(indexOfAnnotationToDrag).setPosition(newLocation); //If midpoint, drag midpoint, else drag corner
        if (doneDragging) {
            List<LatLng> points = polygonContainer.polygon.getPoints();
            if (deletePoint) {
                if (!isMidpoint) { //Only delete if it is not a midpoint
                    if (corners.size() > 3) { //Need user to keep at least 3 points for valid polygon
                        points.remove(indexOfAnnotationToDrag);
                        map.removeAnnotation(corners.remove(indexOfAnnotationToDrag));
                        if (indexOfAnnotationToDrag == 0) {
                            points.set(points.size() - 1, points.get(0)); //Set the last point to be the first point for complete polygon
                        } else if (indexOfAnnotationToDrag == corners.size() - 1) {
                            points.set(0, points.get(points.size() - 1)); //Set the first point to last point for complete polygon
                        }
                    } else {
                        corners.get(indexOfAnnotationToDrag).setPosition(points.get(indexOfAnnotationToDrag)); //Instead of deleting, reset it back to its initial position
                    }
                }

                Analytics.logEvent(Analytics.Page.POLYGON_CREATE_FLIGHT, Analytics.Action.drop, Analytics.Label.DROP_POINT_TRASH_ICON);
            } else if (isMidpoint) {
                //Add the midpoint as a new normal point
                points.add((indexOfAnnotationToDrag + 1) % corners.size(), newLocation);
                corners.add((indexOfAnnotationToDrag + 1) % corners.size(), map.addMarker(mListener.getAnnotationsFactory().getDefaultMarkerOptions(newLocation)));
                if (indexOfAnnotationToDrag == midpoints.size() - 1) { //If the user picked the last midpoint, it affects the first point on the polygon
                    points.set(points.size() - 1, points.get(0)); //The last point need to be the same as the first
                }
                //New midpoint annotations will be added when recalculating all midpoints

                Analytics.logEvent(Analytics.Page.POLYGON_CREATE_FLIGHT, Analytics.Action.drag, Analytics.Label.DRAG_NEW_POINT);
            } else {
                points.set(indexOfAnnotationToDrag, newLocation); //If not midpoint, then the index of the point to change on the line is the same as the index of the corner annotation
                if (indexOfAnnotationToDrag == 0) {
                    points.set(points.size() - 1, newLocation); //First and last point both need to be set in polygon and line
                }

                Analytics.logEvent(Analytics.Page.POLYGON_CREATE_FLIGHT, Analytics.Action.drag, Analytics.Label.DRAG_POLYGON_POINT);
            }
            polygonContainer.polygon.setPoints(points);
            polygonContainer.outline.setPoints(points);
            clearMidpoints(); //Delete old midpoints
            for (LatLng latLng : PointMath.getMidpointsFromLatLngs(points)) {
                midpoints.add(map.addMarker(mListener.getAnnotationsFactory().getDefaultMidpointMarker(latLng))); //Compute new midpoints
            }
            checkForIntersections(points); //calculate intersections when done dragging
            checkPolygonStatus(); //check status when done dragging
        } else {
            int indexOfPreviousAnnotation;
            int indexOfNextAnnotation;
            if (isMidpoint) {
                indexOfPreviousAnnotation = indexOfAnnotationToDrag;
                indexOfNextAnnotation = (indexOfAnnotationToDrag + 1) % corners.size();
                scratchpad.dragTo(
                        map.getProjection().toScreenLocation(corners.get(indexOfPreviousAnnotation).getPosition()),
                        map.getProjection().toScreenLocation(midpoints.get(indexOfAnnotationToDrag).getPosition()),
                        map.getProjection().toScreenLocation(corners.get(indexOfNextAnnotation).getPosition())
                );
            } else {
                indexOfNextAnnotation = (indexOfAnnotationToDrag + 1) % corners.size();
                indexOfPreviousAnnotation = (indexOfAnnotationToDrag - 1) < 0 ? corners.size() - 1 : indexOfAnnotationToDrag - 1; //Since we only ever decrease by 1, the most it can wrap around to is from the first element to the last element
                scratchpad.dragTo(
                        map.getProjection().toScreenLocation(corners.get(indexOfPreviousAnnotation).getPosition()),
                        map.getProjection().toScreenLocation(corners.get(indexOfAnnotationToDrag).getPosition()),
                        map.getProjection().toScreenLocation(corners.get(indexOfNextAnnotation).getPosition())
                );
            }
        }
    }

    private void hideSeekBar() {
        seekBarContainer.setVisibility(View.GONE);
        seekBar.setOnSeekBarChangeListener(null);
        seekBar.setProgress(0);
    }

    private void showSeekBarForCircle() {
        seekBarContainer.setVisibility(View.VISIBLE);
        seekBarLabelTextView.setText(R.string.airmap_buffer);
        seekBar.setOnSeekBarChangeListener(null); //This is needed because when setting max, it might cause progress to change, which we don't want
        seekBar.setMax(Utils.useMetric(getActivity()) ? getBufferPresetsMetric().length - 1 : getBufferPresets().length - 1);
        seekBar.setProgress(0);
        circleContainer.center = map.getCameraPosition().target;
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                double bufferPreset = Utils.useMetric(getActivity()) ? getBufferPresetsMetric()[progress] : getBufferPresets()[progress];
                Log.e(TAG, "buffer preset");
                String bufferText = Utils.getMeasurementText(bufferPreset, Utils.useMetric(getActivity()));
                seekBarValueTextView.setText(bufferText);
                drawCircle(circleContainer.center, bufferPreset);
                if (!fromUser) {
                    checkCircleStatus();
                    zoomToCircle();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                checkCircleStatus();
                zoomToCircle();

                Analytics.logEvent(Analytics.Page.POINT_CREATE_FLIGHT, Analytics.Action.slide, Analytics.Label.BUFFER);
            }
        });
        seekBar.setProgress(18); //1000 ft
    }

    private void checkCircleStatus() {
        cancelNetworkCalls();
        Coordinate coordinate = new Coordinate(circleContainer.center);
        map.removeAnnotations(redPolygons);
        redPolygons.clear();
        statusCall = AirMap.checkCoordinate(coordinate, circleContainer.radius, null, null, false, null, this);
    }

    private void checkPathStatus() {
        cancelNetworkCalls();
        List<Coordinate> coordinates = new ArrayList<>();
        for (LatLng point : lineContainer.line.getPoints()) {
            coordinates.add(new Coordinate(point));
        }
        map.removeAnnotations(redPolygons);
        redPolygons.clear();
        statusCall = AirMap.checkFlightPath(coordinates, (int) lineContainer.width, coordinates.get(0), null, null, false, null, this);
    }

    private void checkPolygonStatus() {
        cancelNetworkCalls();
        List<Coordinate> coordinates = new ArrayList<>();
        for (LatLng point : polygonContainer.polygon.getPoints()) {
            coordinates.add(new Coordinate(point));
        }
        map.removeAnnotations(redPolygons);
        redPolygons.clear();
        statusCall = AirMap.checkPolygon(coordinates, coordinates.get(0), null, null, false, null, this);
    }

    private void showSeekBarForPath() {
        seekBarContainer.setVisibility(View.VISIBLE);
        seekBarLabelTextView.setText(R.string.airmap_width);
        seekBar.setOnSeekBarChangeListener(null); //This is needed because when setting max, it might cause progress to change, which we don't want since it would call the listener
        seekBar.setMax(Utils.useMetric(getActivity()) ? Utils.getBufferPresetsMetric().length - 1 : Utils.getBufferPresets().length - 1);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                double bufferPreset = Utils.useMetric(getActivity()) ? getBufferPresetsMetric()[progress] : getBufferPresets()[progress];
                String bufferText = Utils.getMeasurementText(bufferPreset, Utils.useMetric(getActivity()));

                lineContainer.width = getPathWidthFromSeekBar(getActivity(), progress);
                seekBarValueTextView.setText(bufferText);
                if (lineContainer.line != null) {
                    calculatePathBufferAndDisplayLineAndBuffer(lineContainer.line.getPoints(), lineContainer.width, true);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                scratchpad.reset();
                scratchpad.invalidate();
                if (lineContainer.line != null) {
                    calculatePathBufferAndDisplayLineAndBuffer(lineContainer.line.getPoints(), lineContainer.width, false);
                }

                Analytics.logEvent(Analytics.Page.PATH_CREATE_FLIGHT, Analytics.Action.slide, Analytics.Label.BUFFER);
            }
        });
        seekBar.setProgress(1); //50 ft
    }

    /**
     * This will calculate the buffer around a path, invoke the necessary javascript (turf.js), will
     * also add
     * the buffer annotation to the map. The javascript buffer result is returned in a callback, so
     * it may be a little delayed
     *
     * @param linePoints The points of the line to buffer
     * @param width      The width of the buffer, in meters
     * @param dashed     Whether to show the buffer as dashed
     */
    public void calculatePathBufferAndDisplayLineAndBuffer(final List<LatLng> linePoints, double width, final boolean dashed) {
        try {
            JSONArray coordinatesArray = new JSONArray();
            for (LatLng latLng : linePoints) {
                JSONArray point = new JSONArray();
                point.put(latLng.getLongitude());
                point.put(latLng.getLatitude());
                coordinatesArray.put(point);
            }

            JSONObject geometryJSON = new JSONObject();
            geometryJSON.put("type", "LineString");
            geometryJSON.put("coordinates", coordinatesArray);

            JSONObject linestringJSON = new JSONObject();
            linestringJSON.put("type", "Feature");
            linestringJSON.put("properties", new JSONObject());
            linestringJSON.put("geometry", geometryJSON);

            JSONObject json = new JSONObject();
            json.put("line", linestringJSON);
            json.put("buffer", width);
            json.put("tunnel", !dashed);

            webView.send(json.toString(), new CallBackFunction() {
                @Override
                public void onCallBack(String data) {
                    try {
                        JSONObject json = new JSONObject(data);
                        JSONObject geometry = json.getJSONObject("geometry");
                        String geometryType = geometry.getString("type");
                        JSONArray coordinates = geometry.getJSONArray("coordinates");

                        List<PolygonOptions> optionsList = new ArrayList<>();
                        if ("Polygon".equals(geometryType)) {
                            JSONArray outerPolygon = coordinates.getJSONArray(0);

                            List<LatLng> bufferPoints = new ArrayList<>();
                            for (int i = 0; i < outerPolygon.length(); i++) {
                                JSONArray coords = outerPolygon.getJSONArray(i);
                                bufferPoints.add(new LatLng(coords.getDouble(1), (coords.getDouble(0))));
                            }

                            if (dashed) {
                                List<PointF> bufferScreenLoc = new ArrayList<>();
                                for (LatLng latLng : bufferPoints) {
                                    bufferScreenLoc.add(map.getProjection().toScreenLocation(latLng));
                                }
                                scratchpad.drawShape(bufferScreenLoc);

                                // if coordinates[] > 1, there's inner polygons aka holes
                                // we don't need to tunnel the holes in dashed mode, just add them to scratchpad
                                if (coordinates.length() > 1) {
                                    for (int k = 1; k < coordinates.length(); k++) {
                                        JSONArray innerPolygon = coordinates.getJSONArray(k);

                                        List<PointF> innerBufferScreenLoc = new ArrayList<>();
                                        for (int j = 0; j < innerPolygon.length(); j++) {
                                            JSONArray coords = innerPolygon.getJSONArray(j);
                                            LatLng latLng = new LatLng(coords.getDouble(1), (coords.getDouble(0)));
                                            innerBufferScreenLoc.add(map.getProjection().toScreenLocation(latLng));
                                        }

                                        scratchpad.drawShapeDisconnected(innerBufferScreenLoc);
                                    }
                                }
                            } else {
                                PolygonOptions options = mListener.getAnnotationsFactory().getDefaultPolygonOptions().addAll(bufferPoints);
                                optionsList.add(options);
                            }

                            // multi-polygon comes back when we're tunneling the polygon holes and they slice the buffer into multiple polygons
                            // this is fine, the json is just a little different and we have to add all the polygons
                        } else if ("MultiPolygon".equals(geometryType)) {
                            scratchpad.reset();
                            for (int k = 0; k < coordinates.length(); k++) {
                                JSONArray polygon = coordinates.getJSONArray(k);
                                for (int i = 0; i < polygon.length(); i++) {
                                    JSONArray coords = polygon.getJSONArray(i);
                                    List<LatLng> bufferPoints = new ArrayList<>();
                                    for (int j = 0; j < coords.length(); j++) {
                                        bufferPoints.add(new LatLng(coords.getJSONArray(j).getDouble(1), (coords.getJSONArray(j).getDouble(0))));
                                    }

                                    if (dashed) {
                                        List<PointF> bufferScreenLoc = new ArrayList<>();
                                        for (LatLng latLng : bufferPoints) {
                                            bufferScreenLoc.add(map.getProjection().toScreenLocation(latLng));
                                        }
                                        scratchpad.drawShapeDisconnected(bufferScreenLoc);
                                    } else {
                                        PolygonOptions options = mListener.getAnnotationsFactory().getDefaultPolygonOptions().addAll(bufferPoints);
                                        optionsList.add(options);
                                    }
                                }
                            }
                        }

                        // add polygon(s) to map
                        if (map != null && !dashed) {
                            if (lineContainer.buffers != null && !lineContainer.buffers.isEmpty()) {
                                map.removeAnnotations(lineContainer.buffers);
                            }
                            if (lineContainer.line != null) {
                                map.removeAnnotation(lineContainer.line);
                            }

                            scratchpad.reset();
                            scratchpad.invalidate();

                            lineContainer.buffers = map.addPolygons(optionsList); //Need to add buffer first for proper z ordering
                            lineContainer.line = map.addPolyline(mListener.getAnnotationsFactory().getDefaultPolylineOptions().addAll(linePoints));

                            checkPathStatus();

                            LatLngBounds.Builder builder = new LatLngBounds.Builder();
                            for (Polygon polygon : lineContainer.buffers) {
                                builder.includes(polygon.getPoints());
                            }
                            map.easeCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), Utils.dpToPixels(getActivity(), 80).intValue()));
                        }
                    } catch (JSONException e) {
                        Log.e(TAG, "Error parsing turf json", e);
                    }
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCameraChange(CameraPosition position) {
        setMidpointVisibilities();
    }

    private void setMidpointVisibilities() {
        for (MarkerView midpoint1 : midpoints) {
            boolean continueOuterLoop = false;
            for (MarkerView corner : corners) {
                //Look at distance between the midpoint and corners around it
                PointF midpointScreenLoc = map.getProjection().toScreenLocation(midpoint1.getPosition());
                PointF cornerScreenLoc = map.getProjection().toScreenLocation(corner.getPosition());
                double distance = distanceBetween(midpointScreenLoc, cornerScreenLoc);
                if (distance < 100) {
                    midpoint1.setVisible(false);
                    continueOuterLoop = true;
                    break;
                } else {
                    midpoint1.setVisible(true);
                }
            }

            if (continueOuterLoop) {
                continue;
            }

            //Look at distance between midpoint and other midpoints
            for (MarkerView midpoint2 : midpoints) {
                if (midpoint1 == midpoint2) continue;
                PointF midpoint1ScreenLoc = map.getProjection().toScreenLocation(midpoint1.getPosition());
                PointF midpoint2ScreenLoc = map.getProjection().toScreenLocation(midpoint2.getPosition());
                double distance = distanceBetween(midpoint1ScreenLoc, midpoint2ScreenLoc);
                if (distance < 100) {
                    midpoint1.setVisible(false);
                    break;
                } else {
                    midpoint1.setVisible(true);
                }
            }
        }
    }

    private void updateTip(@StringRes int textId) {
        updateTip(textId, R.drawable.rounded_corners_gray);
    }

    private void updateTip(@StringRes int textId, @DrawableRes int drawable) {
        tipTextView.setVisibility(View.VISIBLE);
        tipTextView.setText(textId);
        tipTextView.setBackgroundResource(drawable);
    }

    private void showDeleteButton(boolean show) {
        deleteButton.setVisibility(show ? View.VISIBLE : View.GONE);
        enableDrawingSwitch.setVisibility(show ? View.GONE : View.VISIBLE);
    }

    private void cancelNetworkCalls() {
        if (statusCall != null) statusCall.cancel();
        if (airspaceCall != null) airspaceCall.cancel();
    }

    @Override
    public void onSuccess(final AirMapStatus response) {
        if (!isFragmentActive()) return;

        latestStatus = response;
        updateButtonColor(response != null ? response.getAdvisoryColor() : null);

        boolean requiresPermit = false;
        if (latestStatus.getAdvisories() != null && !latestStatus.getAdvisories().isEmpty()) {
            final Map<String, AirMapStatusAdvisory> advisoryMap = new HashMap<>();
            for (AirMapStatusAdvisory advisory : latestStatus.getAdvisories()) {
                if (advisory.getAvailablePermits() != null && !advisory.getAvailablePermits().isEmpty()) {
                    advisoryMap.put(advisory.getId(), advisory);
                    requiresPermit = true;
                } else if (advisory.getColor() == AirMapStatus.StatusColor.Red) {
                    advisoryMap.put(advisory.getId(), advisory);
                }
            }

            // check if advisories have changed
            if (!permitAdvisories.keySet().equals(advisoryMap.keySet())) {
                // new polygons
                if (!permitAdvisories.keySet().containsAll(advisoryMap.keySet())) {
                    updatePermitPolygons(advisoryMap);

                    // invalid polygons that should be removed
                } else {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            for (String key : permitAdvisories.keySet()) {
                                if (!advisoryMap.containsKey(key)) {
                                    Polygon polygon = polygonMap.remove(key);
                                    if (polygon != null) {
                                        map.removePolygon(polygon);
                                        polygonMap.remove(key);
                                    }
                                }
                            }

                            permitAdvisories = advisoryMap;
                        }
                    });
                }
            }
        }
        redrawFlightPolygon();

        // tell the user if conflicting permits
        if (requiresPermit && (latestStatus.getApplicablePermits() == null || latestStatus.getApplicablePermits().isEmpty())) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getActivity(), R.string.airmap_flight_area_overlap_permit_conflict, Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    @Override
    public void onError(AirMapException e) {
        if (isFragmentActive()) {
            updateButtonColor(null);
            AirMapLog.e(TAG, e.getMessage());
        }
    }

    private void updatePermitPolygons(final Map<String, AirMapStatusAdvisory> advisoryMap) {
        airspaceCall = AirspaceService.getAirspace(new ArrayList<>(advisoryMap.keySet()), new AirMapCallback<List<AirMapAirspace>>() {
            @Override
            public void onSuccess(final List<AirMapAirspace> airspacesResponse) {
                // use pilot permits to show green, yellow or red
                if (AirMap.hasValidAuthenticatedUser()) {
                    AirMap.getAuthenticatedPilotPermits(new AirMapCallback<List<AirMapPilotPermit>>() {
                        @Override
                        public void onSuccess(List<AirMapPilotPermit> pilotPermitsResponse) {
                            if (!isFragmentActive()) {
                                return;
                            }

                            final Map<String, AirMapPilotPermit> pilotPermitIds = new HashMap<>();
                            if (pilotPermitsResponse != null) {
                                for (AirMapPilotPermit pilotPermit : pilotPermitsResponse) {
                                    pilotPermitIds.put(pilotPermit.getShortDetails().getPermitId(), pilotPermit);
                                }
                            }

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    drawPolygons(airspacesResponse, advisoryMap, pilotPermitIds);
                                }
                            });
                        }

                        @Override
                        public void onError(AirMapException e) {
                            Log.e(TAG, "getPilotPermits failed", e);
                            redrawFlightPolygon();
                        }
                    });
                } else {
                    if (!isFragmentActive()) return;

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            drawPolygons(airspacesResponse, advisoryMap, null);
                        }
                    });
                }
            }

            @Override
            public void onError(AirMapException e) {
                Log.e(TAG, "getAirspaces failed", e);
                redrawFlightPolygon();
            }
        });
    }

    private void drawPolygons(final List<AirMapAirspace> airspaces, final Map<String, AirMapStatusAdvisory> advisoryMap, Map<String, AirMapPilotPermit> pilotPermitIds) {
        if (map == null) {
            return;
        }

        Map<String, AirMapStatusAdvisory> updatedMap = new HashMap<>();
        Map<String, Polygon> polygonsToRemove = new HashMap<>(polygonMap);

        for (AirMapAirspace airspace : airspaces) {
            if (!advisoryMap.containsKey(airspace.getAirspaceId())) {
                continue;
            }

            AirMapStatusAdvisory advisory = advisoryMap.get(airspace.getAirspaceId());
            updatedMap.put(advisory.getId(), advisory);

            AirMapStatus.StatusColor statusColor = Yellow;
            if (pilotPermitIds != null) {
                for (AirMapAvailablePermit availablePermit : advisory.getAvailablePermits()) {
                    AirMapPilotPermit pilotPermit = pilotPermitIds.get(availablePermit.getId());
                    if (pilotPermit != null && (pilotPermit.getExpiresAt() == null || pilotPermit.getExpiresAt().after(new Date()))) {
                        statusColor = Green;
                        break;
                    }
                }
            }

            if (advisory.getColor() == AirMapStatus.StatusColor.Red) {
                statusColor = AirMapStatus.StatusColor.Red;
            }


            if (!polygonMap.containsKey(airspace.getAirspaceId())) {
                Polygon polygon = drawPermitPolygon(airspace, statusColor);
                if (polygon != null) {
                    polygonMap.put(airspace.getAirspaceId(), polygon);
                }
            }
            polygonsToRemove.remove(airspace.getAirspaceId());
            advisoryMap.remove(airspace.getAirspaceId());
        }

        // remove polygons no longer valid
        for (String key : polygonsToRemove.keySet()) {
            Polygon polygon = polygonsToRemove.get(key);
            map.removePolygon(polygon);
            polygonMap.remove(key);
        }

        // redraw flight radius/path/polygon so its highest in z-index
        redrawFlightPolygon();
        permitAdvisories = updatedMap;
    }

    private Polygon drawPermitPolygon(AirMapAirspace airspace, AirMapStatus.StatusColor statusColor) {
        AirMapGeometry geometry = airspace.getGeometry();
        if (geometry instanceof AirMapPolygon) {
            PolygonOptions polygonOptions = AnnotationsFactory.getMapboxPolygon((AirMapPolygon) geometry);
            final int color;
            switch (statusColor) {
                case Red:
                    color = ContextCompat.getColor(getActivity(), R.color.airmap_red);
                    polygonOptions.alpha(0.6f);
                    break;
                case Yellow:
                    color = ContextCompat.getColor(getActivity(), R.color.airmap_yellow);
                    polygonOptions.alpha(0.6f);
                    break;
                default:
                case Green:
                    color = ContextCompat.getColor(getActivity(), R.color.airmap_green);
                    polygonOptions.alpha(0.6f);
                    break;
            }
            polygonOptions.fillColor(color);
            return map.addPolygon(polygonOptions);
        }

        //TODO: handle airspaces that are advisories that aren't polygons?
        return null;
    }

    private void redrawFlightPolygon() {
        if (!isFragmentActive()) return;
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //FIXME: a better way to determine which type of flight annotation to redraw
                if (tabLayout.getSelectedTabPosition() == INDEX_OF_POLYGON_TAB) {
                    // can't redraw if it hasn't been drawn yet
                    if (polygonContainer == null || polygonContainer.polygon == null) {
                        return;
                    }

                    //redraw polygon
                    PolygonOptions polygonOptions = mListener.getAnnotationsFactory().getDefaultPolygonOptions();
                    for (LatLng point : polygonContainer.polygon.getPoints()) {
                        polygonOptions.add(point);
                    }
                    map.removePolygon(polygonContainer.polygon);
                    polygonContainer.polygon = map.addPolygon(polygonOptions);

                    //redraw outline
                    PolylineOptions lineOptions = mListener.getAnnotationsFactory().getDefaultPolylineOptions();
                    for (LatLng point : polygonContainer.outline.getPoints()) {
                        lineOptions.add(point);
                    }
                    map.removePolyline(polygonContainer.outline);
                    polygonContainer.outline = map.addPolyline(lineOptions);
                } else if (tabLayout.getSelectedTabPosition() == INDEX_OF_PATH_TAB) {
                    //FIXME: one day mapbox will allow you to change the z-index without this hack :(
                    // can't redraw if it hasn't been drawn yet
                    if (lineContainer == null || lineContainer.buffers == null || lineContainer.buffers.isEmpty() || lineContainer.line == null) {
                        return;
                    }

                    //redraw buffer
                    List<PolygonOptions> bufferOptions = new ArrayList<>();
                    for (Polygon polygon : lineContainer.buffers) {
                        PolygonOptions options = mListener.getAnnotationsFactory().getDefaultPolygonOptions();
                        for (LatLng point : polygon.getPoints()) {
                            options.add(point);
                        }
                        bufferOptions.add(options);
                        map.removePolygon(polygon);
                    }
                    lineContainer.buffers = map.addPolygons(bufferOptions);

                    //redraw line
                    PolylineOptions lineOptions = mListener.getAnnotationsFactory().getDefaultPolylineOptions();
                    for (LatLng point : lineContainer.line.getPoints()) {
                        lineOptions.add(point);
                    }
                    map.removePolyline(lineContainer.line);
                    lineContainer.line = map.addPolyline(lineOptions);
                } else {
                    // can't redraw if it hasn't been drawn yet
                    if (circleContainer == null || circleContainer.circle == null) {
                        return;
                    }

                    //redraw circle
                    PolygonOptions circleOptions = mListener.getAnnotationsFactory().getDefaultPolygonOptions();
                    for (LatLng point : circleContainer.circle.getPoints()) {
                        circleOptions.add(point);
                    }
                    map.removePolygon(circleContainer.circle);
                    circleContainer.circle = map.addPolygon(circleOptions);

                    //redraw outline
                    PolylineOptions lineOptions = mListener.getAnnotationsFactory().getDefaultPolylineOptions();
                    for (LatLng point : circleContainer.outline.getPoints()) {
                        lineOptions.add(point);
                    }
                    map.removePolyline(circleContainer.outline);
                    circleContainer.outline = map.addPolyline(lineOptions);
                }
            }
        });
    }

    public void updateButtonColor(@Nullable AirMapStatus.StatusColor color) {
        if (nextButton != null) { //Called from callback, Activity might have been destroyed
            final int textAndIconColor = color == Yellow ? Color.BLACK : Color.WHITE;
            final int buttonColor;
            if (color == AirMapStatus.StatusColor.Red) {
                buttonColor = ContextCompat.getColor(getActivity(), R.color.airmap_red);
            } else if (color == Yellow) {
                buttonColor = ContextCompat.getColor(getActivity(), R.color.airmap_yellow);
            } else if (color == Green) {
                buttonColor = ContextCompat.getColor(getActivity(), R.color.airmap_green);
            } else {
                buttonColor = ContextCompat.getColor(getActivity(), R.color.colorPrimary);
            }
            nextButton.post(new Runnable() {
                @Override
                public void run() {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        nextButton.getBackground().setColorFilter(buttonColor, PorterDuff.Mode.MULTIPLY);
                    } else {
                        nextButton.getBackground().setColorFilter(buttonColor, PorterDuff.Mode.SRC_IN);
                    }
                    nextButton.setTextColor(textAndIconColor);
                    for (Drawable drawable : nextButton.getCompoundDrawables()) {
                        if (drawable != null) {
                            drawable.setColorFilter(textAndIconColor, PorterDuff.Mode.MULTIPLY);
                        }
                    }
                }
            });
        }
    }

    private boolean isFragmentActive() {
        return getActivity() != null && !getActivity().isFinishing() && isResumed();
    }

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
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof FlightDetailsFragment.OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    /**
     * @return True if this fragment consumed the back press, false otherwise
     */
    public boolean onActivityBackPressed() {
        if (bottomSheetBehavior != null) {
            if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                return true;
            }
        }

        if (tabLayout != null) {
            String page = Analytics.Page.POINT_CREATE_FLIGHT;
            switch (tabLayout.getSelectedTabPosition()) {
                case INDEX_OF_PATH_TAB:
                    page = Analytics.Page.PATH_CREATE_FLIGHT;
                    break;
                case INDEX_OF_POLYGON_TAB:
                    page = Analytics.Page.POLYGON_CREATE_FLIGHT;
                    break;
            }
            Analytics.logEvent(page, Analytics.Action.tap, Analytics.Label.CANCEL);
        }
        return false;
    }

    public interface OnFragmentInteractionListener {
        List<MappingService.AirMapLayerType> getMapLayers();

        AirMapFlight getFlight();

        void freehandNextClicked();

        void bottomSheetOpened();

        void bottomSheetClosed();

        void setPathBufferPoints(List<LatLng>[] buffers);

        TabLayout getTabLayout();

        AnnotationsFactory getAnnotationsFactory();

        MappingService.AirMapMapTheme getMapTheme();
    }
}
