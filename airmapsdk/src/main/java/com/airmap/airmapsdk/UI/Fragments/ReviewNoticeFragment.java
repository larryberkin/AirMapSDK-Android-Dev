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
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.airmap.airmapsdk.R;
import com.airmap.airmapsdk.models.flight.AirMapFlight;
import com.airmap.airmapsdk.models.permits.AirMapPermitIssuer;
import com.airmap.airmapsdk.models.status.AirMapStatus;
import com.airmap.airmapsdk.models.status.AirMapStatusAdvisory;
import com.airmap.airmapsdk.models.status.AirMapStatusRequirementNotice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Vansh Gandhi on 7/25/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */
public class ReviewNoticeFragment extends Fragment {

    private static final String ARG_STATUS = "status";
    private static final String ARG_NOTICE = "notice";

    private OnFragmentInteractionListener mListener;

    private TextView submitNoticeSwitch;
    private ListView digitalNoticeListView;
    private TextView notDigitalLabelTextView;
    private ListView notDigitalNoticeListView;

    private AirMapStatus status;
    private List<AirMapStatusRequirementNotice> digitalNotices;
    private Set<String> digitalNoticeNames;
    private List<AirMapStatusRequirementNotice> notDigitalNotices;
    private List<String> notDigitalNoticeNames;

    public ReviewNoticeFragment() {
        // Required empty public constructor
    }

    public static ReviewNoticeFragment newInstance(AirMapStatus status, boolean submitNotice) {
        ReviewNoticeFragment fragment = new ReviewNoticeFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_STATUS, status);
        args.putBoolean(ARG_NOTICE, submitNotice);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_review_notices, container, false);
        initializeViews(view);
        status = (AirMapStatus) getArguments().getSerializable(ARG_STATUS);
        getNotices();
        setupDigitalNoticeList();
        setupNotDigitalNoticeList();
        return view;
    }

    private void initializeViews(View view) {
        submitNoticeSwitch = (TextView) view.findViewById(R.id.submit_notice_switch);
        digitalNoticeListView = (ListView) view.findViewById(R.id.digital_notice_list);
        notDigitalLabelTextView = (TextView) view.findViewById(R.id.not_digital_label);
        notDigitalNoticeListView = (ListView) view.findViewById(R.id.not_digital_list);
    }

    private void getNotices() {
        digitalNotices = new ArrayList<>();
        digitalNoticeNames = new HashSet<>();
        notDigitalNotices = new ArrayList<>();
        notDigitalNoticeNames = new ArrayList<>();
        for (AirMapStatusAdvisory advisory : status.getAdvisories()) {
            if (advisory.getRequirements() != null && advisory.getRequirements().getNotice() != null) {
                AirMapStatusRequirementNotice notice = advisory.getRequirements().getNotice();
                if (notice.isDigital()) {
                    digitalNotices.add(notice);

                    boolean useAdvisoryName = true;
                    if (!TextUtils.isEmpty(advisory.getOrganizationId())) {
                        for (AirMapPermitIssuer issuer : status.getOrganizations()) {
                            if (advisory.getOrganizationId().equals(issuer.getId())) {
                                digitalNoticeNames.add(issuer.getName());
                                useAdvisoryName = false;
                            }
                        }
                    }

                    if (useAdvisoryName) {
                        digitalNoticeNames.add(advisory.getName());
                    }
                } else if (notice.isNoticeRequired()) {
                    notDigitalNotices.add(notice);
                    notDigitalNoticeNames.add(advisory.getName());
                }
            }
        }

        // If there aren't any advisories that accept digital notice, then set notify to false
        if (digitalNotices.size() == 0) {
            submitNoticeSwitch.setVisibility(View.GONE);
            mListener.getFlight().setNotify(false);
        }
    }

    private void setupDigitalNoticeList() {
        if (digitalNoticeNames.isEmpty() && status.getApplicablePermits().isEmpty()) {
            submitNoticeSwitch.setVisibility(View.GONE);
        }

        if (!digitalNoticeNames.isEmpty()) {
            digitalNoticeListView.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_list_item_1, new ArrayList<>(digitalNoticeNames)));
        }
    }

    private void setupNotDigitalNoticeList() {
        if (notDigitalNoticeNames.isEmpty()) {
            notDigitalLabelTextView.setVisibility(View.GONE);
        } else {
            List<Map<String, String>> list = new ArrayList<>();
            for (int i = 0; i < notDigitalNotices.size(); i++) {
                Map<String, String> map = new HashMap<>();
                map.put("name", notDigitalNoticeNames.get(i));
                String number = notDigitalNotices.get(i).getPhoneNumber();
                if (number == null || number.length() < 10) {
                    number = getString(R.string.no_known_number);
                }
                map.put("phone", number);
                list.add(map);
            }
            notDigitalNoticeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                    String number = notDigitalNotices.get(position).getPhoneNumber();
                    if (number != null && number.length() >= 10) {
                        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + number));
                        try {
                            startActivity(intent);
                        } catch (Exception e) {
                            // No phone app installed
                            e.printStackTrace();
                        }
                    }
                }
            });
            notDigitalNoticeListView.setAdapter(new SimpleAdapter(getContext(), list, android.R.layout.simple_list_item_2, new String[]{"name", "phone"}, new int[]{android.R.id.text1, android.R.id.text2}));
        }
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
    }
}
