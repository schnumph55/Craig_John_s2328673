package org.me.craig_john_s2328673.viewmodel;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import org.me.craig_john_s2328673.data.FeedResult;
import org.me.craig_john_s2328673.data.TrafficRepository;
import org.me.craig_john_s2328673.model.Situation;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class TrafficViewModel extends AndroidViewModel {

    private static final long REFRESH_MINUTES = 1;

    private final TrafficRepository repository;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private final MutableLiveData<List<Situation>> searchResults = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Situation>> nearbyResults = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Situation>> mapSituations = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Situation>> incidents = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<Situation>> roadworks = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<String> status = new MutableLiveData<>("Loading…");
    private final MutableLiveData<Situation> selected = new MutableLiveData<>();

    public TrafficViewModel(@NonNull Application application) {
        super(application);
        repository = new TrafficRepository(application);
        scheduler.scheduleWithFixedDelay(this::refreshInBackground, 0, REFRESH_MINUTES, TimeUnit.MINUTES);
    }

    public void refreshNow() {
        scheduler.execute(this::refreshInBackground);
    }

    private void refreshInBackground() {
        try {
            loading.postValue(true);
            FeedResult result = repository.loadAll();
            incidents.postValue(result.incidents);
            roadworks.postValue(result.roadworks);

            String time = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"));
            if (result.live) status.postValue("Live data · updated " + time);
            else if (result.usedSample) status.postValue("Offline · showing sample data");
            else status.postValue("Offline · showing last downloaded data");
        } catch (Exception e) {
            Log.e("TrafficViewModel", "Refresh failed", e);
            status.postValue("Could not load data");
        } finally {
            loading.postValue(false);
        }
    }

    public LiveData<List<Situation>> getIncidents() { return incidents; }
    public LiveData<List<Situation>> getRoadworks() { return roadworks; }
    public LiveData<Boolean> isLoading() { return loading; }
    public LiveData<String> getStatus() { return status; }
    public LiveData<Situation> getSelected() { return selected; }
    public void select(Situation s) { selected.setValue(s); }

    public List<Situation> getAll() {
        List<Situation> all = new ArrayList<>();
        if (incidents.getValue() != null) all.addAll(incidents.getValue());
        if (roadworks.getValue() != null) all.addAll(roadworks.getValue());
        return all;
    }

    public List<String> getRoadNames() {
        java.util.TreeSet<String> names = new java.util.TreeSet<>();
        for (Situation s : getAll()) {
            if (s.getRoadName() != null && !s.getRoadName().equals("Unknown")) names.add(s.getRoadName());
        }
        return new ArrayList<>(names);
    }

    public void searchByRoad(String query) {
        String q = query == null ? "" : query.trim().toUpperCase(java.util.Locale.UK);
        List<Situation> results = new ArrayList<>();
        if (!q.isEmpty()) {
            for (Situation s : getAll()) {
                if (matchesRoad(s, q)) results.add(s);
            }
        }
        searchResults.setValue(results);
    }

    private boolean matchesRoad(Situation s, String q) {
        if (q.equalsIgnoreCase(s.getRoadName())) return true;
        for (org.me.craig_john_s2328673.model.Location l : s.getLocations()) {
            if (q.equalsIgnoreCase(l.getRoadName())) return true;
        }
        if (s.getComment() == null) return false;
        String c = " " + s.getComment().toUpperCase(java.util.Locale.UK)
                .replace(",", " ").replace(".", " ").replace("/", " ") + " ";
        return c.contains(" " + q + " ");
    }

    public void searchNearby(double lat, double lon, double radiusKm) {
        List<Situation> results = new ArrayList<>();
        for (Situation s : getAll()) {
            if (s.distanceKmTo(lat, lon) <= radiusKm) results.add(s);
        }
        results.sort(java.util.Comparator.comparingDouble(s -> s.distanceKmTo(lat, lon)));
        nearbyResults.setValue(results);
    }

    public void showOnMap(List<Situation> list) {
        mapSituations.setValue(list == null ? new ArrayList<>() : new ArrayList<>(list));
    }

    public LiveData<List<Situation>> getSearchResults() { return searchResults; }
    public LiveData<List<Situation>> getNearbyResults() { return nearbyResults; }
    public LiveData<List<Situation>> getMapSituations() { return mapSituations; }

    @Override
    protected void onCleared() {
        scheduler.shutdownNow();
    }
}