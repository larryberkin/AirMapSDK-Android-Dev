package com.airmap.airmapsdk.ui.fragments;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.airmap.airmapsdk.Analytics;
import com.airmap.airmapsdk.R;
import com.airmap.airmapsdk.models.permits.AirMapAvailablePermit;
import com.airmap.airmapsdk.models.permits.AirMapPilotPermit;
import com.airmap.airmapsdk.models.status.AirMapStatusPermits;
import com.airmap.airmapsdk.ui.activities.WebActivity;
import com.airmap.airmapsdk.ui.adapters.PermitsAdapter;
import com.airmap.airmapsdk.util.Constants;

import java.util.ArrayList;

public class ListPermitsFragment extends Fragment {

    private OnFragmentInteractionListener mListener;

    private TextView summaryTextView;
    private RecyclerView recyclerView;
    private PermitsAdapter adapter;
    private Button nextButton;

    public ListPermitsFragment() {
    }

    public static ListPermitsFragment newInstance() {
        return new ListPermitsFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.airmap_fragment_list_permits, container, false);
        initializeViews(view);
        setupRecyclerView();
        updateSummaryText();
        updateNextButton();
        nextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onListPermitsNextClicked(adapter.getSelectedPermits());

                Analytics.logEvent(Analytics.Page.PERMITS_CREATE_FLIGHT, Analytics.Action.tap, Analytics.Label.NEXT);
            }
        });
        return view;
    }

    private void initializeViews(View view) {
        recyclerView = (RecyclerView) view.findViewById(R.id.permit_list);
        nextButton = (Button) view.findViewById(R.id.next_button);
        summaryTextView = (TextView) view.findViewById(R.id.summary);
    }

    private void setupRecyclerView() {
        if (adapter == null) {
            adapter = new PermitsAdapter(mListener.getStatusPermits(), mListener.getPermitsFromWallet(), this, mListener);

            if (!mListener.getPermitsToShowInReview().isEmpty()) {
                for (AirMapAvailablePermit selectedPermit : mListener.getPermitsToShowInReview()) {
                    adapter.addEnabledPermit(selectedPermit);
                    adapter.addSelectedPermit(selectedPermit);
                }
            }

            recyclerView.setAdapter(adapter);
        }
    }

    public void onEnabledPermit(AirMapAvailablePermit permit) {
        adapter.addEnabledPermit(permit);
        updateSummaryText();
        updateNextButton();
    }

    public void onSelectPermit(AirMapAvailablePermit permit) {
        adapter.addSelectedPermit(permit);
        updateSummaryText();
        updateNextButton();
    }

    public void updateNextButton() {
        nextButton.setEnabled(adapter.getSelectedPermits().size() == adapter.getItemCount());
    }

    public void updateSummaryText() {
        // if there is no available permits for an organization of this flight tell the user
        for (AirMapStatusPermits statusPermit : mListener.getStatusPermits()) {
            if (statusPermit.getApplicablePermits().isEmpty()) {
                summaryTextView.setText(R.string.no_available_permits_for_flight);
                return;
            }
        }

//        String summary = getString(R.string.you_have_selected_permits_of_total_required, adapter.getSelectedPermits().size(), adapter.getItemCount());
        String summary = getString(R.string.requires_permits_from_authorities);
        summaryTextView.setText(summary);
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

    @Override
    public void onResume() {
        super.onResume();
        setupRecyclerView();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
        super.onCreateOptionsMenu(menu, menuInflater);
        menuInflater.inflate(R.menu.menu_permits, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        if (menuItem.getItemId() == R.id.permit_faq) {
            Analytics.logEvent(Analytics.Page.PERMITS_CREATE_FLIGHT, Analytics.Action.tap, Analytics.Label.INFO_FAQ_BUTTON);

            Intent intent = new Intent(getActivity(), WebActivity.class);
            intent.putExtra(Intent.EXTRA_TITLE, getString(R.string.faqs));
            intent.putExtra(Constants.URL_EXTRA, Constants.FAQ_PERMITS_URL);
            startActivity(intent);
            return true;
        } else {
            return super.onOptionsItemSelected(menuItem);
        }
    }

    public interface OnFragmentInteractionListener {
        ArrayList<AirMapStatusPermits> getStatusPermits();

        ArrayList<AirMapPilotPermit> getSelectedPermits();

        ArrayList<AirMapPilotPermit> getPermitsFromWallet();

        ArrayList<AirMapAvailablePermit> getPermitsToShowInReview();

        void selectPermit(AirMapStatusPermits permit);

        void onListPermitsNextClicked(ArrayList<AirMapAvailablePermit> selectedPermits);
    }
}
