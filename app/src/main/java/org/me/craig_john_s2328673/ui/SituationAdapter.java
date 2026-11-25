package org.me.craig_john_s2328673.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.me.craig_john_s2328673.R;
import org.me.craig_john_s2328673.model.Situation;

import java.util.ArrayList;
import java.util.List;

public class SituationAdapter extends RecyclerView.Adapter<SituationAdapter.ViewHolder> {

    public interface OnSituationClickListener {
        void onSituationClick(Situation s);
    }

    private final List<Situation> items = new ArrayList<>();
    private final OnSituationClickListener listener;

    private boolean showDistance = false;
    private double refLat, refLon;

    public void setReferencePoint(double lat, double lon) {
        showDistance = true;
        refLat = lat;
        refLon = lon;
    }

    public SituationAdapter(OnSituationClickListener listener) {
        this.listener = listener;
    }

    public void setItems(List<Situation> newItems) {
        items.clear();
        if (newItems != null) items.addAll(newItems);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_situation, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        Situation s = items.get(position);
        int colour = DisplayHelper.colourFor(s);
        h.title.setText(DisplayHelper.typeLabel(s));
        h.comment.setText(s.getComment());
        String meta = DisplayHelper.metaLine(s);
        if (showDistance) {
            meta += String.format(java.util.Locale.UK, " • %.1f km away", s.distanceKmTo(refLat, refLon));
        }
        h.meta.setText(meta);
        h.icon.setImageResource(DisplayHelper.iconFor(s));
        h.icon.setColorFilter(colour);
        h.colourBar.setBackgroundColor(colour);
        h.itemView.setOnClickListener(v -> listener.onSituationClick(s));
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        final View colourBar;
        final ImageView icon;
        final TextView title, comment, meta;

        ViewHolder(View v) {
            super(v);
            colourBar = v.findViewById(R.id.colourBar);
            icon = v.findViewById(R.id.icon);
            title = v.findViewById(R.id.title);
            comment = v.findViewById(R.id.comment);
            meta = v.findViewById(R.id.meta);
        }
    }
}