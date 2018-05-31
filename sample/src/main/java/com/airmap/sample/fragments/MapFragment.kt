package com.airmap.sample.fragments

import android.os.Bundle
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.airmap.airmapsdk.models.rules.AirMapRuleset
import com.airmap.airmapsdk.models.status.AirMapAirspaceStatus
import com.airmap.airmapsdk.ui.activities.MyLocationMapActivity
import com.airmap.airmapsdk.ui.views.AirMapMapView
import com.airmap.sample.R
import com.airmap.sample.activities.MapDemoActivity

class MapFragment : Fragment(), AirMapMapView.OnMapDataChangeListener {

    lateinit var mapView: AirMapMapView
    private lateinit var myLocationFab: FloatingActionButton // FAB to view/change selected rulesets

    private lateinit var configuration: AirMapMapView.DynamicConfiguration

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_map, container, false)

        mapView = view.findViewById(R.id.map_view)
        mapView.onCreate(savedInstanceState)
        mapView.addOnMapDataChangedListener(this)


        myLocationFab = view.findViewById(R.id.my_location_fab)
        myLocationFab.setOnClickListener { (activity as MyLocationMapActivity).goToLastLocation(true) }

        return view
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        (activity as MapDemoActivity).setMapView(mapView)
        mapView.getMapAsync(null)

        configuration = AirMapMapView.DynamicConfiguration(null, null, true)
        mapView.configure(configuration)
    }

    override fun onRulesetsChanged(availableRulesets: List<AirMapRuleset>, selectedRulesets: List<AirMapRuleset>) {
        (activity as MapDemoActivity).setRulesets(availableRulesets, selectedRulesets)
    }

    override fun onAdvisoryStatusChanged(status: AirMapAirspaceStatus) {
        (activity as MapDemoActivity).setAdvisoryStatus(status)
    }

    override fun onAdvisoryStatusLoading() {}

    fun onRulesetSelected(ruleset: AirMapRuleset) {
        configuration.preferredRulesetIds.add(ruleset.id)
        configuration.unpreferredRulesetIds.remove(ruleset.id)
        mapView.configure(configuration)
    }

    fun onRulesetDeselected(ruleset: AirMapRuleset) {
        configuration.preferredRulesetIds.remove(ruleset.id)
        configuration.unpreferredRulesetIds.add(ruleset.id)
        mapView.configure(configuration)
    }

    fun onRulesetSwitched(fromRuleset: AirMapRuleset, toRuleset: AirMapRuleset) {
        configuration.preferredRulesetIds.remove(fromRuleset.id)
        configuration.preferredRulesetIds.add(toRuleset.id)
        mapView.configure(configuration)
    }

    // Mapbox requires lifecycle
    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mapView.onDestroy()
        mapView.removeOnMapDataChangedListener(this)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    companion object {
        fun newInstance() = MapFragment()
    }
}