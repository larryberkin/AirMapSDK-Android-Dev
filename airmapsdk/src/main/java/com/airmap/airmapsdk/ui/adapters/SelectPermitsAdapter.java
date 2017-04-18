package com.airmap.airmapsdk.ui.adapters;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.airmap.airmapsdk.Analytics;
import com.airmap.airmapsdk.R;
import com.airmap.airmapsdk.models.permits.AirMapAvailablePermit;
import com.airmap.airmapsdk.models.permits.AirMapPilotPermit;
import com.airmap.airmapsdk.models.status.AirMapStatusPermits;
import com.airmap.airmapsdk.ui.activities.CustomPropertiesActivity;
import com.airmap.airmapsdk.util.Constants;
import com.airmap.airmapsdk.util.Utils;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Vansh Gandhi on 7/19/16.
 * Copyright © 2016 AirMap, Inc. All rights reserved.
 */
public class SelectPermitsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public final int HEADER_VIEW_TYPE = 0;
    public final int PERMIT_VIEW_TYPE = 1;
    public final int INSTRUCTIONS_VIEW_TYPE = 2;

    private Activity activity;
    private List permitsAndHeadersList;
    private Set<String> applicablePermitIds;
    private Map<String, AirMapPilotPermit> walletPermitsMap;
    private DateFormat dateFormat;

    public SelectPermitsAdapter(Activity activity, AirMapStatusPermits statusPermits, List<AirMapPilotPermit> permitsFromWallet) {
        this.activity = activity;

        dateFormat = Utils.getDateTimeFormat();

        permitsAndHeadersList = new ArrayList<>();
        applicablePermitIds = new HashSet<>();

        walletPermitsMap = new HashMap<>();
        if (permitsFromWallet != null && !permitsFromWallet.isEmpty()) {
            for (AirMapPilotPermit permit : permitsFromWallet) {
                if ((permit.getStatus() == AirMapPilotPermit.PermitStatus.Accepted || permit.getStatus() == AirMapPilotPermit.PermitStatus.Pending) && (permit.getExpiresAt() == null || permit.getExpiresAt().after(new Date()))) {
                    // if there's already a wallet permit, use the one with the better status (accepted > pending > *)
                    AirMapPilotPermit duplicate = walletPermitsMap.get(permit.getShortDetails().getPermitId());
                    if (duplicate == null || (duplicate.getStatus() != AirMapPilotPermit.PermitStatus.Accepted)) {
                        walletPermitsMap.put(permit.getShortDetails().getPermitId(), permit);
                    }
                }
            }
        }

        List<AirMapAvailablePermit> existingPermits = new ArrayList<>();
        List<AirMapAvailablePermit> otherAvailablePermits = new ArrayList<>();
        List<AirMapAvailablePermit> nonAvailablePermits = new ArrayList<>();

        for (AirMapAvailablePermit applicablePermit : statusPermits.getApplicablePermits()) {
            applicablePermitIds.add(applicablePermit.getId());

            AirMapPilotPermit pilotPermit = walletPermitsMap.get(applicablePermit.getId());
            if (pilotPermit != null && (pilotPermit.getStatus() == AirMapPilotPermit.PermitStatus.Accepted || pilotPermit.getStatus() == AirMapPilotPermit.PermitStatus.Pending)
                    && (pilotPermit.getExpiresAt() == null || pilotPermit.getExpiresAt().after(new Date()))) {
                existingPermits.add(applicablePermit);
            } else {
                otherAvailablePermits.add(applicablePermit);
            }
        }

        for (AirMapAvailablePermit availablePermit : statusPermits.getAvailablePermits()) {
            if (!applicablePermitIds.contains(availablePermit.getId())) {
                nonAvailablePermits.add(availablePermit);
            }
        }

        // add Existing Permits header and section
        if (!existingPermits.isEmpty()) {
            permitsAndHeadersList.add(new Header(activity.getString(R.string.existing_permits)));
            permitsAndHeadersList.addAll(existingPermits);
        }

        // add Other Available Permits
        if (!otherAvailablePermits.isEmpty()) {
            permitsAndHeadersList.add(new Header(activity.getString(R.string.available_permits)));
            permitsAndHeadersList.addAll(otherAvailablePermits);
        }

        // add Non-Available Permits header and section
        if (!nonAvailablePermits.isEmpty()) {
            permitsAndHeadersList.add(new Header(activity.getString(R.string.unavailable_permits)));
            permitsAndHeadersList.addAll(nonAvailablePermits);
        }
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case INSTRUCTIONS_VIEW_TYPE: {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_instructions, parent, false);
                return new InstructionsViewHolder(view);
            }
            case HEADER_VIEW_TYPE: {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.permit_header_item, parent, false);
                return new HeaderViewHolder(view);
            }
            case PERMIT_VIEW_TYPE: {
                View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.available_permit_list_item, parent, false);
                return new PermitViewHolder(view);
            }
        }

        return null;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder viewHolder, int position) {
        if (viewHolder instanceof InstructionsViewHolder) {
            final InstructionsViewHolder holder = (InstructionsViewHolder) viewHolder;
            holder.titleTextView.setText(R.string.select_a_permit);
            holder.descriptionTextView.setText(R.string.select_a_permit_description);
        } else if (viewHolder instanceof HeaderViewHolder) {
            final HeaderViewHolder holder = (HeaderViewHolder) viewHolder;
            holder.titleTextView.setText(((Header) getItem(position)).name);
        } else if (viewHolder instanceof PermitViewHolder) {
            final PermitViewHolder holder = (PermitViewHolder) viewHolder;
            final AirMapAvailablePermit permit = (AirMapAvailablePermit) getItem(position);

            holder.permitNameTextView.setText(permit.getName());
            holder.permitDescriptionTextView.setText(permit.getDescription());

            AirMapPilotPermit walletPermit = walletPermitsMap.get(permit.getId());
            if (walletPermit != null && (walletPermit.getStatus() == AirMapPilotPermit.PermitStatus.Accepted || walletPermit.getStatus() == AirMapPilotPermit.PermitStatus.Pending)
                    && (walletPermit.getExpiresAt() == null || walletPermit.getExpiresAt().after(new Date()))) {
                holder.iconImageView.setVisibility(View.VISIBLE);
                if (activity != null) {
                    holder.statusTextView.setText(activity.getString(R.string.permit_status, Utils.titleCase(walletPermit.getStatus().toString())));
                    holder.expirationTextView.setText(activity.getString(R.string.pilot_permit_expiration_format,
                            walletPermit.getExpiresAt() != null ? dateFormat.format(walletPermit.getExpiresAt()) : activity.getString(R.string.na)));
                }
                holder.statusTextView.setVisibility(View.VISIBLE);
                holder.expirationTextView.setVisibility(View.VISIBLE);
            } else {
                holder.iconImageView.setVisibility(View.GONE);
                holder.statusTextView.setVisibility(View.GONE);
                holder.expirationTextView.setVisibility(View.GONE);
            }

            boolean isApplicable = applicablePermitIds.contains(permit.getId());

            if (isApplicable) {
                //enable view/text
                holder.permitNameTextView.setEnabled(true);
                holder.permitDescriptionTextView.setEnabled(true);
                holder.cardView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        AirMapPilotPermit pilotPermit = walletPermitsMap.get(permit.getId());
                        if (pilotPermit != null && (pilotPermit.getStatus() == AirMapPilotPermit.PermitStatus.Accepted || pilotPermit.getStatus() == AirMapPilotPermit.PermitStatus.Pending)
                                && (pilotPermit.getExpiresAt() == null || pilotPermit.getExpiresAt().after(new Date()))) {
                            Analytics.logEvent(Analytics.Page.AVAILABLE_PERMITS_CREATE_FLIGHT, Analytics.Action.tap, Analytics.Label.SELECT_PERMIT);

                            Intent data = new Intent();
                            data.putExtra(Constants.AVAILABLE_PERMIT_EXTRA, permit);
                            activity.setResult(Activity.RESULT_OK, data);
                            activity.finish();
                        } else {
                            Analytics.logEvent(Analytics.Page.AVAILABLE_PERMITS_CREATE_FLIGHT, Analytics.Action.tap, Analytics.Label.PERMIT_DETAILS);

                            Intent intent = new Intent(activity, CustomPropertiesActivity.class);
                            intent.putExtra(Constants.AVAILABLE_PERMIT_EXTRA, permit);
                            activity.startActivityForResult(intent, Constants.CUSTOM_PROPERTIES_REQUEST_CODE);
                        }
                    }
                });
                holder.cardView.setClickable(true);

                setElevation(holder.itemView, true);
            } else {
                //disable view/text
                holder.permitNameTextView.setEnabled(false);
                holder.permitDescriptionTextView.setEnabled(false);
                holder.cardView.setOnClickListener(null);
                holder.cardView.setClickable(false);

                setElevation(holder.itemView, false);
            }
        }
    }

    private void setElevation(View view, boolean elevate) {
        if (elevate) {
            ViewCompat.setElevation(view, Utils.dpToPixels(activity, 2));
        } else {
            ViewCompat.setElevation(view, 0f);
        }
    }

    @Override
    public int getItemCount() {
        return permitsAndHeadersList.size() + 1;
    }

    public Object getItem(int position) {
        return permitsAndHeadersList.get(position - 1);
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return INSTRUCTIONS_VIEW_TYPE;
        } else if (getItem(position) instanceof Header) {
            return HEADER_VIEW_TYPE;
        } else {
            return PERMIT_VIEW_TYPE;
        }
    }

    public class Header {
        public final String name;

        public Header(String name) {
            this.name = name;
        }
    }

    public class PermitViewHolder extends RecyclerView.ViewHolder {

        public View cardView;
        public ImageView iconImageView;
        public TextView permitNameTextView;
        public TextView permitDescriptionTextView;
        public TextView statusTextView;
        public TextView expirationTextView;

        PermitViewHolder(View itemView) {
            super(itemView);

            cardView = itemView.findViewById(R.id.card_view);
            iconImageView = (ImageView) itemView.findViewById(R.id.icon_image_view);
            permitNameTextView = (TextView) itemView.findViewById(R.id.permit_name_text_view);
            permitDescriptionTextView = (TextView) itemView.findViewById(R.id.permit_description_text_view);
            statusTextView = (TextView) itemView.findViewById(R.id.status_text_view);
            expirationTextView = (TextView) itemView.findViewById(R.id.expiration_text_view);
        }
    }

    public class HeaderViewHolder extends RecyclerView.ViewHolder {
        public TextView titleTextView;

        HeaderViewHolder(View itemView) {
            super(itemView);
            titleTextView = (TextView) itemView.findViewById(R.id.header_text_view);
        }
    }

    public class InstructionsViewHolder extends RecyclerView.ViewHolder {
        public TextView titleTextView;
        public TextView descriptionTextView;

        InstructionsViewHolder(View itemView) {
            super(itemView);
            titleTextView = (TextView) itemView.findViewById(R.id.title_text_view);
            descriptionTextView = (TextView) itemView.findViewById(R.id.description_text_view);
        }
    }
}
