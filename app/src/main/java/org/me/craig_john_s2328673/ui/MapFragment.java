package org.me.craig_john_s2328673.ui;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.me.craig_john_s2328673.MainActivity;
import org.me.craig_john_s2328673.R;
import org.me.craig_john_s2328673.model.Location;
import org.me.craig_john_s2328673.model.Situation;
import org.me.craig_john_s2328673.viewmodel.TrafficViewModel;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapFragment extends Fragment implements OnMapReadyCallback {

    private static final LatLng SCOTLAND = new LatLng(56.8, -4.2);

    private TrafficViewModel viewModel;
    private GoogleMap map;
    private TextView mapInfo;
    private final Map<String, BitmapDescriptor> iconCache = new HashMap<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        viewModel = new ViewModelProvider(requireActivity()).get(TrafficViewModel.class);
        mapInfo = view.findViewById(R.id.mapInfo);
        view.findViewById(R.id.showAllButton).setOnClickListener(v -> viewModel.showOnMap(null));

        SupportMapFragment mapFragment =
                (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.googleMap);
        if (mapFragment != null) mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        map = googleMap;
        map.getUiSettings().setZoomControlsEnabled(true);
        map.setOnInfoWindowClickListener(marker -> {
            Situation s = (Situation) marker.getTag();
            if (s != null) {
                viewModel.select(s);
                ((MainActivity) requireActivity()).showDetail();
            }
        });

        viewModel.getMapSituations().observe(getViewLifecycleOwner(), list -> draw());
        viewModel.getIncidents().observe(getViewLifecycleOwner(), list -> draw());
        viewModel.getRoadworks().observe(getViewLifecycleOwner(), list -> draw());
    }

    private void draw() {
        if (map == null) return;
        map.clear();

        List<Situation> selected = viewModel.getMapSituations().getValue();
        boolean showingAll = selected == null || selected.isEmpty();
        List<Situation> toShow = showingAll ? viewModel.getAll() : selected;

        LatLngBounds.Builder bounds = new LatLngBounds.Builder();
        int points = 0;
        LatLng lastPoint = null;

        for (Situation s : toShow) {
            Location main = s.getMainLocation();
            if (main == null) continue;
            LatLng pos = new LatLng(main.getLatitude(), main.getLongitude());

            Marker m = map.addMarker(new MarkerOptions()
                    .position(pos)
                    .title(DisplayHelper.typeLabel(s) + " · " + s.getRoadName())
                    .snippet(s.getComment())
                    .icon(markerIcon(s)));
            if (m != null) m.setTag(s);

            PolylineOptions line = new PolylineOptions()
                    .color(DisplayHelper.colourFor(s))
                    .width(10f);
            int linePoints = 0;
            for (Location l : s.getLocations()) {
                if (l.getLatitude() == 0 && l.getLongitude() == 0) continue;
                LatLng p = new LatLng(l.getLatitude(), l.getLongitude());
                line.add(p);
                bounds.include(p);
                linePoints++;
                points++;
                lastPoint = p;
            }
            if (linePoints >= 2) map.addPolyline(line);
        }

        mapInfo.setText(showingAll
                ? "Showing all " + toShow.size() + " situations"
                : "Showing " + toShow.size() + " selected situation" + (toShow.size() == 1 ? "" : "s"));

        if (points == 0) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(SCOTLAND, 6f));
        } else if (toShow.size() == 1 && lastPoint != null) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(lastPoint, 12f));
        } else {
            try {
                map.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds.build(), 120));
            } catch (IllegalStateException e) {
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(SCOTLAND, 6f));
            }
        }
    }

    private BitmapDescriptor markerIcon(Situation s) {
        int iconRes = DisplayHelper.iconFor(s);
        int colour = DisplayHelper.colourFor(s);
        String key = iconRes + "_" + colour;
        BitmapDescriptor cached = iconCache.get(key);
        if (cached != null) return cached;

        int size = (int) (34 * getResources().getDisplayMetrics().density);
        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.WHITE);
        canvas.drawCircle(size / 2f, size / 2f, size / 2f, paint);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeWidth(size / 12f);
        paint.setColor(colour);
        canvas.drawCircle(size / 2f, size / 2f, size / 2f - size / 24f, paint);

        Drawable d = ContextCompat.getDrawable(requireContext(), iconRes);
        if (d != null) {
            d = d.mutate();
            d.setTint(colour);
            int pad = size / 5;
            d.setBounds(pad, pad, size - pad, size - pad);
            d.draw(canvas);
        }
        BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(bitmap);
        iconCache.put(key, icon);
        return icon;
    }
}
