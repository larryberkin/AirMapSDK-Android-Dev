package com.airmap.airmapsdk.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.airmap.airmapsdk.AirMapException;
import com.airmap.airmapsdk.Analytics;
import com.airmap.airmapsdk.R;
import com.airmap.airmapsdk.models.Coordinate;
import com.airmap.airmapsdk.models.flight.AirMapFlight;
import com.airmap.airmapsdk.models.permits.AirMapAvailablePermit;
import com.airmap.airmapsdk.models.permits.AirMapPermitIssuer;
import com.airmap.airmapsdk.models.permits.AirMapPilotPermit;
import com.airmap.airmapsdk.models.pilot.AirMapPilot;
import com.airmap.airmapsdk.models.status.AirMapStatus;
import com.airmap.airmapsdk.models.status.AirMapStatusAdvisory;
import com.airmap.airmapsdk.models.status.AirMapStatusPermits;
import com.airmap.airmapsdk.models.status.AirMapStatusRequirement;
import com.airmap.airmapsdk.models.status.AirMapStatusRequirementNotice;
import com.airmap.airmapsdk.networking.callbacks.AirMapCallback;
import com.airmap.airmapsdk.networking.services.AirMap;
import com.airmap.airmapsdk.networking.services.MappingService;
import com.airmap.airmapsdk.ui.adapters.SectionsPagerAdapter;
import com.airmap.airmapsdk.ui.fragments.FlightDetailsFragment;
import com.airmap.airmapsdk.ui.fragments.FlightNoticeFragment;
import com.airmap.airmapsdk.ui.fragments.FreehandMapFragment;
import com.airmap.airmapsdk.ui.fragments.ListPermitsFragment;
import com.airmap.airmapsdk.ui.fragments.ReviewFlightFragment;
import com.airmap.airmapsdk.ui.fragments.ReviewNoticeFragment;
import com.airmap.airmapsdk.ui.views.CustomViewPager;
import com.airmap.airmapsdk.util.AnnotationsFactory;
import com.airmap.airmapsdk.util.Constants;
import com.airmap.airmapsdk.util.Utils;
import com.mapbox.mapboxsdk.MapboxAccountManager;
import com.mapbox.mapboxsdk.geometry.LatLng;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.airmap.airmapsdk.util.Utils.feetToMeters;

