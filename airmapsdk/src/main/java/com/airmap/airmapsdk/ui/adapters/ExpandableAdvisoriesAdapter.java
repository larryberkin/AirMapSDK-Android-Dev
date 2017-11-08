package com.airmap.airmapsdk.ui.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.ColorRes;
import android.support.annotation.IntegerRes;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.airmap.airmapsdk.Analytics;
import com.airmap.airmapsdk.R;
import com.airmap.airmapsdk.models.status.AirMapAdvisory;
import com.airmap.airmapsdk.models.status.AirMapStatus;
import com.airmap.airmapsdk.models.status.properties.AirMapAirportProperties;
import com.airmap.airmapsdk.models.status.properties.AirMapControlledAirspaceProperties;
import com.airmap.airmapsdk.models.status.properties.AirMapHeliportProperties;
import com.airmap.airmapsdk.models.status.properties.AirMapNotamProperties;
import com.airmap.airmapsdk.models.status.properties.AirMapPowerPlantProperties;
import com.airmap.airmapsdk.models.status.properties.AirMapTfrProperties;
import com.airmap.airmapsdk.models.status.properties.AirMapWildfireProperties;
import com.airmap.airmapsdk.networking.services.MappingService;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by collin@airmap.com on 5/30/17.
 */

public class ExpandableAdvisoriesAdapter extends ExpandableRecyclerAdapter<MappingService.AirMapAirspaceType, AirMapAdvisory> {

