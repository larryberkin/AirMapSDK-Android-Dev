package com.airmap.airmapsdk.util;

import com.airmap.airmapsdk.models.flight.AirMapEvaluation;
import com.airmap.airmapsdk.models.flight.AirMapFlightBriefing;
import com.airmap.airmapsdk.models.flight.AirMapFlightFeature;
import com.airmap.airmapsdk.models.rules.AirMapRule;
import com.airmap.airmapsdk.models.rules.AirMapRuleset;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import timber.log.Timber;

public class BriefingEvaluator {

    public static LinkedHashMap<AirMapRule.Status, List<AirMapRule>> computeRulesViolations(AirMapFlightBriefing briefing) {
        List<AirMapRuleset> rulesets = briefing.getRulesets();
        Map<AirMapRule.Status, List<AirMapRule>> rulesMap = new HashMap<>();
        for (AirMapRuleset ruleset : rulesets) {
            for (AirMapRule rule : ruleset.getRules()) {
                List<AirMapRule> rules = rulesMap.get(rule.getStatus());
                if (rules == null) {
                    rules = new ArrayList<>();
                }

                // don't show duplicates
                boolean hasDuplicate = false;
                for (AirMapRule r : rules) {
                    if (r.toString().equals(rule.toString())) {
                        hasDuplicate = true;
                        break;
                    }
                }

                if (!hasDuplicate) {
                    rules.add(rule);
                    rulesMap.put(rule.getStatus(), rules);
                }
            }
        }

        List<Map.Entry<AirMapRule.Status, List<AirMapRule>>> entries = new ArrayList<>(rulesMap.entrySet());
        Collections.sort(entries, (o1, o2) -> {
            if (o1.getKey().intValue() > o2.getKey().intValue()) {
                return 1;
            } else if (o2.getKey().intValue() > o1.getKey().intValue()) {
                return -1;
            }
            return 0;
        });

        LinkedHashMap<AirMapRule.Status, List<AirMapRule>> sortedRulesMap = new LinkedHashMap<>();
        for (Map.Entry<AirMapRule.Status, List<AirMapRule>> entry : entries) {
            List<AirMapRule> rules = entry.getValue();
            Collections.sort(rules, (o1, o2) -> {
                if (o1.getDisplayOrder() > o2.getDisplayOrder()) {
                    return 1;
                } else if (o2.getDisplayOrder() > o1.getDisplayOrder()) {
                    return -1;
                }
                return o1.toString().compareTo(o2.toString());
            });
            sortedRulesMap.put(entry.getKey(), rules);
        }

        return sortedRulesMap;
    }

    public static Set<AirMapFlightFeature> getApplicableFlightFeatures(AirMapEvaluation evaluation) {
        Set<AirMapFlightFeature> flightFeatures = new HashSet<>();

        for (AirMapRuleset ruleset : evaluation.getRulesets()) {
            for (AirMapRule rule : ruleset.getRules()) {
                // Rules that are conflicting, missing information or are informational should be shown
                if (rule.getStatus() != AirMapRule.Status.NotConflicting) {
                    for (AirMapFlightFeature flightFeature : rule.getFlightFeatures()) {
                        if (!flightFeature.isCalculated()) {
                            flightFeatures.add(flightFeature);
                        }
                    }
                }
            }
        }

        return flightFeatures;
    }

    public static AirMapRule.Status getStatus(AirMapFlightBriefing briefing) {
        // set worst status to overall status
        AirMapRule.Status overallStatus = AirMapRule.Status.NotConflicting;
        for (AirMapRuleset ruleset : briefing.getRulesets()) {
            AirMapRule.Status rulesetStatus = getStatus(ruleset);
            if (rulesetStatus == AirMapRule.Status.Conflicting) {
                overallStatus = AirMapRule.Status.Conflicting;
                break;
            } else if (rulesetStatus == AirMapRule.Status.MissingInfo) {
                overallStatus = AirMapRule.Status.MissingInfo;
            }
        }

        return overallStatus;
    }

