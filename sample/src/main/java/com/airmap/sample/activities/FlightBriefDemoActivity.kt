package com.airmap.sample.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.widget.DividerItemDecoration
import android.support.v7.widget.RecyclerView
import android.support.v7.widget.Toolbar
import android.view.*
import com.airmap.airmapsdk.AirMapException
import com.airmap.airmapsdk.models.Coordinate
import com.airmap.airmapsdk.models.flight.AirMapFlightBriefing
import com.airmap.airmapsdk.models.flight.AirMapFlightPlan
import com.airmap.airmapsdk.models.rules.AirMapRule
import com.airmap.airmapsdk.models.shapes.AirMapGeometry
import com.airmap.airmapsdk.models.shapes.AirMapPolygon
import com.airmap.airmapsdk.networking.callbacks.AirMapCallback
import com.airmap.airmapsdk.networking.services.AirMap
import com.airmap.airmapsdk.ui.adapters.ExpandableRecyclerAdapter
import com.airmap.airmapsdk.ui.adapters.ExpandableRulesAdapter
import com.airmap.airmapsdk.util.AirMapConstants
import com.airmap.airmapsdk.util.BriefingEvaluator
import com.airmap.sample.R
import timber.log.Timber
import java.util.*

class FlightBriefDemoActivity : BaseActivity() {

    companion object {
        fun Context.FlightBriefIntent(flightPlanId: String): Intent {
            return Intent(this, FlightBriefDemoActivity::class.java).putExtra(AirMapConstants.FLIGHT_PLAN_ID_EXTRA, flightPlanId)
        }
    }

    private val toolbar by lazy { findViewById<Toolbar>(R.id.toolbar) }
    private val rulesRecyclerView by lazy { findViewById<RecyclerView>(R.id.rules_recycler_view) }
    private val loadingView by lazy { findViewById<View>(R.id.loading_view) }

    private lateinit var flightPlanId: String

    // create polygon from coordinates
    // default max alt - 100m
    // default start & end time - now to 4 hours from now
    private val sampleFlightPlan: AirMapFlightPlan
        get() {
            val rulesetIds = listOf(
                    "usa_national_marine_sanctuary",
                    "usa_ama", "usa_sec_91",
                    "usa_national_park",
                    "usa_airmap_rules",
                    "usa_sec_336"
            )

            val coordinates = listOf(
                    Coordinate(34.02440874647921, -117.49167761708696),
                    Coordinate(34.020040687842254, -117.4968401460024),
                    Coordinate(34.01648293903452, -117.4923151205652),
                    Coordinate(34.02080536486173, -117.48725884231055),
                    Coordinate(34.02440874647921, -117.49167761708696)
            )

            val polygon = AirMapPolygon().apply {
                this.coordinates = coordinates
            }
            val geometryJSON = AirMapGeometry.getGeoJSONFromGeometry(polygon)

            return AirMapFlightPlan().apply {
                pilotId = AirMap.getUserId()
                geometry = geometryJSON.toString()
                buffer = 0f
                takeoffCoordinate = coordinates[0]
                isPublic = true
                maxAltitude = 100f
                durationInMillis = (4 * 60 * 60 * 1000)
                startsAt = Date()
                endsAt = Date(System.currentTimeMillis() + durationInMillis)
                this.rulesetIds = rulesetIds
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_flight_brief)

        toolbar.title = title
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        flightPlanId = intent.getStringExtra(AirMapConstants.FLIGHT_PLAN_ID_EXTRA)
        rulesRecyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))

        // no flight plan, create one
        if (flightPlanId.isNotBlank()) {
            AirMap.createFlightPlan(sampleFlightPlan, object : AirMapCallback<AirMapFlightPlan>() {
                override fun onSuccess(response: AirMapFlightPlan) {
                    flightPlanId = response.planId
                    loadBrief()
                    invalidateOptionsMenu()
                }

                override fun onError(e: AirMapException) {
                    Timber.e(e, "Flight plan creation failed: %s", e.detailedMessage)
                    showErrorDialog("An error occurred while creating your flight plan or retrieving your flight brief.")
                }
            })
        } else {
            loadBrief()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // only show Fly option if user has flight plan
        if (flightPlanId.isNotBlank()) {
            menuInflater.inflate(R.menu.brief, menu)
        }

        return true
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        if (menuItem.itemId == R.id.fly) {
            AirMap.submitFlightPlan(flightPlanId, object : AirMapCallback<AirMapFlightPlan>() {
                override fun onSuccess(response: AirMapFlightPlan) {
                    Timber.i("Successfully created flight with flight ID %s from flight plan", response.flightId)
                    toast("Flight created!")
                }

                override fun onError(e: AirMapException) {
                    Timber.e(e, "Error submitting flight plan: %s", e.detailedMessage)
                    toast("Failed to create flight")
                }
            })
        }

        return super.onOptionsItemSelected(menuItem)
    }

    private fun loadBrief() {
        AirMap.getFlightBrief(flightPlanId, object : AirMapCallback<AirMapFlightBriefing>() {
            override fun onSuccess(briefing: AirMapFlightBriefing) {
                if (isActive()) {
                    loadingView.visibility = View.GONE

                    // calculate which rules are being violated, followed, etc
                    val sortedRulesMap = BriefingEvaluator.computeRulesViolations(briefing)

                    val rulesRecyclerAdapter = BriefingAdapter(sortedRulesMap)
                    rulesRecyclerView.adapter = rulesRecyclerAdapter
                    rulesRecyclerAdapter.expandAll()
                }
            }

            override fun onError(e: AirMapException) {
                Timber.e(e, "Erring getting flight brief: %s", e.detailedMessage)
                showErrorDialog("An error occurred while creating your flight plan or retrieving your flight brief.")
            }
        })
    }

    /**
     * You can customize the UI of the briefing rows by overriding onCreateViewHolder
     */
    private inner class BriefingAdapter internal constructor(rulesMap: LinkedHashMap<AirMapRule.Status, List<AirMapRule>>) : ExpandableRulesAdapter(rulesMap) {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = when (viewType) {
            ExpandableRecyclerAdapter.PARENT_VIEW_TYPE -> SectionViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_rule_section, parent, false))
            ExpandableRecyclerAdapter.CHILD_VIEW_TYPE -> RuleViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_rule, parent, false))
            else -> super.onCreateViewHolder(parent, viewType)
        }
    }
}
