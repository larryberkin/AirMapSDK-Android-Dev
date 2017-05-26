package com.airmap.airmapsdk.ui.activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.airmap.airmapsdk.AirMapException;
import com.airmap.airmapsdk.Analytics;
import com.airmap.airmapsdk.R;
import com.airmap.airmapsdk.models.pilot.AirMapPilot;
import com.airmap.airmapsdk.networking.callbacks.AirMapCallback;
import com.airmap.airmapsdk.networking.services.AirMap;
import com.bumptech.glide.Glide;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Map;

import static android.telephony.PhoneNumberUtils.formatNumber;

/**
 * Activity for viewing your own profile (not another person's profile)
 * @see com.airmap.airmapsdk.ui.activities.PilotProfileActivity
 */
public class ProfileActivity extends AppCompatActivity implements View.OnClickListener {

    public static final String ARG_PILOT_ID = "pilotId";

    private Toolbar toolbar;
    private ImageView profileImageView;
    private TextView nameTextView;
    private TextView usernameTextView;
    private TextView flightCounterTextView;
    private TextView aircraftCounterTextView;
    private EditText firstNameEditText;
    private EditText lastNameEditText;
    private EditText emailEditText;
    private EditText phoneEditText;
    private TextView pilotInformationTextView;
    private LinearLayout extrasLayout;
    private Button saveButton;
    private AirMapPilot profile;
    private HashMap<String, String> extras;
    private HashMap<String, String> editedExtras;
    int sizeInDp;
    float scale;
    int dpAsPixels;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.airmap_activity_profile);
        initializeViews();
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setTitle(R.string.airmap_title_activity_profile);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if (getIntent().hasExtra(CreateFlightActivity.KEY_VALUE_EXTRAS)) {
            //noinspection unchecked
            extras = (HashMap<String, String>) getIntent().getSerializableExtra(CreateFlightActivity.KEY_VALUE_EXTRAS);
            editedExtras = new HashMap<>();
        }
        String pilotId = getIntent().getStringExtra(ARG_PILOT_ID);
        if (pilotId == null || pilotId.isEmpty()) {
            pilotId = AirMap.getUserId();
        }
        getPilot(pilotId);
        sizeInDp = 16;
        scale = getResources().getDisplayMetrics().density;
        dpAsPixels = (int) (sizeInDp * scale + 0.5f);
    }

    private void getPilot(final String pilotId) {
        AirMap.getPilot(pilotId, new AirMapCallback<AirMapPilot>() {
            @Override
            public void onSuccess(AirMapPilot response) {
                if (!isFinishing()) {
                    profile = response;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            populateViews();
                        }
                    });
                }
            }

            @Override
            public void onError(final AirMapException e) {
                if (!isFinishing()) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Snackbar.make(toolbar, R.string.error_getting_profile, Snackbar.LENGTH_LONG)
                                    .setAction(R.string.retry, new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            getPilot(pilotId);
                                        }
                                    })
                                    .show();
                        }
                    });
                    Log.e("ProfileFragment", e.getMessage(), e);
                }
            }
        });
    }

    private void initializeViews() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        profileImageView = (ImageView) findViewById(R.id.profile_image);
        nameTextView = (TextView) findViewById(R.id.name);
        usernameTextView = (TextView) findViewById(R.id.username);
        flightCounterTextView = (TextView) findViewById(R.id.flight_counter_text);
        aircraftCounterTextView = (TextView) findViewById(R.id.aircraft_counter_text);
        firstNameEditText = (EditText) findViewById(R.id.first_name_edit_text);
        lastNameEditText = (EditText) findViewById(R.id.last_name_edit_text);
        emailEditText = (EditText) findViewById(R.id.email_edit_text);
        phoneEditText = (EditText) findViewById(R.id.phone_edit_text);
        pilotInformationTextView = (TextView) findViewById(R.id.pilot_information_label);
        extrasLayout = (LinearLayout) findViewById(R.id.extras_container);
        saveButton = (Button) findViewById(R.id.save);
    }

    private void populateViews() {
        Glide.with(this)
                .load(profile.getPictureUrl())
                .placeholder(R.drawable.airmap_profile_default)
                .into(profileImageView);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                findViewById(R.id.progress_bar_container).setVisibility(View.GONE);
                findViewById(R.id.scroll_view).setVisibility(View.VISIBLE);
            }
        });
        boolean editable = setEditable();
        if (!editable) {
            return; //Don't fill in data if not logged in user
        }
        nameTextView.setText(String.format("%s %s", profile.getFirstName(), profile.getLastName()));
        usernameTextView.setText(profile.getUsername());
        firstNameEditText.setText(profile.getFirstName());
        lastNameEditText.setText(profile.getLastName());
        emailEditText.setText(profile.getEmail());
        if (!TextUtils.isEmpty(profile.getPhone())) {
            phoneEditText.setText(formatNumber(profile.getPhone())); //If we don't check, the EditText might show "null"
        }
        populateExtras();
        NumberFormat format = NumberFormat.getIntegerInstance();
        try {
            aircraftCounterTextView.setText(format.format(profile.getStats().getAircraftStats().getTotal()));
        } catch (Exception e) {
            e.printStackTrace(); //Probably some NPE
            aircraftCounterTextView.setText("-");
        }
        try {
            flightCounterTextView.setText(format.format(profile.getStats().getFlightStats().getTotal()));
        } catch (Exception e) {
            e.printStackTrace(); //Probably some NPE
            flightCounterTextView.setText("-");
        }
        saveButton.setOnClickListener(this);
        phoneEditText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(final View v) {
                showPhoneDialog();
            }
        });
    }

    private void showPhoneDialog() {
        final DialogInterface.OnClickListener dismissOnClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        };
        final TextInputLayout phoneLayout = new TextInputLayout(this); //The phone EditText
        phoneLayout.setHint(getString(R.string.phone_number));
        TextInputEditText editText = new TextInputEditText(this);
        editText.setInputType(EditorInfo.TYPE_CLASS_PHONE);
        editText.setMaxLines(1);
        editText.setSingleLine();
        editText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        phoneLayout.addView(editText);
        phoneLayout.setPadding(dpAsPixels, dpAsPixels, dpAsPixels, 0);
        final AlertDialog dialog = new AlertDialog.Builder(this, R.style.Theme_AppCompat_Dialog_Alert)
                .setMessage(R.string.airmap_phone_number_disclaimer)
                .setTitle(R.string.phone_number)
                .setView(phoneLayout)
                .setNegativeButton(android.R.string.cancel, dismissOnClickListener)
                .setPositiveButton(R.string.submit, new DialogInterface.OnClickListener() { //Display dialog to enter the verification token
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Analytics.logEvent(Analytics.Page.PHONE_NUMBER_PHONE_VERIFICATION, Analytics.Action.tap, Analytics.Label.SAVE);
                        onSubmitPhoneNumber(phoneLayout);
                        dialog.dismiss();
                    }
                })
                .show();
        dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    onSubmitPhoneNumber(phoneLayout);
                    dialog.dismiss();
                    return true;
                }
                return false;
            }
        });
    }

    private void onSubmitPhoneNumber(final TextInputLayout phoneLayout) {
        //noinspection ConstantConditions
        final String phone = phoneLayout.getEditText().getText().toString();
        AirMap.updatePhoneNumber(phone, new AirMapCallback<Void>() {
            @Override
            public void onSuccess(Void response) {
                AirMap.sendVerificationToken(new AirMapCallback<Void>() {
                    @Override
                    public void onSuccess(Void response) {
                        Analytics.logEvent(Analytics.Page.PHONE_NUMBER_PHONE_VERIFICATION, Analytics.Action.save, Analytics.Label.SUCCESS);

                        phoneEditText.post(new Runnable() {
                            @Override
                            public void run() {
                                phoneEditText.setText(phone); //Update the UI with the new phone number
                                showVerifyDialog();
                            }
                        });
                    }

                    @Override
                    public void onError(AirMapException e) {
                        Analytics.logEvent(Analytics.Page.PHONE_NUMBER_PHONE_VERIFICATION, Analytics.Action.save, Analytics.Label.ERROR, e.getErrorCode());

                        e.printStackTrace();
                        toast(e.getMessage());
                    }
                });
            }

            @Override
            public void onError(AirMapException e) {
                Analytics.logEvent(Analytics.Page.PHONE_NUMBER_PHONE_VERIFICATION, Analytics.Action.save, Analytics.Label.ERROR, e.getErrorCode());

                e.printStackTrace();
                toast(e.getMessage());
            }
        });
    }

    private void showVerifyDialog() {
        final TextInputLayout verifyLayout = new TextInputLayout(this); //The verify token EditText
        verifyLayout.setPadding(dpAsPixels, dpAsPixels, dpAsPixels, 0);
        final TextInputEditText editText = new TextInputEditText(this);
        editText.setInputType(EditorInfo.TYPE_CLASS_NUMBER);
        editText.setMaxLines(1);
        editText.setSingleLine();
        editText.setImeOptions(EditorInfo.IME_ACTION_DONE);
        verifyLayout.addView(editText);
        phoneEditText.post(new Runnable() { //run on UI thread
            @Override
            public void run() {
                final AlertDialog dialog = new AlertDialog.Builder(ProfileActivity.this, R.style.Theme_AppCompat_Dialog_Alert)
                        .setView(verifyLayout)
                        .setMessage(R.string.enter_verification_token)
                        .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int i) {
                                dialog.dismiss();
                            }
                        })
                        .setPositiveButton(R.string.verify, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog, int which) {
                                onSubmitVerificationToken(verifyLayout);
                            }
                        })
                        .show();
                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
                editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
                        if (actionId == EditorInfo.IME_ACTION_DONE) {
                            dialog.dismiss();
                            onSubmitVerificationToken(verifyLayout);
                            return true;
                        }
                        return false;
                    }
                });
            }
        });
    }

    private void onSubmitVerificationToken(TextInputLayout verifyLayout) {
        Analytics.logEvent(Analytics.Page.SMS_CODE_PHONE_VERIFICATION, Analytics.Action.tap, Analytics.Label.SUBMIT);

        //noinspection ConstantConditions
        AirMap.verifyPhoneToken(verifyLayout.getEditText().getText().toString(), new AirMapCallback<Void>() {
            @Override
            public void onSuccess(Void response) {
                Analytics.logEvent(Analytics.Page.SMS_CODE_PHONE_VERIFICATION, Analytics.Action.save, Analytics.Label.SUCCESS);

                toast(getString(R.string.successfully_verified_number));
            }

            @Override
            public void onError(AirMapException e) {
                Analytics.logEvent(Analytics.Page.SMS_CODE_PHONE_VERIFICATION, Analytics.Action.save, Analytics.Label.ERROR, e.getErrorCode());

                toast(getString(R.string.error_verifying_number));
                e.printStackTrace();
            }
        });
    }

    /**
     * Disables EditTexts if not viewing own profile
     */
    private boolean setEditable() {
        boolean editable = profile.getPilotId().equals(AirMap.getUserId()); //You can edit if you are displaying your own profile
        int visibility = editable ? View.VISIBLE : View.GONE;
        firstNameEditText.setVisibility(visibility);
        lastNameEditText.setVisibility(visibility);
        emailEditText.setVisibility(visibility);
        phoneEditText.setVisibility(visibility);
        saveButton.setVisibility(visibility);
        return editable;
    }

    private void populateExtras() {
        if (extras != null) {
            for (final Map.Entry<String, String> entry : extras.entrySet()) {
                TextInputLayout layout = new TextInputLayout(this);
                TextInputEditText editText = new TextInputEditText(this);
                layout.setHint(entry.getValue());
                if (profile.getUserMetaData().getMetaData().get(entry.getKey()) instanceof String) {
                    editText.setText((String) profile.getUserMetaData().getMetaData().get(entry.getKey()));
                }
                editText.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        editedExtras.put(entry.getKey(), s.toString());
                    }
                });
                layout.addView(editText);
                extrasLayout.addView(layout);
            }
        } else {
            pilotInformationTextView.setVisibility(View.GONE);
        }
    }

    //Save button onClick
    @Override
    public void onClick(final View v) {
        Analytics.logEvent(Analytics.Page.PILOT_PROFILE, Analytics.Action.tap, Analytics.Label.SAVE);

        profile.setEmail(emailEditText.getText().toString());
        profile.setFirstName(firstNameEditText.getText().toString());
        profile.setLastName(lastNameEditText.getText().toString());
        if (editedExtras != null) {
            profile.getUserMetaData().setMetaData(new HashMap<String, Object>(editedExtras)); //Convert from <String, String> to <String, Object>
        }
        AirMap.updatePilot(profile, new AirMapCallback<AirMapPilot>() {
            @Override
            public void onSuccess(AirMapPilot response) {
                Analytics.logEvent(Analytics.Page.PILOT_PROFILE, Analytics.Action.save, Analytics.Label.SUCCESS, 200);
                toast(getString(R.string.successfully_updated));
                finish();
            }

            @Override
            public void onError(AirMapException e) {
                Analytics.logEvent(Analytics.Page.PILOT_PROFILE, Analytics.Action.save, Analytics.Label.ERROR, e.getErrorCode());
                toast(getString(R.string.error_updating_profile));
            }
        });
    }

    private void toast(final String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ProfileActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
