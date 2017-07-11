package com.telenor.connect;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ParametersHolder implements Parcelable {

    private Map<String, List<String>> parameters = new HashMap<>();

    public ParametersHolder() {
    }

    public ParametersHolder(Map<String, String> map) {
        addAll(map);
    }

    public void add(String key, String value) {
        if (!parameters.containsKey(key)) {
            parameters.put(key, new ArrayList<String>());
        }
        if (!parameters.get(key).contains(value)) {
            parameters.get(key).add(value);
        }
    }

    public void put(String key, String value) {
        parameters.put(key, Arrays.asList(value));
    }

    public void put(String key, List<String> value) {
        parameters.put(key, value);
    }

    public void addAll(ParametersHolder other) {
        for (Map.Entry<String, List<String>> entry : other.parameters.entrySet()) {
            List<String> value = entry.getValue();
            for (String s: value) {
                add(entry.getKey(), s);
            }
        }
    }

    public void addAll(Map<String, String> map) {
        for (Map.Entry<String, String> entry : map.entrySet()) {
            add(entry.getKey(), entry.getValue());
        }
    }

    public List<String> get(String key) {
        return parameters.get(key);
    }

    public Set<Map.Entry<String, String>> entrySet() {
        Set<Map.Entry<String, String>> set = new HashSet<>();
        for (Map.Entry<String, List<String>> entry : parameters.entrySet()) {
            List<String> value = entry.getValue();
            for (String s: value) {
                set.add(new ParamEntry(entry.getKey(), s));
            }
        }
        return set;
    }

    protected ParametersHolder(Parcel in) {
        int size = in.readInt();
        for (int i = 0; i < size; i++) {
            String key = in.readString();
            String value = in.readString();
            add(key, value);
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        int size = size();
        dest.writeInt(size);
        for (Map.Entry<String, List<String>> entry : parameters.entrySet()) {
            List<String> value = entry.getValue();
            for (String s: value) {
                dest.writeString(entry.getKey());
                dest.writeString(s);
            }
        }
    }

    private int size() {
        int size = 0;
        for (Map.Entry<String, List<String>> entry : parameters.entrySet()) {
            size += entry.getValue().size();
        }
        return size;
    }

    public static final Parcelable.Creator<ParametersHolder> CREATOR =
            new Parcelable.Creator<ParametersHolder>() {
        @Override
        public ParametersHolder createFromParcel(Parcel in) {
            return new ParametersHolder(in);
        }

        @Override
        public ParametersHolder[] newArray(int size) {
            return new ParametersHolder[size];
        }
    };


    private static class ParamEntry implements Map.Entry<String, String> {
        private final String key;
        private String value;

        public ParamEntry(String key, String value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public String getKey() {
            return key;
        }

        @Override
        public String getValue() {
            return value;
        }

        @Override
        public String setValue(String value) {
            String oldValue = this.value;
            this.value = value;
            return oldValue;
        }
    }
}
