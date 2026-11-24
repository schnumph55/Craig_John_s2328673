/*  Starter project for Mobile Platform Development - 1st diet 26/27
    You should use this project as the starting point for your assignment.
    This project simply reads the data from the required source and displays the
    raw data in a TextField
*/

//
// Name                 john craig
// Student ID           s2328673
// Programme of Study   computer science
//


package org.me.craig_john_s2328673;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import org.me.craig_john_s2328673.model.Situation;
import org.me.craig_john_s2328673.viewmodel.TrafficViewModel;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private TextView rawDataDisplay;
    private TrafficViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        rawDataDisplay = findViewById(R.id.rawDataDisplay);
        Button startButton = findViewById(R.id.startButton);
        startButton.setText("Refresh now");

        viewModel = new ViewModelProvider(this).get(TrafficViewModel.class);
        startButton.setOnClickListener(v -> viewModel.refreshNow());

        viewModel.getStatus().observe(this, s -> updateDisplay());
        viewModel.getIncidents().observe(this, list -> updateDisplay());
        viewModel.getRoadworks().observe(this, list -> updateDisplay());
    }

    private void updateDisplay() {
        List<Situation> inc = viewModel.getIncidents().getValue();
        List<Situation> rw = viewModel.getRoadworks().getValue();
        StringBuilder sb = new StringBuilder();
        sb.append(viewModel.getStatus().getValue()).append("\n\n");
        sb.append("Incidents: ").append(inc == null ? 0 : inc.size()).append("\n");
        sb.append("Roadworks: ").append(rw == null ? 0 : rw.size()).append("\n\n");
        if (inc != null) for (Situation s : inc) sb.append("• ").append(s).append("\n\n");
        rawDataDisplay.setText(sb.toString());
    }
}