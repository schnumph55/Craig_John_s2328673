package org.me.craig_john_s2328673.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.me.craig_john_s2328673.MainActivity;
import org.me.craig_john_s2328673.R;
import org.me.craig_john_s2328673.viewmodel.TrafficViewModel;

public class NearbyFragment extends Fragment {

    private static final String[] PLACE_NAMES = {
            "GCU campus, Glasgow", "Edinburgh", "Stirling", "Perth",
            "Dundee", "Aberdeen", "Inverness", "Ayr", "Dumfries"
    };
    private static final double[][] PLACE_COORDS = {
            {55.8667, -4.2500}, {55.9533, -3.1883}, {56.1165, -3.9369}, {56.3950, -3.4308},
            {56.4620, -2.9707}, {57.1497, -2.0943}, {57.4778, -4.2247}, {55.4586, -4.6292},
            {55.0700, -3.6050}
    };

    private TrafficViewModel viewModel;
    private SituationAdapter adapter;
    private Spinner placeSpinner;
    private SeekBar radiusSeek;
    private TextView radiusLabel, nearbyCount;
    private Button mapButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_nearby, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(requireActivity()).get(TrafficViewModel.class);
        placeSpinner = view.findViewById(R.id.placeSpinner);
        radiusSeek = view.findViewById(R.id.radiusSeek);
        radiusLabel = view.findViewById(R.id.radiusLabel);
        nearbyCount = view.findViewById(R.id.nearbyCount);
        mapButton = view.findViewById(R.id.nearbyMapButton);

        ArrayAdapter<String> places = new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_spinner_item, PLACE_NAMES);
        places.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        placeSpinner.setAdapter(places);

        RecyclerView rv = view.findViewById(R.id.nearbyRecycler);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
        adapter = new SituationAdapter(s -> {
            viewModel.select(s);
            ((MainActivity) requireActivity()).showDetail();
        });
        rv.setAdapter(adapter);

        placeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View v, int position, long id) {
                runSearch();
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) { }
        });

        radiusSeek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                updateRadiusLabel();
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) { }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                runSearch();
            }
        });
        updateRadiusLabel();

        mapButton.setOnClickListener(v -> {
            viewModel.showOnMap(viewModel.getNearbyResults().getValue());
            ((MainActivity) requireActivity()).openMap();
        });

        viewModel.getNearbyResults().observe(getViewLifecycleOwner(), list -> {
            adapter.setItems(list);
            int n = list == null ? 0 : list.size();
            mapButton.setEnabled(n > 0);
            nearbyCount.setText(n + " situations within " + radiusKm() + " km");
        });

        viewModel.getIncidents().observe(getViewLifecycleOwner(), list -> runSearch());
        viewModel.getRoadworks().observe(getViewLifecycleOwner(), list -> runSearch());
    }

    private int radiusKm() {
        return radiusSeek.getProgress() + 5;
    }

    private void updateRadiusLabel() {
        radiusLabel.setText("Within " + radiusKm() + " km");
    }

    private void runSearch() {
        int i = Math.max(0, placeSpinner.getSelectedItemPosition());
        double lat = PLACE_COORDS[i][0];
        double lon = PLACE_COORDS[i][1];
        adapter.setReferencePoint(lat, lon);
        viewModel.searchNearby(lat, lon, radiusKm());
    }
}