    public static AirMapRule.Status getStatus(AirMapRuleset ruleset) {
        // set worst status to overall status
        AirMapRule.Status overallStatus = AirMapRule.Status.NotConflicting;
        for (AirMapRule rule : ruleset.getRules()) {
            if (rule.getStatus() == AirMapRule.Status.Conflicting) {
                overallStatus = rule.getStatus();
                break;
            } else if (rule.getStatus() == AirMapRule.Status.MissingInfo) {
                overallStatus = rule.getStatus();
            }
        }

        return overallStatus;
    }

    public static LinkedHashMap<AirMapRule.Status,List<AirMapRule>> getRulesWithFlightFeatures(AirMapRuleset ruleset, AirMapEvaluation evaluation) {
        LinkedHashMap<AirMapRule.Status,List<AirMapRule>> ruleStatusMap = new LinkedHashMap<>();
        // pre-populate status for correct order
        ruleStatusMap.put(AirMapRule.Status.Conflicting, new ArrayList<>());
        ruleStatusMap.put(AirMapRule.Status.MissingInfo, new ArrayList<>());
        ruleStatusMap.put(AirMapRule.Status.InformationRules, new ArrayList<>());
        ruleStatusMap.put(AirMapRule.Status.NotConflicting, new ArrayList<>());

        for (AirMapRule rule : ruleset.getRules()) {
            List<AirMapRule> rules = new ArrayList<>();
            if (ruleStatusMap.containsKey(rule.getStatus())) {
                rules = ruleStatusMap.get(rule.getStatus());
            }

            AirMapRule evaluationRule = getRuleFromEvaluation(evaluation, rule);
            for (AirMapFlightFeature flightFeature : CopyCollections.copy(rule.getFlightFeatures())) {
                AirMapFlightFeature evaluationFlightFeature = getFlightFeatureFromEvaluation(evaluationRule, flightFeature);
                if (evaluationFlightFeature == null) {
                    rule.getFlightFeatures().remove(flightFeature);
                    Timber.e("No match found for %s in evaluation", flightFeature.getFlightFeature());
                    continue;
                }

                boolean ruleIsFailingDueToInput = rule.getStatus() != AirMapRule.Status.NotConflicting && !evaluationFlightFeature.isCalculated();
                boolean requiresInputBasedOnEvaluation = !evaluationFlightFeature.isCalculated() && evaluationRule.getStatus() != AirMapRule.Status.NotConflicting;

                // replace flight feature with the one from evaluation (includes the question)
                if (ruleIsFailingDueToInput || requiresInputBasedOnEvaluation) {
                    rule.getFlightFeatures().remove(flightFeature);
                    evaluationFlightFeature.setStatus(flightFeature.getStatus());
                    rule.getFlightFeatures().add(evaluationFlightFeature);

                // otherwise hide flight feature
                } else {
                    rule.getFlightFeatures().remove(flightFeature);
                }
            }

            rules.add(rule);
            ruleStatusMap.put(rule.getStatus(), rules);
        }

        // strip out empty sections
        for (AirMapRule.Status status : new HashSet<>(ruleStatusMap.keySet())) {
            if (ruleStatusMap.get(status).isEmpty()) {
                ruleStatusMap.remove(status);
            }
        }

        return ruleStatusMap;
    }

    private static AirMapRule getRuleFromEvaluation(AirMapEvaluation evaluation, AirMapRule rule) {
        for (AirMapRuleset ruleset : evaluation.getRulesets()) {
            for (AirMapRule evaluationRule : ruleset.getRules()) {
                if (rule.getShortText().equals(evaluationRule.getShortText())) {
                    return evaluationRule;
                }
            }
        }

        return null;
    }

    private static AirMapFlightFeature getFlightFeatureFromEvaluation(AirMapRule evaluationRule, AirMapFlightFeature flightFeature) {
        for (AirMapFlightFeature evaluationFlightFeature : evaluationRule.getFlightFeatures()) {
            if (evaluationFlightFeature.getFlightFeature().equals(flightFeature.getFlightFeature())) {
                return evaluationFlightFeature;
            }
        }

        return null;
    }

}
