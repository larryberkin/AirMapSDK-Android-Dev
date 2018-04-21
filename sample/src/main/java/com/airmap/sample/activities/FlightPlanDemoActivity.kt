package com.airmap.sample.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.text.TextUtils
import android.view.View
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Toast
import com.airmap.airmapsdk.AirMapException
import com.airmap.airmapsdk.controllers.RulesetsEvaluator
import com.airmap.airmapsdk.models.Coordinate
import com.airmap.airmapsdk.models.flight.AirMapFlightFeature
import com.airmap.airmapsdk.models.flight.AirMapFlightPlan
import com.airmap.airmapsdk.models.rules.AirMapRule
import com.airmap.airmapsdk.models.rules.AirMapRuleset
import com.airmap.airmapsdk.models.shapes.AirMapGeometry
import com.airmap.airmapsdk.models.shapes.AirMapPolygon
import com.airmap.airmapsdk.networking.callbacks.AirMapCallback
import com.airmap.airmapsdk.networking.services.AirMap
import com.airmap.airmapsdk.ui.views.AirMapMapView
import com.airmap.airmapsdk.util.AirMapConstants
import com.airmap.sample.R
import com.airmap.sample.ui.FlightPlanDetailsAdapter
import org.json.JSONObject
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class FlightPlanDemoActivity : AppCompatActivity() {

    companion object {
        fun Context.FlightPlanDemoIntent() = Intent(this, FlightPlanDemoActivity::class.java)
    }

    private val toolbar by lazy { findViewById<Toolbar>(R.id.toolbar) }
    private val startTimeTextView by lazy { findViewById<TextView>(R.id.start_time_value) }
    private val endTimeTextView by lazy { findViewById<TextView>(R.id.end_time_value) }
    private val altitudeTextView by lazy { findViewById<TextView>(R.id.altitude_value) }
    private val altitudeSeekBar by lazy { findViewById<SeekBar>(R.id.altitude_seekbar) }
    private val flightFeaturesRecyclerView by lazy { findViewById<RecyclerView>(R.id.flight_features_recycler_view) }
    private val loadingView by lazy { findViewById<View>(R.id.loading_view) }

    private var flightPlan: AirMapFlightPlan? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flight_plan)

        toolbar.title = title
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        altitudeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                val altitude = 10 + 10 * progress
                altitudeTextView!!.text = altitude.toString() + " meters"
                flightPlan!!.maxAltitude = altitude.toFloat()
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {

            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {

            }
        })

        // create polygon from coordinates
        val coordinates = ArrayList<Coordinate>()
        listOf(Coordinate(34.02440874647921, -117.49167761708696),
                Coordinate(34.020040687842254, -117.4968401460024),
                Coordinate(34.01648293903452, -117.4923151205652),
                Coordinate(34.02080536486173, -117.48725884231055),
                Coordinate(34.02440874647921, -117.49167761708696))

        val polygon = AirMapPolygon().apply {
            this.coordinates = coordinates
        }

        val geometryJSON = AirMapGeometry.getGeoJSONFromGeometry(polygon)
        /* or use raw json
            {
                "type": "Polygon",
                "coordinates": [
                    [
                        [-118.49167761708696, 34.02440874647921],
                        [-118.4968401460024, 34.020040687842254],
                        [-118.4923151205652, 34.01648293903452],
                        [-118.48725884231055, 34.02080536486173],
                        [-118.49167761708696, 34.02440874647921]
                    ]
                ]
            }
         */

        // get rulesets (from jurisdictions) from geometry
        AirMap.getRulesets(geometryJSON, object : AirMapCallback<List<AirMapRuleset>>() {
            override fun onSuccess(availableRulesets: List<AirMapRuleset>) {
                if (!availableRulesets.isEmpty()) {
                    // Calculate selected rulesets based off preferred & unpreferred rulesets
                    // If no preferred/unpreferred, defaults are selected
                    val selectedRulesets = RulesetsEvaluator.computeSelectedRulesets(availableRulesets, AirMapMapView.AutomaticConfiguration())

                    createFlightPlan(geometryJSON, 100f, null, selectedRulesets)

                    loadingView.visibility = View.GONE
                }
            }

            override fun onError(e: AirMapException) = Timber.e(e, "Error getting rulesets from geometry %s : %s", geometryJSON, e.detailedMessage)
        })
    }

    fun createFlightPlan(geometry: JSONObject, buffer: Float, takeoff: Coordinate?, selectedRulesets: List<AirMapRuleset>) {
        val selectedRulesetIds = mutableListOf<String>()
        val flightFeatures = mutableListOf<AirMapFlightFeature>()
        val featuresMap = mutableMapOf<AirMapFlightFeature, MutableList<AirMapRule>>()

        for (ruleset in selectedRulesets) {
            selectedRulesetIds.add(ruleset.id)
            ruleset.rules
                    .filterNotNull()
                    .filter { it.flightFeatures?.isNotEmpty() ?: false }
                    .forEach { rule ->
                        flightFeatures.addAll(rule.flightFeatures)
                        rule.flightFeatures.forEach { flightFeature ->
                            featuresMap[flightFeature] = featuresMap[flightFeature] ?: mutableListOf()
                            featuresMap[flightFeature]?.add(rule)
                        }
                    }
        }

        // default max alt - 100m
        altitudeTextView.text = "100m"

        // sets required params
        flightPlan = AirMapFlightPlan().apply {
            this.pilotId = AirMap.getUserId()
            this.geometry = geometry.toString()
            this.buffer = buffer
            this.takeoffCoordinate = takeoff
            this.rulesetIds = selectedRulesetIds
            this.isPublic = true
            this.maxAltitude = 100f

            // default start & end time - now to 4 hours from now
            this.durationInMillis = 4 * 60 * 60 * 1000
            this.startsAt = Date()
            this.endsAt = Date(System.currentTimeMillis() + this.durationInMillis)
        }

        val sdf = SimpleDateFormat("h:mm a")
        startTimeTextView.text = sdf.format(flightPlan!!.startsAt)
        endTimeTextView.text = sdf.format(flightPlan!!.endsAt)

        val detailsAdapter = FlightPlanDetailsAdapter(this, flightPlan, featuresMap, null, object : FlightPlanDetailsAdapter.FlightPlanChangeListener {
            override fun onFlightPlanChanged() = Unit

            override fun onFlightFeatureRemoved(flightFeature: String) = Unit

            override fun onFlightPlanSave() = saveFlightPlan()
        })

        flightFeaturesRecyclerView.adapter = detailsAdapter
    }

    private fun saveFlightPlan() {
        val callback = object : AirMapCallback<AirMapFlightPlan>() {
            override fun onSuccess(response: AirMapFlightPlan) {
                Toast.makeText(this@FlightPlanDemoActivity, "Flight plan successfully saved", Toast.LENGTH_SHORT).show()

                PreferenceManager.getDefaultSharedPreferences(this@FlightPlanDemoActivity)
                        .edit()
                        .putString(AirMapConstants.FLIGHT_PLAN_ID_EXTRA, response.planId)
                        .apply()
            }

            override fun onError(e: AirMapException) {
                Toast.makeText(this@FlightPlanDemoActivity, "Flight plan failed to save", Toast.LENGTH_SHORT).show()
            }
        }

        if (TextUtils.isEmpty(flightPlan!!.flightId)) {
            AirMap.createFlightPlan(flightPlan, callback)
        } else {
            AirMap.patchFlightPlan(flightPlan, callback)
        }
    }
}
