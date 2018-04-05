package com.airmap.airmapsdk.models.flight;

import java.io.Serializable;

public class FlightFeatureValue<T> implements Serializable {

    private String key;
    private T value;

    public FlightFeatureValue(String key, T value) {
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof FlightFeatureValue && ((FlightFeatureValue) o).key.equals(this.key);
    }

    @Override
    public int hashCode() {
        return key.hashCode();
    }
}
