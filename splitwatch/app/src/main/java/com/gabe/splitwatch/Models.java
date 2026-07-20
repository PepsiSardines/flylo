package com.gabe.splitwatch;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

final class SplitTemplate {
    String id;
    String name;
    ArrayList<String> splits;

    SplitTemplate(String id, String name, List<String> splits) {
        this.id = id;
        this.name = name;
        this.splits = new ArrayList<>(splits);
    }

    static SplitTemplate create(String name, List<String> splits) {
        return new SplitTemplate(UUID.randomUUID().toString(), name, splits);
    }

    JSONObject toJson() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("id", id);
        object.put("name", name);
        JSONArray array = new JSONArray();
        for (String split : splits) {
            array.put(split);
        }
        object.put("splits", array);
        return object;
    }

    static SplitTemplate fromJson(JSONObject object) throws JSONException {
        JSONArray array = object.getJSONArray("splits");
        ArrayList<String> splits = new ArrayList<>();
        for (int i = 0; i < array.length(); i++) {
            splits.add(array.getString(i));
        }
        return new SplitTemplate(object.getString("id"), object.getString("name"), splits);
    }
}

final class RunRecord {
    String id;
    String templateId;
    long timestamp;
    long totalMs;
    ArrayList<String> splitNames;
    ArrayList<Long> splitMs;
    ArrayList<Long> cumulativeMs;
    String note;

    RunRecord(
            String id,
            String templateId,
            long timestamp,
            long totalMs,
            List<String> splitNames,
            List<Long> splitMs,
            List<Long> cumulativeMs,
            String note
    ) {
        this.id = id;
        this.templateId = templateId;
        this.timestamp = timestamp;
        this.totalMs = totalMs;
        this.splitNames = new ArrayList<>(splitNames);
        this.splitMs = new ArrayList<>(splitMs);
        this.cumulativeMs = new ArrayList<>(cumulativeMs);
        this.note = note == null ? "" : note;
    }

    static RunRecord create(
            String templateId,
            long totalMs,
            List<String> splitNames,
            List<Long> splitMs,
            List<Long> cumulativeMs
    ) {
        return new RunRecord(
                UUID.randomUUID().toString(),
                templateId,
                System.currentTimeMillis(),
                totalMs,
                splitNames,
                splitMs,
                cumulativeMs,
                ""
        );
    }

    JSONObject toJson() throws JSONException {
        JSONObject object = new JSONObject();
        object.put("id", id);
        object.put("templateId", templateId);
        object.put("timestamp", timestamp);
        object.put("totalMs", totalMs);
        object.put("note", note);

        JSONArray names = new JSONArray();
        for (String splitName : splitNames) {
            names.put(splitName);
        }
        object.put("splitNames", names);

        JSONArray splits = new JSONArray();
        for (Long value : splitMs) {
            splits.put(value);
        }
        object.put("splitMs", splits);

        JSONArray cumulative = new JSONArray();
        for (Long value : cumulativeMs) {
            cumulative.put(value);
        }
        object.put("cumulativeMs", cumulative);
        return object;
    }

    static RunRecord fromJson(JSONObject object) throws JSONException {
        ArrayList<String> names = new ArrayList<>();
        JSONArray namesJson = object.optJSONArray("splitNames");
        if (namesJson != null) {
            for (int i = 0; i < namesJson.length(); i++) {
                names.add(namesJson.getString(i));
            }
        }

        ArrayList<Long> splits = new ArrayList<>();
        JSONArray splitsJson = object.getJSONArray("splitMs");
        for (int i = 0; i < splitsJson.length(); i++) {
            splits.add(splitsJson.getLong(i));
        }

        ArrayList<Long> cumulative = new ArrayList<>();
        JSONArray cumulativeJson = object.optJSONArray("cumulativeMs");
        if (cumulativeJson != null) {
            for (int i = 0; i < cumulativeJson.length(); i++) {
                cumulative.add(cumulativeJson.getLong(i));
            }
        } else {
            long running = 0L;
            for (Long split : splits) {
                running += split;
                cumulative.add(running);
            }
        }

        return new RunRecord(
                object.getString("id"),
                object.getString("templateId"),
                object.getLong("timestamp"),
                object.getLong("totalMs"),
                names,
                splits,
                cumulative,
                object.optString("note", "")
        );
    }

    boolean matchesSplits(List<String> currentSplits) {
        if (splitNames.size() != currentSplits.size()) {
            return false;
        }
        for (int i = 0; i < splitNames.size(); i++) {
            if (!splitNames.get(i).equals(currentSplits.get(i))) {
                return false;
            }
        }
        return true;
    }
}
