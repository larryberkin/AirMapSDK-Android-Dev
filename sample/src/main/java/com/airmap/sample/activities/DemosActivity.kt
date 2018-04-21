package com.airmap.sample.activities

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.widget.CardView
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.Toast

import com.airmap.airmapsdk.AirMapException
import com.airmap.airmapsdk.models.pilot.AirMapPilot
import com.airmap.airmapsdk.networking.callbacks.AirMapCallback
import com.airmap.airmapsdk.networking.callbacks.LoginCallback
import com.airmap.airmapsdk.networking.services.AirMap
import com.airmap.airmapsdk.util.AirMapConstants
import com.airmap.sample.R
import com.airmap.sample.activities.FlightBriefDemoActivity.Companion.FlightBriefIntent

import timber.log.Timber

class DemosActivity : BaseActivity() {

    private val toolbar by lazy { findViewById<Toolbar>(R.id.toolbar) }
    private val mapCardView by lazy { findViewById<CardView>(R.id.map_card_view) }
    private val loginCardView by lazy { findViewById<CardView>(R.id.login_card_view) }
    private val anonymousLoginCardView by lazy { findViewById<CardView>(R.id.anonymous_login_card_view) }
    private val flightPlanCardView by lazy { findViewById<CardView>(R.id.flight_plan_card_view) }
    private val briefingCardView by lazy { findViewById<CardView>(R.id.briefing_card_view) }
    private val trafficCardView by lazy { findViewById<CardView>(R.id.traffic_card_view) }
    private val telemetryCardView by lazy { findViewById<CardView>(R.id.telemetry_card_view) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_demos)
        setupViews()
        refreshAccessToken()
    }

    private fun setupViews() {
        toolbar.title = title
        setSupportActionBar(toolbar)

        mapCardView.setOnClickListener { startActivity(Intent(this, MapDemoActivity::class.java)) }
        anonymousLoginCardView.setOnClickListener { startActivity(Intent(this, AnonymousLoginDemoActivity::class.java)) }
        flightPlanCardView.setOnClickListener { startActivity(Intent(this, FlightPlanDemoActivity::class.java)) }
        trafficCardView.setOnClickListener { startActivity(Intent(this, TrafficDemoActivity::class.java)) }
        telemetryCardView.setOnClickListener { startActivity(Intent(this, TelemetryDemoActivity::class.java)) }
        briefingCardView.setOnClickListener {
            val flightPlanId = PreferenceManager.getDefaultSharedPreferences(this).getString(AirMapConstants.FLIGHT_PLAN_ID_EXTRA, null)
            if (flightPlanId != null) {
                startActivity(FlightBriefIntent(flightPlanId))
            } else {
                toast("Please create a flight plan first")
            }
        }
        loginCardView.setOnClickListener {
            AirMap.showLogin(this, object : LoginCallback {
                override fun onSuccess(pilot: AirMapPilot) {
                    Timber.v("Token is: %s", AirMap.getAuthToken())
                    toast("Logged in as " + pilot.username)
                }

                override fun onError(e: AirMapException) {
                    Timber.e(e, e.detailedMessage)
                    toast("Login failed")
                }
            })
        }
    }

    private fun refreshAccessToken() = AirMap.refreshAccessToken(object : AirMapCallback<Void>() {
        public override fun onSuccess(response: Void) = Timber.i("Successfully refreshed access token")
        public override fun onError(e: AirMapException) = Timber.e(e, "Refreshing access token failed: %s", e.detailedMessage)
    })
}
