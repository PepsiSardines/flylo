package com.gabe.splitwatch;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

final class DataStore {
    private static final String PREFS = "splitwatch_data";
    private static final String KEY_TEMPLATES = "templates";
    private static final String KEY_RUNS = "runs";

    private final SharedPreferences preferences;
    private final ArrayList<SplitTemplate> templates = new ArrayList<>();
    private final ArrayList<RunRecord> runs = new ArrayList<>();

    DataStore(Context context) {
        preferences = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        load();
    }

    List<SplitTemplate> getTemplates() {
        return Collections.unmodifiableList(templates);
    }

    SplitTemplate getTemplate(String id) {
        for (SplitTemplate template : templates) {
            if (template.id.equals(id)) {
                return template;
            }
        }
        return null;
    }

    void upsertTemplate(SplitTemplate template) {
        for (int i = 0; i < templates.size(); i++) {
            if (templates.get(i).id.equals(template.id)) {
                templates.set(i, template);
                saveTemplates();
                return;
            }
        }
        templates.add(template);
        saveTemplates();
    }

    void deleteTemplate(String id) {
        templates.removeIf(template -> template.id.equals(id));
        runs.removeIf(run -> run.templateId.equals(id));
        saveTemplates();
        saveRuns();
    }

    List<RunRecord> getRunsForTemplate(String templateId) {
        ArrayList<RunRecord> result = new ArrayList<>();
        for (RunRecord run : runs) {
            if (run.templateId.equals(templateId)) {
                result.add(run);
            }
        }
        result.sort((a, b) -> Long.compare(b.timestamp, a.timestamp));
        return result;
    }

    List<RunRecord> getComparableRuns(SplitTemplate template) {
        ArrayList<RunRecord> result = new ArrayList<>();
        for (RunRecord run : runs) {
            if (run.templateId.equals(template.id) && run.matchesSplits(template.splits)) {
                result.add(run);
            }
        }
        result.sort((a, b) -> Long.compare(b.timestamp, a.timestamp));
        return result;
    }

    void addRun(RunRecord run) {
        runs.add(run);
        saveRuns();
    }

    void updateRunNote(String runId, String note) {
        for (RunRecord run : runs) {
            if (run.id.equals(runId)) {
                run.note = note == null ? "" : note.trim();
                saveRuns();
                return;
            }
        }
    }

    void deleteRun(String runId) {
        runs.removeIf(run -> run.id.equals(runId));
        saveRuns();
    }

    private void load() {
        loadTemplates();
        loadRuns();
        if (templates.isEmpty()) {
            templates.add(SplitTemplate.create(
                    "Morning Routine Split",
                    Arrays.asList("Shower", "Shave", "Change")
            ));
            saveTemplates();
        }
    }

    private void loadTemplates() {
        templates.clear();
        String raw = preferences.getString(KEY_TEMPLATES, "[]");
        try {
            JSONArray array = new JSONArray(raw);
            for (int i = 0; i < array.length(); i++) {
                templates.add(SplitTemplate.fromJson(array.getJSONObject(i)));
            }
        } catch (JSONException ignored) {
            templates.clear();
        }
    }

    private void loadRuns() {
        runs.clear();
        String raw = preferences.getString(KEY_RUNS, "[]");
        try {
            JSONArray array = new JSONArray(raw);
            for (int i = 0; i < array.length(); i++) {
                runs.add(RunRecord.fromJson(array.getJSONObject(i)));
            }
        } catch (JSONException ignored) {
            runs.clear();
        }
    }

    private void saveTemplates() {
        JSONArray array = new JSONArray();
        for (SplitTemplate template : templates) {
            try {
                array.put(template.toJson());
            } catch (JSONException ignored) {
                // A template made from in-app text should always serialize.
            }
        }
        preferences.edit().putString(KEY_TEMPLATES, array.toString()).apply();
    }

    private void saveRuns() {
        JSONArray array = new JSONArray();
        for (RunRecord run : runs) {
            try {
                array.put(run.toJson());
            } catch (JSONException ignored) {
                // A completed run should always serialize.
            }
        }
        preferences.edit().putString(KEY_RUNS, array.toString()).apply();
    }
}
