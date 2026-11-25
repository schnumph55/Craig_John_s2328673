package org.me.craig_john_s2328673.ui;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
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

import java.util.Locale;

public class SearchFragment extends Fragment {

    private TrafficViewModel viewModel;
    private SituationAdapter adapter;
    private AutoCompleteTextView roadInput;
    private TextView resultCount;
    private Button mapButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(requireActivity()).get(TrafficViewModel.class);
        roadInput = view.findViewById(R.id.roadInput);
        resultCount = view.findViewById(R.id.resultCount);
        mapButton = view.findViewById(R.id.mapButton);
        Button searchButton = view.findViewById(R.id.searchButton);

        RecyclerView rv = view.findViewById(R.id.searchRecycler);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
        adapter = new SituationAdapter(s -> {
            viewModel.select(s);
            ((MainActivity) requireActivity()).showDetail();
        });
        rv.setAdapter(adapter);

        searchButton.setOnClickListener(v -> runSearch());
        roadInput.setOnItemClickListener((parent, v, position, id) -> runSearch());
        roadInput.setOnEditorActionListener((tv, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                runSearch();
                return true;
            }
            return false;
        });

        mapButton.setOnClickListener(v -> {
            viewModel.showOnMap(viewModel.getSearchResults().getValue());
            ((MainActivity) requireActivity()).openMap();
        });

        viewModel.getSearchResults().observe(getViewLifecycleOwner(), list -> {
            adapter.setItems(list);
            int n = list == null ? 0 : list.size();
            mapButton.setEnabled(n > 0);
            String q = roadInput.getText().toString().trim().toUpperCase(Locale.UK);
            resultCount.setText(q.isEmpty() ? "Enter a road name" : n + " situations on " + q);
        });

        viewModel.getIncidents().observe(getViewLifecycleOwner(), list -> onDataChanged());
        viewModel.getRoadworks().observe(getViewLifecycleOwner(), list -> onDataChanged());
    }

    private void onDataChanged() {
        roadInput.setAdapter(new ArrayAdapter<>(requireContext(),
                android.R.layout.simple_dropdown_item_1line, viewModel.getRoadNames()));
        if (!roadInput.getText().toString().trim().isEmpty()) {
            viewModel.searchByRoad(roadInput.getText().toString());
        }
    }

    private void runSearch() {
        viewModel.searchByRoad(roadInput.getText().toString());
        roadInput.dismissDropDown();
        InputMethodManager imm = (InputMethodManager)
                requireContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(roadInput.getWindowToken(), 0);
    }
}