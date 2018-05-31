package com.airmap.sample.fragments

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.airmap.airmapsdk.AirMapException
import com.airmap.airmapsdk.models.shapes.AirMapPolygon
import com.airmap.airmapsdk.models.status.AirMapAdvisory
import com.airmap.airmapsdk.models.status.AirMapAirspaceStatus
import com.airmap.airmapsdk.networking.callbacks.AirMapCallback
import com.airmap.airmapsdk.networking.services.AirMap
import com.airmap.airmapsdk.networking.services.MappingService
import com.airmap.airmapsdk.ui.adapters.ExpandableAdvisoriesAdapter
import com.airmap.sample.R

import java.util.ArrayList
import java.util.Collections
import java.util.Comparator
import java.util.LinkedHashMap

import timber.log.Timber

class AdvisoriesFragment : Fragment() {

    private lateinit var advisoriesRecyclerView: RecyclerView
    private lateinit var advisoriesAdapter: ExpandableAdvisoriesAdapter
    private lateinit var loadingView: View

    private val isFragmentActive: Boolean
        get() = activity != null && !activity!!.isFinishing && !activity!!.isDestroyed && isResumed && !isDetached

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_advisories, container, false)

        advisoriesRecyclerView = view.findViewById<View>(R.id.advisories_recycler_view) as RecyclerView
        advisoriesRecyclerView.layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)

        loadingView = view.findViewById(R.id.loading_view)

        return view
    }

    private fun loadAdvisories(rulesets: List<String>, polygon: AirMapPolygon) {
        AirMap.getAirspaceStatus(polygon, rulesets, object : AirMapCallback<AirMapAirspaceStatus>() {
            public override fun onSuccess(advisoryStatus: AirMapAirspaceStatus?) {
                // if the activity has been destroy, ignore response
                if (!isFragmentActive) {
                    return
                }

                if (advisoryStatus != null) {
                    Collections.sort(advisoryStatus.advisories, Comparator { o1, o2 ->
                        if (o1.color.intValue() > o2.color.intValue()) {
                            return@Comparator -1
                        } else if (o2.color.intValue() > o1.color.intValue()) {
                            return@Comparator 1
                        }

                        0
                    })

                    // put into a linked hash map for the adapter
                    val advisoryMap = LinkedHashMap<MappingService.AirMapAirspaceType, MutableList<AirMapAdvisory>>()
                    for (advisory in advisoryStatus.advisories) {
                        advisoryMap[advisory.type] = (advisoryMap[advisory.type] ?: mutableListOf()).apply { add(advisory) }
                    }

                    // ExpandableAdvisoriesAdapter shows the advisories grouped by category
                    advisoriesAdapter = ExpandableAdvisoriesAdapter(advisoryMap)
                } else {
                    advisoriesAdapter = ExpandableAdvisoriesAdapter(LinkedHashMap())
                }

                advisoriesRecyclerView.adapter = advisoriesAdapter
                loadingView.visibility = View.GONE
            }

            public override fun onError(e: AirMapException) = Timber.e(e, "Getting advisories failed")
        })
    }

    fun setAdvisoryStatus(advisoryStatus: AirMapAirspaceStatus?) {
        if (advisoryStatus != null) {
            Collections.sort(advisoryStatus.advisories, Comparator { o1, o2 ->
                if (o1.color.intValue() > o2.color.intValue()) {
                    return@Comparator -1
                } else if (o2.color.intValue() > o1.color.intValue()) {
                    return@Comparator 1
                }
                0
            })

            // put into a linked hash map for the adapter
            val advisoryMap = LinkedHashMap<MappingService.AirMapAirspaceType, MutableList<AirMapAdvisory>>()
            for (advisory in advisoryStatus.advisories) {
                advisoryMap[advisory.type] = (advisoryMap[advisory.type] ?: mutableListOf()).apply { add(advisory) }

                // ExpandableAdvisoriesAdapter shows the advisories grouped by category
            }

            advisoriesAdapter = ExpandableAdvisoriesAdapter(advisoryMap)
        } else {
            advisoriesAdapter = ExpandableAdvisoriesAdapter(LinkedHashMap())
        }

        advisoriesRecyclerView.adapter = advisoriesAdapter
        loadingView.visibility = View.GONE
    }

    companion object {
        fun newInstance() = AdvisoriesFragment()
    }
}
