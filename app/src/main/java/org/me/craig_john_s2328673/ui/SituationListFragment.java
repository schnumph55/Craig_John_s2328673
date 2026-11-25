package org.me.craig_john_s2328673.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioGroup;
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
import org.me.craig_john_s2328673.model.Situation;
import org.me.craig_john_s2328673.viewmodel.TrafficViewModel;

import java.util.ArrayList;
import java.util.List;

public class SituationListFragment extends Fragment {

    private TrafficViewModel viewModel;
    private SituationAdapter adapter;
    private RadioGroup filterGroup;
    private TextView countText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_situation_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(requireActivity()).get(TrafficViewModel.class);
        filterGroup = view.findViewById(R.id.filterGroup);
        countText = view.findViewById(R.id.countText);

        RecyclerView rv = view.findViewById(R.id.recyclerView);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        rv.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
        adapter = new SituationAdapter(s -> {
            viewModel.select(s);
            ((MainActivity) requireActivity()).showDetail();
        });
        rv.setAdapter(adapter);

        filterGroup.setOnCheckedChangeListener((group, checkedId) -> refreshList());
        viewModel.getIncidents().observe(getViewLifecycleOwner(), list -> refreshList());
        viewModel.getRoadworks().observe(getViewLifecycleOwner(), list -> refreshList());
    }

    private void refreshList() {
        List<Situation> combined = new ArrayList<>();
        int checked = filterGroup.getCheckedRadioButtonId();
        List<Situation> inc = viewModel.getIncidents().getValue();
        List<Situation> rw = viewModel.getRoadworks().getValue();
        if (checked != R.id.filterRoadworks && inc != null) combined.addAll(inc);
        if (checked != R.id.filterIncidents && rw != null) combined.addAll(rw);
        adapter.setItems(combined);
        countText.setText(combined.size() + " situations");
    }
}