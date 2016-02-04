package com.luorrak.ouroboros.catalog;

import android.app.FragmentTransaction;
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

import com.koushikdutta.ion.Ion;
import com.luorrak.ouroboros.R;
import com.luorrak.ouroboros.miscellaneous.OpenSourceLicenseFragment;
import com.luorrak.ouroboros.settings.SettingsFragment;
import com.luorrak.ouroboros.util.DragAndDropRecyclerView.WatchListTouchHelper;
import com.luorrak.ouroboros.util.InfiniteDbHelper;
import com.luorrak.ouroboros.util.SettingsHelper;
import com.luorrak.ouroboros.util.Util;

import java.util.Random;

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
    private DrawerLayout drawerLayout;
    private ProgressBar progressBar;
    private InfiniteDbHelper infiniteDbHelper;
    private RecyclerView watchList;
    private WatchListAdapter watchListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Util.onActivityCreateSetTheme(this, SettingsHelper.getTheme(this));
        super.onCreate(savedInstanceState);
        Ion.getDefault(getApplicationContext()).getCache().setMaxSize(150 * 1024 * 1024);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
        if (SettingsHelper.getPostPassword(getApplicationContext()).equals("")){
            Random random = new Random();
            SettingsHelper.setPostPassword(getApplicationContext(), Long.toHexString(random.nextLong()));
        }
        infiniteDbHelper = new InfiniteDbHelper(getApplicationContext());
        setContentView(R.layout.activity_catalog);

        if (savedInstanceState == null){
            board = getIntent().getStringExtra(CatalogAdapter.BOARD_NAME);
            if (board == null){
                BoardListFragment boardListFragment = new BoardListFragment();
                android.app.FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.activity_catalog_fragment_container, boardListFragment).commit();
            } else {
                CatalogFragment catalogFragment = new CatalogFragment().newInstance(board);
                android.app.FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
                fragmentTransaction.replace(R.id.activity_catalog_fragment_container, catalogFragment).commit();
            }
        }

        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        Toolbar toolbar = (Toolbar) findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(this);

        watchList = (RecyclerView) findViewById(R.id.watch_list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getApplicationContext());
        watchList.setLayoutManager(layoutManager);

        watchListAdapter = new WatchListAdapter(infiniteDbHelper.getWatchlistCursor(), drawerLayout, infiniteDbHelper);
        watchList.setAdapter(watchListAdapter);

        ItemTouchHelper.Callback callback = new WatchListTouchHelper(watchListAdapter);
        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(watchList);

        ActionBarDrawerToggle drawerToggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.drawer_open, R.string.drawer_close);
        drawerLayout.setDrawerListener(drawerToggle);
        drawerToggle.syncState();
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
        if (watchListAdapter != null){
            watchListAdapter.changeCursor(infiniteDbHelper.getWatchlistCursor());
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putString("board", board);
        super.onSaveInstanceState(outState);
    }

    public void launchBoardFragment(String board){
        this.board = board; //The real reason for this method being here
        CatalogFragment catalogFragment = new CatalogFragment().newInstance(board);
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.activity_catalog_fragment_container, catalogFragment).commit();
    }
}
