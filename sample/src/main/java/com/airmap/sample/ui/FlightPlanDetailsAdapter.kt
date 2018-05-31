package com.airmap.sample.ui

import android.content.Context
import android.content.DialogInterface
import android.support.v7.app.AlertDialog
import android.support.v7.widget.RecyclerView
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView

import com.airmap.airmapsdk.Analytics
import com.airmap.airmapsdk.R
import com.airmap.airmapsdk.models.AirMapBaseModel
import com.airmap.airmapsdk.models.flight.AirMapFlightFeature
import com.airmap.airmapsdk.models.flight.AirMapFlightPlan
import com.airmap.airmapsdk.models.flight.FlightFeatureConfiguration
import com.airmap.airmapsdk.models.flight.FlightFeatureValue
import com.airmap.airmapsdk.models.rules.AirMapRule
import com.airmap.airmapsdk.ui.views.ToggleButton
import com.airmap.airmapsdk.util.Utils

import java.util.ArrayList
import java.util.Collections
import java.util.Comparator
import java.util.HashSet

class FlightPlanDetailsAdapter(
        private val context: Context,
        private val flightPlan: AirMapFlightPlan,
        private val flightFeaturesMap: MutableMap<AirMapFlightFeature, MutableList<AirMapRule>>,
        private val flightFeaturesConfigMap: Map<String, FlightFeatureConfiguration>,
        private val flightPlanChangeListener: FlightPlanChangeListener
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val flightFeatures: MutableList<AirMapFlightFeature>
    private var duplicateFlightFeatures: MutableList<AirMapFlightFeature>? = null

    init {
        this.flightFeatures = ArrayList(flightFeaturesMap.keys)

        removeDuplicateFlightFeatures()
    }

    private fun removeDuplicateFlightFeatures() {
        duplicateFlightFeatures = ArrayList()

        for (flightFeature in ArrayList(flightFeaturesMap.keys)) {
            // don't displaying altitude flight features for now
            if (flightFeature.isAltitudeFeature) {
                flightFeaturesMap.remove(flightFeature)
                flightFeatures.remove(flightFeature)
                duplicateFlightFeatures?.add(flightFeature)
            }
        }

        flightFeatures.sortWith(Comparator { o1, o2 ->
            if (o1.inputType != o2.inputType) {
                o1.inputType.value() - o2.inputType.value()
            } else {
                o1.flightFeature.compareTo(o2.flightFeature)
            }
        })
    }

    private fun onFlightPlanChanged() = flightPlanChangeListener.onFlightPlanChanged()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder = when (viewType) {
        BINARY_VIEW_TYPE -> {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_flight_plan_feature_binary, parent, false)
            FlightFeatureBinaryViewHolder(view)
        }
        FIELD_VIEW_TYPE -> {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_flight_plan_feature_field, parent, false)
            FlightFeatureFieldViewHolder(view)
        }
        TEXT_VIEW_TYPE -> {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_flight_plan_feature_text, parent, false)
            FlightFeatureTextViewHolder(view)
        }
        SAVE_BUTTON_TYPE -> {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_flight_plan_next, parent, false)
            NextButtonViewHolder(view)
        }
        else -> throw IllegalArgumentException("Invalid viewType")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) = when (getItemViewType(position)) {
        BINARY_VIEW_TYPE -> {
            val flightFeature = getItem(position) as AirMapFlightFeature
            val savedValue = if (flightPlan.flightFeatureValues != null) flightPlan.flightFeatureValues[flightFeature.flightFeature] else null

            val binaryViewHolder = holder as FlightFeatureBinaryViewHolder
            binaryViewHolder.descriptionTextView.text = flightFeature.description

            binaryViewHolder.infoButton.setOnClickListener { showFlightFeatureInfo(flightFeature) }

            val noSelected = savedValue != null && !(savedValue.value as Boolean)
            binaryViewHolder.noButton.isSelected = noSelected
            binaryViewHolder.noButton.setOnClickListener {
                val featureValue = FlightFeatureValue(flightFeature.flightFeature, false)
                flightPlan.setFlightFeatureValue(featureValue)
                onFlightPlanChanged()

                Analytics.logEvent(Analytics.Event.flightPlanCheck, Analytics.Action.change, flightFeature.flightFeature)
            }

            val yesSelected = savedValue != null && savedValue.value as Boolean
            binaryViewHolder.yesButton.isSelected = yesSelected
            binaryViewHolder.yesButton.setOnClickListener {
                val featureValue = FlightFeatureValue(flightFeature.flightFeature, true)
                flightPlan.setFlightFeatureValue(featureValue)
                onFlightPlanChanged()

                Analytics.logEvent(Analytics.Event.flightPlanCheck, Analytics.Action.change, flightFeature.flightFeature)
            }
        }
        FIELD_VIEW_TYPE -> {
            val flightFeature = getItem(position) as AirMapFlightFeature
            val fieldViewHolder = holder as FlightFeatureFieldViewHolder
            val savedValue = if (flightPlan.flightFeatureValues != null) flightPlan.flightFeatureValues[flightFeature.flightFeature] else null

            fieldViewHolder.descriptionTextView.text = flightFeature.description

            if (fieldViewHolder.textWatcher != null) {
                fieldViewHolder.editText.removeTextChangedListener(fieldViewHolder.textWatcher)
            }

            if (savedValue != null) {
                fieldViewHolder.editText.text = savedValue.value as Editable?
                fieldViewHolder.editText.setText(savedValue.value as CharSequence?)
            }

            val textWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

                }

                override fun afterTextChanged(s: Editable) {
                    var flightFeatureValue: FlightFeatureValue<*>
                    try {
                        val floatValue = java.lang.Float.parseFloat(s.toString())
                        flightFeatureValue = FlightFeatureValue(flightFeature.flightFeature, floatValue)
                    } catch (e: NumberFormatException) {
                        flightFeatureValue = FlightFeatureValue(flightFeature.flightFeature, s.toString())
                    }

                    flightPlan.setFlightFeatureValue(flightFeatureValue)
                    onFlightPlanChanged()

                    Analytics.logEvent(Analytics.Event.flightPlanCheck, Analytics.Action.change, flightFeature.flightFeature)
                }
            }

            fieldViewHolder.textWatcher = textWatcher
            fieldViewHolder.editText.addTextChangedListener(textWatcher)

            fieldViewHolder.infoButton.setOnClickListener { showFlightFeatureInfo(flightFeature) }
        }
        TEXT_VIEW_TYPE -> {
            val flightFeature = getItem(position) as AirMapFlightFeature
            val fieldViewHolder = holder as FlightFeatureTextViewHolder
            fieldViewHolder.descriptionTextView.text = flightFeature.description
            fieldViewHolder.infoButton.setOnClickListener { showFlightFeatureInfo(flightFeature) }
        }
        SAVE_BUTTON_TYPE -> {
            val nextButtonViewHolder = holder as NextButtonViewHolder
            nextButtonViewHolder.nextButton.text = "Save"
            nextButtonViewHolder.nextButton.setOnClickListener {
                Analytics.logEvent(Analytics.Event.flightPlanCheck, Analytics.Action.tap, Analytics.Label.BOTTOM_NEXT_BUTTON)

                flightPlanChangeListener.onFlightPlanSave()
            }
        }
        else -> throw IllegalArgumentException("Invalid viewType")
    }

    private fun showFlightFeatureInfo(flightFeature: AirMapFlightFeature) {
        val rules = flightFeaturesMap[flightFeature]
        rules?.sortWith(Comparator { o1, o2 -> o1.shortText.compareTo(o2.shortText) })

        val rulesTextBuilder = StringBuilder()
        val ruleSet = HashSet<String>()
        var learnMore = false
        rulesTextBuilder.append("The following rule(s) apply:").append("\n").append("\n")
        for (rule in rules ?: emptyList<AirMapRule>()) {
            if (!ruleSet.contains(rule.shortText)) {
                if (!ruleSet.isEmpty()) {
                    rulesTextBuilder.append("\n").append("\n")
                }
                rulesTextBuilder.append(rule.shortText)
                ruleSet.add(rule.shortText)
                if (rule.shortText != null && rule.shortText != rule.description) {
                    learnMore = true
                }
            }
        }

        val builder = AlertDialog.Builder(context)
                .setTitle("Why We're Asking")
                .setMessage(rulesTextBuilder.toString())
                .setPositiveButton(android.R.string.ok, null)

        if (learnMore) {
            builder.setNegativeButton(R.string.learn_more) { dialog, _ ->
                dialog.dismiss()
                showDetailedRules(flightFeature)
            }
        }

        builder.show()
    }

    private fun showDetailedRules(flightFeature: AirMapFlightFeature) {
        val rules = flightFeaturesMap[flightFeature]
        rules?.sortWith(Comparator { o1, o2 -> o1.toString().compareTo(o2.toString()) })

        val rulesTextBuilder = StringBuilder()
        val ruleSet = HashSet<String>()
        for (rule in rules ?: emptyList<AirMapRule>()) {
            if (!ruleSet.contains(rule.toString())) {
                if (!ruleSet.isEmpty()) {
                    rulesTextBuilder.append("\n").append("\n")
                }
                rulesTextBuilder.append(rule.toString())
                ruleSet.add(rule.toString())
            }
        }
        AlertDialog.Builder(context)
                .setTitle("Official Rule")
                .setMessage(rulesTextBuilder.toString())
                .setPositiveButton(android.R.string.ok, null)
                .show()
    }

    override fun getItemViewType(position: Int) = when {
        position == itemCount - 1 -> SAVE_BUTTON_TYPE
        getItem(position) is AirMapFlightFeature -> {
            when ((getItem(position) as AirMapFlightFeature).inputType) {
                AirMapFlightFeature.InputType.Double -> FIELD_VIEW_TYPE
                AirMapFlightFeature.InputType.String -> FIELD_VIEW_TYPE
                AirMapFlightFeature.InputType.Boolean -> BINARY_VIEW_TYPE
                AirMapFlightFeature.InputType.Info -> TEXT_VIEW_TYPE
                else -> TEXT_VIEW_TYPE
            }

        }
        else -> TEXT_VIEW_TYPE
    }

    private fun showStatusIcon(rule: AirMapRule): Boolean {
        for (flightFeature in rule.flightFeatures) {
            if (flightPlan.flightFeatureValues == null || !flightPlan.flightFeatureValues.containsKey(flightFeature.flightFeature)) {
                return true
            }
        }

        return false
    }

    private fun getMeasurementWithUnits(value: Double, flightFeatureConfig: FlightFeatureConfiguration): String {
        val units = flightFeatureConfig.getValueConfig(false).unit.let {  }
        //TODO: i18n support?
        return when {
            value == value.toLong().toDouble() -> String.format("%d %s", value.toInt(), units)
            value % .5 == 0.0 -> String.format("%.1f %s", value, units)
            else -> String.format("%.2f %s", value, units)
        }
    }

    private fun getItem(position: Int): AirMapBaseModel = flightFeatures[position]

    override fun getItemCount() = flightFeaturesMap.size + 1

    private inner class FlightFeatureBinaryViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var descriptionTextView: TextView = itemView.findViewById(R.id.description_text_view)
        internal var noButton: ToggleButton = itemView.findViewById(R.id.no_button)
        internal var yesButton: ToggleButton = itemView.findViewById(R.id.yes_button)
        internal var infoButton: ImageButton = itemView.findViewById(R.id.info_button)
    }

    private inner class FlightFeatureSeekbarViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var descriptionTextView: TextView = itemView.findViewById(R.id.description_text_view)
        internal var valueTextView: TextView = itemView.findViewById(R.id.value_text_view)
        internal var seekBar: SeekBar = itemView.findViewById(R.id.seekbar)
        internal var infoButton: ImageButton = itemView.findViewById(R.id.info_button)
    }

    private inner class FlightFeatureFieldViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var descriptionTextView: TextView = itemView.findViewById(R.id.description_text_view)
        internal var editText: EditText = itemView.findViewById(R.id.edit_text)
        internal var infoButton: ImageButton = itemView.findViewById(R.id.info_button)
        internal var textWatcher: TextWatcher? = null
    }

    private inner class FlightFeatureTextViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var descriptionTextView: TextView = itemView.findViewById(R.id.description_text_view)
        internal var infoButton: ImageButton = itemView.findViewById(R.id.info_button)
    }

    private inner class NextButtonViewHolder internal constructor(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var nextButton: Button = itemView.findViewById(R.id.next_button)
    }

    interface FlightPlanChangeListener {
        fun onFlightPlanChanged()

        fun onFlightFeatureRemoved(flightFeature: String)

        fun onFlightPlanSave()
    }

    companion object {
        private const val BINARY_VIEW_TYPE = 2
        private const val FIELD_VIEW_TYPE = 4
        private const val TEXT_VIEW_TYPE = 5
        private const val SAVE_BUTTON_TYPE = 6
    }
}
