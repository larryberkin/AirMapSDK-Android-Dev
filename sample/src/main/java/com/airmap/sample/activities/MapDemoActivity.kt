package com.airmap.sample.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.design.widget.TabLayout
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.support.v7.widget.Toolbar

import com.airmap.airmapsdk.models.rules.AirMapRuleset
import com.airmap.airmapsdk.models.status.AirMapAirspaceStatus
import com.airmap.airmapsdk.ui.activities.MyLocationMapActivity
import com.airmap.airmapsdk.ui.views.AirMapMapView
import com.airmap.airmapsdk.ui.views.SwipeDisableableViewPager
import com.airmap.sample.R
import com.airmap.sample.fragments.AdvisoriesFragment
import com.airmap.sample.fragments.MapFragment
import com.airmap.sample.fragments.RulesetsFragment

class MapDemoActivity : MyLocationMapActivity() {

    companion object {
        @JvmStatic
        fun Context.MapDemoIntent() = Intent(this, MapDemoActivity::class.java)
    }

    private val toolbar by lazy { findViewById<Toolbar>(R.id.toolbar) }
    private val tabLayout by lazy { findViewById<TabLayout>(R.id.tab_layout) }
    private val viewPager by lazy { findViewById<SwipeDisableableViewPager>(R.id.view_pager) }

    private val mapFragment: MapFragment? get() = supportFragmentManager.fragments.filterIsInstance<MapFragment>().firstOrNull()
    private val rulesetsFragment: RulesetsFragment? get() = supportFragmentManager.fragments.filterIsInstance<RulesetsFragment>().firstOrNull()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setupViews()
    }

    private fun setupViews() {
        toolbar.title = title
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


        viewPager.setPagingEnabled(false)
        viewPager.offscreenPageLimit = 3
        viewPager.adapter = MapPagerAdapter(supportFragmentManager)

        tabLayout.tabMode = TabLayout.MODE_FIXED
        tabLayout.tabGravity = TabLayout.GRAVITY_FILL
        tabLayout.setupWithViewPager(viewPager)
    }

    fun setRulesets(availableRulesets: List<AirMapRuleset>, selectedRulesets: List<AirMapRuleset>) = rulesetsFragment?.setRulesets(availableRulesets, selectedRulesets)

    fun selectRuleset(ruleset: AirMapRuleset) = mapFragment?.onRulesetSelected(ruleset)

    fun deselectRuleset(ruleset: AirMapRuleset) = mapFragment?.onRulesetDeselected(ruleset)

    fun switchSelectedRulesets(fromRuleset: AirMapRuleset, toRuleset: AirMapRuleset) = mapFragment?.onRulesetSwitched(fromRuleset, toRuleset)

    fun setAdvisoryStatus(status: AirMapAirspaceStatus) = supportFragmentManager.fragments
            .filterIsInstance<AdvisoriesFragment>()
            .forEach { it.setAdvisoryStatus(status) }

    override fun getMapView(): AirMapMapView? = mapFragment?.mapView

    private inner class MapPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

        override fun getItem(position: Int): Fragment = when (position) {
            0 -> MapFragment.newInstance()
            1 -> RulesetsFragment.newInstance()
            else -> AdvisoriesFragment.newInstance()
        }

        override fun getPageTitle(position: Int) = when (position) {
            0 -> "Map"
            1 -> "Rules"
            2 -> "Advisories"
            else -> ""
        }

        override fun getCount() = 3
    }
}
