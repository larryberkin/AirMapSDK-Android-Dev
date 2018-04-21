package com.airmap.sample.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.airmap.airmapsdk.models.rules.AirMapRuleset
import com.airmap.sample.R
import com.airmap.sample.activities.MapDemoActivity
import com.airmap.sample.ui.RulesetRecyclerViewAdapter

import timber.log.Timber

class RulesetsFragment : Fragment() {

    companion object {
        fun newInstance() = RulesetsFragment()
    }

    private lateinit var loadingView: View
    private lateinit var rulesetsRecyclerView: RecyclerView
    private lateinit var rulesetsRecyclerAdapter: RulesetRecyclerViewAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_rulesets, container, false)
        loadingView = view.findViewById(R.id.loading_view)
        rulesetsRecyclerView = view.findViewById(R.id.rulesets_recycler_view)

        rulesetsRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        rulesetsRecyclerAdapter = RulesetRecyclerViewAdapter(context, object : RulesetRecyclerViewAdapter.RulesetListener {
            override fun onRulesetSelected(ruleset: AirMapRuleset) {
                Timber.i("Ruleset selected: %s", ruleset.shortName)
                (activity as? MapDemoActivity)?.selectRuleset(ruleset)
            }

            override fun onRulesetDeselected(ruleset: AirMapRuleset) {
                Timber.i("Ruleset deselected: %s", ruleset.shortName)
                (activity as? MapDemoActivity)?.deselectRuleset(ruleset)
            }

            override fun onRulesetInfoPressed(ruleset: AirMapRuleset) {
                Timber.i("Ruleset info pressed: %s", ruleset.shortName)
            }

            override fun onRulesetSwitched(fromRuleset: AirMapRuleset, toRuleset: AirMapRuleset) {
                Timber.i("Ruleset switched from: %s to: %s", fromRuleset.shortName, toRuleset.shortName)
                (activity as? MapDemoActivity)?.switchSelectedRulesets(fromRuleset, toRuleset)
            }
        })
        rulesetsRecyclerView.adapter = rulesetsRecyclerAdapter

        return view
    }


    fun setRulesets(availableRulesets: List<AirMapRuleset>, selectedRulesets: List<AirMapRuleset>) {
        rulesetsRecyclerAdapter.setData(availableRulesets, selectedRulesets)
        loadingView.visibility = View.GONE
    }
}
