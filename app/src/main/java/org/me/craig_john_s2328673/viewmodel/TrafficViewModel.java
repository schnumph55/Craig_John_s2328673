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

    private static final long REFRESH_MINUTES = 60;

    private final TrafficRepository repository;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

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

    @Override
    protected void onCleared() {
        scheduler.shutdownNow();
    }
}