    public ExpandableAdvisoriesAdapter(LinkedHashMap<MappingService.AirMapAirspaceType, List<AirMapAdvisory>> advisories) {
        super(advisories);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case PARENT_VIEW_TYPE: {
                return new AirspaceTypeViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_advisory_type, parent, false));
            }
            case CHILD_VIEW_TYPE: {
                return new AdvisoryViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.item_advisory, parent, false));
            }
        }
        return null;
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        if (holder instanceof AirspaceTypeViewHolder) {
            MappingService.AirMapAirspaceType type = (MappingService.AirMapAirspaceType) getItem(position);
            Context context = holder.itemView.getContext();
            String typeText = context.getString(type.getTitle());
            String typeAndQuantityText = context.getString(R.string.advisory_type_quantity, typeText, Integer.toString(dataMap.get(type).size()));
            AirMapStatus.StatusColor color = calculateStatusColor(type);

            ((AirspaceTypeViewHolder) holder).type = type;
            ((AirspaceTypeViewHolder) holder).backgroundView.setBackgroundColor(ContextCompat.getColor(context, color.getColorRes()));
            ((AirspaceTypeViewHolder) holder).textView.setText(typeAndQuantityText);
            ((AirspaceTypeViewHolder) holder).textView.setTextColor(ContextCompat.getColor(context, getTextColor(color)));
            ((AirspaceTypeViewHolder) holder).expandImageView.setImageResource(isExpanded(type) ? R.drawable.ic_drop_down_up : R.drawable.ic_drop_down);
            ((AirspaceTypeViewHolder) holder).expandImageView.setColorFilter(ContextCompat.getColor(context, getTextColor(color)), PorterDuff.Mode.SRC_ATOP);

        } else if (holder instanceof AdvisoryViewHolder) {
            final AirMapAdvisory advisory = (AirMapAdvisory) getItem(position);
            ((AdvisoryViewHolder) holder).backgroundView.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), advisory.getColor().getColorRes()));
            ((AdvisoryViewHolder) holder).titleTextView.setText(advisory.getName());
            ((AdvisoryViewHolder) holder).infoTextView.setOnClickListener(null);
            ((AdvisoryViewHolder) holder).infoTextView.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.font_light_grey));

            String description = "";
            if (advisory.getType() != null) {
                switch (advisory.getType()) {
                    case TFR: {
                        AirMapTfrProperties tfr = advisory.getTfrProperties();
                        SimpleDateFormat dateFormat;
                        if (tfr.getStartTime() != null && tfr.getEndTime() != null) {
                            if (DateUtils.isToday(tfr.getStartTime().getTime())) {
                                dateFormat = new SimpleDateFormat("h:mm a");
                            } else {
                                dateFormat = new SimpleDateFormat("MMM d h:mm a");
                            }
                            description = dateFormat.format(tfr.getStartTime()) + " - " + dateFormat.format(tfr.getEndTime());
                        }

                        final String url = tfr.getUrl();
                        if (!TextUtils.isEmpty(url)) {
                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Analytics.logEvent(Analytics.Page.ADVISORIES, Analytics.Action.tap, Analytics.Label.TFR_DETAILS);

                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                    holder.itemView.getContext().startActivity(intent);
                                }
                            });
                        }
                        break;
                    }
                    case PowerPlant: {
                        AirMapPowerPlantProperties powerPlant = advisory.getPowerPlantProperties();
                        description = powerPlant.getTech();
                        break;
                    }
                    case Fires:
                    case Wildfires: {
                        AirMapWildfireProperties wildfire = advisory.getWildfireProperties();
                        SimpleDateFormat dateFormat = new SimpleDateFormat("h:mm a");
                        String unknownSize = holder.itemView.getContext().getString(R.string.unknown_size);

                        if (wildfire != null && wildfire.getEffectiveDate() != null) {
                            description = dateFormat.format(wildfire.getEffectiveDate())  + " - " + (wildfire.getSize() == -1 ? unknownSize : String.format(Locale.US, "%d acres", wildfire.getSize()));
                        }
                        break;
                    }
                    case Airport: {
                        final AirMapAirportProperties airport = advisory.getAirportProperties();
                        description = formatPhoneNumber(holder.itemView.getContext(), airport.getPhone());

                        if (!TextUtils.isEmpty(airport.getPhone())) {
                            ((AdvisoryViewHolder) holder).infoTextView.setTextColor(ContextCompat.getColor(((AdvisoryViewHolder) holder).infoTextView.getContext(), R.color.airmap_aqua));
                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    call(holder.itemView.getContext(), advisory.getName(), airport.getPhone());
                                }
                            });
                        }
                        break;
                    }
                    case Heliport: {
                        final AirMapHeliportProperties heliport = advisory.getHeliportProperties();
                        description = formatPhoneNumber(holder.itemView.getContext(), heliport.getPhoneNumber());

                        if (!TextUtils.isEmpty(heliport.getPhoneNumber())) {
                            ((AdvisoryViewHolder) holder).infoTextView.setTextColor(ContextCompat.getColor(((AdvisoryViewHolder) holder).infoTextView.getContext(), R.color.airmap_aqua));
                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    call(holder.itemView.getContext(), advisory.getName(), heliport.getPhoneNumber());
                                }
                            });
                        }
                        break;
                    }
                    case Notam: {
                        AirMapNotamProperties notam = advisory.getNotamProperties();
                        SimpleDateFormat dateFormat;
                        if (notam.getStartTime() != null && DateUtils.isToday(notam.getStartTime().getTime())) {
                            dateFormat = new SimpleDateFormat("h:mm a");
                        } else {
                            dateFormat = new SimpleDateFormat("MMM d h:mm a");
                        }
                        description = dateFormat.format(notam.getStartTime()) + " - " + dateFormat.format(notam.getEndTime());

                        final String url = notam.getUrl();
                        if (!TextUtils.isEmpty(url)) {
                            holder.itemView.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Analytics.logEvent(Analytics.Page.ADVISORIES, Analytics.Action.tap, Analytics.Label.TFR_DETAILS);

                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                                    holder.itemView.getContext().startActivity(intent);
                                }
                            });
                        }
                        break;
                    }
                    case ControlledAirspace: {
                        AirMapControlledAirspaceProperties controlledAirspaceProperties = advisory.getControlledAirspaceProperties();

                        if (controlledAirspaceProperties.isLaanc() && controlledAirspaceProperties.isAuthorization()) {
                            description = holder.itemView.getContext().getString(R.string.airspace_laanc_authorization_automated);
                            ((AdvisoryViewHolder) holder).infoTextView.setTextColor(ContextCompat.getColor(((AdvisoryViewHolder) holder).infoTextView.getContext(), R.color.airmap_aqua));
                        }
                        break;
                    }
                }
            }

            // if this advisory accepts digital notice, show user
            if (advisory.getRequirements() != null && advisory.getRequirements().getNotice() != null && advisory.getRequirements().getNotice().isDigital()) {
                description = holder.itemView.getContext().getString(R.string.accepts_digital_notice);
                holder.itemView.setOnClickListener(null);
                ((AdvisoryViewHolder) holder).infoTextView.setTextColor(ContextCompat.getColor(((AdvisoryViewHolder) holder).infoTextView.getContext(), R.color.airmap_aqua));
            }

            ((AdvisoryViewHolder) holder).infoTextView.setText(description);
            ((AdvisoryViewHolder) holder).infoTextView.setVisibility(TextUtils.isEmpty(description) ? View.GONE : View.VISIBLE);
        }
    }

    private String formatPhoneNumber(Context context, String number) {
        if (TextUtils.isEmpty(number)) {
            return context.getString(R.string.no_phone_number_provided);
        }

        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
        Locale locale = Locale.getDefault();
        String country = locale != null && locale.getCountry() != null && !TextUtils.isEmpty(locale.getCountry()) ? locale.getCountry() : "US";
        try {
            Phonenumber.PhoneNumber phoneNumber = phoneUtil.parse(number, country);
            return phoneUtil.format(phoneNumber, PhoneNumberUtil.PhoneNumberFormat.NATIONAL);
        } catch (NumberParseException e) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                return PhoneNumberUtils.formatNumber(number, country);
            }
            return PhoneNumberUtils.formatNumber(number);
        }
    }

    private void call(final Context context, String name, final String phoneNumber) {
        new AlertDialog.Builder(context)
                .setTitle(name)
                .setMessage(context.getString(R.string.do_you_want_to_call, name))
                .setPositiveButton(R.string.call, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneNumber));
                        if (intent.resolveActivity(context.getPackageManager()) != null) {
                            context.startActivity(intent); //Only start activity if the device has a phone (e.g. A tablet might not)
                        } else {
                            Toast.makeText(context, R.string.no_dialer_found, Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }


    @Override
    protected void toggleExpandingViewHolder(final RecyclerView.ViewHolder holder, final boolean expanded) {
        ((AirspaceTypeViewHolder) holder).expandImageView.setImageResource(expanded ? R.drawable.ic_drop_down_up : R.drawable.ic_drop_down);

        if (expanded) {
            Analytics.logEvent(Analytics.Event.advisories, Analytics.Action.expand, ((AirspaceTypeViewHolder) holder).type.toString());
        }
    }

    private AirMapStatus.StatusColor calculateStatusColor(MappingService.AirMapAirspaceType type) {
        AirMapStatus.StatusColor color = AirMapStatus.StatusColor.Green;
        for (AirMapAdvisory advisory : dataMap.get(type)) {
            if (advisory.getColor() == AirMapStatus.StatusColor.Red) {
                color = AirMapStatus.StatusColor.Red;
                break;
            } else if (advisory.getColor() == AirMapStatus.StatusColor.Orange) {
                color = AirMapStatus.StatusColor.Orange;
            } else if (advisory.getColor() == AirMapStatus.StatusColor.Yellow && color == AirMapStatus.StatusColor.Green) {
                color = AirMapStatus.StatusColor.Yellow;
            }
        }

        return color;
    }

    @ColorRes
    private int getTextColor(AirMapStatus.StatusColor statusColor) {
        if (statusColor == AirMapStatus.StatusColor.Yellow) {
            return R.color.font_black;
        }

        return R.color.font_white;
    }

    private class AirspaceTypeViewHolder extends RecyclerView.ViewHolder {
        View backgroundView;
        TextView textView;
        ImageView expandImageView;
        MappingService.AirMapAirspaceType type;

        AirspaceTypeViewHolder(View itemView) {
            super(itemView);

            backgroundView = itemView.findViewById(R.id.background_view);
            textView = itemView.findViewById(R.id.title_text_view);
            expandImageView = itemView.findViewById(R.id.expand_image_view);
        }
    }

    private class AdvisoryViewHolder extends RecyclerView.ViewHolder {
        View backgroundView;
        TextView titleTextView;
        TextView infoTextView;

        AdvisoryViewHolder(View itemView) {
            super(itemView);

            backgroundView = itemView.findViewById(R.id.background_view);
            titleTextView = itemView.findViewById(R.id.title_text_view);
            infoTextView = itemView.findViewById(R.id.info_text_view);
        }
    }
}
