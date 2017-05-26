package com.airmap.airmapsdk.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.airmap.airmapsdk.Analytics;
import com.airmap.airmapsdk.R;
import com.airmap.airmapsdk.models.flight.AirMapFlight;
import com.airmap.airmapsdk.models.permits.AirMapPermitIssuer;
import com.airmap.airmapsdk.models.status.AirMapStatus;
import com.airmap.airmapsdk.models.status.AirMapStatusAdvisory;
import com.airmap.airmapsdk.models.status.AirMapStatusRequirementNotice;
import com.airmap.airmapsdk.ui.activities.WebActivity;
import com.airmap.airmapsdk.util.Constants;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FlightNoticeFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    private TextView submitNoticeSwitch;
    private ListView digitalNoticeListView;
    private TextView notDigitalLabelTextView;
    private ListView notDigitalNoticeListView;
    private Button nextButton;

    private List<AirMapStatusRequirementNotice> digitalNotices;
    private Map<String,String> digitalNoticeNamesAndTypes;

    private List<AirMapStatusRequirementNotice> nonDigitalNotices;
    private List<String> nonDigitalNoticeNames;
    private List<String> nonDigitalNoticeTypes;

    public FlightNoticeFragment() {
        // Required empty public constructor
    }

    public static FlightNoticeFragment newInstance() {
        FlightNoticeFragment fragment = new FlightNoticeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.airmap_fragment_flight_notice, container, false);
        initializeViews(view);
        getNotices();
        setupDigitalNoticeList();
        setupNotDigitalNoticeList();
        continueToNextFragmentIfNecessary();
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onNextButton();
            }
        });
        return view;
    }

    private void initializeViews(View view) {
        submitNoticeSwitch = (TextView) view.findViewById(R.id.submit_notice_switch);
        digitalNoticeListView = (ListView) view.findViewById(R.id.digital_notice_list);
        notDigitalLabelTextView = (TextView) view.findViewById(R.id.not_digital_label);
        notDigitalLabelTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), WebActivity.class);
                intent.putExtra(Constants.URL_EXTRA, Constants.FAQ_URL);
                intent.putExtra(Intent.EXTRA_TITLE, "FAQ");
                startActivity(intent);
            }
        });
        notDigitalNoticeListView = (ListView) view.findViewById(R.id.not_digital_list);
        nextButton = (Button) view.findViewById(R.id.next_button);
    }

    private void getNotices() {
        digitalNotices = new ArrayList<>();
        digitalNoticeNamesAndTypes = new HashMap<>();
        nonDigitalNotices = new ArrayList<>();
        nonDigitalNoticeNames = new ArrayList<>();
        nonDigitalNoticeTypes = new ArrayList<>();

        AirMapStatus status = mListener.getFlightStatus();
        for (AirMapStatusAdvisory advisory : status.getAdvisories()) {
            if (advisory.getRequirements() != null && advisory.getRequirements().getNotice() != null) {
                AirMapStatusRequirementNotice notice = advisory.getRequirements().getNotice();
                if (notice.isDigital() || !TextUtils.isEmpty(advisory.getOrganizationId())) {
                    digitalNotices.add(notice);

                    boolean useAdvisoryName = true;
                    if (!TextUtils.isEmpty(advisory.getOrganizationId())) {
                        for (AirMapPermitIssuer issuer : status.getOrganizations()) {
                            if (advisory.getOrganizationId().equals(issuer.getId())) {
                                String type = advisory.getType() != null ? getString(advisory.getType().getTitle()) : "";
                                digitalNoticeNamesAndTypes.put(issuer.getName(), type);
                                useAdvisoryName = false;
                            }
                        }
                    }

                    if (useAdvisoryName) {
                        String type = advisory.getType() != null ? getString(advisory.getType().getTitle()) : "";
                        digitalNoticeNamesAndTypes.put(advisory.getName(), type);
                    }
                } else if (notice.isNoticeRequired()) {
                    nonDigitalNotices.add(notice);
                    nonDigitalNoticeNames.add(advisory.getName());
                    String type = advisory.getType() != null ? getString(advisory.getType().getTitle()) : "";
                    nonDigitalNoticeTypes.add(type);
                }
            }
        }

        // If there aren't any advisories that accept digital notice, then set notify to false
        if (digitalNotices.size() == 0) {
            submitNoticeSwitch.setVisibility(View.GONE);
            mListener.getFlight().setNotify(false);
        } else {
            mListener.getFlight().setNotify(true);
        }
    }

    private void setupDigitalNoticeList() {
        AirMapStatus status = mListener.getFlightStatus();
        if (digitalNoticeNamesAndTypes.isEmpty() && status.getApplicablePermits().isEmpty()) {
            submitNoticeSwitch.setVisibility(View.GONE);
        }

        List<Map<String, String>> list = new ArrayList<>();
        for (String digitalNoticeName : digitalNoticeNamesAndTypes.keySet()) {
            Map<String, String> map = new HashMap<>();
            map.put("name", digitalNoticeName);
            map.put("type", digitalNoticeNamesAndTypes.get(digitalNoticeName));
            list.add(map);
        }
        if (!digitalNoticeNamesAndTypes.isEmpty()) {
            digitalNoticeListView.setAdapter(new SimpleAdapter(getContext(), list, R.layout.flight_notice_digital_list_item, new String[]{"name", "type"}, new int[]{R.id.authority_name, R.id.authority_type}));
        }
    }

    private void setupNotDigitalNoticeList() {
        if (nonDigitalNoticeNames.isEmpty()) {
            notDigitalLabelTextView.setVisibility(View.GONE);
        } else {
            List<Map<String, String>> list = new ArrayList<>();
            for (int i = 0; i < nonDigitalNotices.size(); i++) {
                Map<String, String> map = new HashMap<>();
                map.put("name", nonDigitalNoticeNames.get(i));
                String number = nonDigitalNotices.get(i).getPhoneNumber();
                if (number == null || number.length() < 10) {
                    number = getString(R.string.no_phone_number_provided);
                }
                map.put("type", nonDigitalNoticeTypes.get(i));
                map.put("phone", number);
                list.add(map);
            }
            notDigitalNoticeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String number = nonDigitalNotices.get(position).getPhoneNumber();
                    if (number != null && number.length() >= 10) {
                        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + number));
                        if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                            startActivity(intent);
                        }
                    }
                }
            });
            notDigitalNoticeListView.setAdapter(new SimpleAdapter(getContext(), list, R.layout.flight_notice_not_digital_list_item, new String[]{"name", "type", "phone"}, new int[]{R.id.authority_name, R.id.authority_type, R.id.phone_number}));
        }
    }

    //If there are no digital notices or non-digital airports that need notice, go to next fragment
    private void continueToNextFragmentIfNecessary() {
        if (digitalNotices.isEmpty() && nonDigitalNotices.isEmpty()) {
            onNextButton();
        }
    }

    private void onNextButton() {
        Analytics.logEvent(Analytics.Page.NOTICES_CREATE_FLIGHT, Analytics.Action.tap, Analytics.Label.REVIEW);

        mListener.onFlightNoticeNextButtonClicked();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public interface OnFragmentInteractionListener {
        AirMapFlight getFlight();

        AirMapStatus getFlightStatus();

        void onFlightNoticeNextButtonClicked();
    }
}
