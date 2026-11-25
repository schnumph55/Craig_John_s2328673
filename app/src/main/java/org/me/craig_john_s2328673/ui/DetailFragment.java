package org.me.craig_john_s2328673.ui;

import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import org.me.craig_john_s2328673.R;
import org.me.craig_john_s2328673.model.Impact;
import org.me.craig_john_s2328673.model.Location;
import org.me.craig_john_s2328673.model.Situation;
import org.me.craig_john_s2328673.viewmodel.TrafficViewModel;

import java.util.Locale;

public class DetailFragment extends Fragment {

    private LinearLayout sections;
    private TrafficViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        sections = view.findViewById(R.id.detailSections);
        viewModel = new ViewModelProvider(requireActivity()).get(TrafficViewModel.class);
        viewModel.getSelected().observe(getViewLifecycleOwner(), this::show);
    }

    private void show(Situation s) {
        if (s == null) return;
        View v = requireView();
        int colour = DisplayHelper.colourFor(s);

        ImageView icon = v.findViewById(R.id.detailIcon);
        icon.setImageResource(DisplayHelper.iconFor(s));
        icon.setColorFilter(colour);

        ((TextView) v.findViewById(R.id.detailType)).setText(DisplayHelper.typeLabel(s));
        String direction = DisplayHelper.directionLabel(s.getDirection());
        ((TextView) v.findViewById(R.id.detailRoad)).setText(
                s.getRoadName() + (direction.isEmpty() ? "" : " · " + direction));

        TextView badge = v.findViewById(R.id.detailBadge);
        badge.setText(s.isRoadworks() ? "Duration: " + DisplayHelper.durationLabel(s) : "Unplanned incident");
        badge.setBackgroundColor(colour);

        ((TextView) v.findViewById(R.id.detailComment)).setText(s.getComment());

        v.findViewById(R.id.detailMapButton).setOnClickListener(btn -> {
            viewModel.showOnMap(java.util.Collections.singletonList(s));
            ((org.me.craig_john_s2328673.MainActivity) requireActivity()).openMap();
        });

        sections.removeAllViews();

        if (s.isRoadworks()) {
            String[] parts = DisplayHelper.splitRoadworksComment(s.getComment());
            if (parts.length >= 2) {
                String[] labels = {"Road", "Location", "Effect"};
                StringBuilder b = new StringBuilder();
                for (int i = 0; i < parts.length; i++) {
                    String label = i < labels.length ? labels[i] : "Note";
                    b.append(label).append(": ").append(DisplayHelper.expandAbbreviations(parts[i])).append('\n');
                }
                addSection("Summary", b.toString().trim());
            }
        }

        StringBuilder when = new StringBuilder();
        when.append("Started: ").append(DisplayHelper.formatDate(s.getValidity().getStart()));
        if (s.getValidity().hasEnd()) {
            when.append("\nEnds: ").append(DisplayHelper.formatDate(s.getValidity().getEnd()));
            when.append("\nPlanned duration: ").append(DisplayHelper.durationLabel(s));
        } else {
            when.append("\nEnd: not specified");
        }
        addSection("When", when.toString());

        Impact imp = s.getImpact();
        StringBuilder impact = new StringBuilder();
        if (imp.getLanesRestricted() != null) {
            int n = imp.getLanesRestricted();
            impact.append(n).append(n == 1 ? " lane" : " lanes").append(" restricted\n");
        }
        if (imp.getOperationalLanes() != null) {
            int n = imp.getOperationalLanes();
            impact.append(n == 0 ? "No lanes open (closed)" : n + " lanes open").append('\n');
        }
        if (imp.getDelaysType() != null) {
            impact.append(DisplayHelper.delaysLabel(imp.getDelaysType())).append('\n');
        }
        addSection("Impact", impact.length() == 0 ? "No impact information" : impact.toString().trim());

        StringBuilder loc = new StringBuilder();
        for (String role : new String[]{"from", "to"}) {
            for (Location l : s.getLocations()) {
                if (!role.equals(l.getRole())) continue;
                String name = l.getPointName() != null ? l.getPointName() : l.getRoadName();
                loc.append(role.equals("from") ? "From: " : "To: ").append(name);
                if (l.getJunction() != null) loc.append(" (").append(l.getJunction()).append(")");
                loc.append(String.format(Locale.UK, "\n   %.5f, %.5f\n", l.getLatitude(), l.getLongitude()));
            }
        }
        addSection("Location", loc.length() == 0 ? "No location data" : loc.toString().trim());

        StringBuilder more = new StringBuilder();
        more.append("Category: ").append(DisplayHelper.camelToWords(s.getRecordType()));
        if (s.getSubType() != null) more.append(" / ").append(DisplayHelper.camelToWords(s.getSubType()));
        if (s.getProbability() != null) more.append("\nProbability: ").append(DisplayHelper.camelToWords(s.getProbability()));
        if (s.getCompliance() != null) more.append("\nCompliance: ").append(DisplayHelper.camelToWords(s.getCompliance()));
        more.append("\nLast updated: ").append(DisplayHelper.formatDate(s.getLastUpdated()));
        more.append("\nReference: ").append(s.getId());
        addSection("More details", more.toString());
    }

    private void addSection(String title, String body) {
        TextView t = new TextView(requireContext());
        t.setText(title);
        t.setTypeface(null, Typeface.BOLD);
        t.setTextSize(15);
        t.setPadding(0, dp(16), 0, dp(4));
        TextView b = new TextView(requireContext());
        b.setText(body);
        b.setTextSize(14);
        sections.addView(t);
        sections.addView(b);
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density);
    }
}