package com.luorrak.ouroboros.catalog;

import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.koushikdutta.ion.Ion;
import com.luorrak.ouroboros.R;
import com.luorrak.ouroboros.miscellaneous.OpenSourceLicenseFragment;
import com.luorrak.ouroboros.settings.SettingsFragment;
import com.luorrak.ouroboros.util.DragAndDropRecyclerView.WatchListTouchHelper;
import com.luorrak.ouroboros.util.InfiniteDbHelper;
import com.luorrak.ouroboros.util.Util;

/**
 * Ouroboros - An 8chan browser
 * Copyright (C) 2015  Luorrak
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

public class CatalogActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    private String board;
    DrawerLayout drawerLayout;
    ProgressBar progressBar;
    InfiniteDbHelper infiniteDbHelper;
    RecyclerView watchList;
    WatchListAdapter watchListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Util.onActivityCreateSetTheme(this, Util.getTheme(this));
        super.onCreate(savedInstanceState);
        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        Ion.getDefault(getApplicationContext()).getCache().setMaxSize(150 * 1024 * 1024);
        infiniteDbHelper = new InfiniteDbHelper(getApplicationContext());
        setContentView(R.layout.activity_catalog);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);

        if (savedInstanceState == null){
            board = getIntent().getStringExtra(CatalogAdapter.BOARD_NAME);
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);

        //temp board list
        watchList = (RecyclerView) findViewById(R.id.watch_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        watchList.setLayoutManager(layoutManager);

        watchListAdapter = new WatchListAdapter(infiniteDbHelper.getWatchlistCursor(), getFragmentManager(), getApplicationContext());
        watchList.setAdapter(watchListAdapter);

        ItemTouchHelper.Callback callback = new WatchListTouchHelper(watchListAdapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(watchList);
        //end temp board list

        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.setDrawerListener(drawerToggle);
        drawerToggle.syncState();

        if (board != null){
            CatalogFragment catalogFragment = new CatalogFragment().newInstance(board);
            android.app.FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.activity_catalog_fragment_container, catalogFragment).commit();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()){
            case R.id.drawer_item_boards:{
                BoardListFragment boardListFragment = new BoardListFragment();
                android.app.FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.activity_catalog_fragment_container, boardListFragment).commit();
                progressBar.setVisibility(View.INVISIBLE);
                break;
            }
            case R.id.drawer_item_watchlist:{
                Toast.makeText(getApplicationContext(), "Feature not yet implemented", Toast.LENGTH_SHORT).show();
                break;
            }
            case R.id.drawer_item_settings: {
                SettingsFragment settingsFragment = new SettingsFragment();
                android.app.FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.activity_catalog_fragment_container, settingsFragment).commit();
                progressBar.setVisibility(View.INVISIBLE);
                break;
            }
            case R.id.drawer_item_licences: {
                OpenSourceLicenseFragment openSourceLicenseFragment = new OpenSourceLicenseFragment();
                android.app.FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.activity_catalog_fragment_container, openSourceLicenseFragment).commit();
                progressBar.setVisibility(View.INVISIBLE);
                break;
            }
        }
        drawerLayout.closeDrawers();
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        watchListAdapter.changeCursor(infiniteDbHelper.getWatchlistCursor());
    }
}