public class CreateFlightActivity extends AppCompatActivity implements
        FreehandMapFragment.OnFragmentInteractionListener,
        FlightDetailsFragment.OnFragmentInteractionListener,
        ListPermitsFragment.OnFragmentInteractionListener,
        FlightNoticeFragment.OnFragmentInteractionListener,
        ReviewFlightFragment.OnFragmentInteractionListener,
        ReviewNoticeFragment.OnFragmentInteractionListener {


    public static final String COORDINATE = "coordinate";
    public static final String KEY_VALUE_EXTRAS = "keyValueExtras";
    public static final String KEY_LAYERS = "layers";
    public static final String KEY_THEME = "theme";

    public static final String FLIGHT = "flight";
    public static final String FLIGHT_STATUS = "flight_status";
    public static final String PILOT = "pilot";
    public static final String NOTICES = "notices";
    public static final String STATUS_PERMITS_LIST = "status_permits_list";
    public static final String PERMITS_FROM_WALLET = "permits_from_wallet";
    public static final String SELECTED_PERMITS = "selected_permits";
    public static final String PERMITS_TO_APPLY_FOR = "permits_to_apply_for";
    public static final String PERMITS_TO_REVIEW = "permits_to_review";

    private static final int REQUEST_DECISION_FLOW = 1;
    public static final int REQUEST_CUSTOM_PROPERTIES = 2;

    private Toolbar toolbar;
    private CustomViewPager viewPager;
    private SectionsPagerAdapter adapter;

    private int currentPage;
    private AirMapFlight flight; //The flight the user wants to create
    private AirMapStatus flightStatus; //The status for the flight
    private AirMapPilot pilot;
    private ArrayList<AirMapStatusRequirementNotice> notices;
    private ArrayList<AirMapStatusPermits> statusPermitsList;

    private ArrayList<AirMapPilotPermit> permitsFromWallet; //Permits the user has in their wallet that pertains to this flight

    private ArrayList<AirMapPilotPermit> selectedPermits; //Permits that do not need to be applied for and can be attached to flight
    private ArrayList<AirMapAvailablePermit> permitsToApplyFor; //Permits that need to be applied for before attaching to flight
    private ArrayList<AirMapAvailablePermit> permitsToShowInReview; //selectedPermits + permitsToApplyFor temporarily transformed into AvailablePermit

    private List<MappingService.AirMapLayerType> mapLayers;
    private MappingService.AirMapMapTheme mapTheme;

    private List<LatLng>[] pathBuffers; //So that we don't need to recalculate the buffer polygon using turf on flight details screen
    private AnnotationsFactory annotationsFactory;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        notices = new ArrayList<>();
        statusPermitsList = new ArrayList<>();
        permitsFromWallet = new ArrayList<>();
        selectedPermits = new ArrayList<>();
        permitsToApplyFor = new ArrayList<>();
        permitsToShowInReview = new ArrayList<>();
        mapLayers = new ArrayList<>();
        mapTheme = MappingService.AirMapMapTheme.Standard;
        if (getIntent().getExtras() != null) {
            if (getIntent().getStringArrayListExtra(KEY_LAYERS) != null) {
                ArrayList<String> stringLayers = getIntent().getStringArrayListExtra(KEY_LAYERS);
                for (String stringLayer : stringLayers) {
                    if (MappingService.AirMapLayerType.fromString(stringLayer) != null) {
                        mapLayers.add(MappingService.AirMapLayerType.fromString(stringLayer));
                    }
                }
            }
            if (getIntent().getSerializableExtra(KEY_THEME) != null) {
                mapTheme = (MappingService.AirMapMapTheme) getIntent().getSerializableExtra(KEY_THEME);
            }
        }

        setupFlight(savedInstanceState);
        MapboxAccountManager.start(this, Utils.getMapboxApiKey());
        setContentView(R.layout.airmap_activity_create_flight);
        initializeViews();
        setupToolbar();
        setupViewPager();

        annotationsFactory = new AnnotationsFactory(this);

        Analytics.logEvent(Analytics.Page.CREATE_FLIGHT, Analytics.Action.start, Analytics.Label.START_CREATE_FLIGHT);
    }

    private void setupFlight(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            flight = (AirMapFlight) savedInstanceState.getSerializable(FLIGHT);
            currentPage = savedInstanceState.getInt("index");

            if (savedInstanceState.containsKey(FLIGHT_STATUS)) {
                flightStatus = (AirMapStatus) savedInstanceState.getSerializable(FLIGHT_STATUS);
            }

            if (savedInstanceState.containsKey(PILOT)) {
                pilot = (AirMapPilot) savedInstanceState.getSerializable(PILOT);
            }

            if (savedInstanceState.containsKey(NOTICES)) {
                notices = (ArrayList<AirMapStatusRequirementNotice>) savedInstanceState.getSerializable(NOTICES);
            }

            if (savedInstanceState.containsKey(STATUS_PERMITS_LIST)) {
                statusPermitsList = (ArrayList<AirMapStatusPermits>) savedInstanceState.getSerializable(STATUS_PERMITS_LIST);
            }

            if (savedInstanceState.containsKey(PERMITS_FROM_WALLET)) {
                permitsFromWallet = (ArrayList<AirMapPilotPermit>) savedInstanceState.getSerializable(PERMITS_FROM_WALLET);
            }

            if (savedInstanceState.containsKey(SELECTED_PERMITS)) {
                selectedPermits = (ArrayList<AirMapPilotPermit>) savedInstanceState.getSerializable(SELECTED_PERMITS);
            }

            if (savedInstanceState.containsKey(PERMITS_TO_APPLY_FOR)) {
                permitsToApplyFor = (ArrayList<AirMapAvailablePermit>) savedInstanceState.getSerializable(PERMITS_TO_APPLY_FOR);
            }

            if (savedInstanceState.containsKey(PERMITS_TO_REVIEW)) {
                permitsToShowInReview = (ArrayList<AirMapAvailablePermit>) savedInstanceState.getSerializable(PERMITS_TO_REVIEW);
            }
        } else {
            //Initialize Flight with default values
            Coordinate coordinate = (Coordinate) getIntent().getSerializableExtra(COORDINATE);
            currentPage = 0;
            if (coordinate == null) {
                if (flight != null && flight.getCoordinate() != null) {
                    coordinate = flight.getCoordinate();
                } // else We don't know where to create the flight anymore...
            }
            Date startsAt = new Date();
            Date endsAt = new Date(startsAt.getTime() + 60 * 60 * 1000); //Default duration of 1 hr
            flight = new AirMapFlight()
//                    .setCoordinate(coordinate) //The passed in coordinate
                    .setStartsAt(startsAt) //Default to null to prevent submitting a flight in the past
                    .setEndsAt(endsAt) //Default duration of 1 hour
                    .setPublic(true) //Share with AirMap by default
                    .setNotify(true) //Notify airports by default
                    .setMaxAltitude(feetToMeters(400)); //400 ft by default
//                    .setBuffer(feetToMeters(1000)); //1000 ft by default
        }
    }

    private void initializeViews() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        viewPager = (CustomViewPager) findViewById(R.id.container);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        getSupportActionBar().setTitle(R.string.airmap_flight_details);
    }

    private void setupViewPager() {
        List<Fragment> fragments = new ArrayList<>();
        switch (currentPage) { //Using fall through to add all necessary fragments
            case 4:
                fragments.add(0, ReviewFlightFragment.newInstance());
            case 3:
                fragments.add(0, FlightNoticeFragment.newInstance());
            case 2:
                fragments.add(0, ListPermitsFragment.newInstance());
            case 1:
                fragments.add(0, FlightDetailsFragment.newInstance());
            case 0:
                fragments.add(0, FreehandMapFragment.newInstance((Coordinate) getIntent().getSerializableExtra(COORDINATE)));
        }
        adapter = new SectionsPagerAdapter(getSupportFragmentManager(), fragments);
        viewPager.setOffscreenPageLimit(5);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @SuppressWarnings("ConstantConditions")
            @Override
            public void onPageSelected(int position) {
                currentPage = position;
                Fragment fragment = adapter.getItem(position);
                viewPager.setPagingEnabled(!(fragment instanceof FreehandMapFragment));
                if (fragment instanceof FreehandMapFragment) {
                    getSupportActionBar().setTitle(R.string.airmap_title_activity_create_flight);
                    getTabLayout().setVisibility(View.VISIBLE);
                    invalidateFurtherFragments(0);
                } else if (fragment instanceof FlightDetailsFragment) {
                    getSupportActionBar().setTitle(R.string.flight_details);
                    getTabLayout().setVisibility(View.GONE);
                } else if (fragment instanceof ListPermitsFragment) {
                    getSupportActionBar().setTitle(R.string.permits);
                    getTabLayout().setVisibility(View.GONE);
                } else if (fragment instanceof FlightNoticeFragment) {
                    getSupportActionBar().setTitle(R.string.airmap_flight_notice);
                    getTabLayout().setVisibility(View.GONE);
                } else if (fragment instanceof ReviewFlightFragment) {
                    getSupportActionBar().setTitle(R.string.airmap_review);
                    getTabLayout().setVisibility(View.GONE);
                }
            }
        });
        goToLastPage();
    }

    private void invalidateFurtherFragments(final int position) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (int i = adapter.getCount() - 1; i > position; i--) {
                    adapter.remove(i);
                }
            }
        });

    }

    @Override
    public void selectPermit(AirMapStatusPermits permit) {
        Intent intent = new Intent(this, PermitSelectionActivity.class);
        intent.putExtra(Constants.STATUS_PERMIT_EXTRA, permit);
        intent.putExtra(Constants.PERMIT_WALLET_EXTRA, permitsFromWallet);
        startActivityForResult(intent, REQUEST_DECISION_FLOW);
    }

    @Override
    public void flightDetailsSaveClicked(AirMapFlight response) {
        Intent data = new Intent();
        data.putExtra(FLIGHT, response);
        setResult(RESULT_OK, data);
        finish();
    }

    @Override
    public void onFlightSubmitted(AirMapFlight response) {
        Intent data = new Intent();
        data.putExtra(FLIGHT, response);
        setResult(RESULT_OK, data);
        finish();
    }

    @Override
    public void freehandNextClicked() {
        invalidateFurtherFragments(0);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                adapter.add(FlightDetailsFragment.newInstance());
                viewPager.setCurrentItem(1, true);
            }
        });
    }

    @Override
    public void setPathBufferPoints(List<LatLng>[] buffers) {
        pathBuffers = buffers;
    }

    @Override
    public void bottomSheetOpened() {
        getSupportActionBar().setTitle(R.string.airmap_airspace_advisories);
//        getTabLayout().setVisibility(View.GONE); //This causes wonky behavior
    }

    @Override
    public void bottomSheetClosed() {
        getSupportActionBar().setTitle(R.string.airmap_title_activity_create_flight);
//        getTabLayout().setVisibility(View.VISIBLE);

        Analytics.logEvent(Analytics.Page.ADVISORIES, Analytics.Action.tap, Analytics.Label.CLOSE_BUTTON);
    }

    @Override
    public TabLayout getTabLayout() {
        return (TabLayout) findViewById(R.id.tabs);
    }

    @Override
    public AnnotationsFactory getAnnotationsFactory() {
        return annotationsFactory;
    }

    @Override
    public void flightDetailsNextClicked(final AirMapStatus flightStatus) {
        this.flightStatus = flightStatus;
        notices.clear();

        // map applicable permits to organization (issuer)
        Map<String, AirMapStatusPermits> statusPermitMap = new HashMap<>();
        Map<String, AirMapPermitIssuer> organizationMap = new HashMap<>();
        for (AirMapPermitIssuer issuer : flightStatus.getOrganizations()) {
            organizationMap.put(issuer.getId(), issuer);
        }
        for (AirMapAvailablePermit applicablePermit : flightStatus.getApplicablePermits()) {
            if (organizationMap.containsKey(applicablePermit.getOrganizationId())) {
                AirMapPermitIssuer organization = organizationMap.get(applicablePermit.getOrganizationId());

                AirMapStatusPermits statusPermits = statusPermitMap.get(organization.getId());
                if (statusPermits == null) {
                    statusPermits = new AirMapStatusPermits();
                    statusPermits.setAuthorityName(organization.getName());
                }
                statusPermits.getApplicablePermits().add(applicablePermit);
                statusPermitMap.put(organization.getId(), statusPermits);
            }
        }

        // check if advisories have required permits
        boolean isPermitRequired = false;

        List<AirMapStatusAdvisory> advisories = flightStatus.getAdvisories();
        for (AirMapStatusAdvisory advisory : advisories) {
            isPermitRequired = isPermitRequired || !advisory.getAvailablePermits().isEmpty();

            // show digital notice even if advisory has required permit
            AirMapStatusRequirement requirement = advisory.getRequirements();
            if (requirement.getNotice() != null) {
                notices.add(requirement.getNotice());
            }

            // map available permits to organization (issuer)
            for (AirMapAvailablePermit availablePermit : advisory.getAvailablePermits()) {
                if (organizationMap.containsKey(advisory.getOrganizationId())) {
                    AirMapPermitIssuer organization = organizationMap.get(advisory.getOrganizationId());

                    AirMapStatusPermits statusPermits = statusPermitMap.get(organization.getId());
                    if (statusPermits == null) {
                        statusPermits = new AirMapStatusPermits();
                        statusPermits.setAuthorityName(organization.getName());
                    }
                    statusPermits.getAvailablePermits().add(availablePermit);
                    statusPermitMap.put(organization.getId(), statusPermits);
                }
            }
        }

        statusPermitsList = new ArrayList<>(statusPermitMap.values());

        invalidateFurtherFragments(1); //To prevent creating multiple instances of the next fragment

        if (isPermitRequired) {
            AirMap.getAuthenticatedPilotPermits(new AirMapCallback<List<AirMapPilotPermit>>() {
                @Override
                public void onSuccess(List<AirMapPilotPermit> response) {
                    for (AirMapPilotPermit pilotPermit : response) {
                        if (pilotPermit.getShortDetails().isSingleUse()) {
                            continue;
                        }

                        for (AirMapAvailablePermit availablePermit : flightStatus.getApplicablePermits()) {
                            if (availablePermit.getId().equals(pilotPermit.getShortDetails().getPermitId())) {
                                permitsFromWallet.add(pilotPermit); //Only add permits that would pertain to this flight
                            }
                        }
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.add(ListPermitsFragment.newInstance());
                            viewPager.setCurrentItem(2, true);
                        }
                    });
                }

                @Override
                public void onError(AirMapException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(CreateFlightActivity.this, R.string.error_getting_wallet, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            });

        } else if (!notices.isEmpty()) {
            //Show only submit digital notices
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    adapter.add(FlightNoticeFragment.newInstance());
                    viewPager.setCurrentItem(2, true);
                }
            });
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                viewPager.setCurrentItem(2, true);
            }
        });
    }

    private void goToLastPage() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                viewPager.setCurrentItem(adapter.getCount(), true);
            }
        });
    }

    private void onCustomPropertiesNextButtonClick(AirMapAvailablePermit permit) {
        for (Fragment fragment : adapter.getItems()) {
            if (fragment instanceof ListPermitsFragment) {
                ((ListPermitsFragment) fragment).onEnabledPermit(permit);
                ((ListPermitsFragment) fragment).onSelectPermit(permit);
            }
        }
    }

    @Override
    public void onListPermitsNextClicked(ArrayList<AirMapAvailablePermit> selectedInAdapter) {
        permitsToShowInReview = selectedInAdapter;
        //Since permit wallet permits and all other permits were combined in the adapter, they need to be separated out again
        for (AirMapAvailablePermit availablePermit : selectedInAdapter) {
            AirMapPilotPermit pilotPermit = isFromWallet(availablePermit);
            if (pilotPermit != null) {
                selectedPermits.add(pilotPermit);
            } else {
                permitsToApplyFor.add(availablePermit);
            }
        }

        for (int i = 0; i < adapter.getCount(); i++) {
            Fragment fragment = adapter.getItem(i);
            if (fragment instanceof FlightNoticeFragment) {
                viewPager.setCurrentItem(i, true);
                return;
            }
        }
        adapter.add(FlightNoticeFragment.newInstance());
        goToLastPage();
    }

    private AirMapPilotPermit isFromWallet(AirMapAvailablePermit availablePermit) {
        for (AirMapPilotPermit pilotPermit : permitsFromWallet) {
            if (pilotPermit.getShortDetails().getPermitId().equals(availablePermit.getId())) {
                if ((pilotPermit.getStatus() == AirMapPilotPermit.PermitStatus.Accepted || pilotPermit.getStatus() == AirMapPilotPermit.PermitStatus.Pending)
                        && (pilotPermit.getExpiresAt() == null || pilotPermit.getExpiresAt().after(new Date()))) {
                    return pilotPermit;
                }
            }
        }
        return null;
    }

    @Override
    public void onFlightNoticeNextButtonClicked() {
        for (int i = 0; i < adapter.getCount(); i++) {
            Fragment fragment = adapter.getItem(i);
            if (fragment instanceof ReviewFlightFragment) {
                viewPager.setCurrentItem(i, true);
                return;
            }
        }
        adapter.add(ReviewFlightFragment.newInstance());
        goToLastPage();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_DECISION_FLOW) { //Decision flow opens up Custom Properties Activity
            if (resultCode == RESULT_OK) {
                onCustomPropertiesNextButtonClick((AirMapAvailablePermit) data.getSerializableExtra(Constants.AVAILABLE_PERMIT_EXTRA));
            }
        }
    }

    @Override
    public void flightChanged() {
        invalidateFurtherFragments(1);
        flightStatus = null;
        notices.clear();
        statusPermitsList.clear();
        permitsFromWallet.clear();
        selectedPermits.clear();
        permitsToApplyFor.clear();
        permitsToShowInReview.clear();
    }

    @Override
    public ArrayList<AirMapStatusPermits> getStatusPermits() {
        return statusPermitsList;
    }

    @Override
    public ArrayList<AirMapPilotPermit> getPermitsFromWallet() {
        return permitsFromWallet;
    }

    @Override
    public ArrayList<AirMapPilotPermit> getSelectedPermits() {
        return selectedPermits;
    }

    @Override
    public ArrayList<AirMapAvailablePermit> getPermitsToShowInReview() {
        return permitsToShowInReview;
    }

    @Override
    public ArrayList<AirMapAvailablePermit> getPermitsToApplyFor() {
        return permitsToApplyFor;
    }

    @Override
    public AirMapStatus getFlightStatus() {
        return flightStatus;
    }

    @Override
    public ArrayList<AirMapStatusRequirementNotice> getNotices() {
        return notices;
    }

    @Override
    public AirMapFlight getFlight() {
        return flight;
    }

    public AirMapPilot getPilot() {
        return pilot;
    }

    public void setPilot(AirMapPilot pilot) {
        this.pilot = pilot;
    }

    @Override
    public List<LatLng>[] getPathBuffers() {
        return pathBuffers;
    }

    public List<MappingService.AirMapLayerType> getMapLayers() {
        return mapLayers;
    }

    public MappingService.AirMapMapTheme getMapTheme() {
        return mapTheme;
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        flight = (AirMapFlight) savedInstanceState.getSerializable(FLIGHT);
        flightStatus = (AirMapStatus) savedInstanceState.getSerializable(FLIGHT_STATUS);
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable(FLIGHT, flight);
        outState.putInt("index", currentPage);
        getIntent().putExtra(FLIGHT, flight);

        if (flightStatus != null) {
            getIntent().putExtra(FLIGHT_STATUS, flightStatus);
            outState.putSerializable(FLIGHT_STATUS, flightStatus);
        }

        if (pilot != null) {
            outState.putSerializable(PILOT, pilot);
        }

        if (notices != null) {
            outState.putSerializable(NOTICES, notices);
        }

        if (statusPermitsList != null) {
            outState.putSerializable(STATUS_PERMITS_LIST, statusPermitsList);
        }

        if (permitsFromWallet != null) {
            outState.putSerializable(PERMITS_FROM_WALLET, permitsFromWallet);
        }

        if (selectedPermits != null) {
            outState.putSerializable(SELECTED_PERMITS, selectedPermits);
        }

        if (permitsToApplyFor != null) {
            outState.putSerializable(PERMITS_TO_APPLY_FOR, permitsToApplyFor);
        }

        if (permitsToShowInReview != null) {
            outState.putSerializable(PERMITS_TO_REVIEW, permitsToShowInReview);
        }

        super.onSaveInstanceState(outState);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        if (viewPager.getCurrentItem() > 0) {
            viewPager.setCurrentItem(viewPager.getCurrentItem() - 1, true);
        } else {
            if (!((FreehandMapFragment) adapter.getItem(0)).onActivityBackPressed()) { //If this was true, the method call will close the status bottom sheet in the first fragment
                super.onBackPressed();
            }
        }
    }
}
