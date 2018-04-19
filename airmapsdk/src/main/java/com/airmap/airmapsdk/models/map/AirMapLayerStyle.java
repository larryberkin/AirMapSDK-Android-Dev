package com.airmap.airmapsdk.models.map;

import android.graphics.Color;
import android.support.annotation.NonNull;

import com.google.gson.JsonParser;
import com.mapbox.mapboxsdk.style.expressions.Expression;
import com.mapbox.mapboxsdk.style.layers.Layer;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.layers.PropertyValue;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Arrays;

import timber.log.Timber;

import static com.airmap.airmapsdk.util.Utils.optString;

public abstract class AirMapLayerStyle {

    public final String id;
    public final String source;
    public final String sourceLayer;
    public final String type;
    public final float minZoom;
    public Expression filter;
    public final boolean interactive;

    protected AirMapLayerStyle(JSONObject json) {
        id = optString(json, "id");
        source = optString(json, "source");
        sourceLayer = optString(json, "source-layer");
        type = optString(json, "type");
        minZoom = (float) json.optDouble("minzoom", 0);
        Timber.d("LayerStyle: %s %s %s %s %f", id, source, sourceLayer, type, minZoom);

        interactive = json.optBoolean("interactive", false);

        filter = getFilter(json.optJSONArray("filter"));
    }

    public boolean isBackgroundStyle() {
        return id.contains("background");
    }

    public abstract Layer toMapboxLayer(Layer layerToClone, String sourceId);

    private static Expression getFilter(JSONArray filterJsonArray) {
        if (filterJsonArray == null) {
            return null;
        }

        Expression filter;
        String operator = optString(filterJsonArray, 0);
        Timber.d("operator: %s", operator);
        final Object[] operands = new Object[filterJsonArray.length() - 1];
        for (int i = 0; i < operands.length; i++) {
            operands[i] = filterJsonArray.opt(i + 1);
            Timber.d("operand[%d]: %s", i, operands[i]);
        }

        Object operand1 = filterJsonArray.opt(1);
        Object operand2 = filterJsonArray.opt(2);

        switch (operator) {
            case "all":
                filter = Expression.all(getStatements(operands));
                break;
            case "any":
                filter = Expression.any(getStatements(operands));
                break;
            case "none":
                filter = Expression.not(Expression.any(getStatements(operands)));
                break;
            case "has":
                filter = Expression.has((String) operand1);
                break;
            case "!has":
                filter = Expression.not(Expression.has((String) operand1));
                break;
            case "==":
                if ("$type".equals(operand1)) {
                    operand1 = Expression.geometryType();
                } else {
                    operand1 = Expression.get((String) operand1);
                }
                filter = Expression.eq((Expression) operand1, Expression.literal(operand2));
                break;
            case "!=":
                filter = Expression.neq(Expression.get((String) operand1), Expression.literal(operand2));
                break;
            case ">":
                filter = Expression.gt(Expression.get((String) operand1), Expression.literal(operand2));
                break;
            case ">=":
                filter = Expression.gte(Expression.get((String) operand1), Expression.literal(operand2));
                break;
            case "<":
                filter = Expression.lt(Expression.get((String) operand1), Expression.literal(operand2));
                break;
            case "<=":
                filter = Expression.lte(Expression.get((String) operand1), Expression.literal(operand2));
                break;
            case "in":
                // TODO: Verify this is correct way to implement "in"
                filter = Expression.has(Expression.literal(operand1), Expression.literal(convertToExpressionArray(Arrays.copyOfRange(operands, 1, operands.length))));
                break;
            case "!in":
                // TODO: Verify this is correct way to implement "in"
                filter = Expression.not(Expression.has(Expression.literal(operand1), Expression.literal(convertToExpressionArray(Arrays.copyOfRange(operands, 1, operands.length)))));
                break;
            default:
                filter = new Expression("") {
                    @NonNull
                    @Override
                    public Object[] toArray() {
                        return new Object[0];
                    }
                };
        }

        return filter;
    }

    private static Expression[] convertToExpressionArray(Object[] operands) {
        Expression[] expressions = new Expression[operands.length];
        for (int i = 0; i < operands.length; i++) {
            expressions[i] = Expression.literal(operands[i]);
        }
        return expressions;
    }

    private static Expression[] getStatements(Object[] jsonArrays) {
        Expression[] statements = new Expression[jsonArrays.length];
        for (int i = 0; i < jsonArrays.length; i++) {
            statements[i] = getFilter((JSONArray) jsonArrays[i]);
        }
        return statements;
    }

    public static Expression getFillColorFunction(JSONObject fillColor) {
        String property = optString(fillColor, "property");
        Timber.d("property: %s", property);
        String type = optString(fillColor, "type");
        String defaultColor = optString(fillColor, "default", "#000000");
        JSONArray stopsArray = fillColor.optJSONArray("stops");

        switch (type) {
            case "categorical": {
                Expression.Stop[] stops;
                if (stopsArray != null) {
                    stops = new Expression.Stop[stopsArray.length()];
                    for (int i = 0; i < stopsArray.length(); i++) {
                        JSONArray stopArray = stopsArray.optJSONArray(i);
                        Timber.d("stopArray: %s", stopArray.toString());
                        if (stopArray != null) {
                            Object value1 = stopArray.opt(0);
                            Object value2 = stopArray.opt(1);

                            stops[i] = Expression.stop(value1, Expression.color(Color.parseColor((String) value2)));
                        }
                    }
                } else {
                    Timber.d("Empty stop");
                    stops = new Expression.Stop[0];
                }

                return Expression.step(Expression.get(property), Expression.color(Color.parseColor(defaultColor)), stops);
            }
        }

        return null;
    }

    public static Expression getFillOpacityFunction(JSONObject fillOpacity) {
        String property = optString(fillOpacity, "property");
        String type = optString(fillOpacity, "type");
        float defaultOpacity = (float) fillOpacity.optDouble("default", 1.0f);
        JSONArray stopsArray = fillOpacity.optJSONArray("stops");

        switch (type) {
            case "categorical": {
                Expression.Stop[] stops;
                if (stopsArray != null) {
                    stops = new Expression.Stop[stopsArray.length()];
                    for (int i = 0; i < stopsArray.length(); i++) {
                        JSONArray stopArray = stopsArray.optJSONArray(i);
                        if (stopArray != null) {
                            Object value1 = stopArray.opt(0);
                            Object value2 = stopArray.opt(1);
                            stops[i] = (Expression.stop(value1, value2));
                        }
                    }
                } else {
                    Timber.d("Empty stop");
                    stops = new Expression.Stop[0];
                }

                return Expression.step(Expression.get(property), defaultOpacity, stops);
            }
        }

        return null;
    }

    public static AirMapLayerStyle fromJson(JSONObject jsonObject) {
        String type = optString(jsonObject, "type");

        switch (type) {
            case "fill": {
                return new AirMapFillLayerStyle(jsonObject);
            }
            case "line": {
                return new AirMapLineLayerStyle(jsonObject);
            }
            case "symbol": {
                return new AirMapSymbolLayerStyle(jsonObject);
            }
            default: {
                return null;
            }
        }
    }
}
