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
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.navigation.NavigationBarView;
import org.me.craig_john_s2328673.ui.DetailFragment;
import org.me.craig_john_s2328673.ui.MapFragment;
import org.me.craig_john_s2328673.ui.NearbyFragment;
import org.me.craig_john_s2328673.ui.SearchFragment;
import org.me.craig_john_s2328673.ui.SituationListFragment;
import org.me.craig_john_s2328673.viewmodel.TrafficViewModel;

public class MainActivity extends AppCompatActivity {

    private TrafficViewModel viewModel;
    private NavigationBarView bottomNav;

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

        MaterialToolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        viewModel = new ViewModelProvider(this).get(TrafficViewModel.class);
        TextView statusText = findViewById(R.id.statusText);
        viewModel.getStatus().observe(this, statusText::setText);

        bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_list) { showTab(new SituationListFragment()); return true; }
            if (id == R.id.nav_search) { showTab(new SearchFragment()); return true; }
            if (id == R.id.nav_nearby) { showTab(new NearbyFragment()); return true; }
            if (id == R.id.nav_map) { showTab(new MapFragment()); return true; }
            return false;
        });

        if (savedInstanceState == null) {
            showTab(new SituationListFragment());
        }
    }

    private void showTab(Fragment fragment) {
        FragmentManager fm = getSupportFragmentManager();
        fm.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        fm.beginTransaction().replace(R.id.fragment_container, fragment).commit();
    }

    public void showDetail() {
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container, new DetailFragment())
                .addToBackStack("detail")
                .commit();
    }

    public void openMap() {
        bottomNav.setSelectedItemId(R.id.nav_map);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_refresh) {
            viewModel.refreshNow();
            Toast.makeText(this, "Refreshing…", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}